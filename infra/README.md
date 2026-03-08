# Docker + Jenkins + Selenium Grid Setup

This setup runs your framework in containers and adds **YAML-driven Job DSL seed generation** so jobs can be changed at any time by editing `.yml` files.

## Architecture

GitHub/GitLab -> Jenkins (Docker) -> Seed DSL -> YAML Job Definitions -> Selenium Grid -> Test Runner Container -> Parallel Cucumber -> Reports

## Jenkins Port

A local Jenkins already uses `8080`, so this Dockerized Jenkins is mapped to host port **`8090`** by default.

- Host URL: `http://localhost:8090`
- Container HTTP port: `8080`
- Override via `JENKINS_HOST_PORT` and `JENKINS_HTTP_PORT`

## Sensitive Config (No Build Params)

Seed job no longer asks repo/credentials in **Build with Parameters**.

Set these in `infra/.env`:

- `SEED_REPO_URL`
- `SEED_REPO_BRANCH`
- `SEED_CREDENTIALS_ID` (optional for public repo)

Credentials secret stays in Jenkins Credentials store, not in job parameters.

## Core Files

- `infra/docker-compose.grid.yml`: Selenium Hub + scalable Chrome nodes + test-runner service.
- `infra/docker-compose.jenkins.yml`: Jenkins container with Docker CLI/Compose and non-8080 host mapping.
- `infra/jenkins/casc/jenkins.yaml`: JCasC baseline + auto-load seed job.
- `infra/jenkins/casc/jobs/seed-job.groovy`: Seed job definition (uses env, no sensitive params).
- `infra/jenkins/job-dsl/jobs_from_yaml.groovy`: Reads YAML and generates pipeline jobs.
- `infra/jenkins/jobs/*.yml`: Per-job runtime defaults (`browser`, `suiteFile`, tags, execution mode, grid URL, node scale).
- `infra/jenkins/pipelines/cucumber-job-template.groovy`: Shared generated job pipeline logic.

## Start Jenkins (Docker)

```bash
# Optional for Docker Desktop on Windows:
# set DOCKER_SOCK_PATH=//var/run/docker.sock

docker compose -f infra/docker-compose.jenkins.yml --env-file infra/.env up -d --build
```

Default login (override via env vars):

- username: `admin`
- password: `admin123`

## One-Time Credentials Setup in Jenkins (for private repo)

1. Manage Jenkins -> Credentials -> System -> Global credentials -> Add Credentials
2. Kind: `Username with password`
3. Username: your Git username
4. Password: GitHub/GitLab token
5. ID: value matching `SEED_CREDENTIALS_ID` in `infra/.env` (example: `github-pat`)

For public repos, keep `SEED_CREDENTIALS_ID` empty.

## Seed Workflow

1. Open Jenkins: `http://localhost:8090`
2. Run job: `seed-jobs`
3. Job reads repo/branch/credentials from env and generates jobs from `infra/jenkins/jobs/*.yml`

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
