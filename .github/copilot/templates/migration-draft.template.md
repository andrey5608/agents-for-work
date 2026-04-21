# Migration draft — <feature-file>:<scenario-name>

Date: <YYYY-MM-DD>
Feature: <path/to/file.feature>
Scenario: "<exact scenario name>"
Is Scenario Outline: <yes | no>
Outline port plan: <path or N/A>

## Test concept

<one English paragraph describing what behavior the new test verifies and why it matters>

## Target test class

- Path: `src/test/kotlin/<package-path>/<ClassName>.kt`
- Class: `<ClassName>`
- Package: `<package>`
- Method signature(s):
  - `fun <methodName>()` — `@Test`, `@DisplayName("...")`

## Allure annotation mapping

| Source in Cucumber | Value | Annotation on JUnit 5 test |
|--------------------|-------|-----------------------------|
| `Feature:` line    |       | `@Feature("...")`           |
| `Rule:` / `@story:`|       | `@Story("...")`             |
| Scenario name      |       | `@DisplayName("...")`       |
| `@severity:...`    |       | `@Severity(SeverityLevel....)` |
| `@TMS-...`         |       | `@TmsLink("...")`           |
| `@BUG-...`         |       | `@Issue("...")`             |
| `@smoke` etc.      |       | `@Tag("...")`               |
| Epic (chosen)      |       | `@Epic("...")`              |
| Description        |       | `@Description("...")`       |

## Preserved vs changed behavior

- Preserved: <bullet list — assertions, inputs, external interactions that stay identical>
- Changed: <bullet list with one-line rationale each>

## External dependencies

- WireMock stubs: <list / none>
- Testcontainers: <list / none>
- DB fixtures: <list / none>
- Test doubles / fakes: <list / none>

## Relevant prior lessons

- <bullet list of entries from `lessons-learned/migration.md`, `migration-patterns.md`, `migration-pitfalls.md` that apply, cited by anchor>

## Step bindings (pre-resolved)

| Step | Feature line | Kotlin method | File line |
|------|--------------|----------------|-----------|

## Parity counts (for results-verifier Gate 7)

These numbers drive the Gate 7 parity check in `results-verifier` and the cleanup-step sanity check in `migrate-conductor`. They are a fact about the scenario, not a preference — fill them from the `.feature` and the approved port plan, do not tune them to match the test.

- Expected Cucumber cases: `<1 for plain Scenario | total Examples rows for Outline>`
- Expected JUnit cases: `<1 for plain Scenario | split_rows + merge_cases for Outline>`
- Rows dropped: `<0 for plain Scenario | count of rows with disposition "drop" in port plan>`
- Port plan path: `<path, or N/A for plain Scenario>`

Sanity check (for Outline only): `Expected Cucumber cases == split_rows + merge_cases + dropped_rows`. If this does not hold, the port plan itself is inconsistent — fix the port plan before proceeding.

## Open questions for the user

- <bullet list; empty if none>

## Draft approval

- Requested on: <YYYY-MM-DD HH:MM>
- Approved by: <user statement or `--approved-concept=...` value>
- Approval notes: <free text or N/A>
