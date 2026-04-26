---
name: review
description: Review a diff (or an explicit ref) against the pre-filled Kotlin / JUnit 5 / Cucumber / Allure backend rubric set. Emits a severity-tagged checklist with `file:line` anchors and a lesson-recording offer at the end. Use when the user asks for a code review, a diff review, or runs /review.
allowed-tools: shell
---

# /review

Review the current changes.

## Usage

```
/review
/review <ref>
/review <pr-number>
/review <path>
```

## Arguments

- Default: working tree + staged changes vs `origin/main` (or `main`).
- `<ref>` / `<pr-number>` / `<path>` — explicit scope.

## Behavior

1. **Resolve scope.**
   - PR number → fetch its diff.
   - Otherwise `git diff --merge-base main -- .`. Fall back to `origin/main` if local `main` is unavailable.
2. Read full contents of every changed file so anchors are accurate.
3. Load `.github/instructions/review-rules.instructions.md` and the cross-referenced instructions (`kotlin`, `junit5`, `cucumber`, `allure`, `editorconfig-compliance`, `english-output`).
4. Load `.github/copilot/knowledge/lessons-learned/review.md`. For each lesson, check whether its pattern matches anything in the diff.
5. Walk the diff file by file, applying the 10 rubrics. Anchor every finding at `file:line`.
6. Assign severity: `blocker` (must fix) | `major` (should fix) | `minor` (worth fixing) | `nit` (opinion).
7. Emit the report using `.github/copilot/templates/review-checklist.template.md`.

## End of run

Ask once:

> Record a new lesson from this review? (y / n)

`y` → ask for the lesson title, append to `lessons-learned/review.md` per `docs/self-learning.md`. `n` or no answer → stop.

## DO / DON'T

- DO: English only.
- DO: group findings by rubric, sort by severity within each rubric.
- DO: cite lesson matches as `see lessons-learned/review.md#<anchor>`.
- DON'T: suggest refactors beyond the diff scope.
- DON'T: produce code patches — review, not edit.

## Refuses

- Request to merge or push from this command — out of scope.
- Request to skip a rubric.
