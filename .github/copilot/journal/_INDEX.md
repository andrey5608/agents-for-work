# Migration journal index

Chronological record of every migration executed through `/migrate`. Newest entries on top.

| Date | Feature | Scenario | Journal | Status | New test class |
|------|---------|----------|---------|--------|----------------|
|      |         |          |         |        |                |

## How to read this index

- **Status** is the final verdict of the verifier gate: `green` (all gates passed), `blocked` (verifier rejected), `rolled-back` (accepted then reverted due to later findings).
- **Journal** links to the per-migration file under `.github/copilot/journal/`.
- **New test class** is the Kotlin class produced by the migration.

## How to add an entry

`migrate-conductor` appends a row at the top of the table on successful completion. No manual edits are expected; if the table drifts, run `/migrate --reconcile-index` (future work) or fix by hand and note the reason in a commit.
