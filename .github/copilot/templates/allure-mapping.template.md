# Allure mapping — Cucumber tags → JUnit 5 annotations

Used inside the migration draft to enumerate exactly which annotations land on the new test. This table is the authority the verifier checks against.

| Source in Cucumber | Example | Annotation on JUnit 5 test | Import |
|--------------------|---------|-----------------------------|--------|
| `Feature:` line | `Feature: User login` | `@Feature("User login")` | `io.qameta.allure.Feature` |
| `Rule:` line | `Rule: Credential validation` | `@Story("Credential validation")` | `io.qameta.allure.Story` |
| Scenario tag `@story:<slug>` | `@story:login-success` | `@Story("login-success")` | `io.qameta.allure.Story` |
| Scenario tag `@epic:<slug>` | `@epic:identity` | `@Epic("identity")` | `io.qameta.allure.Epic` |
| Scenario name | `Scenario: Successful login returns a token` | `@DisplayName("Successful login returns a token")` | `org.junit.jupiter.api.DisplayName` |
| Scenario description block | (Gherkin narrative above steps) | `@Description("...")` | `io.qameta.allure.Description` |
| Scenario tag `@severity:critical` | `@severity:critical` | `@Severity(SeverityLevel.CRITICAL)` | `io.qameta.allure.Severity` + `io.qameta.allure.SeverityLevel` |
| Scenario tag `@severity:normal` | `@severity:normal` | `@Severity(SeverityLevel.NORMAL)` | same |
| Scenario tag `@severity:minor` | `@severity:minor` | `@Severity(SeverityLevel.MINOR)` | same |
| Scenario tag `@severity:trivial` | `@severity:trivial` | `@Severity(SeverityLevel.TRIVIAL)` | same |
| Scenario tag `@TMS-<id>` / `@tms:<id>` | `@TMS-1234` | `@TmsLink("TMS-1234")` | `io.qameta.allure.TmsLink` |
| Scenario tag `@BUG-<id>` / `@issue:<id>` | `@BUG-42` | `@Issue("BUG-42")` | `io.qameta.allure.Issue` |
| Free-form link tag `@link:<url>` | `@link:docs` | `@Link(name = "docs", url = "...")` | `io.qameta.allure.Link` |
| Categorical tag `@smoke` / `@regression` / `@api` / `@db` | `@smoke` | `@Tag("smoke")` | `org.junit.jupiter.api.Tag` |
| Step calls worth reporting | `Given user submits valid credentials` | `@Step("Submit valid credentials")` on the helper | `io.qameta.allure.Step` |

## Rules

- Every row filled in the draft's Allure mapping table **must** land in the generated test; missing any row is a verifier blocker.
- Strings are English. Translate or rewrite Cucumber strings when needed to keep the target test English.
- Defaults when a tag is absent and the user has no preference:
  - `@Severity(SeverityLevel.NORMAL)`
  - No `@Issue` / `@TmsLink`
  - No `@Link`
  - `@Epic` chosen to match the product capability under test; ask the user if ambiguous.
