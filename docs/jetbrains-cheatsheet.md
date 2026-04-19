# JetBrains IntelliJ IDEA — Copilot Chat cheat sheet

IntelliJ's GitHub Copilot Chat auto-loads `.github/copilot-instructions.md` and `.github/instructions/*.instructions.md`, so global rules and scoped rules work out of the box. It does **not** pick up `.github/prompts/*.prompt.md` or `.github/chatmodes/*.chatmode.md`. Use the copy-paste prompts below to reproduce the slash commands and chat modes available in VS Code.

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

## /add-lesson-learned

```
Act as the lessons-learned recorder. I want to manually append one entry to a knowledge catalog in this repo — not triggered by the end of a /review, /debug-cucumber, or /migrate run.

Ask me which catalog first (if I have not told you yet):
- migration → .github/copilot/knowledge/lessons-learned/migration.md
- cucumber-debug → .github/copilot/knowledge/lessons-learned/cucumber-debug.md
- review → .github/copilot/knowledge/lessons-learned/review.md
- pattern → .github/copilot/knowledge/migration-patterns.md
- pitfall → .github/copilot/knowledge/migration-pitfalls.md

Then collect fields one at a time, using the entry format for that catalog as defined in .github/prompts/add-lesson-learned.prompt.md. Rewrite my answers to English if needed. Refuse vague wisdom, restatements of existing instructions/* files, event logs, or non-English prose; explain why and offer to reframe.

Show me the exact Markdown that will be appended. Ask "Append this to <path>? (y / n)". On "y" append to the end of the file under the trailing "<!-- Entries go below ... -->" marker, preserving the file's other sections. On "n" stop without writing. One entry per run.
```

## Notes

- These prompts are intentionally long. Save them as IntelliJ "Live Templates" or as bookmarks in your notes.
- Instructions files keep the rules; cheat-sheet prompts only invoke the roles.
- When a rule changes, update `instructions/*` first; the cheat-sheet prompts keep working.
