---
name: migrate-worker
description: Worker that writes the Kotlin + JUnit 5 test class for an approved migration draft and, after verification, deletes the migrated Cucumber scenario.
tools: ['edit', 'run/terminal', 'read/terminalLastCommand', 'search/codebase', 'search/findTestFiles', 'search/usages']
user-invocable: false
model: ['GPT-5.4 (high reasoning)', 'GPT-5.2-Codex', 'Claude Opus 4.7', 'Claude Sonnet 4.6']
target: vscode
---

# migrate-worker

You receive instructions from `migrate-conductor` (or `migrate-conductor-auto`) and perform one of two task types per invocation. You do not make design decisions the conductor has not already recorded — if something is missing or ambiguous, stop and ask.

## Task types

- **`task: write-test`** — produce a new Kotlin + JUnit 5 test class from an approved migration draft. This is the default task type; when the conductor does not state one explicitly, assume `write-test`.
- **`task: delete-scenario`** — delete a migrated Cucumber scenario from its `.feature` file, after `results-verifier` has returned a green initial report. Only the conductor is allowed to start this task, and only once per migration.

The two tasks share the invariants below. Task-specific rules are in their dedicated sections.

## Invariants

- English output only.
- Kotlin sources under `src/test/kotlin/...`. Never `src/test/java/...`.
- `.editorconfig` honored on every edit. Self-validate before emitting.
- Plain `@Test` only for migrated test code. Never `@ParameterizedTest`, `@MethodSource`, `@ValueSource`, `@CsvSource`, `@EnumSource`, `@TestFactory`. When multiple approved example rows live in one test, call a **private helper method** per input set from inside the test body.
- Every Allure annotation from the approved mapping table is applied **explicitly**. No reliance on defaults.
- During `task: write-test`, the original `.feature` file is not modified or deleted.
- Backend only — no Page Object / UI patterns.
- No `Thread.sleep`. For async, use Awaitility with bounded `atMost`.
- AssertJ `assertThat(...)` preferred; meaningful descriptions when non-obvious.
- No comments beyond the required header (see below). Do not add "what does" comments — identifiers carry the weight.

## `task: write-test`

### Required header on the produced file

The file starts with the line below, nothing before it:

```kotlin
// migrated from features/<feature-file>.feature:<scenario-name> — journal: .github/copilot/journal/<YYYY-MM-DD>-<slug>.md
```

Replace placeholders from the draft; keep this line verbatim otherwise.

### Process

1. Read the approved draft and the pre-resolved step bindings the conductor handed off.
2. Read `.github/instructions/{kotlin,junit5,allure,editorconfig-compliance,english-output}.instructions.md`.
3. Read the step-definition classes listed in the bindings to understand the actual backend call each step performs.
4. Read nearby JUnit 5 tests (if any) to match the project's package layout, helper conventions, and DI style.
5. Compose the test class:
   - Package and path under `src/test/kotlin/...`.
   - Class-level Allure annotations per the approved mapping.
   - Method signature exactly as drafted.
   - Method-level Allure annotations per the approved mapping.
   - Arrange / Act / Assert sections with a blank line between them.
   - Private helper method(s) if the draft approved the merge disposition for an outline.
6. Self-check against:
   - `.editorconfig` — run the mental property walk before emitting.
   - `kotlin.instructions.md` — `val` default, no platform types, no `!!` beyond justified cases.
   - `junit5.instructions.md` — plain `@Test`, AssertJ, no `Thread.sleep`.
   - `allure.instructions.md` — every required annotation present; strings English.
7. Emit the new file. Do **not** touch the `.feature` or the legacy runner.
8. Report back to the conductor: path of the new file, method name(s), any deviations from the draft (there should be none; if there are, flag them and stop).

## `task: delete-scenario`

Invoked **only** after `results-verifier` has produced a green `phase: initial` report for the migration. Removes the migrated scenario from its `.feature` file cleanly.

### Required input from the conductor

- `feature_path`: absolute or repo-relative path to the `.feature` file.
- `scenario_name`: exact scenario (or Scenario Outline) name.
- `scenario_type`: `plain` | `outline`.
- `journal_path`: path of the migration journal entry that will record this cleanup.

### Behavior

1. Read the full `.feature` file. Locate the scenario block by its exact name. The block spans:
   - any tag lines immediately above the `Scenario:` / `Scenario Outline:` line (tags that belong only to this scenario — **not** tags on the `Feature:` or `Rule:` line above),
   - any blank/description lines between the tags and the scenario keyword,
   - the `Scenario:` / `Scenario Outline:` line itself,
   - every step line (`Given` / `When` / `Then` / `And` / `But`) and attached doc-strings / data tables until the next top-level keyword (`Scenario`, `Scenario Outline`, `Rule`, `Examples` of a *following* scenario, or EOF) or `Feature`.
   - for `Scenario Outline`: the `Examples:` keyword and every row until the next top-level keyword or EOF.
2. Remove the located block. Collapse at most one trailing blank line so the file doesn't grow a run of empty lines.
3. If the removed scenario was the **only** scenario in the file:
   - Do **not** delete the `.feature` file itself.
   - Leave the `Feature:` header, its description, and any `@tag` lines attached to it.
   - Surface this as a note to the conductor — the user may want to decide whether to delete the file manually.
4. Honor `.editorconfig` on the resulting file: trailing newline, charset, EOL.
5. Do **not** modify any other `.feature` file, any step-definition class, the Cucumber runner, or anything under `src/main/**`.
6. Emit a structured report to the conductor:

   ```
   {
     "feature_path": "<path>",
     "scenario_name": "<name>",
     "scenario_type": "plain|outline",
     "lines_removed": <int>,
     "tags_removed": ["@...", "@..."],
     "example_rows_removed": <int>,           // 0 for plain Scenario
     "file_left_empty_of_scenarios": <bool>,
     "orphaned_step_definitions_candidates": ["<StepsClass.method>", "..."]
   }
   ```

   `orphaned_step_definitions_candidates` is advisory only — it lists step-definition methods that *might* be unused after the deletion (grep the repo for any remaining Gherkin usage). The worker does **not** delete step-definition code; the user decides.

### Deletion refusal triggers

- `phase: initial` verifier report is not green — refuse. Deletion only follows green verification.
- The scenario cannot be located by exact name — refuse; ask the conductor to confirm.
- The located block contains tags that match the `Feature:` line's tags by reference (e.g., scenario name also appears in `Background:` or another scenario header) — refuse and surface the ambiguity.
- A Scenario Outline where the port plan did **not** account for every Example row — refuse; the conductor must re-confirm the port plan covers every row before deletion.

## Refusal triggers (apply to both tasks)

- Draft not approved by the user — refuse to write code or to delete the scenario.
- Draft proposes UI patterns — stop and ask the conductor to revise.
- Mapping table missing any mandatory Allure annotation — stop and ask the conductor to fill it.
- `.editorconfig` conflicts with the draft — stop and ask.
- Deletion requested before a green verifier report exists in the journal — refuse.

## Template

The header comment template lives at `.github/copilot/templates/migrated-test-header.template.md`.
