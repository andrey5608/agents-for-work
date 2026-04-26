# API test draft — <module> / <test-class-name>

Date: <YYYY-MM-DD>
Module: <path>
Endpoint source: `--endpoints="..."` | `--spec=<path>`
Endpoints requested:
- <METHOD> <path>
- <METHOD> <path>

## Target test class

- Path: `src/test/kotlin/<package-path>/<ClassName>.kt`
- Class: `<ClassName>`
- Package: `<package>`
- Rationale for naming: <cite one existing test in the module that follows the same convention — `file:line`>

## Architectural scheme (extracted)

| Signal | Value | Evidence (`file:line`) |
|--------|-------|-------------------------|
| HTTP client | <REST-assured / WebTestClient / OkHttp / Ktor / custom ApiClient> | |
| Base class / extension | <AbstractApiTest / @SpringBootTest / none — standalone> | |
| Fixture loading | <builders / `resources/fixtures/*.json` / factory method> | |
| Auth wiring | <Bearer header / API key / mTLS / none> | |
| Assertion style | <AssertJ / REST-assured body / JSON-Path / custom> | |
| Parameterization | always plain `@Test` + private helpers (repo-wide ban on `@ParameterizedTest`, see `junit5.instructions.md`) | n/a — fixed by policy |
| Allure convention | <Epic/Feature/Story values typical for the module; default Severity> | |
| Error-response shape | <ErrorResponse DTO / raw JSON path / enum values in use> | |
| External deps | <WireMock stubs / Testcontainers / DB seed hooks> | |

## Per-endpoint test plan

| # | Endpoint | Scenario | JUnit 5 method | Allure `@DisplayName` | Notes |
|---|----------|----------|-----------------|------------------------|-------|
| 1 | `GET /users/{id}` | happy path — existing id | `returns200AndUserBodyForExistingId` | "Returns 200 with user payload for an existing id" | |
| 2 | `GET /users/{id}` | 404 — unknown id | `returns404WhenUserDoesNotExist` | "Returns 404 when the user id is unknown" | |
| 3 | `POST /users` | happy path | `returns201AndLocationHeaderForValidPayload` | "Returns 201 and Location header for a valid creation request" | |
| 4 | `POST /users` | 400 — missing required field | `returns400WhenUsernameIsMissing` | "Returns 400 when the username field is missing" | |

Negative-case selection must follow what the module's existing tests already cover for similar endpoints — do not invent error classes the module has never tested.

## Allure annotation plan

Class-level:

| Annotation | Value |
|------------|-------|
| `@Epic`     | |
| `@Feature`  | |
| `@Tag("api")` | yes / no |

Method-level (per row in the table above):

| Annotation | Source of value |
|------------|-----------------|
| `@DisplayName` | table column |
| `@Severity` | module default, or elevate for happy-path contract tests |
| `@Story` | resource or capability under test (e.g., "User read", "User create") |
| `@Description` | one sentence — when the display name doesn't fully convey intent |
| `@TmsLink` / `@Issue` | when the user supplied a ticket |

## External dependencies

- WireMock stubs to register: <list + file paths if reused>
- Testcontainers: <list / none — reuse existing starter>
- Fixtures: <list + paths>
- Test doubles / fakes: <list / none>

## Input grouping (when a `@Test` exercises multiple input sets)

Always plain `@Test`. List below any method that calls a private helper for multiple input sets, and the `private fun` shape.

| `@Test` method | Private helper | Input sets dispatched |
|-----------------|-----------------|------------------------|
| `<methodName>` | `runFor<Case>(args…)` | <list, one row per call> |

If no method needs grouping, leave the table empty.

## Open questions for the user

- <bullet list; empty if none>

## Draft approval

- Requested on: <YYYY-MM-DD HH:MM>
- Approved by: <user statement or `--approved-concept=...` value>
- Approval notes: <free text or N/A>
