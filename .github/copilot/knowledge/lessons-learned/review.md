# Lessons learned — review

Append-only catalog of lessons harvested from `/review` runs. `/review` reads this file before starting and cites any matched lesson in its findings. New entries are written only after the user answers `y` to the end-of-run prompt.

## Entry format

```markdown
## <YYYY-MM-DD> <short title>
Context: <what was being done>
Observation: <what the review missed or found too late>
Rule: <actionable guideline phrased in imperative English>
Applies to: review
Source: <PR link / chat reference>
```

## Curator rotation

Once per sprint, compact this file per `docs/self-learning.md` — merge duplicates, retire lessons whose rule has been absorbed into `review-rules.instructions.md`.

---

<!-- Entries go below; newest at the bottom. The file starts empty by design. -->
