# Migration pitfalls — known traps

Append-only catalog of traps encountered during Cucumber → Kotlin + JUnit 5 migrations. `/migrate` reads this file during the Draft step and surfaces matched pitfalls so the worker is warned upfront.

## Entry format

```markdown
## <short title>
Added: <YYYY-MM-DD>
Source journal: `.github/copilot/journal/<file>.md`

### Symptom
<what goes wrong, how you notice>

### Cause
<why it happens>

### Mitigation
<what to do instead — short and imperative>

### Applies when
- <bullet list of signals that this pitfall is relevant to the current migration>
```

## Standing traps (always check, not entries)

- Cucumber shared scenario state migrated as a top-level `object` — reintroduces the same cross-test contamination it had in Cucumber. Use per-test fixtures or JUnit 5 extensions.
- `@Before("@tag")` hook silently dropped during migration — preserve its behavior either as `@BeforeEach` logic or an extension that checks a `@Tag(...)`.
- `DataTable` column order assumed — re-read the feature; columns may have been added.
- Async step verified with `Thread.sleep` — migrate to Awaitility `atMost(Duration)` with an explicit polling interval; never keep the sleep.
- Allure `@Step` applied to non-user-facing private methods — cluttered report. `@Step` goes on semantic actions only.
- Test class named after the feature file when the scenario under test is a specific behavior — name after the behavior, not the file.

---

<!-- Entries go below; newest at the bottom. The file starts empty by design. -->
