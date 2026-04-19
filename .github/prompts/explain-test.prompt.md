---
mode: 'agent'
description: 'Explain a JUnit 5 Kotlin test or a Cucumber scenario in a structured, English format.'
tools: ['codebase', 'findTestFiles']
---

# /explain-test

Explain the test at the path provided by the user.

## Input

- A path to a Kotlin JUnit 5 test file (`.kt`), optionally with `#<methodName>` to pin one method.
- OR a path to a Cucumber feature file (`.feature`), optionally with `:<scenarioName>` to pin one scenario.

## Process

1. Read the full target file.
2. For JUnit 5:
   - Identify every `@Test` method (or just the pinned one).
   - Resolve every helper call and every `@Step`-annotated wrapper invoked from the test body.
   - Read relevant helpers to understand the actual behavior exercised.
   - List Allure annotations present (`@Epic`, `@Feature`, `@Story`, `@Severity`, `@DisplayName`, `@Description`, `@Link`, `@Issue`, `@TmsLink`, `@Step`, `@Tag`).
3. For Cucumber:
   - Parse every step in the scenario.
   - Grep step-definition classes under `**/steps/**/*.kt` for `@Given`, `@When`, `@Then`, `@And`, `@But` matching the step text.
   - Build a `step:line → StepsClass.method:line` mapping. Mark unbound steps `UNBOUND STEP` — do not fabricate.
   - List scenario tags and the feature-level `Feature:` / `Rule:` lines.
4. Output the structured explanation below.

## Output shape (English only)

```markdown
# Explanation: <path>[:<methodOrScenario>]

## Purpose
<one paragraph — what behavior is verified and why it matters>

## Preconditions
- <bullet list of inputs, fixtures, state required>

## Actions
- <ordered bullet list of what the test does>

## Assertions
- <bullet list of what must be true at the end, including implicit ones>

## Bugs it catches
- <bullet list of regressions the test would flag>

## Limitations (what it does NOT catch)
- <bullet list of related behaviors outside this test's scope>

## Allure metadata
- Epic: ...
- Feature: ...
- Story: ...
- Severity: ...
- DisplayName: ...
- Description: ...
- Links: ...
- Tags: ...

## Step → step-definition map (Cucumber only)

| Step | Feature line | Method | File line |
|------|--------------|--------|-----------|
|      |              |        |           |
```

## Constraints

- English only, regardless of prompt language.
- Do not paraphrase the code when a precise verb describes it better (e.g., `POSTs to /users` rather than `sends a request`).
- Do not invent assertions or step bindings. If unsure, say so.
- Do not modify any files.
