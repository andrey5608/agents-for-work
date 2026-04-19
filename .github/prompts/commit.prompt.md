---
mode: 'agent'
description: 'Generate a concise English commit message from the staged diff and run git commit after user approval.'
tools: ['codebase', 'terminal']
---

# /commit

Generate a lean English commit message from the current staged changes and run `git commit` after the user approves the message. Does nothing else — no branching, no pushing, no amending.

## Usage

```
/commit
/commit --include-unstaged
/commit --message="<exact subject line>"
```

## Arguments

- `--include-unstaged` — optional. Before generating, run `git add -u` so modifications to already-tracked files are included. Never stages untracked files (avoids accidentally committing `.env`, credentials, build output). If there are untracked files the user wants in, they stage them manually first.
- `--message="..."` — optional. Skip generation entirely; use the provided subject line verbatim. Still blocks for approval before running `git commit`.

## Process

1. **Read the diff.**
   - `git status --short`
   - `git diff --cached --stat`
   - `git diff --cached` (full patch)
   - On `--include-unstaged`: run `git add -u` first, then re-run the reads.
   - If `git diff --cached` is empty: stop with `nothing to commit — stage files first`. Do not create an empty commit.

2. **Read recent history for style.** `git log -n 10 --pretty=format:"%s"`. Match the prevailing subject style (imperative mood, casing, scope prefixes if present, ticket tags if present).

3. **Generate the message.**
   - **Subject line**: imperative, ≤72 characters, no trailing period. States the *why* or the outcome, not a file list. English only.
   - **Body (optional)**: include only when the subject cannot carry the intent alone — for example, non-obvious motivation, a known follow-up, a breaking change note. Wrap at ~72 columns. Skip the body entirely for small single-purpose changes.
   - **No co-author trailer by default.** Add one only when the user explicitly asks.
   - **No emoji.**

4. **Preview.** Show the exact message that will be used, delimited so the user can see trailing/leading whitespace. Also show the list of files that will be committed.

5. **Confirm.** Ask exactly once:

   > Commit with this message? (y / n / edit)

   - `y` → run `git commit` (step 6).
   - `n` → stop without committing. Leave the staging area untouched.
   - `edit` → ask for a replacement subject (and body if needed), re-preview, re-ask.

6. **Run the commit.** Use a HEREDOC to pass the message to avoid quoting issues:

   ```bash
   git commit -m "$(cat <<'EOF'
   <subject>

   <optional body>
   EOF
   )"
   ```

   - Never pass `--no-verify`, `--no-gpg-sign`, or `--amend`. These flags are unconditionally refused — see the Refusals section. If a pre-commit hook fails, report the failure verbatim and stop — the user fixes the issue and reruns `/commit`.
   - After a successful commit, run `git status --short` and print the new commit's short hash (`git rev-parse --short HEAD`).

## Refusals

- **Empty index.** Refuse to create an empty commit.
- **Untracked files the user may have intended to include.** When `git status --short` shows `??` entries and `--include-unstaged` was not passed, surface them and ask whether any should be staged before proceeding. Do not auto-stage untracked files.
- **Secrets in the diff.** If the cached diff contains literals matching an obvious secret signature (`.env` contents, AWS keys, private keys, long base64 blobs next to the word `token`/`secret`/`password`), stop and warn. Only proceed after the user explicitly confirms.
- **`--no-verify` requested.** Refuse unconditionally. Do not proceed even if the user provides a justification.
- **Force-push / amend / rebase requests.** Out of scope for this command. Tell the user to use git directly.

## Invariants restated

- English output only — subject and body both.
- Never create a commit the user has not explicitly approved.
- Never modify files to "fix" a hook failure without the user's involvement.
- Never stage untracked files implicitly.

## Related files

- `.github/copilot-instructions.md` — global hard rules (including English output).
- `.github/prompts/migrate.prompt.md`, `.github/prompts/migrate-auto.prompt.md` — produce the journal entries that `/commit` often has to describe.
