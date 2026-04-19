---
mode: 'agent'
description: 'Diagnose a failing Cucumber scenario with step-to-method trace and lessons-learned cross-reference.'
tools: ['codebase', 'terminal', 'findTestFiles']
---

# /debug-cucumber

Diagnose a failing Cucumber scenario.

## Input

- A path to the `.feature` file, optionally `:<scenarioName>` to pin one scenario.
- Optional: a pasted stack trace, log excerpt, or surefire report path.

## Process

1. **Parse the feature.** Extract the target scenario (or all scenarios if unpinned). Record feature tags, scenario tags, and every step with its line number.
2. **Resolve step bindings.** Grep `**/steps/**/*.kt` for `@Given` / `@When` / `@Then` / `@And` / `@But` annotations. Match by regex / cucumber-expression against each step text. Produce a table `step:line → StepsClass.method:line`. Mark unbound steps explicitly — do not invent bindings.
3. **Analyze the stack trace** (if provided):
   - Find the top user frame (first frame inside `**/steps/**` or project packages).
   - Mark the last step-definition frame reached.
   - Note any framework glue frames (`io.cucumber.*`, `cucumber.runtime.*`) above it.
4. **Cross-reference lessons.** Read `.github/copilot/knowledge/lessons-learned/cucumber-debug.md`. For every entry whose symptom pattern matches the failure signature, cite the lesson and its documented fix.
5. **Check known failure classes** explicitly:
   - Ambiguous step definitions (two methods match the same step).
   - Regex / cucumber-expression vs parameter-list mismatch.
   - Missing `@Before` hook for required state.
   - `DataTable` mapping incorrect (column order, missing header, wrong converter).
   - DI not wired — scenario context `null` when consumed.
   - Non-idempotent test data (previous scenario left state; ordering dependency).
   - External stub (WireMock) not primed; Testcontainers not ready.
6. **Emit a minimal reproduction command**:
   - `./mvnw test -Dtest=<Runner> -Dcucumber.filter.name="<scenario>" -Dcucumber.features="<feature-path>"`
   - If the project uses a different Cucumber plugin config, adjust accordingly; state the assumption.
7. **Propose a fix** at the level of "what to change" — do not edit code unless explicitly asked.

## Output shape (English only)

```markdown
# Cucumber diagnosis: <feature>[:<scenario>]

## Failure signature
<short English restatement of what is failing>

## Step → step-definition map

| Step | Feature line | Method | File line | Status |
|------|--------------|--------|-----------|--------|

## Stack trace alignment (if provided)
<top frames annotated with step / hook / framework layer>

## Matched prior lessons
- <title> → `lessons-learned/cucumber-debug.md#<anchor>` — fix: <one sentence>.

## Candidate root causes
- <ordered list from most to least likely, each with a check to run>

## Minimal repro
./mvnw test -Dtest=<Runner> -Dcucumber.filter.name="<scenario>" -Dcucumber.features="<path>"

## Recommended next action
<one short paragraph>
```

## End of run

If the failure class does not match any existing lesson, ask once:

> Record a new lesson in `lessons-learned/cucumber-debug.md`? (y / n)

- On `y`: append an entry per the format in `docs/self-learning.md`.
- On `n` or no answer: stop without writing.

## Constraints

- English only.
- No fabricated step bindings, no invented methods.
- Do not modify files except the lessons file on explicit `y`.
