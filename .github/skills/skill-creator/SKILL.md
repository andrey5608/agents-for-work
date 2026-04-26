---
name: skill-creator
description: Scaffold a new VS Code Copilot skill at `.github/skills/<name>/SKILL.md` — interview the user for name, description, allowed-tools, target agent (if any), usage, behavior, DO/DON'T, refusals — then preview the full SKILL.md, ask for explicit `y`, write the file, and register the slash command in `.github/copilot-instructions.md`, `docs/README.md`, and `docs/jetbrains-cheatsheet.md`. Use when the user asks to create a new skill, add a new slash command, or runs /skill-creator.
allowed-tools: edit
---

# /skill-creator

Scaffold a new VS Code Copilot skill in this repository. One run produces `.github/skills/<name>/SKILL.md` and three matching registry lines. Nothing else.

## Usage

```
/skill-creator
/skill-creator <name>
/skill-creator <name> --delegates-to=<agent-name>
```

## Arguments

- `<name>` — optional. If omitted, ask. Kebab-case `[a-z][a-z0-9-]*`, length 3–32. No collision with an existing skill.
- `--delegates-to=<agent-name>` — optional. The skill is a thin wrapper for a custom agent. The agent must exist at `.github/agents/<agent-name>.agent.md`; otherwise refuse.

## Behavior

1. **Pick the name.** Validate kebab-case + length. Check `.github/skills/<name>/` doesn't exist — on collision, refuse with `skill "<name>" already exists — pick a different name`. Confirm slash form `/<name>`.

2. **Collect the one-line `description`.**
   - "In one sentence, what does `/<name>` do, and when should Copilot choose it?"
   - Rewrite to English.
   - Refuse vague descriptions ("helps with tests", "improves code") — ask for a concrete trigger + outcome.
   - Final shape: `<imperative verb phrase>. Use when <trigger>.` — one or two sentences.

3. **Decide `allowed-tools`.**
   - `shell` — runs terminal commands.
   - `edit` — writes / appends files.
   - *unset* — purely conversational, or only the delegated agent's tools.
   - A skill that only delegates to an agent does **not** need `allowed-tools`.

4. **Decide delegation.**
   - "Does `/<name>` delegate to a custom agent or run directly?"
   - Delegating: require the agent name (or `--delegates-to=...`); verify the agent file exists; otherwise refuse. Skill body opens with `Delegate to the \`<agent-name>\` agent ...`.
   - Direct: collect the exact steps.

5. **Collect the body fields.** Ask one at a time, accept free-form, rewrite to English:
   - **Arguments** — flag list with one-line explanations. Mark required vs. optional.
   - **Behavior** — numbered list of steps.
   - **DO / DON'T** — lean bullets that capture the non-negotiables. Inherit from `.github/copilot-instructions.md` instead of restating common rules.
   - **Refuses** — bullets the skill refuses. Each names the thing refused AND the reason.
   - **Related** — paths to agents, templates, other skills that matter.

6. **Refuse anti-patterns**:
   - Name duplicates an existing slash command (including `/help`, `/clear`).
   - Description restates what the delegated agent already documents — the skill's description is the *trigger*.
   - Non-English prose anywhere.
   - Instructions that weaken an existing invariant ("skip Allure check", "allow `Thread.sleep`", "permit `@ParameterizedTest`").
   - A skill that writes without `y` when the underlying agent normally asks.

7. **Preview.** Emit the full SKILL.md inside a fenced block + the three registry diffs (copilot-instructions.md, docs/README.md, docs/jetbrains-cheatsheet.md).

8. **Confirm.**

   > Create `/<name>` at `.github/skills/<name>/SKILL.md` and register it in copilot-instructions + docs? (y / n / edit)

   `y` → write SKILL.md, append registry lines, print resulting paths. `n` → stop, nothing written. `edit` → collect delta, re-preview, re-ask.

## Generated SKILL.md shape

Skills follow a fixed lean format. Section order is non-negotiable; sections that don't apply are omitted, never reordered.

````markdown
---
name: <name>
description: <one-line trigger with "Use when ...">
allowed-tools: <shell | edit | omit>
---

# /<name>

<one-paragraph summary>.

## Usage

```
/<name> <args>
```

## Arguments

- `<flag>` — purpose. <required | optional>.

## Behavior

1. <step>
2. <step>

## DO / DON'T

- DO: <imperative>.
- DON'T: <imperative>.

## Refuses

- <thing refused> — <reason>.

## Related

- <path> — <why>.
````

Agent-delegating skills open Behavior step 1 with `` Delegate to the `<agent>` agent (see `.github/agents/<agent>.agent.md`). ``

## Lean-style rules (apply when generating)

- One paragraph under H1 — no preamble, no "this is a skill that...".
- DO/DON'T as imperative bullets, not paragraphs.
- No restatement of `.github/copilot-instructions.md` invariants — refer to them instead.
- Refusals: each one short, naming the thing AND the reason.
- No emoji. English only.
- Skip the section entirely when it's empty — don't write `## Arguments\n\n(none)`.
- Generated file ≤ ~120 lines. Anything longer is usually duplication of agent documentation.

## Registry updates

After writing the SKILL.md, append one registry line to each of:

- `.github/copilot-instructions.md` — under **Available skills (slash commands)**, alphabetical order.
- `docs/README.md` — under **Skills (slash commands)** bullets.
- `docs/jetbrains-cheatsheet.md` — new section `## /<name>` with a one-paragraph copy-paste prompt for IntelliJ users.

If any of those three lacks the expected section, surface to the user and ask where to register — never silently skip.

## DO / DON'T

- DO: enforce the lean format on every generated skill.
- DO: register the skill in all three docs in the same run.
- DO: refuse on name collision rather than overwrite.
- DON'T: write the SKILL.md without explicit `y`.
- DON'T: register a skill in the docs without writing its SKILL.md in the same run.
- DON'T: write production / test code from this command — only skill files and registry entries.

## Refuses

- Name collision with an existing `.github/skills/<name>/`.
- Name not kebab-case or outside length 3–32.
- `--delegates-to=<agent>` names a non-existent agent file.
- Description vague, non-English, or restating the delegated agent's documentation.
- Request to make the skill bypass user approval the underlying agent normally asks for.
- Request to weaken an existing invariant.
- Knowledge-base writes (lessons-learned, patterns, pitfalls) — out of scope; use `/add-lesson-learned`.

## Related

- `.github/copilot-instructions.md` — standing rules + skill registry.
- `docs/README.md` — user-facing skill inventory.
- `docs/jetbrains-cheatsheet.md` — IntelliJ copy-paste prompts.
- `.github/agents/*.agent.md` — agents a skill may delegate to.
- `.github/skills/add-lesson-learned/SKILL.md` — example self-contained skill.
- `.github/skills/migrate/SKILL.md` — example delegating skill.
