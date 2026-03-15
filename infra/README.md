# Docker + Jenkins + Selenium Grid Setup

This setup runs your framework in containers and uses a single Jenkins-native seed flow:

GitHub/GitLab -> Jenkins `Pipeline script from SCM` seed job -> YAML Job Definitions -> Selenium Grid -> Test Runner Container -> Parallel Cucumber -> Reports

## Jenkins Port

A local Jenkins already uses `8080`, so this Dockerized Jenkins is mapped to host port `8090` by default.

- Host URL: `http://localhost:8090`
- Container HTTP port: `8080`

## Core Files

- `infra/docker-compose.grid.yml`: Selenium Hub + scalable Chrome nodes + test-runner service.
- `infra/docker-compose.jenkins.yml`: Jenkins container with Docker CLI/Compose and non-8080 host mapping.
- `infra/jenkins/casc/jenkins.yaml`: Base Jenkins Configuration as Code only.
- `infra/jenkins/pipelines/seed_job.Jenkinsfile`: Seed pipeline to run from Jenkins SCM config.
- `infra/jenkins/job-dsl/jobs_from_yaml.groovy`: Reads YAML and generates pipeline jobs.
- `infra/jenkins/jobs/*.yml`: Per-job runtime defaults (`browser`, `suiteFile`, tags, execution mode, grid URL, node scale).
- `infra/jenkins/pipelines/cucumber-job-template.groovy`: Shared generated job pipeline logic.

## Start Jenkins (Docker)

```bash
docker compose -f infra/docker-compose.jenkins.yml --env-file infra/.env up -d --build
```

Default login:

- username: `admin`
- password: `admin123`

## Create Seed Job In Jenkins

Create one Jenkins pipeline job manually:

- Job name: `seed-jobs`
- Definition: `Pipeline script from SCM`
- SCM: `Git`
- Repository URL: your repo URL
- Credentials: Jenkins Git credential if repo is private
- Branch: your branch, for example `*/master`
- Script Path: `infra/jenkins/pipelines/seed_job.Jenkinsfile`

This is the only supported seed-job model in the repo.

## What Seed Job Does

The seed pipeline checks out the configured SCM, derives repo URL/branch/credentials from the Jenkins SCM config, and runs Job DSL using:

- `infra/jenkins/job-dsl/jobs_from_yaml.groovy`
- `infra/jenkins/jobs/*.yml`
- `infra/jenkins/pipelines/cucumber-job-template.groovy`

## YAML Job Definition Example

```yaml
jobName: "ui-fullrun-chrome"
description: "UI full regression on Selenium Grid with Chrome"
disabled: false
gradle:
  browser: "chrome"
  suiteFile: "testng.xml"
  tags: "@FullRun"
  executionMode: "remote"
  gridUrl: "http://selenium-hub:4444/wd/hub"
  chromeNodes: "4"
```

Any time you edit/add/remove YAML files, run `seed-jobs` again to reconcile jobs.

## Local Grid Run (without Jenkins)

```bash
docker compose -f infra/docker-compose.grid.yml --env-file infra/.env up -d --scale chrome=4

docker compose -f infra/docker-compose.grid.yml --env-file infra/.env --profile runner run --build --name test-runner-local \
  -e BROWSER=chrome \
  -e CUCUMBER_TAGS=@FullRun \
  -e SUITE_FILE=testng.xml \
  -e EXECUTION_MODE=remote \
  -e GRID_URL=http://selenium-hub:4444/wd/hub \
  test-runner

docker cp test-runner-local:/workspace/build ./build
docker rm -f test-runner-local
docker compose -f infra/docker-compose.grid.yml down --remove-orphans
```
