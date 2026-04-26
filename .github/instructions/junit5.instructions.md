---
applyTo: "**/src/test/kotlin/**/*.kt"
---

# JUnit 5 rules

## Annotations

- `@Test` for every test method.
- `@DisplayName("…")` on every test class and test method — English, describes the behavior under test, not the implementation.
- `@Nested` for logical grouping inside a single test class.
- `@BeforeEach` / `@AfterEach` for per-test setup and teardown. `@BeforeAll` / `@AfterAll` only for expensive, immutable shared state.
- No JUnit 4 artifacts: no `@RunWith`, no `expected=`, no `@Rule`, no `@Ignore`. `@Disabled` is permitted only when the reason is recorded in the migration journal.

## Parameterization — DON'Ts and DOs

Plain `@Test` only. The ban applies to **every** test in this repo — migrated, authored, new, or refactored. Reason: Allure's parameterized-test reporting is unreliable. Per-iteration steps, attachments, and Epic/Feature/Story labels merge or duplicate across iterations, and per-row history breaks. Plain `@Test` methods keep one Allure node per case, with stable history, intact attachments, and predictable labels.

### DON'Ts (verifier-enforced — `anti-pattern-verifier` blocks unconditionally)

- **DON'T** use `@ParameterizedTest`.
- **DON'T** use `@MethodSource`, `@ValueSource`, `@CsvSource`, `@CsvFileSource`, `@EnumSource`, `@ArgumentsSource`.
- **DON'T** use `@TestFactory` / dynamic tests as a Test-Matrix workaround.
- **DON'T** use `@RepeatedTest` for input variation (allowed only for stress / flake-detection runs outside the regular suite).
- **DON'T** loop inside one `@Test` to assert multiple input sets — that hides per-case results from Allure too.
- **DON'T** wrap a single helper call in a `forEach` over a list of inputs from inside a `@Test` body — the per-call Allure step nesting still collapses.

### DOs

- **DO** write one `@Test` per behavior/input set, with its own Allure annotations.
- **DO** extract a `private fun runFor<Case>(inputs…)` that performs Arrange / Act / Assert for one input set. Call it **once per input set** from the `@Test` body when input sets are meaningfully grouped (e.g., merged Cucumber `Examples` rows). Each call still represents one logical case to the reader; the gain is removing test-code duplication, not collapsing Allure cases.
- **DO** name each `@Test` after its case (`returns404ForUnknownUserId`, not `getUserScenarios`).

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
