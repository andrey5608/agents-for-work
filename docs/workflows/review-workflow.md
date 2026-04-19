# Review workflow — `/review`

One-command review against the pre-filled rubric set for Kotlin + JUnit 5 / Cucumber / Allure backend tests.

## Sequence

```
user
 │  /review   (default scope: diff vs origin/main)
 │  /review <ref|PR>
 ▼
prompt: .github/prompts/review.prompt.md
 │  1  Resolve the diff scope
 │  2  Read full contents of every changed file
 │  3  Load instructions: review-rules, kotlin, junit5, cucumber, allure, editorconfig-compliance, english-output
 │  4  Load lessons-learned/review.md; match each lesson pattern against the diff
 │  5  Walk the diff file by file, applying the 10 rubrics
 │  6  Assign severity: blocker | major | minor | nit
 │  7  Emit the report using review-checklist.template.md as shape
 │  8  At end: ask once "Record a new lesson? (y / n)" — append to lessons-learned/review.md only on y
 ▼
report
```

## Rubrics (full list in `.github/instructions/review-rules.instructions.md`)

1. Kotlin idioms
2. JUnit 5
3. Cucumber
4. Backend test architecture (any UI pattern is a blocker)
5. Flaky risk (`Thread.sleep` is a blocker)
6. Assertions
7. Test data (hardcoded credentials / PII are blockers)
8. Allure metadata
9. `.editorconfig` compliance
10. Security (secrets, unsafe deserialization — blockers)

## Severity semantics

- `blocker` — must be fixed before merge.
- `major` — should be fixed; merging with a tracked follow-up is acceptable.
- `minor` — worth fixing this pass but not merge-gating.
- `nit` — opinion; feel free to ignore.

## Output constraints

- English only.
- Findings grouped by rubric, sorted by severity within each rubric.
- `file:line` anchor on every finding.
- Cite lessons by anchor: `see lessons-learned/review.md#<anchor>`.
- No code patches — a review lists findings; the author applies them.

## Lessons-learned hook

At the end of every run the agent asks once whether to append a new lesson. The entry format is in `docs/self-learning.md`. The agent refuses to write vague wisdom or restatements of the instruction files.

## IntelliJ IDEA fallback

Paste the `/review` string from `docs/jetbrains-cheatsheet.md` into Copilot Chat. Scoped instructions are auto-loaded.

## Related files

- Prompt: `.github/prompts/review.prompt.md`
- Rubric set: `.github/instructions/review-rules.instructions.md`
- Template: `.github/copilot/templates/review-checklist.template.md`
- Lessons: `.github/copilot/knowledge/lessons-learned/review.md`
