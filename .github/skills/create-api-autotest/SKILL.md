---
name: create-api-autotest
description: Delegate to `api-test-author` to write a Kotlin + JUnit 5 API test class for a specified set of endpoints in a given module, mirroring the module's existing architectural scheme (HTTP client, base class, fixtures, Allure conventions). Use when the user asks to create, author, or generate a new API test for a module / endpoint set, or runs /create-api-autotest.
allowed-tools: shell
---

# /create-api-autotest

Author one new test class under the target module covering a specified list of API endpoints. Mirrors the module's existing scheme (HTTP client, base class, fixtures, auth, Allure convention) — does not invent a new style. Parameterization is **not** mirrored: every test is plain `@Test`, regardless of what the module's existing tests do.

## Usage

```
/create-api-autotest --module=<path> --endpoints="METHOD /path, METHOD /path, ..."
/create-api-autotest --module=<path> --spec=<path>
/create-api-autotest --module=<path> --endpoints="..." --approved-concept="<inline note or path>"
```

## Arguments

- `--module=<path>` — required. Directory with `src/test/kotlin/**`, Maven submodule path (`modules/<name>`), or a Kotlin package under `src/test/kotlin/`.
- `--endpoints="..."` — comma-separated list, each `METHOD /path`. Example: `GET /users/{id}, POST /users, DELETE /users/{id}`.
- `--spec=<path>` — alternative to `--endpoints`. OpenAPI / JSON / YAML / plain-text. Endpoints extracted from it.
- `--approved-concept="..."` — optional. Short-circuits Draft approval; recorded in the journal.

Exactly one of `--endpoints` or `--spec` must be present.

## Behavior

1. Delegate to `api-test-author` (`.github/agents/api-test-author.agent.md`).
2. Locate existing tests under `<module>/src/test/kotlin/**` and extract the architectural scheme. No tests → ask for sibling module; none offered → refuse.
3. Resolve the endpoint list (HTTP method, path, parameters, request/response shapes, auth). Ambiguities block with a targeted question. Nothing is guessed.
4. Fill `.github/copilot/templates/api-test-draft.template.md`; unless `--approved-concept`, ask for approval.
5. Write one Kotlin test class under `src/test/kotlin/...` following module conventions. Plain `@Test` only; multi-input methods dispatch via private helpers.
6. Hand off to `results-verifier` with `source: authored`. Gate 3 is `skipped`; Gate 6 (anti-pattern) applies the same rules as for migration.
7. On green: journal entry `Mode: authored`, `_INDEX.md` row, three independent lesson-harvest questions.
8. On block: surface verifier `blockers[]`, offer revise / abort.

## DO / DON'T

- DO: mirror the module's HTTP client, base class, fixtures, auth wiring, Allure convention.
- DO: one test class per run.
- DO: keep multi-endpoint scope tight — endpoints belonging together in one module.
- DON'T: invent a new architectural scheme.
- DON'T: use `@ParameterizedTest`, `@MethodSource`, `@ValueSource`, `@CsvSource`, `@CsvFileSource`, `@EnumSource`, `@ArgumentsSource`, `@TestFactory` — `private fun` per input set instead. Reason: Allure parameterized-test reporting is unreliable.
- DON'T: modify production code.
- DON'T: drop the module's base class to "simplify".

## Refuses

- Module has no tests AND no sibling module offered.
- Endpoint entry missing method or path.
- UI pattern requested.
- Request to bypass Allure or `.editorconfig` gates.
- Request to drop the module's base class.
- Request to modify production code.

## Related

- `.github/agents/api-test-author.agent.md`
- `.github/agents/results-verifier.agent.md` — reused with `source: authored`.
- `.github/copilot/templates/api-test-draft.template.md`
- `.github/copilot/journal/_TEMPLATE.md`
