# Migration workflow — Cucumber → Kotlin + JUnit 5

The `/migrate` command executes a strict sequence across three agents: **conductor** → **worker** → **verifier**. This document describes that flow end-to-end.

## Sequence

```
user
 │  /migrate <feature> [--scenario=...] [--approved-concept=...]
 ▼
migrate-conductor
 │  Step 0  Scenario Outline? ──► fill scenario-outline-port-plan.template.md ──► AWAIT USER APPROVAL
 │  Step 1  Load instructions, knowledge, lessons; resolve step bindings
 │  Step 2  Draft (migration-draft.template.md, Allure mapping filled)
 │  Step 3  --approved-concept? no  ──► AWAIT USER APPROVAL OF DRAFT
 │  Step 4  delegate ──────────────────────────────────────────────┐
 ▼                                                                  │
migrate-worker                                                      │
 │  Reads approved draft + step bindings                            │
 │  Writes src/test/kotlin/.../<NewTest>.kt                         │
 │  Header comment: // migrated from features/... — journal: ...    │
 │  Plain @Test, Allure annotations explicit, editorconfig honored  │
 └──────────────────────────────────────────────────────────────────┘
 ▼
migrate-verifier
 │  Gate 1  mvn -q verify
 │  Gate 2  mvn test -Dtest=<NewClass>#<method>  + parse surefire XML
 │  Gate 3  legacy Cucumber run for the same scenario + parse surefire XML
 │  Gate 4  Allure metadata present in target/allure-results/*.json
 │  Gate 5  .editorconfig compliance (editorconfig-checker or manual walk)
 │  Gate 6  anti-pattern scan (Thread.sleep, @ParameterizedTest, UI libs, non-English strings)
 │  Report  JSON block with pass/fail per gate and a blockers[] array
 ▼
migrate-conductor
 │  On green                                            On block
 │   write journal entry from _TEMPLATE.md               surface blockers[]
 │   prepend row in _INDEX.md                            offer revise / abort
 │   ask 3 independent y/n questions for lessons        record block in journal
 │   append only on "y"
```

## Approval gates

There are **up to two** user approval gates per run:

1. **Outline port plan** (only when the scenario is a Scenario Outline): the conductor blocks for per-row disposition approval.
2. **Draft concept**: unless `--approved-concept=...` was passed at invocation, the conductor blocks for concept approval before handing off to the worker.

## Short-circuit flag `--approved-concept`

When the user knows exactly how they want the migration done, they pass `--approved-concept="<inline note or path>"` at invocation. The conductor still produces the Draft for the record, but skips the approval question and hands off to the worker. The value of the flag is stored in the journal under *Draft approval*.

Never short-circuit the **outline port plan** approval — the outline decision has too much leverage on the resulting test shape.

## Failure handling

- **Outline disapproval**: run ends; no journal entry. Conductor records an abort note only if the user asks.
- **Draft disapproval**: run ends or user asks for revision; conductor iterates on the draft.
- **Worker deviation from draft**: worker stops and reports; conductor decides whether to revise the draft or block.
- **Verifier block**: journal is updated with the blocker list and the migration is marked `blocked`. Conductor offers to fix and retry.

## Journal

- Every run that reaches the worker phase gets a journal file at `.github/copilot/journal/<YYYY-MM-DD>-<slug>.md`.
- `_INDEX.md` lists every run with status `green` / `blocked` / `rolled-back`.
- The journal is English; it is the single source of truth for what was migrated and how.

## Lessons-learned hooks

At the end of a green run, the conductor asks three independent questions:

1. `Record a lesson to lessons-learned/migration.md? (y / n)`
2. `Add a canonical pattern to migration-patterns.md? (y / n)`
3. `Record a pitfall in migration-pitfalls.md? (y / n)`

Each `y` appends an entry in the format defined in `docs/self-learning.md`.

## Autonomous path — `/migrate-auto`

`/migrate-auto` inherits every step above, with two surgical differences:

1. **Step 3 (Draft approval)** runs through an auto-approval policy instead of asking the user. Every criterion must pass:

   | # | Criterion | Fallback |
   |---|-----------|----------|
   | 1 | Plain `Scenario:`, or approved Outline port plan | Interactive |
   | 2 | Every step binds to exactly one method | Interactive |
   | 3 | Allure mapping fully derivable from source; no `<...>` placeholders | Interactive |
   | 4 | No `migration-pitfalls.md` entry tagged `Severity: human-review` matches | Interactive |
   | 5 | No `auto-escalation-triggers` in `migration-knowledge.instructions.md` matches | Interactive |
   | 6 | Target test class path does not collide | Interactive |
   | 7 | `git status --porcelain src/test` reports clean | Refuse |
   | 8 | `--approved-concept=...` set OR Draft has zero placeholders and zero open questions | Interactive |

   The result is recorded in `.github/copilot/templates/auto-approval-checklist.template.md` and attached to the journal. **Step 0 (Outline port plan) still blocks for human approval even in autonomous mode.**

2. **Verifier blockers[]** go through a bounded retry-with-fix loop (default budget: `3`, max `5`, via `--retry-budget=N`). Each blocker is classified:

   ```
   compile-error | missing-import | allure-missing | editorconfig | anti-pattern   → auto-fixable
   test-assertion  → conditionally auto-fixable; never change an assertion to match an unexpected behavior
   legacy-red | infra-error | unknown                                              → escalate
   ```

   An auto-fixable round re-invokes `migrate-worker` with scope limited to the flagged file(s). The worker is forbidden from:
   - modifying the `.feature` or the legacy runner,
   - weakening or broadening an assertion to fake a green,
   - disabling a verifier gate,
   - stripping Allure annotations to dodge the metadata check.

   Violations surface as anti-patterns on the next verify and trigger immediate escalation even within budget.

### Autonomous sequence

```
user
 │  /migrate-auto <feature> [--scenario=...] [--retry-budget=N] [--approved-concept=...]
 ▼
migrate-conductor-auto
 │  Step 0  Scenario Outline? ──► scenario-outline-port-plan.template.md ──► AWAIT USER APPROVAL (never auto)
 │  Step 1  Load instructions, knowledge, lessons; resolve step bindings
 │  Step 2  Draft (migration-draft.template.md, Allure mapping filled)
 │  Step 3  Auto-approval checklist ──► all ✓? yes: log "auto-approved"
 │                                     any ✗: fall back to /migrate interactive
 │                                     #7 ✗: refuse until committed/stashed
 │  Step 4  delegate ────────────────► migrate-worker (as in /migrate)
 │  Step 5  delegate ────────────────► migrate-verifier (as in /migrate)
 │  Step 5a Verifier blockers? classify ──► all auto-fixable?
 │            yes: scoped fix, re-verify, increment attempt, repeat until green or budget exhausted
 │            no:  escalate
 │  Step 6  Journal with Mode: autonomous, full criterion log, retry log; update _INDEX.md
 │          Novel failure class observed? append one Review: pending stub to lessons-learned/migration.md
 │  Step 7  Escalation (if reached): journal Mode: autonomous → escalated;
 │          preserve last emitted file; surface blockers + retry log + diagnosis;
 │          offer retry interactively / open in worker / abort and revert.
```

### Deploy path — GitHub Copilot coding agent

Assign a GitHub issue titled `Migrate scenario "<name>" from <feature-path>` to GitHub Copilot. Put the exact command in the body. Copilot opens a draft PR and runs the autonomous conductor in CI-like isolation. Escalations land as PR comments with the retry log attached.

## Related files

- Interactive prompt: `.github/prompts/migrate.prompt.md`
- Autonomous prompt: `.github/prompts/migrate-auto.prompt.md`
- Interactive conductor: `.github/chatmodes/migrate-conductor.chatmode.md`
- Autonomous conductor: `.github/chatmodes/migrate-conductor-auto.chatmode.md`
- Worker: `.github/chatmodes/migrate-worker.chatmode.md`
- Verifier: `.github/chatmodes/migrate-verifier.chatmode.md`
- Templates: `.github/copilot/templates/*.template.md` (including `auto-approval-checklist.template.md`)
- Journal: `.github/copilot/journal/{_INDEX,_TEMPLATE}.md`
- Knowledge: `.github/copilot/knowledge/**`
