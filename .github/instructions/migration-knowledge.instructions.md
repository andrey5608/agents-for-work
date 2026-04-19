---
applyTo: "**"
---

# Migration knowledge base

When operating in the migration flow (`/migrate`, `migrate-conductor`, `migrate-worker`), the agent must consult and, when warranted and confirmed by the user, extend three append-only knowledge files:

- `.github/copilot/knowledge/lessons-learned/migration.md` — chronological lessons: what went wrong, what we now do instead.
- `.github/copilot/knowledge/migration-patterns.md` — canonical Cucumber → Kotlin + JUnit 5 mappings that have been validated in this repo.
- `.github/copilot/knowledge/migration-pitfalls.md` — known traps, keyed by symptom.

## Read phase (every migration)

1. Load `lessons-learned/migration.md` (most recent first).
2. Load `migration-patterns.md` to look up idioms for the scenario shapes at hand.
3. Load `migration-pitfalls.md` and check whether any listed trap matches the current feature (shared state, DI ambiguity, `DataTable` quirks, async boundaries).
4. Surface matched entries to the user inside the draft document under a **Relevant prior lessons** section.

## Write phase (post-verification only)

After the verifier produces a green report, the conductor asks the user:

> _Record a lesson from this migration? (y / n / skip)_

On `y`, append a single entry using the format in `docs/self-learning.md`. Never write without explicit `y`.

## Entry format

Entries are short, specific, and actionable. See `docs/self-learning.md` for the full template. Anti-patterns to avoid in entries:

- Vague wisdom (“be careful with DI”) — useless; refuse to write it.
- Restatements of the manual (“always use `val`”) — already covered by `kotlin.instructions.md`.
- Event logs (“migrated the login feature on 2026-04-19”) — belongs in the journal, not the lessons catalog.
