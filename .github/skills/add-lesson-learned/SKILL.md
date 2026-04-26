---
name: add-lesson-learned
description: Append a single new entry to one of the append-only knowledge catalogs (`lessons-learned/migration.md`, `lessons-learned/cucumber-debug.md`, `lessons-learned/review.md`, `migration-patterns.md`, `migration-pitfalls.md`). Use when the user asks to record a lesson, canonical pattern, or pitfall, or says things like "remember this", "add a pattern", "record a pitfall".
allowed-tools: edit
---

# /add-lesson-learned

Manually add an entry to a knowledge catalog. User-initiated at any time, unlike the end-of-run prompts on `/review`, `/debug-cucumber`, and `/migrate`.

## Usage

```
/add-lesson-learned
/add-lesson-learned <catalog>
```

## Arguments

- `<catalog>` — optional. One of:
  - `migration` → `.github/copilot/knowledge/lessons-learned/migration.md`
  - `cucumber-debug` → `.github/copilot/knowledge/lessons-learned/cucumber-debug.md`
  - `review` → `.github/copilot/knowledge/lessons-learned/review.md`
  - `pattern` → `.github/copilot/knowledge/migration-patterns.md`
  - `pitfall` → `.github/copilot/knowledge/migration-pitfalls.md`

  Omitted → ask first.

## Behavior

1. Choose catalog. Confirm target file with the user.
2. Collect fields appropriate to the catalog (see formats below). Ask one at a time. Accept free-form input; rewrite to English when needed.
3. Validate. Refuse:
   - Vague wisdom ("be careful with DI").
   - Restatement of an existing `instructions/*` file.
   - Event log ("migrated feature X on date Y" — belongs in the journal).
   - Non-English prose.

   On refusal: explain why, offer to reframe.
4. Preview the exact Markdown to be appended.
5. Confirm:

   > Append this to `<path>`? (y / n)

6. `y` → append at the end, preserving the existing trailing newline and the `<!-- Entries go below ... -->` marker. `n` → stop without writing.
7. Modify only the target file. Don't remove or rewrite prior entries.

## Catalog formats

### `migration` and `review` (lessons-learned)

```markdown
## <YYYY-MM-DD> <short title>
Context: <what was being done>
Observation: <what was surprising, missed, or worked well>
Rule: <actionable guideline in imperative English>
Applies to: <migration | review>
Source: <journal link / PR link / chat reference / manual>
```

Required: date, title, context, observation, rule, source. Use `Source: manual` for off-run lessons.

### `cucumber-debug`

```markdown
## <YYYY-MM-DD> <short title>
Context: <what the scenario was trying to verify>
Symptom: <what the failure looked like — exception type, message, stack top>
Root cause: <the actual cause once understood>
Fix: <one or two sentences; the change that resolved it>
Prevention: <how to avoid reintroducing this — preferably a review-time check>
Applies to: cucumber-debug
Source: <journal link / PR link / chat reference / manual>
```

Required: date, title, context, symptom, root cause, fix, prevention, source.

### `pattern` (migration-patterns)

```markdown
## <short title>
Added: <YYYY-MM-DD>
Source journal: <`.github/copilot/journal/<file>.md` or `manual`>

### Cucumber shape
<Gherkin fragment or shape description>

### Kotlin + JUnit 5 shape
<Kotlin fragment showing the canonical translation>

### Notes
- <bullet list of caveats, when to use, when NOT to use>
```

Required: title, added date, source journal, both shapes, ≥1 note.

### `pitfall` (migration-pitfalls)

```markdown
## <short title>
Added: <YYYY-MM-DD>
Source journal: <`.github/copilot/journal/<file>.md` or `manual`>

### Symptom
<what goes wrong, how you notice>

### Cause
<why it happens>

### Mitigation
<short, imperative>

### Applies when
- <bullet list of signals>
```

Required: title, added date, source journal, symptom, cause, mitigation, ≥1 "applies when" signal.

## DO / DON'T

- DO: English only.
- DO: one entry per run — to add several, run again.
- DO: resolve "today" to the current `YYYY-MM-DD`.
- DON'T: alter the file's header, format guide, or standing-principles section — only append under the trailing marker.
- DON'T: record vague wisdom or event logs.

## Refuses

- Vague wisdom, instructions-file restatement, event logs, non-English prose.

## Related

- `docs/self-learning.md` — curator rotation, how catalogs stay useful.
- End-of-run prompts in `/review`, `/debug-cucumber`, `/migrate` — agent-triggered equivalent.
