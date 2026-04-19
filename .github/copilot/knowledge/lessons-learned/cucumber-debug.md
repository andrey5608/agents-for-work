# Lessons learned — Cucumber debugging

Append-only catalog of Cucumber failure signatures and their fixes. `/debug-cucumber` reads this file during every diagnosis and cites any matched lesson. New entries are written only after the user answers `y` to the end-of-run prompt.

## Entry format

```markdown
## <YYYY-MM-DD> <short title>
Context: <what the scenario was trying to verify>
Symptom: <what the failure looked like — exception type, message pattern, stack top>
Root cause: <the actual cause once understood>
Fix: <one or two sentences; the change that resolved it>
Prevention: <how to avoid reintroducing this, preferably a review-time check>
Applies to: cucumber-debug
Source: <journal link / PR link / chat reference>
```

## Curator rotation

Once per sprint, compact this file per `docs/self-learning.md` — promote recurring patterns into `.github/instructions/cucumber.instructions.md` and retire lessons whose rule has been absorbed there.

---

<!-- Entries go below; newest at the bottom. The file starts empty by design. -->
