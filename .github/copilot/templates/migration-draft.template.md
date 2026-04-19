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

## Open questions for the user

- <bullet list; empty if none>

## Draft approval

- Requested on: <YYYY-MM-DD HH:MM>
- Approved by: <user statement or `--approved-concept=...` value>
- Approval notes: <free text or N/A>
