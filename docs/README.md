# agents-for-work — GitHub Copilot assets for Kotlin backend autotests

This repository is a **template** of GitHub Copilot assets (instructions, prompts, chat modes, knowledge base, journal) tailored for a Kotlin + JUnit 5 / Cucumber / Allure backend autotest project built with Maven and running on Jenkins CI.

## What you get

- **Global repo instructions** — `.github/copilot-instructions.md`
- **Scoped instructions** — `.github/instructions/*.instructions.md` for Kotlin, JUnit 5, Cucumber, Allure, review rules, `.editorconfig` compliance, English-output enforcement, and migration knowledge.
- **Prompts (slash commands)** — `.github/prompts/*.prompt.md`:
  - `/review` — one-command review against 10 rubrics
  - `/explain-test` — structured test explanation
  - `/debug-cucumber` — Cucumber failure diagnosis with step-to-method trace
  - `/migrate` — Cucumber → Kotlin + JUnit 5 migration
- **Chat modes (agents)** — `.github/chatmodes/*.chatmode.md`:
  - `migrate-conductor` — orchestrates a migration, owns the journal
  - `migrate-worker` — produces Kotlin test code
  - `migrate-verifier` — build / test / Allure / editorconfig gate
- **Knowledge base** — `.github/copilot/knowledge/`:
  - `lessons-learned/{migration,cucumber-debug,review}.md` (append-only)
  - `migration-patterns.md`, `migration-pitfalls.md`
- **Migration journal** — `.github/copilot/journal/`
- **Templates** — `.github/copilot/templates/` (draft, outline port plan, Allure mapping, header, review checklist)
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
- `/migrate <path-to-feature> --scenario="<name>"` — migrate one scenario. Add `--approved-concept=...` to skip the draft approval step.
- Switch to `migrate-conductor` chat mode when you want the full orchestrated flow; the conductor delegates to `migrate-worker` and `migrate-verifier`.

## Use from IntelliJ IDEA Copilot Chat

IntelliJ does not natively understand `.prompt.md` or `.chatmode.md` files. Use `docs/jetbrains-cheatsheet.md` — it contains copy-paste prompt strings that reproduce the behavior. Global `copilot-instructions.md` and `instructions/*` are auto-loaded.

## Principles you are agreeing to

- Output is always English regardless of the language you ask in.
- Backend only — no Page Objects / UI patterns.
- `.editorconfig` is honored on every write.
- Migrations are one scenario at a time, Draft → approval → Final, with a verifier gate that runs the real test and parses surefire + Allure.
- Migrated tests are plain `@Test` methods; parameterization lives inside the method body as helper calls, not as `@ParameterizedTest`.
- All Allure metadata from Cucumber sources is explicitly copied into the JUnit 5 test.
- Lessons-learned are append-only and require explicit user `y` before any write.

## Further reading

- `docs/workflows/migration-workflow.md`
- `docs/workflows/review-workflow.md`
- `docs/self-learning.md`
- `docs/jetbrains-cheatsheet.md`
