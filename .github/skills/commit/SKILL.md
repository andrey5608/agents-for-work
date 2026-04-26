---
name: commit
description: Generate a concise English commit message from the staged diff (subject ≤72 chars, imperative, no trailing period, optional body only when the subject cannot carry the intent) and run `git commit` via HEREDOC after explicit user approval. Refuses `--no-verify` and `--amend`. Use when the user asks to commit staged changes or runs /commit.
allowed-tools: shell
---

# /commit

Generate a lean English commit message from staged changes and run `git commit` after the user approves. Nothing else — no branching, no pushing, no amending.

## Usage

```
/commit
/commit --include-unstaged
/commit --message="<exact subject line>"
```

## Arguments

- `--include-unstaged` — optional. Run `git add -u` first, including modifications to already-tracked files. Never stages untracked files.
- `--message="..."` — optional. Skip generation, use the provided subject verbatim. Still blocks for approval.

## Behavior

1. **Read the diff.**
   - `git status --short`
   - `git diff --cached --stat`
   - `git diff --cached`
   - On `--include-unstaged`: `git add -u` first, then re-read.
   - Empty cached diff → stop with `nothing to commit — stage files first`. Don't create empty commits.

2. **Read recent history for style.** `git log -n 10 --pretty=format:"%s"`. Match imperative mood, casing, scope/ticket prefixes.

3. **Generate the message.**
   - **Subject**: imperative, ≤72 chars, no trailing period. States the *why* / outcome, not a file list. English only.
   - **Body**: only when the subject cannot carry the intent (non-obvious motivation, known follow-up, breaking-change note). Wrap at ~72 cols. Skip for small single-purpose changes.
   - **No co-author trailer by default.** Add only on explicit request.
   - **No emoji.**

4. **Preview.** Show the exact message (delimited so trailing whitespace is visible) plus the file list.

5. **Confirm.**

   > Commit with this message? (y / n / edit)

   `y` → commit (next step). `n` → stop, staging untouched. `edit` → ask for replacement, re-preview, re-ask.

6. **Run.** Pass via HEREDOC:

   ```bash
   git commit -m "$(cat <<'EOF'
   <subject>

   <optional body>
   EOF
   )"
   ```

   On hook failure: report verbatim, stop. The user fixes and reruns. After success: `git status --short` + `git rev-parse --short HEAD`.

## DO / DON'T

- DO: use HEREDOC to avoid quoting issues.
- DO: stop on hook failure — let the user fix and rerun.
- DON'T: pass `--no-verify`, `--no-gpg-sign`, or `--amend`.
- DON'T: create commits the user hasn't approved.
- DON'T: stage untracked files implicitly.
- DON'T: modify files to "fix" a hook failure without the user.

## Refuses

- Empty index.
- Untracked files visible in `git status` when `--include-unstaged` was not passed — surface them, ask whether to stage. No auto-staging.
- Secrets in the diff (`.env` contents, AWS keys, private keys, base64 blobs near `token`/`secret`/`password`) — stop, warn, only proceed on explicit confirmation.
- `--no-verify` requested — unconditional refusal.
- Force-push / amend / rebase — out of scope; tell the user to use git directly.

## Related

- `.github/copilot-instructions.md` — global hard rules (English output included).
- `.github/skills/migrate/SKILL.md`, `migrate-auto/SKILL.md` — produce the journal entries `/commit` often describes.
