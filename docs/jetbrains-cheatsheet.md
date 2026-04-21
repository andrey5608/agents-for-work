# JetBrains IntelliJ IDEA — Copilot Chat cheat sheet

IntelliJ's GitHub Copilot Chat auto-loads `.github/copilot-instructions.md` and `.github/instructions/*.instructions.md`, so global rules and scoped rules work out of the box. It does **not** pick up `.github/skills/<name>/SKILL.md` or `.github/agents/*.agent.md` — those are VS Code features. Use the copy-paste prompts below to reproduce each slash command and each agent's role.

All prompts are English and produce English output regardless of your input language — this is enforced by the global instructions.

## /review

```
Act as a repository reviewer. Scope: the current uncommitted diff (or the PR I will specify next). Apply rules from .github/instructions/review-rules.instructions.md exactly. Produce a grouped English checklist with severity tags (blocker / major / minor / nit) and file:line anchors. Consult .github/copilot/knowledge/lessons-learned/review.md; cite matching lessons. At the end, ask whether to append a new lesson; write to the file only if I answer "y".
```

## /explain-test

```
Act as a test explainer for the file I paste or reference next. Output in English with these sections: Purpose, Preconditions, Actions, Assertions, Bugs it catches, Limitations (what it does NOT catch). List every Allure annotation present. For a .feature scenario, add a table mapping each step to the Kotlin step-definition method at file:line; mark unbound steps UNBOUND STEP. Do not fabricate bindings.
```

## /debug-cucumber

```
Act as a Cucumber failure diagnoser. Input: the .feature path I provide, and optionally a stack trace I paste. Produce in English:
1. A step-to-method table: step text at feature:line → StepsClass.method at file:line (grep by @Given/@When/@Then/@And/@But).
2. If a stack trace is provided, align top frames to step-definition methods; mark the last user frame.
3. Cross-reference .github/copilot/knowledge/lessons-learned/cucumber-debug.md; cite any matched lesson and its fix.
4. Check known failure classes: ambiguous step definitions, regex/parameter mismatch, missing @Before, DataTable mapping, DI not wired, non-idempotent test data.
5. Output a minimal repro: ./mvnw test -Dtest=<Runner> -Dcucumber.filter.name="<scenario>" -Dcucumber.features="<path>".
6. If the failure class is novel, propose a lesson entry to append; write it only on my "y".
```

## /migrate (whole conductor-worker-verifier flow as one paste)

```
Act as the migration conductor for Cucumber → Kotlin + JUnit 5. Input: a feature path and a specific scenario name. Follow this flow strictly:

Step 0 — If the scenario is a Scenario Outline with Examples, fill .github/copilot/templates/scenario-outline-port-plan.template.md with a per-row disposition (merge / split / drop) and rationale, and STOP for my approval before any further step.

Step 1 — One scenario only. Reject batch.

Step 2 — Produce a Draft using .github/copilot/templates/migration-draft.template.md: test concept, target JUnit 5 class and method signature in Kotlin, Allure annotation set mapped per .github/copilot/templates/allure-mapping.template.md, preserved vs changed behavior, external deps, open questions.

Step 3 — Ask for my approval of the Draft unless I already said "--approved-concept=<inline>" at the start. Do not write code until approved.

Step 4 — Worker phase: write the Kotlin test under src/test/kotlin/..., honoring .editorconfig, using only plain @Test (no @ParameterizedTest, no Test Matrix), with all Allure annotations explicitly present, and a header comment "// migrated from features/<file>.feature:<scenario-name> — journal: .github/copilot/journal/<date>-<slug>.md". Do not touch the .feature.

Step 5 — Verifier phase: run ./mvnw -q verify; ./mvnw test -Dtest=<NewClass>#<method>; legacy ./mvnw test -Dtest=<Runner> -Dcucumber.filter.name="<scenario>". Parse target/surefire-reports/TEST-*.xml and target/allure-results/*.json. Check editorconfig compliance. Refuse to pass if any gate fails.

Step 6 — Write the migration journal using .github/copilot/journal/_TEMPLATE.md, update .github/copilot/journal/_INDEX.md, and ask whether to append lessons to lessons-learned/migration.md / migration-patterns.md / migration-pitfalls.md. Write only on my "y".
```

## /migrate-auto (autonomous variant — paste as one block)

```
Act as the autonomous migration conductor for Cucumber → Kotlin + JUnit 5. Same invariants as /migrate, with two differences: the Draft approval step runs through an auto-approval policy, and verifier blockers go through a bounded retry-with-fix loop. Default retry budget is 3; accept --retry-budget=N with 0 ≤ N ≤ 5 and refuse otherwise.

Step 0 — If the scenario is a Scenario Outline with Examples, fill .github/copilot/templates/scenario-outline-port-plan.template.md and STOP for my live approval. Autonomous mode never auto-approves this gate.

Step 1 — One scenario only. Reject batch.

Step 2 — Produce the Draft using .github/copilot/templates/migration-draft.template.md.

Step 3 — Evaluate the 8 auto-approval criteria from .github/agents/migrate-conductor-auto.agent.md and record the result in .github/copilot/templates/auto-approval-checklist.template.md: (1) plain Scenario or approved Outline plan, (2) every step uniquely bound, (3) Allure mapping fully derivable from source with no <...> placeholders, (4) no migration-pitfalls.md entry tagged Severity: human-review matches, (5) no auto-escalation-triggers entry matches, (6) target test class path does not collide, (7) src/test tree is clean in git (refuse if not), (8) --approved-concept given OR draft has zero placeholders and zero open questions. On any fail except #7, fall back to the interactive /migrate skill. On #7 fail, refuse until committed or stashed.

Step 4 — Hand off to the worker as in /migrate.

Step 5 — Verifier phase as in /migrate. On blockers[], classify each: compile-error | missing-import | allure-missing | editorconfig | anti-pattern — auto-fixable; test-assertion — only if the mismatch is a clearly wrong literal copied from the feature (never to match an unexpected behavior); legacy-red | infra-error | unknown — escalate. If any blocker is non-auto-fixable, escalate immediately. Otherwise apply a scoped fix (worker touches only the flagged file) and re-verify. Repeat until green or the retry budget is exhausted.

Step 6 — On green: write the journal with Mode: autonomous, include the criterion checklist and retry log, update .github/copilot/journal/_INDEX.md. If a retry exposed a novel failure class that matches no existing entry in lessons-learned/migration.md, append a single stub marked "Applies to: migration (autonomous)" and "Review: pending" — this is the only autonomous write that does not ask y.

Step 7 — On escalation: write the journal as Mode: autonomous → escalated, preserve the last emitted test file (do not revert), and surface the final blocker list, retry log, and one-sentence diagnosis. Offer: retry interactively / open in worker for manual fix / abort and revert.
```

## /add-lesson-learned

```
Act as the lessons-learned recorder. I want to manually append one entry to a knowledge catalog in this repo — not triggered by the end of a /review, /debug-cucumber, or /migrate run.

Ask me which catalog first (if I have not told you yet):
- migration → .github/copilot/knowledge/lessons-learned/migration.md
- cucumber-debug → .github/copilot/knowledge/lessons-learned/cucumber-debug.md
- review → .github/copilot/knowledge/lessons-learned/review.md
- pattern → .github/copilot/knowledge/migration-patterns.md
- pitfall → .github/copilot/knowledge/migration-pitfalls.md

Then collect fields one at a time, using the entry format for that catalog as defined in .github/skills/add-lesson-learned/SKILL.md. Rewrite my answers to English if needed. Refuse vague wisdom, restatements of existing instructions/* files, event logs, or non-English prose; explain why and offer to reframe.

Show me the exact Markdown that will be appended. Ask "Append this to <path>? (y / n)". On "y" append to the end of the file under the trailing "<!-- Entries go below ... -->" marker, preserving the file's other sections. On "n" stop without writing. One entry per run.
```

## /create-api-autotest

```
Act as the API-test authoring agent. Inputs: --module=<path> (directory with src/test/kotlin/** under it, a Maven submodule, or a Kotlin package path) and either --endpoints="METHOD /path, METHOD /path, ..." or --spec=<path>. Optional --approved-concept="..." short-circuits the Draft gate.

Step 1 — Locate existing tests under <module>/src/test/kotlin/**. Sample a representative set (smallest 2 + newest 3). If the module has zero API tests, ask for a sibling module to mirror; refuse if none is given.

Step 2 — Extract the architectural scheme from the samples: HTTP client (REST-assured / WebTestClient / OkHttp / Ktor / custom), base class or @ExtendWith / @SpringBootTest, fixture loading pattern (builders / resources fixtures / factory methods), auth wiring, assertion style (AssertJ / REST-assured body / JSON-Path / custom), parameterization style (plain @Test + helpers vs. @ParameterizedTest — choose whichever dominates; default to plain when mixed), Allure convention (Epic/Feature/Story values, default Severity, @Tag vocabulary), package/file naming, error-response shape, and external deps. Cite file:line evidence for every claim.

Step 3 — Resolve each endpoint: HTTP method, path, parameters, request body type (from the module's serializer types or resource fixtures), success response shape, declared error responses, auth requirement. Ambiguities block — never guess method, path, or error class.

Step 4 — Fill .github/copilot/templates/api-test-draft.template.md. Include the extracted scheme, per-endpoint happy + negative scenarios (using only error classes the module already tests), Allure annotation plan, parameterization choice with evidence, open questions.

Step 5 — Unless --approved-concept was provided, ask once: "Approve this Draft? (y / n / revise)". No code is written before approval.

Step 6 — Write one Kotlin test class under src/test/kotlin/... following the module's package convention. Header comment: "// authored by api-test-author — journal: .github/copilot/journal/<YYYY-MM-DD>-<slug>.md". Allure annotations explicit on class and methods. English method names. Honor .editorconfig. Reuse the module's base classes, clients, fixtures — do not introduce parallel scaffolding. Refuse to touch production code; stop and surface the gap instead.

Step 7 — Invoke the verifier with source: authored, new_test_class and new_test_method(s) set, legacy fields N/A. Gates 1, 2, 4, 5, 6 run; Gate 3 is skipped; Gate 6 does not reject @ParameterizedTest.

Step 8 — On green: journal entry with Mode: authored (reuse .github/copilot/journal/_TEMPLATE.md, rename the "Scenario → method" section body to "Endpoint → method"), update _INDEX.md, then ask three independent y/n questions for lessons/patterns/pitfalls. Write only on "y". On block: surface blockers[] and offer revise / abort.
```

## /commit

```
Act as the commit-message generator for this repo.

1. Read the staged diff: run `git status --short`, `git diff --cached --stat`, and `git diff --cached`. If the cached diff is empty, stop with "nothing to commit — stage files first". Do not create an empty commit.
2. If I said --include-unstaged, run `git add -u` first (never stage untracked files). If there are untracked `??` files in `git status --short` and I did not say --include-unstaged, surface them and ask whether I want any staged before proceeding.
3. Read `git log -n 10 --pretty=format:"%s"` and match the prevailing subject style (imperative mood, casing, scope prefixes or ticket tags if present).
4. Generate an English commit message: subject line imperative, ≤72 chars, no trailing period, stating the *why* or outcome — not a file list. Add a body only when the subject alone cannot carry the intent; wrap ~72 columns. No co-author trailer unless I ask. No emoji.
5. Preview the exact message (delimited so I can see whitespace) and list the files that will be committed. Ask once: "Commit with this message? (y / n / edit)". On `edit`, take my replacement subject/body, re-preview, re-ask.
6. On `y`, run `git commit` with the message passed via HEREDOC. Never add `--no-verify`, `--no-gpg-sign`, or `--amend`. If a pre-commit hook fails, print the failure verbatim and stop — I fix and rerun.
7. After success, print `git rev-parse --short HEAD` and `git status --short`.

Refuse: empty commits, implicit untracked staging, obvious secret signatures in the diff without my explicit go-ahead, force-push / amend / rebase requests.
```

## /skill-creator

```
Act as the skill scaffolder for this repo. I want to add a new VS Code Copilot skill under `.github/skills/<name>/SKILL.md`.

1. Ask me for the skill name if I have not given it yet. Validate kebab-case `[a-z][a-z0-9-]*`, length 3–32. Refuse if `.github/skills/<name>/` already exists — no overwrites.
2. Ask for a one-line description that names the action, the target artifact, and a "Use when ..." clause. Refuse vague ones like "helps with tests"; ask me to reframe.
3. Ask whether the skill needs `allowed-tools: shell`, `allowed-tools: edit`, or neither. A skill that only delegates to an agent carries no `allowed-tools`.
4. Ask whether `/<name>` delegates to a custom agent. If yes, take the agent name and verify `.github/agents/<agent>.agent.md` exists — refuse otherwise. If no, collect the direct steps.
5. Collect the body fields one at a time and rewrite to English if needed: Arguments, Behavior (numbered), Invariants restated (inherit from `.github/copilot-instructions.md`; do not restate), Refusals (name the refused thing + the reason), Related files.
6. Refuse: name collides with an existing slash command, description restates what the delegated agent already documents, non-English prose, instructions that weaken an existing invariant (e.g., "skip Allure check"), requests to bypass user approval that the underlying agent normally asks for.
7. Preview the full SKILL.md in a fenced block plus the three registry diffs: `.github/copilot-instructions.md` (Available skills), `docs/README.md` (Skills (slash commands) list + per-command paragraph block), `docs/jetbrains-cheatsheet.md` (new `## /<name>` section).
8. Ask exactly once: "Create `/<name>` at `.github/skills/<name>/SKILL.md` and register it in copilot-instructions + docs? (y / n / edit)". On `y`: write the SKILL.md and append the three registry lines. On `n`: stop. On `edit`: collect the delta on whichever field I name, re-preview, re-ask. English only.
```

## Notes

- These prompts are intentionally long. Save them as IntelliJ "Live Templates" or as bookmarks in your notes.
- Instructions files keep the rules; cheat-sheet prompts only invoke the roles.
- When a rule changes, update `instructions/*` first; the cheat-sheet prompts keep working.
