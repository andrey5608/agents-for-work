# sample-cucumber — smoke-test project

A minimal backend autotest project used to exercise the Copilot toolchain end-to-end. Not copied into target repos.

## Stack

- Java 17 runtime
- Kotlin 1.9.x sources under `src/test/kotlin/`
- Cucumber 7 with JUnit 5 Platform (`cucumber-junit-platform-engine`)
- PicoContainer DI (`LoginContext`, `LoginApiClient` injected into `LoginSteps` via constructor)
- Allure 2.27 reporting via `allure-cucumber7-jvm`
- AssertJ for assertions
- Maven build

## Layout

- `src/test/resources/features/login.feature` — one plain `Scenario` (happy path: successful login) and one `Scenario Outline` with four `Examples` rows (wrong password, unknown user, missing username, missing password).
- `src/test/kotlin/steps/LoginSteps.kt` — step definitions (constructor-injected).
- `src/test/kotlin/steps/LoginContext.kt` — scoped scenario state.
- `src/test/kotlin/steps/LoginApiClient.kt` — fake backend client annotated with Allure `@Step`.
- `src/test/kotlin/runners/LoginCucumberTest.kt` — JUnit 5 Suite pointing at the feature resources.

## How it is used by the toolchain

- `/review` — run in this directory against a hypothetical change.
- `/explain-test src/test/resources/features/login.feature:Successful login returns a session token` — returns the structured explanation.
- `/debug-cucumber src/test/resources/features/login.feature` — emits the step → method table for both scenarios.
- `/migrate src/test/resources/features/login.feature --scenario="Successful login returns a session token"` — plain scenario path; expect Draft → approval → Worker writes a new Kotlin + JUnit 5 test → Verifier gates all six checks.
- `/migrate src/test/resources/features/login.feature --scenario="Invalid credentials are rejected"` — Scenario Outline path; expect a `scenario-outline-port-plan.template.md` to be produced and approval to be demanded before any code is written.

## Running the Cucumber suite manually

```
cd migration-examples/sample-cucumber
mvn -q test
```

(The project is self-contained. The fake `LoginApiClient` has no external dependencies.)
