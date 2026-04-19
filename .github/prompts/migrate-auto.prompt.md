---
mode: 'agent'
description: 'Autonomous Cucumber → JUnit 5 migration. Policy-driven Draft approval + bounded retry-with-fix. Human gate kept only for Scenario Outline port plan.'
tools: ['codebase', 'edit', 'terminal', 'findTestFiles']
---

# /migrate-auto

Autonomous variant of `/migrate`. Same worker and verifier, same invariants, same one-scenario-per-run rule. Two differences:

1. The Draft-approval step runs through an **auto-approval policy** (`.github/chatmodes/migrate-conductor-auto.chatmode.md`). On any failing criterion, the run falls back to the interactive conductor.
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

1. Enter `migrate-conductor-auto` chat mode.
2. If the target is a `Scenario Outline`, the conductor fills `scenario-outline-port-plan.template.md` and **blocks for human approval** before anything else.
3. The conductor evaluates the 8 auto-approval criteria and writes the result into `auto-approval-checklist.template.md`. All ✓ → the Draft is auto-approved. Any ✗ → fall back to interactive `/migrate`.
4. The conductor hands off to `migrate-worker`.
5. The conductor hands off to `migrate-verifier`.
6. On `blockers[]`: classify each blocker. If all are auto-fixable, apply a scoped fix (worker touches only flagged files) and re-verify. Repeat until green or budget exhausted.
7. On green: write the migration journal with `Mode: autonomous`, full criterion checklist, and retry log. Update `_INDEX.md`. Novel failure classes observed during retries produce a single `Review: pending` lesson stub.
8. On escalation (non-auto-fixable blocker, or budget exhausted): write the journal as `Mode: autonomous → escalated`, leave the test file in its last emitted state, surface the final blocker list + retry log + one-sentence diagnosis, and offer `retry interactively / open in worker / abort and revert`.

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

- `.github/chatmodes/migrate-conductor-auto.chatmode.md`
- `.github/chatmodes/migrate-conductor.chatmode.md` (interactive fallback)
- `.github/chatmodes/migrate-worker.chatmode.md`
- `.github/chatmodes/migrate-verifier.chatmode.md`
- `.github/copilot/templates/auto-approval-checklist.template.md`
- `.github/copilot/templates/migration-draft.template.md`
- `.github/copilot/templates/scenario-outline-port-plan.template.md`
- `.github/copilot/journal/_TEMPLATE.md`
