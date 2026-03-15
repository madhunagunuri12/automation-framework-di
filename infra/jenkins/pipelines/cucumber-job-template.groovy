pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    parameters {
        choice(name: 'BROWSER', choices: '''__DEFAULT_BROWSER__
chrome
headless-chrome
firefox
headless-firefox
edge''', description: 'Mandatory browser for framework execution.')
        string(name: 'CUCUMBER_TAGS', defaultValue: '__DEFAULT_TAGS__', description: 'Tag expression for Cucumber filter.')
        string(name: 'SUITE_FILE', defaultValue: '__DEFAULT_SUITE_FILE__', description: 'Suite file (e.g., testng.xml or suites/testng.xml).')
        choice(name: 'EXECUTION_MODE', choices: '''__DEFAULT_EXECUTION__
remote
local''', description: 'Execution mode.')
        string(name: 'GRID_URL', defaultValue: '__DEFAULT_GRID_URL__', description: 'Selenium Grid URL reachable from test container.')
        string(name: 'CHROME_NODES', defaultValue: '__DEFAULT_CHROME_NODES__', description: 'Number of chrome node containers to scale.')
    }

    environment {
        GRID_COMPOSE_FILE = 'infra/docker-compose.grid.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                script {
                    if ('__CREDENTIALS_ID__'.trim()) {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: '*/__REPO_BRANCH__']],
                            userRemoteConfigs: [[url: '__REPO_URL__', credentialsId: '__CREDENTIALS_ID__']]
                        ])
                    } else {
                        git branch: '__REPO_BRANCH__', url: '__REPO_URL__'
                    }
                }
            }
        }

        stage('Validate Parameters') {
            steps {
                script {
                    if (!params.BROWSER?.trim()) {
                        error('BROWSER is mandatory.')
                    }
                    if (!params.SUITE_FILE?.trim()) {
                        error('SUITE_FILE is mandatory and cannot be blank.')
                    }

                    int chromeNodeCount
                    try {
                        chromeNodeCount = Integer.parseInt(params.CHROME_NODES?.trim())
                    } catch (Exception ignored) {
                        error("CHROME_NODES must be a positive integer. Current value: ${params.CHROME_NODES}")
                    }

                    if (chromeNodeCount <= 0) {
                        error("CHROME_NODES must be a positive integer. Current value: ${params.CHROME_NODES}")
                    }
                }
            }
        }

        stage('Start Selenium Grid') {
            steps {
                sh """
                    set -e
                    docker compose -f ${GRID_COMPOSE_FILE} down --remove-orphans || true
                    docker compose -f ${GRID_COMPOSE_FILE} up -d --scale chrome=${params.CHROME_NODES}
                """
                script {
                    timeout(time: 3, unit: 'MINUTES') {
                        waitUntil {
                            def health = sh(
                                script: "docker inspect --format='{{if .State.Health}}{{.State.Health.Status}}{{else}}starting{{end}}' selenium-hub",
                                returnStdout: true
                            ).trim()
                            echo "selenium-hub health: ${health}"
                            return health == 'healthy'
                        }
                    }
                }
            }
        }

        stage('Run Tests In Container') {
            steps {
                script {
                    String runnerContainer = ("test-runner-${env.JOB_NAME ?: 'job'}-${env.BUILD_NUMBER ?: '0'}")
                            .replaceAll('[^A-Za-z0-9_.-]', '-')

                    sh 'mkdir -p build'

                    int statusCode = sh(
                        script: """
                            bash -lc 'set -o pipefail
                            docker compose -f ${env.GRID_COMPOSE_FILE} --profile runner run --build --name ${runnerContainer} \\
                                -e BROWSER=\"${params.BROWSER}\" \\
                                -e CUCUMBER_TAGS=\"${params.CUCUMBER_TAGS}\" \\
                                -e SUITE_FILE=\"${params.SUITE_FILE}\" \\
                                -e EXECUTION_MODE=\"${params.EXECUTION_MODE}\" \\
                                -e GRID_URL=\"${params.GRID_URL}\" \\
                                test-runner \\
                                bash -lc "./gradlew clean cucumberTest --no-daemon -Dbrowser=\\\"${params.BROWSER}\\\" -Dcucumber.filter.tags=\\\"${params.CUCUMBER_TAGS}\\\" -DsuiteFile=\\\"${params.SUITE_FILE}\\\" -Dexecution=\\\"${params.EXECUTION_MODE}\\\" -Dgrid.url=\\\"${params.GRID_URL}\\\"" 2>&1 | tee build/test-runner-console.log'
                        """,
                        returnStatus: true
                    )

                    sh """
                        set +e
                        if docker ps -a --format '{{.Names}}' | grep -Fx '${runnerContainer}' >/dev/null; then
                            docker logs ${runnerContainer} > build/test-runner-docker.log 2>&1 || true
                            docker cp ${runnerContainer}:/workspace/build/. ./build >/dev/null 2>&1 || true
                        fi
                        exit 0
                    """

                    boolean hasArtifacts = fileExists('build/cucumber-reports') ||
                            fileExists('build/first-run-report') ||
                            fileExists('build/reports/tests') ||
                            fileExists('build/cucumber.json') ||
                            fileExists('build/cucumber-rerun.json') ||
                            fileExists('build/test-results')

                    if (statusCode != 0) {
                        error("Test execution failed with exit code ${statusCode}")
                    }

                    if (!hasArtifacts) {
                        error('Test runner completed but no build artifacts were collected. Check build/test-runner-console.log and build/test-runner-docker.log.')
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                String runnerContainer = ("test-runner-${env.JOB_NAME ?: 'job'}-${env.BUILD_NUMBER ?: '0'}")
                        .replaceAll('[^A-Za-z0-9_.-]', '-')

                sh """
                    set +e
                    docker logs ${runnerContainer} > build/test-runner-docker.log 2>&1 || true
                    docker rm -f ${runnerContainer} >/dev/null 2>&1 || true
                    docker compose -f ${GRID_COMPOSE_FILE} down --remove-orphans
                    exit 0
                """
            }

            archiveArtifacts artifacts: 'build/cucumber-reports/**,build/first-run-report/**,build/reports/tests/**,build/**/*.json,build/rerun/**,build/test-runner-console.log,build/test-runner-docker.log', allowEmptyArchive: true
            junit testResults: 'build/test-results/**/*.xml', allowEmptyResults: true
        }
    }
}
