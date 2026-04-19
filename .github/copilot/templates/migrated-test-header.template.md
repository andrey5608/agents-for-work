# Migrated-test header

Every Kotlin test produced by `migrate-worker` begins with exactly one header line, nothing above it:

```kotlin
// migrated from features/<feature-file>.feature:<scenario-name> — journal: .github/copilot/journal/<YYYY-MM-DD>-<slug>.md
```

## Substitutions

- `<feature-file>` — the relative path fragment under `src/test/resources/features/` (or wherever features live in the target repo).
- `<scenario-name>` — the exact scenario name as it appears in the `.feature`.
- `<YYYY-MM-DD>` — migration date, same as in the journal.
- `<slug>` — lowercase-hyphenated slug of the scenario name; matches the journal filename.

## Rules

- Only this line. No additional explanatory block at the top.
- Never repeat this line inside the file; Kotlin class-level KDoc, if any, goes below it.
- Do not add inline comments inside test bodies; identifiers carry the weight.
- When the journal is not yet written (draft phase), use a placeholder path and fix it before the verifier gate.
