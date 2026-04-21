---
name: migrate-auto
description: Delegate to the `migrate-conductor-auto` agent for an autonomous Cucumber → Kotlin + JUnit 5 migration with policy-driven Draft approval and a bounded retry-with-fix loop across both verify phases (initial + post-cleanup). Human approval is retained only for the Scenario Outline port plan. Use when the user asks for a hands-free migration or runs /migrate-auto.
allowed-tools: shell
---

# /migrate-auto

Autonomous variant of `/migrate`. Same worker and verifier, same invariants, same one-scenario-per-run rule. Two differences:

1. The Draft-approval step runs through an **auto-approval policy** (`.github/agents/migrate-conductor-auto.agent.md`). On any failing criterion, the run falls back to the interactive conductor.
2. On verifier `blockers[]`, the conductor runs a **bounded retry-with-fix loop** (default budget: 3 retries). Non-auto-fixable classes escalate immediately.

The **Scenario Outline port plan** still requires live human approval. Autonomous mode never short-circuits that gate.

## Usage

```
/migrate-auto <path-to-feature>
/migrate-auto <path-to-feature> --scenario="<exact scenario name>"
/migrate-auto <path-to-feature> --scenario="<exact scenario name>" --retry-budget=N
/migrate-auto <path-to-feature> --scenario="<exact scenario name>" --approved-concept="<inline note or path>"
```

## Arguments

- `<path-to-feature>` — required.
- `--scenario="..."` — optional if the feature has only one scenario; required otherwise.
- `--retry-budget=N` — optional. `0 ≤ N ≤ 5`. Default: `3`. Out-of-range values are refused.
- `--approved-concept="..."` — optional. When present, satisfies auto-approval criterion #8 automatically.

## Behavior

1. Delegate to the `migrate-conductor-auto` agent.
2. If the target is a `Scenario Outline`, the conductor fills `scenario-outline-port-plan.template.md` and **blocks for human approval** before anything else.
3. The conductor evaluates the 8 auto-approval criteria and writes the result into `auto-approval-checklist.template.md`. All ✓ → the Draft is auto-approved. Any ✗ → fall back to interactive `/migrate`.
4. The conductor hands off to `migrate-worker` (`task: write-test`).
5. The conductor hands off to `results-verifier` with `phase: initial`.
6. On `blockers[]`: classify each blocker. If all are auto-fixable, apply a scoped fix (worker touches only flagged files) and re-verify. Repeat until green or budget exhausted.
7. On green `phase: initial`: the conductor hands off to `migrate-worker` again with `task: delete-scenario` to remove the migrated scenario from the `.feature`, then runs `results-verifier` with `phase: post-cleanup` against the same retry budget. Post-cleanup blockers go through the same classifier — cleanup-incomplete is auto-fixable (re-run the deletion); parity-mismatch and cleanup-overreach escalate.
8. On final green: write the migration journal with `Mode: autonomous`, full criterion checklist, retry log, both verifier reports, and the cleanup report. Update `_INDEX.md`. Novel failure classes observed during retries produce a single `Review: pending` lesson stub.
9. On escalation (non-auto-fixable blocker, or budget exhausted): write the journal as `Mode: autonomous → escalated` with the phase at which the escalation happened, leave the test file in its last emitted state, and — if escalation happened during `phase: post-cleanup` — surface the `.feature` modification explicitly. Do not silently revert the `.feature`; ask the user to `git checkout -- <feature_path>` if they want the scenario back. Offer `retry interactively / open in worker / abort and (optionally) revert`.

## Invariants restated

- English output only.
- Backend only — no UI patterns.
- One scenario per run. No batching.
- Plain `@Test` only — no `@ParameterizedTest` for migrated code.
- All Allure metadata preserved explicitly.
- `.editorconfig` honored on every write.
- **Scenario Outline port plan always requires human approval.**
- Verifier gates (build / new test / legacy parity / Allure metadata / editorconfig / anti-patterns) are never disabled or weakened.
- Assertions are never changed to match observed-but-wrong behavior.
- Autonomous lessons-learned writes are limited to `Review: pending` stubs for novel failure classes. All other knowledge-base writes still need live `y`.

## Refusal triggers (in addition to `/migrate`'s)

- Dirty working tree in `src/test/**` — refuse until committed or stashed.
- Retry budget outside `[0, 5]` — refuse and ask for a valid value.
- Any request to auto-approve a Scenario Outline port plan — refuse.
- Any request to disable a verifier gate — refuse.

## Deploy path — GitHub Copilot coding agent

For hands-free runs, create a GitHub issue titled `Migrate scenario "<name>" from <feature-path>` and assign it to GitHub Copilot. Put the exact command in the issue body (e.g., `/migrate-auto path/to.feature --scenario="..."`). Copilot opens a draft PR and runs the autonomous conductor in CI-like isolation. Escalations surface as PR comments with the retry-log attached.

## Related files

- `.github/agents/migrate-conductor-auto.agent.md`
- `.github/agents/migrate-conductor.agent.md` (interactive fallback)
- `.github/agents/migrate-worker.agent.md`
- `.github/agents/results-verifier.agent.md`
- `.github/copilot/templates/auto-approval-checklist.template.md`
- `.github/copilot/templates/migration-draft.template.md`
- `.github/copilot/templates/scenario-outline-port-plan.template.md`
- `.github/copilot/journal/_TEMPLATE.md`
