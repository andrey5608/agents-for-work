# Auto-approval checklist — <feature-file>:<scenario-name>

Date: <YYYY-MM-DD HH:MM>
Feature: <path/to/file.feature>
Scenario: "<exact scenario name>"
Retry budget: <N>
Invocation: `<full command as received>`

## Criteria

Every criterion must resolve to `pass` for the Draft to be auto-approved. A single `fail` triggers fallback to interactive `/migrate` (criterion #7 refuses the run instead).

| # | Criterion | Result | Evidence |
|---|-----------|--------|----------|
| 1 | Scenario is plain `Scenario:`, OR it is a `Scenario Outline:` with an already-approved port plan. | pass \| fail | <path to approved plan, or `plain scenario`> |
| 2 | Every step resolves to exactly one method under `**/steps/**/*.kt` — no UNBOUND STEP, no ambiguous bindings. | pass \| fail | <step → method table excerpt, or list of unbound/ambiguous steps> |
| 3 | Allure mapping fully derivable from source (Feature, scenario name, severity, Story, Epic) — no `<...>` placeholders in the draft. | pass \| fail | <source → value, e.g. `Feature: User login → @Feature("User login")`> |
| 4 | No `migration-pitfalls.md` entry tagged `Severity: human-review` matches this scenario's signature. | pass \| fail | <matched anchors, or `none`> |
| 5 | No `auto-escalation-triggers` entry in `migration-knowledge.instructions.md` matches (custom DataTable converter, async assertion boundary, non-standard DI container, …). | pass \| fail | <matched trigger, or `none`> |
| 6 | Target test class path does not collide with an existing file. | pass \| fail | <target path + collision check result> |
| 7 | Clean baseline — `git status --porcelain src/test` reports no uncommitted changes under the test tree. | pass \| fail | <command output summary; on fail the run is refused, not downgraded> |
| 8 | `--approved-concept="..."` was passed, OR the Draft has zero `<...>` placeholders and zero open questions. | pass \| fail | <approved-concept value, or `draft: 0 placeholders, 0 open questions`> |

## Decision

- Outcome: `auto-approved` \| `fallback: criterion-<id>` \| `refused: criterion-<id>`
- Next action: <worker hand-off / interactive conductor resume / refuse>

## Retry log

Filled by the conductor after each verifier round. Omit if zero retries were needed.

| attempt | blockers (summary) | classifications | applied fix | outcome |
|---------|--------------------|-----------------|-------------|---------|
| 0       |                    |                 |             |         |

Classification vocabulary: `compile-error`, `missing-import`, `allure-missing`, `editorconfig`, `anti-pattern`, `test-assertion`, `legacy-red`, `infra-error`, `unknown`.

Outcome vocabulary: `green`, `retry`, `escalated: <reason>`, `budget-exhausted`.

## Novel failure classes observed

Listed only when the retry log shows a failure class that matches no existing entry in `lessons-learned/migration.md`. These become `Review: pending` stubs — the **only** autonomous-mode write to the knowledge base that does not require live `y`.

- <bullet list, or `none`>
