---
name: migrate
description: Delegate to the `migrate-conductor` agent to migrate exactly one Cucumber scenario to Kotlin + JUnit 5 through the Draft → user-approval → Worker → results-verifier (initial) → Worker cleanup → results-verifier (post-cleanup) → journal pipeline. Use when the user asks to migrate a Cucumber scenario, port a `.feature` to JUnit 5, or runs /migrate. Always one scenario per run — never batch.
allowed-tools: shell
---

# /migrate

Migrate exactly one Cucumber scenario to Kotlin + JUnit 5.

## Usage

```
/migrate <path-to-feature>
/migrate <path-to-feature> --scenario="<exact scenario name>"
/migrate <path-to-feature> --scenario="<exact scenario name>" --approved-concept="<inline note or path>"
```

## Arguments

- `<path-to-feature>` — required. The `.feature` file containing the scenario.
- `--scenario="..."` — optional if the feature has only one scenario; required otherwise.
- `--approved-concept="..."` — optional. When present, the Draft-approval step is short-circuited and the conductor proceeds directly to the worker. The value is recorded in the migration journal under "Draft approval".

## Behavior

1. Delegate to the `migrate-conductor` agent (see `.github/agents/migrate-conductor.agent.md`).
2. The conductor detects Scenario Outline / Examples and, if present, fills `scenario-outline-port-plan.template.md` and blocks for user approval before anything else.
3. The conductor produces a Draft using `migration-draft.template.md` and, unless `--approved-concept` was provided, asks the user to approve it.
4. The conductor hands off to `migrate-worker` to write the Kotlin test class.
5. The conductor hands off to `results-verifier` with `phase: initial` to run build + new test + legacy test + Allure metadata + `.editorconfig` + anti-patterns + migration-parity gates.
6. On green initial: the conductor hands off to `migrate-worker` again with `task: delete-scenario` to remove the migrated Cucumber scenario from the `.feature`, then re-runs `results-verifier` with `phase: post-cleanup`. Post-cleanup confirms the scenario is gone (grep returns zero matches, Cucumber runner reports zero tests) and parity still holds.
7. On green post-cleanup: the conductor writes the migration journal, updates `_INDEX.md`, and asks (one question at a time) whether to append lessons.
8. On block at either phase: the conductor surfaces the verifier's blocker list and offers to revise, revert the `.feature` edit (post-cleanup only), or abort.

## Invariants restated

- English output only.
- Backend only — no UI patterns.
- One scenario per run. No batching.
- Plain `@Test` only — no `@ParameterizedTest` for migrated code.
- All Allure metadata preserved explicitly.
- `.editorconfig` honored on every write.
- Draft → approval → Final unless `--approved-concept` was passed.
- Lessons-learned writes require explicit `y`.

## Related files

- `.github/agents/migrate-conductor.agent.md`
- `.github/agents/migrate-worker.agent.md`
- `.github/agents/results-verifier.agent.md`
- `.github/copilot/templates/migration-draft.template.md`
- `.github/copilot/templates/scenario-outline-port-plan.template.md`
- `.github/copilot/templates/allure-mapping.template.md`
- `.github/copilot/templates/migrated-test-header.template.md`
- `.github/copilot/journal/_TEMPLATE.md`
- `.github/copilot/journal/_INDEX.md`
