# Scenario Outline port plan — <feature-file>:<scenario-name>

Date: <YYYY-MM-DD>
Feature: <path/to/file.feature>
Scenario Outline: "<exact outline name>"

This plan must be approved by the user **before** any code is written. For each example row, the conductor proposes a disposition and a rationale; the user confirms per-row or overall.

## Example rows

| # | Inputs (from `<placeholder>` columns) | Expected (from assertion columns) | Disposition | Rationale | Risk |
|---|---------------------------------------|------------------------------------|-------------|-----------|------|
| 1 |                                       |                                    |             |           |      |
| 2 |                                       |                                    |             |           |      |

### Disposition values

- `merge` — row lives inside one `@Test` method, invoked through a private helper. First failure short-circuits the rest; acceptable when rows share a single business behavior.
- `split` — row becomes its own `@Test` method with an `@DisplayName` that captures the row's intent. Use when rows represent distinct behaviors or when independent failure reporting matters.
- `drop` — row is removed. Use only with explicit rationale (duplicate, obsolete, unstable, covered by another test).

## Overall recommendation

<one paragraph, English, stating the recommended grouping and why>

## Approval

- Requested on: <YYYY-MM-DD HH:MM>
- User decision: <per-row list or overall statement>
- Notes: <free text or N/A>

## Resulting targets

- `@Test` methods produced: <count>
- Helper methods produced: <count>
- Rows dropped: <count, with pointers to the rationale above>
