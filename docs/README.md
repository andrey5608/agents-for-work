# agents-for-work — GitHub Copilot assets for Kotlin backend autotests

This repository is a **template** of GitHub Copilot assets (instructions, skills, custom agents, knowledge base, journal) tailored for a Kotlin + JUnit 5 / Cucumber / Allure backend autotest project built with Maven and running on Jenkins CI.

The agent/skill structure targets VS Code Copilot's custom-agents model: each agent is a standalone assistant with its own tools and subagents, not a chat-mode style of the same assistant.

## What you get

- **Global repo instructions** — `.github/copilot-instructions.md`
- **Scoped instructions** — `.github/instructions/*.instructions.md` for Kotlin, JUnit 5, Cucumber, Allure, review rules, `.editorconfig` compliance, English-output enforcement, and migration knowledge.
- **Skills (slash commands)** — `.github/skills/<name>/SKILL.md`:
  - `/review` — one-command review against 10 rubrics
  - `/explain-test` — structured test explanation
  - `/debug-cucumber` — Cucumber failure diagnosis with step-to-method trace
  - `/migrate` — Cucumber → Kotlin + JUnit 5 migration (interactive)
  - `/migrate-auto` — autonomous migration: policy-driven Draft approval + bounded retry-with-fix
  - `/add-lesson-learned` — manually append an entry to a knowledge catalog
  - `/commit` — generate a concise English commit message from the staged diff and run `git commit` after explicit approval
  - `/create-api-autotest` — author a new Kotlin + JUnit 5 API test class for a specified endpoint set, mirroring the target module's existing architecture
  - `/skill-creator` — scaffold a new skill at `.github/skills/<name>/SKILL.md` and register it across the repo's skill indexes (copilot-instructions, docs/README, jetbrains-cheatsheet)
- **Custom agents** — `.github/agents/*.agent.md`:
  - `migrate-conductor` — orchestrates an interactive migration, owns the journal; subagents: `migrate-worker`, `results-verifier`
  - `migrate-conductor-auto` — autonomous variant with auto-approval + retry loop; handoff to `migrate-conductor` on escalation
  - `migrate-worker` — produces Kotlin test code (`task: write-test`) and, after a green initial verify, removes the migrated Cucumber scenario (`task: delete-scenario`); not user-invocable directly
  - `results-verifier` — orchestrator that composes atomic verifiers into the final JSON report; reused by the authoring flow with `source: authored`; not user-invocable directly
  - Atomic verifiers — `build-and-test-verifier`, `legacy-baseline-verifier`, `scenario-removal-verifier`, `allure-metadata-verifier`, `editorconfig-verifier`, `anti-pattern-verifier`, `migration-parity-verifier` — each with a single responsibility, composed by `results-verifier`; none user-invocable directly
  - `api-test-author` — authors new Kotlin + JUnit 5 API tests for a specified endpoint set; extracts and mirrors the target module's existing architectural scheme; subagent: `results-verifier`
- **Knowledge base** — `.github/copilot/knowledge/`:
  - `lessons-learned/{migration,cucumber-debug,review}.md` (append-only)
  - `migration-patterns.md`, `migration-pitfalls.md`
- **Migration journal** — `.github/copilot/journal/`
- **Templates** — `.github/copilot/templates/` (draft, outline port plan, Allure mapping, header, review checklist, auto-approval checklist, API test draft)
- **Example project** — `migration-examples/sample-cucumber/` (used for smoke testing the whole toolchain)

## Install into a target repo

1. Copy the `.github/`, `docs/`, and `.editorconfig` of this template into the root of your autotest repo. If the target already has a `.editorconfig`, keep the target's — it is the source of truth for the project.
2. Adjust `.github/copilot-instructions.md` if your project uses different libraries (replace WireMock / Testcontainers examples where applicable).
3. Do **not** copy `migration-examples/` — it exists only for smoke-testing the template in isolation.
4. Open the repo in VS Code or IntelliJ IDEA with GitHub Copilot enabled. Assets are picked up automatically.

## Use from VS Code Copilot Chat + Agent Mode

- `/review` — run inside Copilot Chat with the diff loaded.
- `/explain-test <path>` — path to a `.kt` test or a `.feature` scenario.
- `/debug-cucumber <path-to-feature>` — optionally attach a stack trace snippet.
- `/migrate <path-to-feature> --scenario="<name>"` — migrate one scenario. Add `--approved-concept=...` to skip the Draft approval step.
- `/migrate-auto <path-to-feature> --scenario="<name>" [--retry-budget=N]` — autonomous run. The Draft is auto-approved when every criterion in `auto-approval-checklist.template.md` passes; any failing criterion falls back to `/migrate`. A Scenario Outline port plan still requires live approval. Verifier blockers are retried up to `--retry-budget` times (default `3`, max `5`) with scoped fixes; non-auto-fixable classes escalate immediately.
- `/add-lesson-learned [catalog]` — record a past solution or recurring issue manually, outside the end-of-run prompts.
- `/commit` — generate a concise English subject line (and body only when the subject cannot carry the intent alone) from the staged diff, preview it, and run `git commit` on `y`. Add `--include-unstaged` to run `git add -u` first (never stages untracked files). Add `--message="..."` to skip generation and use the provided subject verbatim. Refuses to pass `--no-verify` or to `--amend`.
- `/create-api-autotest --module=<path> --endpoints="METHOD /path, ..."` — author new API tests. The agent locates existing tests under the module, extracts the module's architectural scheme (HTTP client, base class, fixtures, auth wiring, Allure convention), fills `api-test-draft.template.md`, asks for approval (unless `--approved-concept=...`), writes one test class with one plain `@Test` per (endpoint × scenario), and runs the verifier with `source: authored` (Gate 3 legacy-parity is `skipped`; Gate 6 applies the same anti-pattern set as migration — `@ParameterizedTest` is rejected for authored too). Refuses if the module has no tests **and** no sibling module to mirror.
- `/skill-creator [name] [--delegates-to=<agent>]` — interactively scaffold a new skill at `.github/skills/<name>/SKILL.md`. Collects name, description, allowed-tools, delegation target (agent or direct), usage, behavior, invariants, and refusals; previews the full SKILL.md + registry diffs; writes on `y`; registers the command in `.github/copilot-instructions.md`, `docs/README.md`, and `docs/jetbrains-cheatsheet.md`.
- Pick `migrate-conductor` or `migrate-conductor-auto` from the agents dropdown when you want to drive the full orchestrated flow directly without going through a slash command. Either conductor delegates to `migrate-worker` and `results-verifier`.

## Autonomous migration — hands-free run

`/migrate-auto` is designed to run without live approval on the Draft gate, but preserves every safety net that the interactive flow has:

- The Scenario Outline port plan still requires a human — autonomous mode will not auto-approve it.
- Verifier gates (build, new test, legacy parity, Allure metadata, `.editorconfig`, anti-patterns) are unchanged. The retry loop only re-emits the failing file through `migrate-worker`; it never disables a gate, weakens an assertion, or strips Allure annotations.
- Lessons-learned writes still require live `y`, with **one exception**: a novel failure class observed during retries produces a single `Review: pending` stub in `lessons-learned/migration.md`. The curator promotes or discards it at the next rotation.

For CI-isolated runs, assign a GitHub issue titled `Migrate scenario "<name>" from <feature-path>` to **GitHub Copilot** (the coding agent). Put the exact `/migrate-auto …` command in the body. Copilot opens a draft PR and runs the autonomous conductor there; escalations surface as PR comments with the retry log attached.

## Use from IntelliJ IDEA Copilot Chat

IntelliJ, Eclipse, and Xcode do not natively invoke custom agents or skills. Use `docs/jetbrains-cheatsheet.md` — it contains copy-paste prompt strings that reproduce the behavior. Global `copilot-instructions.md` and `instructions/*` are auto-loaded there.

## Principles you are agreeing to

- Output is always English regardless of the language you ask in.
- Backend only — no Page Objects / UI patterns.
- `.editorconfig` is honored on every write.
- Migrations are one scenario at a time, Draft → approval → Final, with a verifier gate that runs the real test and parses surefire + Allure.
- Every test is a plain `@Test` method (migrated and authored alike); parameterization lives inside the method body as private helper calls — never `@ParameterizedTest` / `@MethodSource` / `@ValueSource` / `@CsvSource` / `@CsvFileSource` / `@EnumSource` / `@ArgumentsSource` / `@TestFactory`. Reason: Allure's parameterized-test reporting is unreliable.
- All Allure metadata from Cucumber sources is explicitly copied into the JUnit 5 test.
- Lessons-learned are append-only and require explicit user `y` before any write.

## Further reading

- `docs/workflows/migration-workflow.md`
- `docs/workflows/review-workflow.md`
- `docs/self-learning.md`
- `docs/jetbrains-cheatsheet.md`
