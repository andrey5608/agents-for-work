---
name: skill-creator
description: Scaffold a new VS Code Copilot skill at `.github/skills/<name>/SKILL.md` — interview the user for name, description, allowed-tools, target agent (if any), usage, behavior, invariants, refusals — then preview the full SKILL.md, ask for explicit `y`, write the file, and register the slash command in `.github/copilot-instructions.md`, `docs/README.md`, and `docs/jetbrains-cheatsheet.md`. Use when the user asks to create a new skill, add a new slash command, or runs /skill-creator.
allowed-tools: edit
---

# /skill-creator

Scaffold a new VS Code Copilot skill in this repository. One run produces a directory `.github/skills/<name>/` with a single `SKILL.md` and the three matching registry lines in the top-level docs. Nothing else.

## Usage

```
/skill-creator
/skill-creator <name>
/skill-creator <name> --delegates-to=<agent-name>
```

- `<name>` — optional. If omitted, the agent asks. Must be kebab-case (`[a-z][a-z0-9-]*`), no collision with an existing entry under `.github/skills/`.
- `--delegates-to=<agent-name>` — optional. If the skill is a thin wrapper that just invokes a custom agent, pass the agent's name here. The agent must exist under `.github/agents/<agent-name>.agent.md`; otherwise refuse.

## Process

1. **Pick the name.**
   - Validate: kebab-case, `[a-z][a-z0-9-]*`, length 3–32.
   - Check `.github/skills/<name>/` does not exist. On collision, refuse with: `skill "<name>" already exists — pick a different name`. Do not offer to overwrite.
   - Confirm the slash command form: `/<name>`.

2. **Collect the one-line `description`.**
   - Ask: "In one sentence, what does `/<name>` do, and when should Copilot choose it?"
   - Rewrite to English if the answer was in another language.
   - Refuse vague descriptions ("helps with tests", "improves code") — ask for a concrete trigger and outcome. Good descriptions name the action, the target artifact, and a use-when clause.
   - Final shape: `<imperative verb phrase>. Use when <trigger>.` — one or two sentences.

3. **Decide `allowed-tools`.**
   - Ask what the skill needs at runtime:
     - `shell` — runs terminal commands (git, maven, grep, curl).
     - `edit` — writes or appends files.
     - *unset* — purely conversational / uses only the invoked agent's tools.
   - A skill that only delegates to an agent does **not** need `allowed-tools`; the agent carries its own `tools` list.

4. **Decide delegation target.**
   - Ask: "Does `/<name>` delegate to a custom agent, or run directly as a command?"
   - If agent-delegating:
     - Require the agent name (or take it from `--delegates-to=...`).
     - Verify the agent file exists at `.github/agents/<agent-name>.agent.md` — if not, refuse.
     - The skill body will open with `Delegate to the \`<agent-name>\` agent ...`.
   - If direct:
     - Collect the exact steps the skill should execute.

5. **Collect the body fields.** Ask one at a time, accept free-form input, rewrite to English:
   - **Arguments** — flag list with one-line explanations. Mark required vs. optional.
   - **Behavior** — a numbered list of steps the skill takes.
   - **Invariants** — non-negotiables (English-only output, backend-only, one-per-run, etc.). Inherit from `.github/copilot-instructions.md` where applicable instead of restating.
   - **Refusals** — bullets the skill refuses to do. Every refusal must name the thing refused AND the reason.
   - **Related files** — paths to agents, templates, other skills that matter.

6. **Refuse anti-patterns**:
   - Skill name duplicates an existing slash command (including built-ins like `/help`, `/clear`).
   - Description restates what the target agent already documents — the skill description is the *trigger*, not a re-documentation of the agent.
   - Non-English prose in any field.
   - Instructions that weaken an existing invariant (e.g., "skip Allure check", "allow `Thread.sleep`").
   - Requests to make the skill write without explicit user `y` when the underlying agent would normally ask.

7. **Preview.** Emit the full SKILL.md inside a fenced code block, plus the three registry diffs (copilot-instructions.md, docs/README.md, docs/jetbrains-cheatsheet.md). Let the user see the exact bytes that will be written.

8. **Confirm.** Ask exactly once:

   > Create `/<name>` at `.github/skills/<name>/SKILL.md` and register it in copilot-instructions + docs? (y / n / edit)

   - `y` → write the SKILL.md, append the registry lines, and print the resulting file paths.
   - `n` → stop. Nothing is written.
   - `edit` → collect the delta on whichever field the user names, re-preview, re-ask.

## Shape of the generated SKILL.md

```markdown
---
name: <name>
description: <one-line description with a "Use when ..." clause>
allowed-tools: <shell | edit | omit>
---

# /<name>

<one-paragraph summary>

## Usage

```
/<name> <args>
```

## Arguments

- `<flag>` — <purpose>. <required | optional>.

## Behavior

1. <step>
2. <step>

## Invariants restated

- <rule> — inherited from `.github/copilot-instructions.md`.

## Refusals

- <refusal> — <reason>.

## Related files

- <path> — <why it matters>.
```

Agent-delegating skills open step 1 with `Delegate to the \`<agent>\` agent (see \`.github/agents/<agent>.agent.md\`).`

## Registry updates

After writing the SKILL.md, append one registry line to each of:

- `.github/copilot-instructions.md` — under the **Available skills (slash commands)** list, preserving alphabetical order.
- `docs/README.md` — under the **Skills (slash commands)** bullet block.
- `docs/jetbrains-cheatsheet.md` — a new section titled `## /<name>` with a one-paragraph copy-paste prompt that reproduces the skill's behavior for IntelliJ users (who cannot auto-load skills).

If any of those three files does not contain the expected section, surface this to the user and ask where to register — do not silently skip.

## Invariants restated

- English output only (including the generated SKILL.md).
- One skill per run. No batching.
- Never overwrite an existing skill — refuse on collision.
- Never register a skill in the docs without also writing its SKILL.md in the same run.
- Knowledge-base writes (lessons-learned, patterns, pitfalls) are out of scope for this skill — use `/add-lesson-learned` instead.

## Refusals

- Name collision with an existing `.github/skills/<name>/` directory.
- Name is not kebab-case or is outside length 3–32.
- `--delegates-to=<agent>` names a non-existent agent file.
- Description is vague, non-English, or restates the delegated agent's own documentation.
- A request to make the skill bypass user approval that the underlying agent normally asks for.
- A request to write production code or test code from `/skill-creator` — this command only authors skill files and registry entries.

## Related files

- `.github/copilot-instructions.md` — the repo's standing rules and the skill registry.
- `docs/README.md` — the user-facing skill inventory.
- `docs/jetbrains-cheatsheet.md` — IntelliJ copy-paste prompts.
- `.github/agents/*.agent.md` — custom agents a new skill may delegate to.
- `.github/skills/add-lesson-learned/SKILL.md` — canonical example of a self-contained skill (does not delegate to an agent).
- `.github/skills/migrate/SKILL.md` — canonical example of a delegating skill (thin wrapper over `migrate-conductor`).
