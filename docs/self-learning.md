# Self-learning — how the knowledge base grows and stays useful

GitHub Copilot has no persistent memory between sessions. The toolchain simulates self-learning by maintaining **append-only files in the repo** that are auto-loaded as Copilot instructions on every run. Agents propose additions; only the user approves writes.

## Catalog

Three user-facing lessons-learned files (append-only, read by the corresponding skill):

| File | Consumed by |
|------|-------------|
| `.github/copilot/knowledge/lessons-learned/migration.md` | `migrate-conductor`, `migrate-worker` (via `migration-knowledge.instructions.md`) |
| `.github/copilot/knowledge/lessons-learned/cucumber-debug.md` | `/debug-cucumber` (via `cucumber.instructions.md`) |
| `.github/copilot/knowledge/lessons-learned/review.md` | `/review` (via `review-rules.instructions.md`) |

Two migration-specific catalogs (also append-only):

| File | Purpose |
|------|---------|
| `.github/copilot/knowledge/migration-patterns.md` | Canonical Cucumber → Kotlin + JUnit 5 mappings that have been validated in this repo. |
| `.github/copilot/knowledge/migration-pitfalls.md` | Known traps surfaced at the Draft step so the worker is warned upfront. |

## Three ways to write an entry

1. **Agent-proposed, user-confirmed** — the end-of-run prompt inside `/review`, `/debug-cucumber`, or `/migrate` asks once whether to append. This is the default path.
2. **User-initiated manual entry** — run `/add-lesson-learned [catalog]` at any time to record past solutions or recurring issues outside of a run. The agent collects the fields, previews the entry, and appends only after an explicit `y`. See `.github/skills/add-lesson-learned/SKILL.md`.
3. **Autonomous-mode stub (narrow)** — `/migrate-auto` may append a single entry to `lessons-learned/migration.md` without live `y` **only** when the retry loop exposed a novel failure class that matches no existing entry. The stub is marked `Applies to: migration (autonomous)` and `Review: pending`; the curator promotes or discards it at the next rotation. This is the only write path that does not require an explicit user confirmation.

All three paths use the same entry formats and the same anti-pattern refusals.

## Write flow (agent-proposed, user-confirmed)

1. Agent finishes a run successfully (review, debug, migration).
2. Agent identifies a candidate lesson: something non-obvious, not already covered by an `instructions/*` file, and specific enough to be actionable.
3. Agent prints a preview of the proposed entry.
4. Agent asks **exactly one** yes/no question, for example:

   > Append this to `lessons-learned/migration.md`? (y / n)

5. On `y`: the entry is appended to the file using the shared entry format (below). On `n` or no answer: nothing is written.

If an entry belongs in both `lessons-learned/*.md` and, say, `migration-patterns.md`, the agent asks separately for each file.

## Shared entry format

```markdown
## <YYYY-MM-DD> <short title>
Context: <what was being done>
Observation: <what was surprising, missed, or worked well>
Rule: <actionable guideline phrased in imperative English>
Applies to: migration | cucumber-debug | review | migration (autonomous)
Review: <omit unless this is an autonomous-mode stub — set to "pending" until a curator promotes or discards it>
Source: <journal link / PR link / chat reference>
```

Anti-patterns that agents must refuse to write:

- Vague wisdom (“be careful with DI”).
- Restatements of existing instruction files (“always use `val`”).
- Event logs (“migrated the login feature on 2026-04-19”) — belongs in the journal.
- Non-English prose.

## Read flow (every relevant run)

Each skill loads its catalog at the start of the run. The run output **cites** matched entries by anchor (for example `see lessons-learned/review.md#2026-04-19-sleep-in-cucumber-step`). If no entry matches, the run proceeds without citations.

## Curator rotation (once per sprint)

Files grow. A curator pass keeps them useful.

1. **Skim** each lessons file newest-first.
2. **Promote** recurring lessons:
   - A stable rule that every review should enforce → move it into `.github/instructions/review-rules.instructions.md` (or the appropriate `instructions/*` file). Remove the lesson from `lessons-learned/review.md` once promoted.
   - A recurring successful Cucumber → JUnit 5 shape → move it into `migration-patterns.md` as a named pattern. Remove the original lesson.
   - A recurring failure pattern → move it into `migration-pitfalls.md` or, if stable enough, into `.github/instructions/cucumber.instructions.md`.
3. **Merge duplicates.** Combine entries that say the same thing; keep the clearest phrasing; preserve source links.
4. **Retire superseded entries.** When a rule is now enforced by the build or by an `instructions/*` file, delete the lesson. Keep a single "retired on <date> because <reason>" line in the curator commit message, not in the file.
5. **Commit with a clear message.** Example: `knowledge: curator pass 2026-Q2 — promoted 3 lessons, merged 2, retired 5`.

## What self-learning does NOT do

- It does not modify the target project's test code without going through the normal Draft → approval → Final migration flow.
- It does not write entries without explicit user `y`, with the single narrow exception described above for autonomous-mode `Review: pending` stubs.
- It does not remove or rewrite prior entries implicitly — only the curator rotation does that, intentionally and in a commit.
- It does not cross-contaminate catalogs: a migration lesson does not get written into `lessons-learned/review.md` even if it would be useful there. If a lesson is dual-purpose, the agent asks twice.
