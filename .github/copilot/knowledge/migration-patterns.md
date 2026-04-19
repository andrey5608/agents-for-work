# Migration patterns — Cucumber → Kotlin + JUnit 5

Canonical, validated mappings harvested from successful migrations. Each pattern is one entry. `/migrate` reads this file during the Draft step and surfaces matched patterns to the user.

## Entry format

```markdown
## <short title>
Added: <YYYY-MM-DD>
Source journal: `.github/copilot/journal/<file>.md`

### Cucumber shape
<Gherkin fragment or shape description>

### Kotlin + JUnit 5 shape
<Kotlin fragment showing the canonical translation>

### Notes
- <bullet list of caveats, when to use, when NOT to use>
```

## Standing principles (always apply, not entries)

- `Given/When/Then` structure maps to `// Arrange / // Act / // Assert` separated by blank lines (no literal section comments).
- Step-definition helpers become private `@Step("…")` methods in the test class or shared helper class.
- Shared setup that lived in `@Before("@tag")` moves to `@BeforeEach` when cheap, or an extension using JUnit 5 `@RegisterExtension` when expensive or state-bearing.
- `DataTable` inputs become Kotlin `data class` fixtures built inline.
- `Scenario Outline` with `Examples` → **one `@Test` that calls a private helper per row**, never `@ParameterizedTest`.

---

<!-- Entries go below; newest at the bottom. The file starts empty by design. -->
