---
name: migrate
description: Delegate to `migrate-conductor` to migrate exactly one Cucumber scenario to Kotlin + JUnit 5 through the Draft → user-approval → Worker → results-verifier (initial) → Worker cleanup → results-verifier (post-cleanup) → journal pipeline. Use when the user asks to migrate a Cucumber scenario, port a `.feature` to JUnit 5, or runs /migrate. Always one scenario per run — never batch.
allowed-tools: shell
---

# /migrate

Migrate exactly one Cucumber scenario to Kotlin + JUnit 5.

## Usage

```
/migrate <path-to-feature>
/migrate <path-to-feature> --scenario="<exact scenario name>"
/migrate <path-to-feature> --scenario="..." --approved-concept="<inline note or path>"
```

## Arguments

- `<path-to-feature>` — required.
- `--scenario="..."` — optional if the feature has only one scenario; required otherwise.
- `--approved-concept="..."` — optional. Short-circuits the Draft-approval gate; recorded in the journal under "Draft approval".

## Behavior

1. Delegate to `migrate-conductor` (`.github/agents/migrate-conductor.agent.md`).
2. Conductor detects `Scenario Outline` / `Examples` and, if present, fills `scenario-outline-port-plan.template.md` and **blocks for user approval**.
3. Conductor produces a Draft via `migration-draft.template.md` and, unless `--approved-concept`, asks for approval.
4. `migrate-worker` writes the Kotlin test class.
5. `results-verifier` runs `phase: initial` (build + new test + legacy parity + Allure + `.editorconfig` + anti-patterns + parity).
6. On green initial: worker `task: delete-scenario` removes the migrated scenario from the `.feature`; verifier re-runs `phase: post-cleanup` (scenario gone + parity holds).
7. On green post-cleanup: journal + `_INDEX.md` row + three independent lesson questions (one at a time).
8. On block at either phase: surface blockers; offer revise / revert (`git checkout -- <feature_path>`, post-cleanup only — never silent revert) / abort.

## DO / DON'T

- DO: one scenario per run.
- DON'T: batch.
- DON'T: accept UI patterns or non-English strings.
- DON'T: accept `@ParameterizedTest`, `@MethodSource`, `@ValueSource`, `@CsvSource`, `@CsvFileSource`, `@EnumSource`, `@ArgumentsSource`, `@TestFactory` — multiple input sets go through a `private fun` invoked once per set. Reason: Allure parameterized-test reporting is unreliable.
- DON'T: append to lessons-learned without explicit `y`.

## Refuses

- See `migrate-conductor` refusals.

## Related

- `.github/agents/migrate-conductor.agent.md`
- `.github/agents/migrate-worker.agent.md`
- `.github/agents/results-verifier.agent.md`
- `.github/copilot/templates/migration-draft.template.md`
- `.github/copilot/templates/scenario-outline-port-plan.template.md`
- `.github/copilot/templates/allure-mapping.template.md`
- `.github/copilot/templates/migrated-test-header.template.md`
- `.github/copilot/journal/_TEMPLATE.md`
- `.github/copilot/journal/_INDEX.md`
