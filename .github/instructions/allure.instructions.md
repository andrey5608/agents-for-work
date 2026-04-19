---
applyTo: "**/src/test/kotlin/**/*.kt"
---

# Allure rules

Allure metadata is first-class. Every test must carry enough annotations for the report to be readable without opening the code.

## Mandatory on every test class / method

| Annotation | Placement | Value source |
|------------|-----------|--------------|
| `@Epic("…")` | class or method | Top-level business capability. |
| `@Feature("…")` | class or method | Cucumber `Feature:` line for migrated tests; manually chosen for new tests. |
| `@Story("…")` | method | Cucumber `Rule:` line, or scenario-tag `@story:…`. |
| `@Severity(SeverityLevel.…)` | method | Scenario tag `@severity:…`; default `NORMAL` when absent and no explicit guidance exists. |
| `@DisplayName("…")` | method | Scenario name for migrated tests; English sentence for new tests. |
| `@Description("…")` | method | Longer prose of the behavior under test when the display name is not enough. |

## Optional when applicable

| Annotation | When to use |
|------------|-------------|
| `@Link("…", url = "…")` | External documentation. |
| `@Issue("BUG-42")` | Scenario tag `@BUG-42` or `@issue:BUG-42`. |
| `@TmsLink("TMS-1234")` | Scenario tag `@TMS-1234` or `@tms:…`. |
| `@Step("…")` | User-facing action helpers in test or step-definition code so the report surfaces them. |
| `@Attachment` | For artifacts like request/response bodies captured during the run. |

## Tag-to-annotation mapping (Cucumber → JUnit 5)

- `@severity:critical` → `@Severity(SeverityLevel.CRITICAL)`
- `@severity:normal` → `@Severity(SeverityLevel.NORMAL)`
- `@severity:minor` → `@Severity(SeverityLevel.MINOR)`
- `@severity:trivial` → `@Severity(SeverityLevel.TRIVIAL)`
- `@TMS-<id>` / `@tms:<id>` → `@TmsLink("<id>")`
- `@BUG-<id>` / `@issue:<id>` → `@Issue("<id>")`
- `@smoke`, `@regression`, `@api`, `@db` → `@Tag("smoke")` etc. (JUnit 5 `@Tag`), plus propagate to Allure via `@Feature` / `@Story` when they carry business meaning.

See `.github/copilot/templates/allure-mapping.template.md` for the table used in migration drafts.

## Strings are English

`@DisplayName`, `@Description`, `@Step` values are English sentences. No other language, including transliteration.

## Verifier check

`migrate-verifier` inspects `target/allure-results/*.json` after running the new test and fails the gate if expected metadata is missing.
