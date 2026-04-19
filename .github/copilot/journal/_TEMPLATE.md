# Migration: <feature-name> / <scenario-name>

- Date: <YYYY-MM-DD>
- Feature: <path/to/file.feature>
- Scenario: "<exact scenario name>"
- Target test class: <src/test/kotlin/.../NewTest.kt>
- Is Scenario Outline: <yes | no>
- Outline port plan: <path to scenario-outline-port-plan file, or N/A>
- Draft approval: <user statement / `--approved-concept=...` / N/A>

## Concept (draft snapshot)

<copy of the approved draft here>

## Relevant prior lessons

- <bullet list of lessons-learned entries consulted — link each by anchor>

## Allure mapping

| Cucumber source | Annotation on JUnit 5 test |
|-----------------|----------------------------|
|                 |                            |

## Scenario → method

| # | Example row (if any) | JUnit 5 method / helper | Notes |
|---|----------------------|-------------------------|-------|
| 1 |                      |                         |       |

## Deviations from 1:1

- <explicit list of every intentional deviation and why>

## Verifier report

```json
{
  "build_status": "pass|fail",
  "new_test_status": "pass|fail",
  "legacy_test_status": "pass|fail",
  "allure_metadata_ok": true,
  "editorconfig_ok": true,
  "duration_ms": 0,
  "artifacts": [
    "target/surefire-reports/TEST-<class>.xml",
    "target/allure-results/<uuid>-result.json"
  ]
}
```

## Open questions / follow-ups

- <bullet list; leave empty if none>

## Lessons harvested

- → `lessons-learned/migration.md`: <short title, or `none`>
- → `migration-patterns.md`: <short title, or `none`>
- → `migration-pitfalls.md`: <short title, or `none`>
