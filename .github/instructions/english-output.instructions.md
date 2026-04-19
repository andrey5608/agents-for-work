---
applyTo: "**"
---

# English-output enforcement

All agent-produced text is **English**, regardless of the language of the user prompt, command argument, filename, or stored data.

## Covered artifacts

- Chat answers and clarifying questions.
- Journal entries under `.github/copilot/journal/`.
- Draft migration documents.
- Verifier reports (JSON field names, messages, logs).
- Lessons-learned entries.
- Kotlin code: identifiers, `@DisplayName`, `@Description`, `@Step`, helper method names, error messages, log messages, TODO comments.
- Commit messages and PR descriptions.

## Allowed non-English content

- Feature files (`.feature`) that already exist in the source project — untouched by migration.
- Direct quotations from user prompts when the agent needs to echo them back for confirmation — quote verbatim but frame the surrounding text in English.
- Third-party library identifiers, URLs, user-provided ticket IDs.

## Enforcement

If a draft, report, or code edit contains non-English prose, the agent must rewrite it before emitting. Verifier rejects tests that contain non-English `@DisplayName` / `@Description` / log strings.
