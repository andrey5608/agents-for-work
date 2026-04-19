---
applyTo: "**/*.kt"
---

# Kotlin coding rules

## Idioms

- `val` by default; `var` only when reassignment is required.
- Null-safety: prefer non-nullable types; use `?.`, `?:`, `let`, `requireNotNull`, `checkNotNull`.
- Avoid leaking Java platform types. When calling Java APIs from Kotlin, annotate or narrow explicitly.
- Prefer `data class` for DTOs, `sealed class` / `sealed interface` for closed hierarchies, `object` for singletons, `enum class` for finite sets.
- Prefer Kotlin collection APIs (`map`, `filter`, `associateBy`, `groupBy`, `partition`, `sumOf`) over manual loops.
- Prefer extension functions over utility static methods.
- Use scope functions deliberately: `apply` for configuration, `also` for side effects, `let` for nullable chaining, `run` for block value, `with` for receiver grouping. Do not nest them more than two deep.

## Structure

- Top-level functions and extensions are fine for test helpers; keep them in the same package as the test.
- Do not introduce companion object singletons just to hold mutable state — use proper DI or test fixtures.
- Package names mirror the directory structure and are lowercase.

## Exceptions

- Do not catch `Throwable` / `Exception` broadly in test code. Let failures surface so JUnit reports them accurately.
- Use `runCatching { … }` only when you intentionally want to inspect both success and failure paths.

## Formatting

- Delegated to the project `.editorconfig` and Kotlin defaults (4-space indentation unless `.editorconfig` states otherwise).
- One top-level declaration per logical unit; class files mirror class names.

## Nullability in signatures

- Public API surfaces exposed to tests: explicit types, explicit nullability. No `!!` in production-like code; in test helpers only when a fixture guarantee makes it truly unreachable and a failure there would be a bug worth surfacing.
