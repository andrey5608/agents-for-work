---
mode: 'agent'
description: 'Manually append an entry to one of the knowledge catalogs (lessons-learned, migration-patterns, migration-pitfalls).'
tools: ['codebase', 'edit']
---

# /add-lesson-learned

Manually add an entry to a knowledge catalog. Unlike the end-of-run prompts on `/review`, `/debug-cucumber`, and `/migrate`, this command is initiated by the user at any time to record past solutions to typical problems.

## Usage

```
/add-lesson-learned
/add-lesson-learned <catalog>
```

`<catalog>` is one of:

- `migration` → appends to `.github/copilot/knowledge/lessons-learned/migration.md`
- `cucumber-debug` → appends to `.github/copilot/knowledge/lessons-learned/cucumber-debug.md`
- `review` → appends to `.github/copilot/knowledge/lessons-learned/review.md`
- `pattern` → appends to `.github/copilot/knowledge/migration-patterns.md`
- `pitfall` → appends to `.github/copilot/knowledge/migration-pitfalls.md`

If omitted, the agent asks which catalog first.

## Process

1. **Choose the catalog.** Confirm the target file with the user.
2. **Collect fields** appropriate to the catalog's entry format (see below). Ask one field at a time. Accept free-form user input; rewrite to English if the user answers in another language.
3. **Validate.** Refuse to record entries that are:
   - Vague wisdom ("be careful with DI").
   - Restatements of an existing `instructions/*` file.
   - Event logs ("migrated feature X on date Y" — belongs in the journal).
   - Non-English prose.
   If refused, explain why and offer to reframe.
4. **Preview.** Show the exact Markdown that will be appended.
5. **Confirm.** Ask once:

   > Append this to `<path>`? (y / n)

6. On `y`: append the entry to the end of the file, preserving the file's existing trailing newline and the `<!-- Entries go below ... -->` marker if present. On `n`: stop without writing.
7. Do not modify anything except the target file. Do not remove or rewrite prior entries.

## Catalog entry formats

### `migration` and `review` (lessons-learned)

```markdown
## <YYYY-MM-DD> <short title>
Context: <what was being done>
Observation: <what was surprising, missed, or worked well>
Rule: <actionable guideline phrased in imperative English>
Applies to: <migration | review>
Source: <journal link / PR link / chat reference / manual>
```

Required fields: date, short title, context, observation, rule, source. Use `Applies to: manual` or `Source: manual` when the lesson was recorded outside of a run.

### `cucumber-debug` (lessons-learned)

```markdown
## <YYYY-MM-DD> <short title>
Context: <what the scenario was trying to verify>
Symptom: <what the failure looked like — exception type, message pattern, stack top>
Root cause: <the actual cause once understood>
Fix: <one or two sentences; the change that resolved it>
Prevention: <how to avoid reintroducing this, preferably a review-time check>
Applies to: cucumber-debug
Source: <journal link / PR link / chat reference / manual>
```

Required fields: date, short title, context, symptom, root cause, fix, prevention, source.

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

Required fields: short title, added date, source journal, Cucumber shape, Kotlin + JUnit 5 shape, at least one note.

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
<what to do instead — short and imperative>

### Applies when
- <bullet list of signals that this pitfall is relevant>
```

Required fields: short title, added date, source journal, symptom, cause, mitigation, at least one "applies when" signal.

## Constraints

- English only.
- One entry per run. To add several, run the command again.
- Date format `YYYY-MM-DD`. If the user says "today", resolve to the current date.
- Do not alter the file's header, format guide, or standing-principles sections — only append under the trailing `<!-- Entries go below ... -->` marker.

## Related

- `docs/self-learning.md` — curator rotation and how catalogs stay useful.
- Agent-triggered equivalents: the end-of-run prompts inside `/review`, `/debug-cucumber`, and `/migrate` ask the same "append?" question after a successful run.
