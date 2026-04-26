---
name: explain-test
description: Explain a JUnit 5 Kotlin test (`.kt`) or a Cucumber scenario (`.feature`) in a structured English format (Purpose → Preconditions → Actions → Assertions → Bugs it catches → Limitations) with Allure metadata listed and, for Cucumber, a step → step-definition method trace. Use when the user asks what a test does, how it works, or to explain a `.kt` or `.feature` file.
---

# /explain-test

Explain the test at the path provided by the user.

## Usage

```
/explain-test <path-to-kt>[#<methodName>]
/explain-test <path-to-feature>[:<scenarioName>]
```

## Arguments

- A path to a Kotlin JUnit 5 test (`.kt`), optionally `#<methodName>` to pin one method, OR
- a Cucumber feature (`.feature`), optionally `:<scenarioName>` to pin one scenario.

## Behavior

1. Read the full target file.
2. **JUnit 5**:
   - Identify every `@Test` method (or just the pinned one).
   - Resolve every helper call and every `@Step` wrapper invoked from the test body.
   - Read relevant helpers to understand the actual behavior.
   - List Allure annotations present (`@Epic`, `@Feature`, `@Story`, `@Severity`, `@DisplayName`, `@Description`, `@Link`, `@Issue`, `@TmsLink`, `@Step`, `@Tag`).
3. **Cucumber**:
   - Parse every step in the scenario.
   - Grep step-definition classes under `**/steps/**/*.kt` for `@Given`/`@When`/`@Then`/`@And`/`@But`.
   - Build `step:line → StepsClass.method:line`. Mark unbound steps `UNBOUND STEP` — never fabricate.
   - List scenario tags + the feature-level `Feature:` / `Rule:` lines.
4. Emit the structured explanation.

## Output shape

```markdown
# Explanation: <path>[:<methodOrScenario>]

## Purpose
<one paragraph — what behavior is verified and why it matters>

## Preconditions
- <inputs, fixtures, state required>

## Actions
- <ordered list of what the test does>

## Assertions
- <what must be true at the end, including implicit assertions>

## Bugs it catches
- <regressions the test would flag>

## Limitations (what it does NOT catch)
- <related behaviors outside this test's scope>

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
```

## DO / DON'T

- DO: English only.
- DO: prefer precise verbs over paraphrase (`POSTs to /users`, not `sends a request`).
- DON'T: invent assertions or step bindings — say "unsure" if it isn't clear.
- DON'T: modify any file.
