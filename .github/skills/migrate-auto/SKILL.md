---
name: migrate-auto
description: Delegate to `migrate-conductor-auto` for an autonomous Cucumber → Kotlin + JUnit 5 migration with policy-driven Draft approval and a bounded retry-with-fix loop across both verify phases (initial + post-cleanup). Human approval is retained only for the Scenario Outline port plan. Use when the user asks for a hands-free migration or runs /migrate-auto.
allowed-tools: shell
---

# /migrate-auto

Autonomous variant of `/migrate`. Same worker and verifier, same invariants, same one-scenario rule. Differences:

1. The Draft-approval step runs through an **auto-approval policy**. Any failing criterion → fall back to interactive `/migrate`.
2. On verifier `blockers[]` — **bounded retry-with-fix loop** (default budget: 3). Non-auto-fixable classes escalate immediately.

The **Scenario Outline port plan** still requires live human approval — never short-circuited.

## Usage

```
/migrate-auto <path-to-feature>
/migrate-auto <path-to-feature> --scenario="..."
/migrate-auto <path-to-feature> --scenario="..." --retry-budget=N
/migrate-auto <path-to-feature> --scenario="..." --approved-concept="<inline note or path>"
```

## Arguments

- `<path-to-feature>` — required.
- `--scenario="..."` — optional if the feature has only one scenario; required otherwise.
- `--retry-budget=N` — optional. `0 ≤ N ≤ 5`. Default: `3`. Out-of-range → refuse.
- `--approved-concept="..."` — optional. Satisfies auto-approval criterion #8 automatically.

## Behavior

1. Delegate to `migrate-conductor-auto`.
2. If target is `Scenario Outline` → fill `scenario-outline-port-plan.template.md` and **block for human approval**.
3. Evaluate the 8 auto-approval criteria, fill `auto-approval-checklist.template.md`. All ✓ → auto-approved. Any ✗ → fall back to interactive `/migrate`.
4. Worker `task: write-test`.
5. Verifier `phase: initial`.
6. On `blockers[]` — classify each. All auto-fixable → scoped fix (worker touches only flagged files) → re-verify. Repeat until green or budget exhausted.
7. On green initial: worker `task: delete-scenario`; verifier `phase: post-cleanup` against the same retry budget. Cleanup-incomplete is auto-fixable (re-run deletion); parity-mismatch and cleanup-overreach escalate.
8. On final green: journal `Mode: autonomous` + criterion checklist + retry log + both verifier reports + cleanup report. Update `_INDEX.md`. Novel failure classes during retries → single `Review: pending` lesson stub.
9. On escalation (non-auto-fixable or budget exhausted): journal `Mode: autonomous → escalated` + escalation phase, leave the test file in its last emitted state, surface any `.feature` modification (post-cleanup only), don't silently revert. Offer `retry interactively / open in worker / abort and (optionally) revert`.

## DO / DON'T

- DO: log every criterion check + every retry into the journal.
- DO: keep all worker fixes scoped to flagged file(s).
- DON'T: short-circuit Scenario Outline port-plan approval.
- DON'T: disable or weaken any verifier gate.
- DON'T: change assertions to match observed-but-wrong behavior.
- DON'T: silently revert the `.feature` after a post-cleanup escalation.
- DON'T: write to lessons-learned beyond the `Review: pending` stub for novel failure classes.

## Refuses

- All `/migrate` refusals.
- Dirty working tree in `src/test/**` — until committed or stashed.
- Retry budget outside `[0, 5]`.
- Any request to auto-approve a Scenario Outline port plan.
- Any request to disable a verifier gate.

## Deploy path — GitHub Copilot coding agent

Assign a GitHub issue titled `Migrate scenario "<name>" from <feature-path>` to GitHub Copilot. Body carries the exact command (e.g., `/migrate-auto path/to.feature --scenario="..."`). Copilot opens a draft PR; verifier gates are the safety net; escalations surface as PR comments with the retry-log.

## Related

- `.github/agents/migrate-conductor-auto.agent.md`
- `.github/agents/migrate-conductor.agent.md` — interactive fallback.
- `.github/agents/migrate-worker.agent.md`
- `.github/agents/results-verifier.agent.md`
- `.github/copilot/templates/auto-approval-checklist.template.md`
- `.github/copilot/templates/migration-draft.template.md`
- `.github/copilot/templates/scenario-outline-port-plan.template.md`
- `.github/copilot/journal/_TEMPLATE.md`
