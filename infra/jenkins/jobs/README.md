# Job YAML Schema

Each file in this folder creates one Jenkins pipeline job through the SCM-backed `seed-jobs` flow.

## Required

- `jobName`: Unique Jenkins job name
- `gradle.browser`
- `gradle.suiteFile`

## Optional

- `description`
- `disabled` (`true`/`false`)
- `gradle.tags` (default `@FullRun`)
- `gradle.executionMode` (default `remote`)
- `gradle.gridUrl` (default `http://selenium-hub:4444/wd/hub`)
- `gradle.chromeNodes` (default `4`)

After editing YAML, rerun `seed-jobs` to apply changes.
