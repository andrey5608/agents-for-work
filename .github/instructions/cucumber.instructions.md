---
applyTo: "**/*.feature,**/steps/**/*.kt"
---

# Cucumber rules

Applies to Gherkin `.feature` files and Kotlin step-definition files.

## Gherkin hygiene

- One `Given` / `When` / `Then` idea per step. No conjunctive logic hidden behind a single step.
- No control flow in `.feature` files. If behavior depends on data, use `Scenario Outline` with `Examples`.
- `Background:` holds truly shared preconditions. Do not use it to smuggle setup that only one scenario needs.
- Scenario names are complete English sentences describing the behavior.
- Tags are lowercase with optional `:value` suffix (`@smoke`, `@severity:critical`, `@TMS-1234`).

## Step definitions

- Kotlin files live under `src/test/kotlin/.../steps/`.
- DI via **constructor injection** — PicoContainer, Spring, or Guice, matching the project default. **No** static state holders.
- Shared scenario state travels through a scoped context object injected into every step class — not via `object` singletons, `ThreadLocal`, or file-level `var`.
- Each step method is thin: it delegates to a backend client / service helper. No business logic inline.
- Annotate user-facing action helpers with Allure `@Step("…")` so they appear in the report.
- Method names in English, imperative, matching the step they implement.

## Hooks

- `@Before` / `@After` hooks live in a dedicated hooks class.
- Tag-scoped hooks (`@Before("@db")`) are preferred over global hooks for expensive setup.
- Never mutate global state in a hook without a corresponding cleanup in `@After`.

## Lessons-learned cross-reference

`/debug-cucumber` consults `.github/copilot/knowledge/lessons-learned/cucumber-debug.md` and `.github/copilot/knowledge/migration-pitfalls.md` when diagnosing a failure. Contributors: when you fix a novel class of failure, add a lesson (append-only) after the user confirms.
