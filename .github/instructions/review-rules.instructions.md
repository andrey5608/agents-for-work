---
applyTo: "**"
---

# Review rules (consumed by `/review`)

Review is scoped to the diff under review. Every finding is severity-tagged (`blocker` / `major` / `minor` / `nit`) and anchored with `file:line`. Output is English.

## Rubric 1 — Kotlin idioms

- `val` over `var` where possible — `minor`.
- Null-safety: no gratuitous `!!`, no platform types leaking out of public test helpers — `major`.
- Data / sealed / object / enum classes used when appropriate; no faux-OOP singletons holding mutable state — `major`.
- Idiomatic collection APIs; no manual index loops where `map` / `filter` / `associateBy` works — `minor`.
- Scope functions used deliberately, not nested more than two deep — `minor`.

## Rubric 2 — JUnit 5

- Every test has `@DisplayName` — `minor`.
- No JUnit 4 artifacts (`@RunWith`, `expected=`, `@Rule`, `@Ignore`) — `blocker`.
- No `@ParameterizedTest`, `@MethodSource`, `@ValueSource`, `@CsvSource`, `@CsvFileSource`, `@EnumSource`, `@ArgumentsSource`, or `@TestFactory` introduced by **any** new test code (migrated or authored) — `blocker` (dispatch multiple input sets via a `private fun` invoked once per set from the `@Test` body; reason: Allure's parameterized-test reporting is unreliable).
- `@Nested` used for grouping when a class spans multiple scenarios — `minor`.
- `@Disabled` only with a journal reference — `major`.

## Rubric 3 — Cucumber

- One Given / When / Then idea per step — `major`.
- No logic inside `.feature` files — `blocker`.
- Step definitions use constructor-injection DI, never static state holders — `blocker`.
- Scenario names are complete English sentences — `minor`.
- Hooks scoped via tags rather than global where feasible — `minor`.

## Rubric 4 — Backend test architecture

- **Any introduction of `Page Object`, `WebDriver`, `Selenium`, `Selenide`, `Screen`, or UI-test pattern is a `blocker`.**
- Direct API / service / DB / queue exercise; WireMock / Testcontainers / REST-assured usage is explicit — `major` if opaque.
- External services are stubbed deterministically; no live network calls from tests — `blocker`.

## Rubric 5 — Flaky risk

- `Thread.sleep` is a `blocker` in test code.
- Awaitility or equivalent with an explicit `atMost(...)` and polling interval — `major` if timeouts are unbounded.
- Tests must be order-independent — `major` on ordering assumptions.

## Rubric 6 — Assertions

- AssertJ `assertThat(...)` preferred over bare JUnit asserts — `minor`.
- Meaningful assertion description when not self-evident — `minor`.
- No tautological assertions — `major`.
- Exception testing via `assertThatThrownBy { … }` — `minor` for `try/catch`.

## Rubric 7 — Test data

- Fixtures over magic literals — `minor`.
- No real credentials / PII / production endpoints hardcoded — `blocker`.
- Time-sensitive tests use a clock abstraction; no `LocalDateTime.now()` inline — `major`.

## Rubric 8 — Allure

- `@Epic` / `@Feature` / `@Story` / `@Severity` / `@DisplayName` / `@Description` present — `major` per missing one.
- `@Step` on user-facing action helpers — `minor`.
- `@Issue` / `@TmsLink` when the scenario references an external ticket — `minor`.
- All Allure strings in English — `major`.

## Rubric 9 — `.editorconfig` compliance

- Indentation style / size, charset, line ending, final newline, trailing whitespace — `major` per violation class.

## Rubric 10 — Security

- No hardcoded secrets — `blocker`.
- No secrets logged, including in test output — `blocker`.
- Safe deserialization (no `ObjectInputStream` on untrusted input) — `blocker`.

## Cross-reference

- Consult `.github/copilot/knowledge/lessons-learned/review.md` before starting. If a lesson entry matches the current diff, cite it in the finding.
- Offer to append new lessons **only** on user `y`.
