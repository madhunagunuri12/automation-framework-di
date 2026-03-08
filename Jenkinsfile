pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'headless-chrome', 'firefox', 'headless-firefox', 'edge'], description: 'Mandatory browser for framework execution.')
        string(name: 'CUCUMBER_TAGS', defaultValue: '@FullRun', description: 'Tag expression for Cucumber filter.')
        string(name: 'SUITE_FILE', defaultValue: 'testng.xml', description: 'Suite file (e.g., testng.xml or suites/testng.xml).')
        choice(name: 'EXECUTION_MODE', choices: ['remote'], description: 'Use remote for Grid execution.')
        string(name: 'GRID_URL', defaultValue: 'http://selenium-hub:4444/wd/hub', description: 'Selenium Grid URL reachable from test container.')
        string(name: 'CHROME_NODES', defaultValue: '4', description: 'Number of chrome node containers to scale.')
    }

    environment {
        GRID_COMPOSE_FILE = 'infra/docker-compose.grid.yml'
        TEST_RUNNER_CONTAINER = "test-runner-${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
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
                    if (!(params.CHROME_NODES ==~ /^[1-9]\\d*$/)) {
                        error("CHROME_NODES must be a positive integer. Current value: ${params.CHROME_NODES}")
                    }
                }
            }
        }

        stage('Start Selenium Grid') {
            steps {
                sh '''
                    set -e
                    docker compose -f ${GRID_COMPOSE_FILE} down --remove-orphans || true
                    docker compose -f ${GRID_COMPOSE_FILE} up -d --scale chrome=${CHROME_NODES}
                '''
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
                    int statusCode = sh(
                        script: """
                            set +e
                            docker compose -f ${env.GRID_COMPOSE_FILE} --profile runner run --build --name ${env.TEST_RUNNER_CONTAINER} \\
                                -e BROWSER=\"${params.BROWSER}\" \\
                                -e CUCUMBER_TAGS=\"${params.CUCUMBER_TAGS}\" \\
                                -e SUITE_FILE=\"${params.SUITE_FILE}\" \\
                                -e EXECUTION_MODE=\"${params.EXECUTION_MODE}\" \\
                                -e GRID_URL=\"${params.GRID_URL}\" \\
                                test-runner
                            exit \$?
                        """,
                        returnStatus: true
                    )

                    if (statusCode != 0) {
                        error("Test execution failed with exit code ${statusCode}")
                    }
                }
            }
        }
    }

    post {
        always {
            sh '''
                set +e
                docker cp ${TEST_RUNNER_CONTAINER}:/workspace/build ./build
                docker rm -f ${TEST_RUNNER_CONTAINER} >/dev/null 2>&1
                docker compose -f ${GRID_COMPOSE_FILE} down --remove-orphans
                exit 0
            '''

            archiveArtifacts artifacts: 'build/cucumber-reports/**,build/first-run-report/**,build/reports/tests/**,build/**/*.json,build/rerun/**', allowEmptyArchive: true
            junit testResults: 'build/test-results/**/*.xml', allowEmptyResults: true
        }
    }
}
