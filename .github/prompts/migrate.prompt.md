---
mode: 'agent'
description: 'Migrate one Cucumber scenario to Kotlin + JUnit 5 via conductor → worker → verifier with Draft approval.'
tools: ['codebase', 'edit', 'terminal', 'findTestFiles']
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

1. Enter `migrate-conductor` chat mode (see `.github/chatmodes/migrate-conductor.chatmode.md`).
2. The conductor detects Scenario Outline / Examples and, if present, fills `scenario-outline-port-plan.template.md` and blocks for user approval before anything else.
3. The conductor produces a Draft using `migration-draft.template.md` and, unless `--approved-concept` was provided, asks the user to approve it.
4. The conductor hands off to `migrate-worker` to write the Kotlin test class.
5. The conductor hands off to `migrate-verifier` to run build + new test + legacy test + Allure metadata + `.editorconfig` gates.
6. On green: the conductor writes the migration journal, updates `_INDEX.md`, and asks (one question at a time) whether to append lessons.
7. On block: the conductor surfaces the verifier's blocker list and offers to revise or abort.

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

- `.github/chatmodes/migrate-conductor.chatmode.md`
- `.github/chatmodes/migrate-worker.chatmode.md`
- `.github/chatmodes/migrate-verifier.chatmode.md`
- `.github/copilot/templates/migration-draft.template.md`
- `.github/copilot/templates/scenario-outline-port-plan.template.md`
- `.github/copilot/templates/allure-mapping.template.md`
- `.github/copilot/templates/migrated-test-header.template.md`
- `.github/copilot/journal/_TEMPLATE.md`
- `.github/copilot/journal/_INDEX.md`
