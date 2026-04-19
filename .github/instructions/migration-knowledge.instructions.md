---
applyTo: "**"
---

# Migration knowledge base

When operating in the migration flow (`/migrate`, `/migrate-auto`, `migrate-conductor`, `migrate-conductor-auto`, `migrate-worker`), the agent must consult and, when warranted and confirmed by the user, extend three append-only knowledge files:

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

## Auto-escalation triggers (autonomous mode only)

`migrate-conductor-auto` checks these as criterion #5 of the auto-approval policy. Any match forces fallback to the interactive conductor — the autonomous path does not have the judgment headroom to resolve them safely.

- **Custom `DataTable` converter in play.** The scenario's steps consume `DataTable`, `List<Map<...>>`, or a custom `io.cucumber.java.DataTableType` / `@DefaultDataTableCellTransformer`. Porting the row shape is a design call, not a mechanical copy.
- **Async assertion boundary.** The scenario relies on eventual consistency — polling, message queues, background workers. Picking the right Awaitility timeout / poll cadence requires the user's knowledge of SLAs.
- **Non-standard DI container.** The project uses something other than PicoContainer / Spring for step injection (e.g., Guice, Koin), or wires shared state via a static holder. JUnit 5's lifecycle differs enough that the equivalence is non-obvious.
- **Side-effecting `@Before` / `@After` hooks with ordering dependencies.** Hook order (`order = N`) that materially changes behavior — a mechanical copy would silently reorder.
- **Step definitions span multiple files via inheritance.** The target step is defined on a base class or a trait; resolving the real implementation needs structural judgment.
- **External fixture not reachable from the autonomous runner** (network-bound secrets, VPN-only DBs, license-gated containers). The verifier's legacy-parity gate will flake without live access.
- **Scenario touches global singletons** (a shared JVM-level cache, a `static` registry, `System.setProperty`). Ordering across `@Test` methods in JUnit 5 is not guaranteed the same way Cucumber orders scenarios.

When adding a new trigger, keep it observable from source (regex / AST scan) so the conductor can actually check it before writing the draft. Triggers that require runtime probing belong in `migration-pitfalls.md` tagged `Severity: human-review` instead.
