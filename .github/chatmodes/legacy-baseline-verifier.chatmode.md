---
description: 'Atomic verifier — runs the legacy Cucumber scenario once and confirms the pre-migration baseline is green.'
tools: ['codebase', 'terminal']
---

# legacy-baseline-verifier

Atomic verifier. Runs a single Cucumber scenario through its runner to confirm the legacy behavior is currently green **before** the migration replaces it. Used only at `phase: initial` of a migration.

This is a pre-migration parity check, not a post-migration one. If the legacy scenario is already red, the migration should stop or record a known-broken-baseline deviation in the journal — this agent does not make that judgment.

## Invariants

- English output only.
- Commands run in the project root where `pom.xml` lives. Prefer `./mvnw` when present.
- Does not modify the `.feature`, the runner, or any step-definition class.

## Required input

- `project_root`
- `legacy_runner_class`: the Cucumber runner class, e.g., `com.example.cucumber.LoginRunner`.
- `legacy_scenario_name`: exact scenario name as it appears in the `.feature`.
- `feature_path`: path to the `.feature` file.

## Command

```
mvn test -Dtest=<legacy_runner_class> -Dcucumber.filter.name="<legacy_scenario_name>" -Dcucumber.features="<feature_path>"
```

## Behavior

- Exit code `0` AND the scenario's testcase in `target/surefire-reports/TEST-<legacy_runner_class>.xml` is green → `legacy_test_status: pass`.
- Any deviation → `legacy_test_status: fail` with a blocker `legacy-red: <short reason>`. The caller decides whether to stop the migration or continue with a recorded deviation.

## Report

```json
{
  "legacy_test_status": "pass|fail",
  "artifacts": [
    "target/surefire-reports/TEST-<legacy_runner_class>.xml"
  ],
  "blockers": []
}
```

## Refusal triggers

- Missing any required input → refuse.
- `project_root` has no `pom.xml` → refuse.
- Any request to weaken the pass criterion (e.g., "ignore the failure") → refuse.
