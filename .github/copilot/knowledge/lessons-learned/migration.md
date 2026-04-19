# Lessons learned — migration

Append-only catalog of lessons harvested from `/migrate` runs. Read by `migrate-conductor` and `migrate-worker` during every migration. New entries are written only after the user answers `y` at the end-of-run prompt.

## Entry format

```markdown
## <YYYY-MM-DD> <short title>
Context: <what was being migrated>
Observation: <what went wrong, what was ambiguous, or what worked unexpectedly well>
Rule: <actionable guideline phrased in imperative English>
Applies to: migration
Source: `.github/copilot/journal/<file>.md`
```

## Curator rotation

Once per sprint, compact this file per `docs/self-learning.md`:

- Fold recurring lessons into `migration-patterns.md` (success shapes) or `migration-pitfalls.md` (failure shapes) or `.github/instructions/*.instructions.md` (stable rules).
- Retire lessons whose rule has been absorbed into an instruction file.
- Keep the file focused on current, non-canonicalized wisdom.

---

<!-- Entries go below; newest at the bottom. The file starts empty by design. -->
