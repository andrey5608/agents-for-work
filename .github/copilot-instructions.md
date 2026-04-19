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
7. **Migrations are one scenario at a time.** Never batch. For `Scenario Outline` / `Examples`, produce and await approval of a per-row port plan before any code. See `chatmodes/migrate-conductor.chatmode.md`.
8. **Migration goes Draft → user approval → Final.** Short-circuit only when the user passes `--approved-concept=...` at invocation.
9. **Self-learning writes are append-only and only after explicit user `y`.** Knowledge files live under `.github/copilot/knowledge/`.

## Available commands (prompts)

- `/review` — one-command review against the pre-filled rubric set.
- `/explain-test <path>` — structured explanation of a JUnit 5 or Cucumber test.
- `/debug-cucumber <feature> [stack-trace]` — Cucumber failure diagnosis with step-to-method trace.
- `/migrate <feature> [--scenario="..."] [--approved-concept=...]` — Cucumber → Kotlin + JUnit 5 migration.
- `/migrate-auto <feature> [--scenario="..."] [--retry-budget=N] [--approved-concept=...]` — autonomous variant with policy-driven Draft approval and bounded retry-with-fix. Falls back to `/migrate` on any failing criterion; always blocks on Scenario Outline port plan.
- `/add-lesson-learned [catalog]` — manually append an entry to one of the knowledge catalogs (migration / cucumber-debug / review / pattern / pitfall).
- `/commit` — generate a concise English commit message from the staged diff and run `git commit` after explicit approval.

## Available custom chat modes (agents)

- `migrate-conductor` — orchestrates an interactive migration, owns the journal.
- `migrate-conductor-auto` — autonomous variant with auto-approval policy + retry-with-fix loop; delegates to the same worker and verifier.
- `migrate-worker` — produces the Kotlin test code.
- `migrate-verifier` — build / test / Allure / editorconfig gate.

## Knowledge base layout

- `.github/copilot/knowledge/lessons-learned/migration.md` — feeds conductor + worker.
- `.github/copilot/knowledge/lessons-learned/cucumber-debug.md` — feeds `/debug-cucumber`.
- `.github/copilot/knowledge/lessons-learned/review.md` — feeds `/review`.
- `.github/copilot/knowledge/migration-patterns.md` — canonical Cucumber → JUnit 5 mappings.
- `.github/copilot/knowledge/migration-pitfalls.md` — known traps.
- `.github/copilot/journal/` — per-migration journals, `_INDEX.md` lists them.

## JetBrains IntelliJ IDEA fallback

Chat modes and prompt files are primarily a VS Code feature. In IntelliJ, use the paste-in prompts in `docs/jetbrains-cheatsheet.md`. This file (`copilot-instructions.md`) and every `instructions/*.instructions.md` are still auto-loaded there.
