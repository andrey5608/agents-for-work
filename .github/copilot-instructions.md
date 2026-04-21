# Copilot repository instructions

This repository hosts autotests for a **backend-only** service. Test stack:

- Runtime: **Java 17**
- Sources: **Kotlin**
- Test frameworks: **JUnit 5** and **Cucumber** (Gherkin)
- Reporting: **Allure**
- Build: **Maven**
- CI: **Jenkins** (pipeline is defined elsewhere; no GitHub Actions)

## Hard rules for all Copilot surfaces (Chat, Agent Mode, IDE inline)

1. **Output language is English.** Every chat answer, report, journal entry, draft, code comment, `@DisplayName`, `@Description`, commit message — English. The user prompt may be in any language; the response is English. See `instructions/english-output.instructions.md`.
2. **Backend only.** Never introduce `Page Object`, `Screen`, `WebDriver`, `Selenium`, `Selenide`, or any UI-test pattern. Test subjects are HTTP/gRPC services, databases, queues. Stubs via WireMock / Testcontainers / REST-assured.
3. **Respect `.editorconfig`.** Before writing any file, resolve the nearest `.editorconfig` and honor `indent_style`, `indent_size`, `tab_width`, `charset`, `end_of_line`, `trim_trailing_whitespace`, `insert_final_newline`. See `instructions/editorconfig-compliance.instructions.md`.
4. **Kotlin sources live under `src/test/kotlin/...`** — never under `src/test/java/...`.
5. **Allure annotations are preserved explicitly** on every migrated and every new test. See `instructions/allure.instructions.md`.
6. **Migrated tests are plain `@Test`.** Do not introduce `@ParameterizedTest`, `@MethodSource`, or any Test Matrix construct. Parameterization lives inside the test body via private helper methods.
7. **Migrations are one scenario at a time.** Never batch. For `Scenario Outline` / `Examples`, produce and await approval of a per-row port plan before any code. See `agents/migrate-conductor.agent.md`.
8. **Migration goes Draft → user approval → Final.** Short-circuit only when the user passes `--approved-concept=...` at invocation.
9. **Self-learning writes are append-only and only after explicit user `y`.** Knowledge files live under `.github/copilot/knowledge/`.
10. **Atomicity of agents.** Every agent has exactly one responsibility. Verifier gates (build, test execution, legacy baseline, scenario removal, Allure metadata, `.editorconfig`, anti-patterns, migration parity) are split across separate atomic custom agents; `results-verifier` is a thin orchestrator that composes their partial reports. When adding new behavior, prefer a new atomic agent over bolting a second responsibility onto an existing one. The invariant: if a single agent needs the word "and" to describe what it does, consider whether it should be two.

## Repository layout for Copilot surfaces

This repo targets **VS Code Copilot** as the primary surface. The configuration is split across three layers per the VS Code custom-agents spec:

- **Custom agents** — `.github/agents/*.agent.md`. Each agent has its own tools, subagents, and model preferences in YAML frontmatter; the body is the system prompt. An agent is a standalone Copilot assistant with its own toolset, not just a conversation style.
- **Skills** — `.github/skills/<skill-name>/SKILL.md`. A skill is a task-specific instruction package that Copilot selects based on its description. Skills map to the slash commands the user types (`/migrate`, `/review`, etc.) and delegate to the appropriate agent(s) for execution.
- **Instructions** — `.github/copilot-instructions.md` (this file; always loaded) and `.github/instructions/*.instructions.md` (scoped by `applyTo` glob). Instructions capture constraints that apply to every task in their scope; skills capture what the user asks the assistant to *do*; agents capture who does it.

JetBrains IntelliJ IDEA, Eclipse, and Xcode auto-load the two instructions layers but do not natively invoke custom agents or skills. For those surfaces, see `docs/jetbrains-cheatsheet.md` for paste-in equivalents.

## Available skills (slash commands)

Each skill lives at `.github/skills/<name>/SKILL.md`. The slash command is the skill name.

- `/review` — one-command review against the pre-filled rubric set.
- `/explain-test <path>` — structured explanation of a JUnit 5 or Cucumber test.
- `/debug-cucumber <feature> [stack-trace]` — Cucumber failure diagnosis with step-to-method trace.
- `/migrate <feature> [--scenario="..."] [--approved-concept=...]` — Cucumber → Kotlin + JUnit 5 migration. Delegates to `migrate-conductor`.
- `/migrate-auto <feature> [--scenario="..."] [--retry-budget=N] [--approved-concept=...]` — autonomous variant with policy-driven Draft approval and bounded retry-with-fix. Delegates to `migrate-conductor-auto`. Falls back to `/migrate` on any failing criterion; always blocks on Scenario Outline port plan.
- `/add-lesson-learned [catalog]` — manually append an entry to one of the knowledge catalogs (migration / cucumber-debug / review / pattern / pitfall).
- `/commit` — generate a concise English commit message from the staged diff and run `git commit` after explicit approval.
- `/create-api-autotest --module=<path> --endpoints="..."` — author a new Kotlin + JUnit 5 API test class for the given endpoint set, mirroring the module's existing architectural scheme. Delegates to `api-test-author`.
- `/skill-creator [name] [--delegates-to=<agent>]` — scaffold a new skill at `.github/skills/<name>/SKILL.md` and register it in `.github/copilot-instructions.md`, `docs/README.md`, and `docs/jetbrains-cheatsheet.md`. Refuses name collisions and vague descriptions.

## Available custom agents

Each agent lives at `.github/agents/<name>.agent.md`. The frontmatter declares its tools, subagents, preferred models, and whether it is user-invocable directly or only via a parent agent.

### Orchestrators / authors

- `migrate-conductor` — orchestrates an interactive migration, owns the journal. Subagents: `migrate-worker`, `results-verifier`.
- `migrate-conductor-auto` — autonomous variant with auto-approval policy + retry-with-fix loop across both verify phases; delegates to the same worker and verifier. Handoff to `migrate-conductor` on escalation.
- `migrate-worker` — produces the Kotlin test code (`task: write-test`) and, after a green initial verify, deletes the migrated Cucumber scenario (`task: delete-scenario`). Not user-invocable directly — called by the conductors.
- `api-test-author` — authors new Kotlin + JUnit 5 API tests for a specified endpoint set, mirroring the target module's existing architectural scheme. Subagent: `results-verifier`.

### Verifier orchestrator

- `results-verifier` — selects and composes the atomic verifiers below based on `source` × `phase`. Owns no gate logic itself. Not user-invocable directly — called by the conductors and the authoring agent.

### Atomic verifiers (each single-responsibility, not user-invocable directly)

- `build-and-test-verifier` — builds the project and runs a specific JUnit 5 test; emits `build_status` + `new_test_status`.
- `legacy-baseline-verifier` — runs the legacy Cucumber scenario and confirms the pre-migration baseline is green (migration + `phase: initial`).
- `scenario-removal-verifier` — confirms a migrated Cucumber scenario has been deleted from its `.feature` and no longer runs (migration + `phase: post-cleanup`).
- `allure-metadata-verifier` — checks `target/allure-results/*-result.json` for the required Allure labels on the new test.
- `editorconfig-verifier` — checks a file's compliance with the nearest `.editorconfig`.
- `anti-pattern-verifier` — static scan for `Thread.sleep`, UI libs, non-English strings, and (for migrated tests only) JUnit 5 parameterization.
- `migration-parity-verifier` — counts cases in the new test file and compares them to the port plan's non-dropped-row count.

## Tool namespaces used by agents

Tools are declared in the `tools:` field of each agent's YAML frontmatter. The values used in this repo:

- `agent` — enables the agent to invoke the subagents listed in its `agents:` field.
- `edit` — edit files in the workspace.
- `run/terminal` — run shell commands.
- `search/codebase` — semantic/textual search over the workspace.
- `search/usages` — find all usages of a symbol.
- `search/findTestFiles` — locate test files by convention.
- `web/fetch` — fetch a URL (used when the skill receives a path to an external spec, e.g., a hosted OpenAPI document).

Atoms are scoped tightly; conductors and authors are given broad tool access so they can operate autonomously. `user-invocable: false` in the frontmatter hides a subagent from the agents dropdown — it is still reachable via its parent.

## Knowledge base layout

- `.github/copilot/knowledge/lessons-learned/migration.md` — feeds conductor + worker.
- `.github/copilot/knowledge/lessons-learned/cucumber-debug.md` — feeds `/debug-cucumber`.
- `.github/copilot/knowledge/lessons-learned/review.md` — feeds `/review`.
- `.github/copilot/knowledge/migration-patterns.md` — canonical Cucumber → JUnit 5 mappings.
- `.github/copilot/knowledge/migration-pitfalls.md` — known traps.
- `.github/copilot/journal/` — per-migration journals, `_INDEX.md` lists them.

## JetBrains IntelliJ IDEA fallback

Agents and skills are a VS Code feature. In IntelliJ, use the paste-in prompts in `docs/jetbrains-cheatsheet.md`. This file (`copilot-instructions.md`) and every `instructions/*.instructions.md` are still auto-loaded there.
