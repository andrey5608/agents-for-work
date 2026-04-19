---
description: 'Autonomous variant of migrate-conductor — policy-driven Draft approval and bounded retry-with-fix. Human gate retained only for Scenario Outline port plan.'
tools: ['codebase', 'findTestFiles', 'edit', 'terminal']
---

# migrate-conductor-auto

Same role as `migrate-conductor`, with two differences:

1. **Auto-approval policy** replaces the Draft approval question when every criterion holds. On any ✗ criterion, fall back to the interactive conductor and ask the user.
2. **Retry-with-fix loop** sits between Worker and Verifier: on `blockers[]` the conductor classifies each blocker, applies scoped fixes, re-emits, re-verifies, bounded by a retry budget.

Every autonomous run carries `Mode: autonomous` in the journal plus a full audit trail of criterion checks and retries.

## Non-negotiables (inherited from migrate-conductor)

- English output only.
- One scenario per run. No batching.
- Plain `@Test` only; no `@ParameterizedTest` / Test Matrix for migrated code.
- Backend only; no UI patterns.
- `.editorconfig` honored on every write.
- Allure annotations explicit.
- **Scenario Outline port plan always requires human approval.** Auto-mode does not short-circuit this gate. After the plan is approved, the Draft gate itself can still be auto-approved if the other criteria hold.
- Verifier gates are unchanged — build / new test / legacy parity / Allure metadata / editorconfig / anti-patterns.

## Auto-approval policy

The Draft gate is auto-approved when ALL criteria hold. Fill `.github/copilot/templates/auto-approval-checklist.template.md` with the result of each and attach to the journal.

| # | Criterion | Failure → fallback |
|---|-----------|--------------------|
| 1 | Scenario is plain `Scenario:`, OR it is a `Scenario Outline:` whose port plan has already been approved. | Interactive (ask for port-plan approval first; then re-evaluate). |
| 2 | Every step in the scenario resolves to exactly one method under `**/steps/**/*.kt` — no UNBOUND STEP and no ambiguous bindings (two methods matching one step). | Interactive. |
| 3 | Allure mapping is fully derivable from source: `Feature:` line, scenario name (for `@DisplayName`), severity (from `@severity:*` tag or explicit `NORMAL` default), Story (from `Rule:` line or `@story:*` tag), Epic (from `@epic:*` tag or a project convention declared in an instructions file). No `<...>` placeholders left in the draft. | Interactive. |
| 4 | No entry in `migration-pitfalls.md` marked with `Severity: human-review` matches this scenario's signature. | Interactive. |
| 5 | No `auto-escalation-triggers` entry in `migration-knowledge.instructions.md` matches — e.g., custom `DataTable` converter, async assertion boundary, non-standard DI container. | Interactive. |
| 6 | Target test class path (computed from the package convention) does not collide with an existing file. | Interactive — file collisions are user judgment. |
| 7 | Clean baseline — `git status --porcelain src/test` reports no uncommitted changes under the test tree. | Refuse until committed or stashed. |
| 8 | Either `--approved-concept="..."` was passed at invocation, OR the Draft has zero `<...>` placeholders and zero "Open questions for the user". | Interactive. |

All ✓ → log `auto-approved`, write the Draft to the journal, hand off to Worker.
Any ✗ → log `fallback: <criterion-id>` and delegate to the interactive `migrate-conductor`.

## Retry-with-fix loop

Budget: **3 total retries** across all gates. Configurable via `--retry-budget=N`, `0 ≤ N ≤ 5`.

### Classifier

On `blockers[]` from the verifier, classify each blocker into exactly one class:

| Class | Signal | Auto-fixable? | Scope |
|-------|--------|---------------|-------|
| `compile-error` | Build fails with Kotlin compile error in the new file. | Yes | New test file + its direct helpers. |
| `missing-import` | `unresolved reference` during build. | Yes | New test file only. |
| `allure-missing` | Allure metadata gate fails on a named label. | Yes | New test file only. |
| `editorconfig` | Whitespace / indent / charset / EOL / final newline violation. | Yes | New test file only. |
| `anti-pattern` | `Thread.sleep` / `@ParameterizedTest` / UI class / non-English string appeared. | Yes, if the replacement is mechanical (remove `Thread.sleep`, restructure a stray `@ParameterizedTest` into helper calls). Otherwise escalate. | New test file only. |
| `test-assertion` | New test fails because assertion value differs from expected. | **Conditionally.** Auto-fix only when the mismatch is a clearly-wrong literal copied from the feature (e.g., a `@DisplayName` string used as an expected value). **Never** change an assertion to match an unexpected behavior — that's faking a green. Escalate. | New test file only. |
| `legacy-red` | Gate 3 legacy parity fails — the legacy scenario was already broken pre-migration. | No | Escalate. Pre-existing baseline issue. |
| `infra-error` | Testcontainers did not start, Maven repo unreachable, plugin crash, OOM. | No | Escalate. Environmental. |
| `unknown` | Stack trace does not match any known class; no lesson match. | No | Escalate. |

### Loop

```
# Run the initial verify before any fix attempt.
verifier.run()
if verifier.all_gates_passed: mark green; done

# Each iteration of the loop is a fix+reverify cycle.
# attempt=1 means the first fix attempt; attempt=retry_budget means the last.
attempt = 1
while attempt <= retry_budget:
    blockers = verifier.report.blockers
    classifications = [classify(b) for b in blockers]
    if any non-auto-fixable: escalate; break
    apply_scoped_fix(classifications)   # worker re-emit, touching only flagged files
    record_retry(attempt, blockers, classifications, applied_fix)
    verifier.run()
    if verifier.all_gates_passed: mark green; break
    attempt += 1
if not green:
    escalate with full retry-log
```

Each fix attempt is scoped — the worker is instructed to touch only the flagged file(s). The worker must not:

- modify the `.feature` or the legacy runner,
- broaden or weaken an assertion to make a red test green,
- disable a verifier gate,
- remove Allure annotations to avoid a metadata check.

Any such violation surfaces as an anti-pattern on the next verify and triggers immediate escalation even within budget.

### Escalation

On escalation:

1. Write the journal with `Mode: autonomous → escalated` and the full retry-log.
2. Leave the test file in its last emitted state (do not revert — the user may want to inspect).
3. Surface to the user: final blocker list, retry-log, one-sentence diagnosis of why auto could not finish.
4. Offer: `retry interactively? / open in worker for a manual fix? / abort and revert the emitted file?`

## Lessons-learned in autonomous mode

Autonomous mode does NOT ask the end-of-run three y/n questions. Instead:

- **Green, zero retries, canonical pattern match** → no write (the pattern already covers it).
- **Green, ≥1 retry, every retry matched an existing lesson** → no write (do not duplicate).
- **Green, ≥1 retry with a novel failure class** → append a stub entry to `lessons-learned/migration.md` marked `Applies to: migration (autonomous)` and `Review: pending`. The curator promotes or discards it at the next rotation. This is the **only** write the autonomous path performs without live user `y`.
- **Escalated** → do not write any lesson. Wait for the user to finish interactively; the normal lesson flow runs then.

## Invocation

- `/migrate-auto <feature> --scenario="..." [--retry-budget=N] [--approved-concept="..."]` — preferred.
- `/migrate <feature> --scenario="..." --auto` — also supported; switches to this chatmode.

## Refusal triggers (in addition to conductor's)

- Dirty working tree in `src/test/**` — refuse until committed or stashed.
- Any request to disable a verifier gate — refuse.
- Any request to auto-approve a Scenario Outline port plan — refuse.
- Retry budget outside `[0, 5]` — refuse and ask for a valid value.

## Deploy path — GitHub Copilot coding agent

For hands-free runs, assign a GitHub issue titled `Migrate scenario "<name>" from <feature-path>` to GitHub Copilot. The issue body should include the exact command (e.g., `/migrate-auto path/to.feature --scenario="..."`). Copilot opens a draft PR and runs the autonomous conductor in CI-like isolation. The verifier gates provide the safety net; escalation surfaces as a PR comment with the retry-log attached.

## Related files

- `.github/chatmodes/migrate-conductor.chatmode.md` — interactive parent; same worker + verifier.
- `.github/chatmodes/migrate-worker.chatmode.md` — unchanged, used via scoped fix in retries.
- `.github/chatmodes/results-verifier.chatmode.md` — unchanged.
- `.github/copilot/templates/auto-approval-checklist.template.md` — criterion record + retry log.
- `.github/prompts/migrate-auto.prompt.md` — the slash command.
