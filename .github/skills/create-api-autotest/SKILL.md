---
name: create-api-autotest
description: Delegate to the `api-test-author` agent to write a Kotlin + JUnit 5 API test class for a specified set of endpoints in a given module, mirroring the module's existing architectural scheme (HTTP client, base class, fixtures, Allure conventions). Use when the user asks to create, author, or generate a new API test for a module / endpoint set, or runs /create-api-autotest.
allowed-tools: shell
---

# /create-api-autotest

Author one new test class under the target module covering a specified list of API endpoints. The agent mirrors the module's existing architectural scheme (HTTP client, base class, fixtures, auth wiring, Allure convention) — it does not invent a new style. Parameterization is **not** mirrored: every test is plain `@Test` regardless of what the module's existing tests do (repo-wide ban on `@ParameterizedTest`; see `junit5.instructions.md`).

## Usage

```
/create-api-autotest --module=<path> --endpoints="METHOD /path, METHOD /path, ..."
/create-api-autotest --module=<path> --spec=<path>
/create-api-autotest --module=<path> --endpoints="..." --approved-concept="<inline note or path>"
```

## Arguments

- `--module=<path>` — required. Any of:
  - directory with `src/test/kotlin/**` under it,
  - Maven submodule path (`modules/<name>`),
  - Kotlin package path under `src/test/kotlin/`.
- `--endpoints="..."` — comma-separated list, each element `METHOD /path`. Example: `GET /users/{id}, POST /users, DELETE /users/{id}`.
- `--spec=<path>` — alternative to `--endpoints`. Path to an OpenAPI / JSON / YAML / plain-text file. The agent extracts the endpoint list from it.
- `--approved-concept="..."` — optional. Short-circuits the Draft-approval gate; the value is recorded in the journal.

Exactly one of `--endpoints` or `--spec` must be present.

## Behavior

1. Delegate to the `api-test-author` agent (see `.github/agents/api-test-author.agent.md`).
2. The agent locates existing tests in `<module>/src/test/kotlin/**` and extracts the module's architectural scheme. If the module has no tests, it asks for a sibling module to mirror and refuses otherwise.
3. The agent resolves the endpoint list — HTTP method, path, parameters, request/response shapes, auth requirement. Ambiguities block with a targeted question. Nothing is guessed.
4. The agent fills `.github/copilot/templates/api-test-draft.template.md` and, unless `--approved-concept` was passed, asks for approval.
5. The agent writes one Kotlin test class under `src/test/kotlin/...`, following the module's conventions (client, base class, fixtures, Allure). Tests are plain `@Test`; multi-input methods dispatch via private helpers — never `@ParameterizedTest`.
6. The agent hands off to `results-verifier` with `source: authored`. Gate 3 (legacy parity) is `skipped`; Gate 6 (anti-pattern) applies the same rules as for migration — `@ParameterizedTest` is rejected for authored tests too.
7. On green: the agent writes a journal entry with `Mode: authored`, updates `_INDEX.md`, and asks the three independent lesson-harvest questions.
8. On block: the agent surfaces the verifier's `blockers[]` and offers to revise or abort.

## Invariants restated

- English output only.
- Backend only — no UI patterns.
- One test class per run; multiple endpoints are fine as long as they belong to the same module and belong together.
- Plain `@Test` only — repo-wide ban on `@ParameterizedTest`, `@MethodSource`, `@ValueSource`, `@CsvSource`, `@CsvFileSource`, `@EnumSource`, `@ArgumentsSource`, `@TestFactory`. Multiple input sets are dispatched via a `private fun` invoked once per set from the `@Test` body. Reason: Allure's parameterized-test reporting is unreliable.
- All Allure metadata explicit on class and methods.
- `.editorconfig` honored on every write.
- No production-code changes. If the test cannot be written without touching prod, the agent stops and surfaces the gap.
- Draft → approval → Final unless `--approved-concept` was passed.
- Lessons-learned writes require explicit `y`.

## Refusals

- Module has no existing tests **and** no sibling module was provided — refuse.
- Endpoint entry missing method or path — refuse that entry; ask for it.
- Request to introduce a UI pattern — refuse.
- Request to bypass Allure or editorconfig gates — refuse.
- Request to drop the module's base class or fixtures to "simplify" — refuse; mirroring the existing scheme is the whole point.
- Request to modify production code from this command — refuse; direct the user to stage the prod change separately.

## Related files

- `.github/agents/api-test-author.agent.md`
- `.github/agents/results-verifier.agent.md` (reused with `source: authored`)
- `.github/copilot/templates/api-test-draft.template.md`
- `.github/copilot/journal/_TEMPLATE.md`
