---
applyTo: "**"
---

# `.editorconfig` compliance

Before writing or editing any file, the agent must resolve and obey the nearest `.editorconfig`.

## Resolution

1. Start from the directory of the target file.
2. Walk up toward the repository root, collecting rules (`[pattern]` sections) that match the target path.
3. Inner-most rules override outer-most. `root = true` stops the upward walk.

## Mandatory properties to honor

| Property | Behavior |
|----------|----------|
| `indent_style` | `space` → use spaces only; `tab` → use tabs only. Never mix. |
| `indent_size` / `tab_width` | Exact width; apply consistently across the whole file edit. |
| `charset` | `utf-8` is expected; emit `utf-8` without BOM unless `charset = utf-8-bom`. |
| `end_of_line` | `lf` → `\n`; `crlf` → `\r\n`. No mixed line endings. |
| `trim_trailing_whitespace` | `true` → remove trailing whitespace on every edited line. |
| `insert_final_newline` | `true` → file ends with exactly one newline. |
| `max_line_length` | Respect as a soft cap when choosing line breaks. |

## Self-validation before emitting

The agent performs a mental/manual diff against the resolved properties. If the output conflicts (for example, a tab snuck in under `indent_style = space`), the output is rewritten before emission. No edit is emitted with known violations.

## Verifier gate

`migrate-verifier` runs `editorconfig-checker` when present on `PATH`. When absent, it re-reads the properties and performs rule matching on the changed hunks. A violation blocks the migration.
