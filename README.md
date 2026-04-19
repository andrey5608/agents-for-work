# agents-for-work

GitHub Copilot assets (instructions, prompts, chat modes, knowledge base, journal templates) for Kotlin + JUnit 5 / Cucumber backend autotest projects. This repo is a **template**: copy `.github/`, `docs/`, and `.editorconfig` into the target autotest repo.

See [`docs/README.md`](docs/README.md) for the full manual, and [`docs/workflows/`](docs/workflows/) for the migration and review workflows.

## Commands available to Copilot Chat

- `/review` — one-command review against 10 pre-filled rubrics.
- `/explain-test <path>` — structured explanation of a JUnit 5 or Cucumber test.
- `/debug-cucumber <feature>` — Cucumber failure diagnosis with step-to-method trace.
- `/migrate <feature> --scenario="..."` — Cucumber → Kotlin + JUnit 5 migration via conductor → worker → verifier.
- `/migrate-auto <feature> --scenario="..." [--retry-budget=N]` — autonomous variant: auto-approves the Draft when policy criteria pass, retries with scoped fixes on recoverable verifier blockers, falls back to `/migrate` otherwise. Scenario Outline port plan still requires human approval.
- `/add-lesson-learned [catalog]` — manually append an entry to a knowledge catalog (lessons-learned, migration-patterns, migration-pitfalls).
- `/commit` — generate a concise English commit message from the staged diff and run `git commit` after you approve it.
- `/create-api-autotest --module=<path> --endpoints="..."` — author a new Kotlin + JUnit 5 API test class against the given endpoints, mirroring the target module's existing test architecture.

## Hard rules (apply to every run)

- Output is always English.
- Backend only — no Page Objects / UI patterns.
- `.editorconfig` is honored on every write.
- Migrations go one scenario at a time, Draft → user approval → Final, with a verifier gate that runs the real test.
- Migrated tests are plain `@Test`; parameterization lives inside the body as private helper calls.
- All Allure metadata is preserved explicitly.
- Lessons-learned writes require explicit user `y`.
