# Migration: <feature-name> / <scenario-name>

- Date: <YYYY-MM-DD>
- Feature: <path/to/file.feature>
- Scenario: "<exact scenario name>"
- Target test class: <src/test/kotlin/.../NewTest.kt>
- Is Scenario Outline: <yes | no>
- Outline port plan: <path to scenario-outline-port-plan file, or N/A>
- Mode: <interactive | autonomous | autonomous → escalated>
- Draft approval: <user statement / `--approved-concept=...` / `auto-approved` / N/A>
- Auto-approval checklist: <path to filled auto-approval-checklist file, or N/A>
- Retry budget: <N, or N/A for interactive>

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

## Retry log (autonomous only — omit the section if `Mode: interactive`)

| attempt | phase | blockers (summary) | classifications | applied fix | outcome |
|---------|-------|--------------------|-----------------|-------------|---------|
| 0       |       |                    |                 |             |         |

## Verifier report — phase: initial

```json
{
  "source": "migration",
  "phase": "initial",
  "build_status": "pass|fail",
  "new_test_status": "pass|fail",
  "legacy_test_status": "pass|fail",
  "allure_metadata_ok": true,
  "editorconfig_ok": true,
  "antipatterns_ok": true,
  "parity_ok": true,
  "parity_counts": {
    "junit_cases_actual": 0,
    "cucumber_cases_expected": 0,
    "rows_dropped": 0
  },
  "duration_ms": 0,
  "artifacts": [
    "target/surefire-reports/TEST-<class>.xml",
    "target/allure-results/<uuid>-result.json"
  ],
  "blockers": []
}
```

## Cleanup — delete-scenario report

```json
{
  "feature_path": "<path>",
  "scenario_name": "<name>",
  "scenario_type": "plain|outline",
  "lines_removed": 0,
  "tags_removed": [],
  "example_rows_removed": 0,
  "file_left_empty_of_scenarios": false,
  "orphaned_step_definitions_candidates": []
}
```

Notes on orphaned step definitions (user decides whether to delete):

- <bullet list; or `none`>

## Verifier report — phase: post-cleanup

```json
{
  "source": "migration",
  "phase": "post-cleanup",
  "build_status": "pass|fail",
  "new_test_status": "pass|fail",
  "legacy_test_status": "removed|fail",
  "allure_metadata_ok": true,
  "editorconfig_ok": true,
  "antipatterns_ok": true,
  "parity_ok": true,
  "parity_counts": {
    "junit_cases_actual": 0,
    "cucumber_cases_expected": 0,
    "rows_dropped": 0
  },
  "duration_ms": 0,
  "artifacts": [
    "target/surefire-reports/TEST-<class>.xml",
    "target/allure-results/<uuid>-result.json"
  ],
  "blockers": []
}
```

## Open questions / follow-ups

- <bullet list; leave empty if none>

## Lessons harvested

- → `lessons-learned/migration.md`: <short title, or `none`>
- → `migration-patterns.md`: <short title, or `none`>
- → `migration-pitfalls.md`: <short title, or `none`>
