---
name: debug-cucumber
description: Diagnose a failing Cucumber scenario with a step → Kotlin step-definition trace, cross-reference to the `cucumber-debug.md` lessons catalog, and a minimal repro command. Use when the user reports a failing `.feature` scenario, pastes a Cucumber stack trace, or asks to debug a Gherkin test.
allowed-tools: shell
---

# /debug-cucumber

Diagnose a failing Cucumber scenario.

## Usage

```
/debug-cucumber <path-to-feature>
/debug-cucumber <path-to-feature>:<scenarioName>
```

## Arguments

- `<path-to-feature>` — required. Optionally `:<scenarioName>` to pin one scenario.
- Optional follow-ups (pasted): stack trace, log excerpt, surefire report path.

## Behavior

1. **Parse the feature.** Extract target scenario(s). Record feature tags, scenario tags, every step with line number.
2. **Resolve step bindings.** Grep `**/steps/**/*.kt` for `@Given`/`@When`/`@Then`/`@And`/`@But`. Match by regex / cucumber-expression. Build `step:line → StepsClass.method:line`. Mark unbound steps `UNBOUND STEP` — never invent bindings.
3. **Stack-trace alignment** (when provided): top user frame (first frame inside `**/steps/**` or project packages), last step-definition frame reached, framework glue frames above it.
4. **Cross-reference lessons.** Read `.github/copilot/knowledge/lessons-learned/cucumber-debug.md`. Cite every entry whose symptom pattern matches.
5. **Check known failure classes**:
   - Ambiguous step definitions (two methods match one step).
   - Regex / cucumber-expression vs parameter-list mismatch.
   - Missing `@Before` hook for required state.
   - `DataTable` mapping incorrect (column order, missing header, wrong converter).
   - DI not wired — scenario context `null` when consumed.
   - Non-idempotent test data (previous scenario leaked state; ordering dependency).
   - External stub (WireMock) not primed; Testcontainers not ready.
6. **Emit a minimal repro**:
   ```
   mvn test -Dtest=<Runner> -Dcucumber.filter.name="<scenario>" -Dcucumber.features="<feature-path>"
   ```
   Adjust for non-default plugin config; state the assumption.
7. **Propose a fix** at the level of "what to change" — don't edit code unless asked.

## Output shape

```markdown
# Cucumber diagnosis: <feature>[:<scenario>]

## Failure signature
<short English restatement>

## Step → step-definition map

| Step | Feature line | Method | File line | Status |
|------|--------------|--------|-----------|--------|

## Stack trace alignment (if provided)
<top frames annotated with step / hook / framework layer>

## Matched prior lessons
- <title> → `lessons-learned/cucumber-debug.md#<anchor>` — fix: <one sentence>.

## Candidate root causes
- <ordered list, most → least likely, each with a check>

## Minimal repro
mvn test -Dtest=<Runner> -Dcucumber.filter.name="<scenario>" -Dcucumber.features="<path>"

## Recommended next action
<one short paragraph>
```

## End of run

If the failure class doesn't match any existing lesson, ask once:

> Record a new lesson in `lessons-learned/cucumber-debug.md`? (y / n)

`y` → append per `docs/self-learning.md`. `n` or no answer → stop without writing.

## DO / DON'T

- DO: English only.
- DO: cite line numbers for every binding and finding.
- DON'T: fabricate step bindings or invent methods.
- DON'T: modify any file except the lessons file on explicit `y`.

## Refuses

- Request to "make the test green" by editing assertions or step definitions without a diagnosed root cause.
- Request to write a lesson without a matched failure pattern.

## Related

- `.github/copilot/knowledge/lessons-learned/cucumber-debug.md`
- `docs/self-learning.md`
