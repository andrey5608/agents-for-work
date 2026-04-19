---
mode: 'agent'
description: 'One-command code review against the pre-filled Kotlin/JUnit5/Cucumber/Allure backend rubrics.'
tools: ['codebase', 'terminal', 'findTestFiles']
---

# /review

Run a thorough review of the current changes.

## Scope

- Default scope is the diff of the working tree and staged changes vs `origin/main` (or `main`).
- If the user passes a ref, PR number, or path, review that instead.

## Process

1. Resolve the scope:
   - If a PR number is provided, fetch its diff.
   - Otherwise run `git diff --merge-base main -- .` to get the review surface. If `main` is unavailable locally, try `origin/main`.
2. Read the full file contents of every changed file so findings have accurate line anchors.
3. Load `.github/instructions/review-rules.instructions.md` and the cross-referenced instructions (`kotlin`, `junit5`, `cucumber`, `allure`, `editorconfig-compliance`, `english-output`).
4. Load `.github/copilot/knowledge/lessons-learned/review.md`. For each lesson, check whether its pattern matches anything in the current diff.
5. Walk the diff file by file, applying the 10 rubrics. Attach each finding to `file:line`.
6. Assign severity: `blocker` (must fix), `major` (should fix), `minor` (worth fixing), `nit` (opinion).
7. Emit the report using `.github/copilot/templates/review-checklist.template.md` as a shape guide.

## Output constraints

- English only.
- Findings grouped by rubric, sorted by severity within each rubric.
- Every finding has `file:line`, a one-sentence description of the problem, and (when non-trivial) one sentence of suggested fix.
- Cite any lesson-learned match as `see lessons-learned/review.md#<anchor>`.
- Do not suggest refactors beyond the diff scope.
- Do not produce code patches — this is a review, not an edit.

## End of run

Ask once:

> Record a new lesson from this review? (y / n)

- On `y`: ask for the lesson title and append an entry to `.github/copilot/knowledge/lessons-learned/review.md` using the format in `docs/self-learning.md`.
- On `n` or no answer: stop without writing.
