---
applyTo: "**/src/test/kotlin/**/*.kt"
---

# JUnit 5 rules

## Annotations

- `@Test` for every test method.
- `@DisplayName("…")` on every test class and test method — English, describes the behavior under test, not the implementation.
- `@Nested` for logical grouping inside a single test class.
- `@BeforeEach` / `@AfterEach` for per-test setup and teardown. `@BeforeAll` / `@AfterAll` only for expensive, immutable shared state.
- Do **not** use `@ParameterizedTest`, `@MethodSource`, `@ValueSource`, `@CsvSource`, `@EnumSource`, or any Test-Matrix construct for migrated tests. Parameterization belongs inside the test body as private helper invocations.
- No JUnit 4 artifacts: no `@RunWith`, no `expected=`, no `@Rule`, no `@Ignore`. `@Disabled` is permitted only when the reason is recorded in the migration journal.

## Assertions

- Prefer **AssertJ** (`assertThat(...)`) over `org.junit.jupiter.api.Assertions`.
- Every assertion carries a meaningful description (`as("expected …")`) when it is not self-evident.
- No tautological assertions (`assertThat(true).isTrue()`), no `assertThat(x).isEqualTo(x)`.
- For exceptions: `assertThatThrownBy { … }.isInstanceOf(T::class.java)` rather than `try/catch`.

## Waits and timing

- No `Thread.sleep`.
- For asynchronous assertions, use **Awaitility** with an explicit `atMost(...)` and a bounded polling interval.
- Avoid `@Timeout` unless the timeout represents a business requirement under test; do not use it as a flakiness band-aid.

## Structure

- One test class per production class under test, or per scenario family.
- Test method naming: imperative English describing the behavior, for example `returnsEmptyListWhenNoUsersExist`. Use `@DisplayName` for the human-readable form.
- Arrange / Act / Assert sections visually separated by a blank line.

## Allure

- Every test carries `@DisplayName`, `@Epic`, `@Feature`, `@Story`, `@Severity`, and `@Description` as appropriate. See `allure.instructions.md` for the full mapping.
