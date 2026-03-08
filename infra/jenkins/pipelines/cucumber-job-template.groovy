pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    parameters {
        choice(name: 'BROWSER', choices: ['__DEFAULT_BROWSER__', 'chrome', 'headless-chrome', 'firefox', 'headless-firefox', 'edge'].unique(), description: 'Mandatory browser for framework execution.')
        string(name: 'CUCUMBER_TAGS', defaultValue: '__DEFAULT_TAGS__', description: 'Tag expression for Cucumber filter.')
        string(name: 'SUITE_FILE', defaultValue: '__DEFAULT_SUITE_FILE__', description: 'Suite file (e.g., testng.xml or suites/testng.xml).')
        choice(name: 'EXECUTION_MODE', choices: ['__DEFAULT_EXECUTION__', 'remote', 'local'].unique(), description: 'Execution mode.')
        string(name: 'GRID_URL', defaultValue: '__DEFAULT_GRID_URL__', description: 'Selenium Grid URL reachable from test container.')
        string(name: 'CHROME_NODES', defaultValue: '__DEFAULT_CHROME_NODES__', description: 'Number of chrome node containers to scale.')
    }

    environment {
        GRID_COMPOSE_FILE = 'infra/docker-compose.grid.yml'
        TEST_RUNNER_CONTAINER = ''
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
                    if (!(params.CHROME_NODES ==~ /^[1-9]\\d*$/)) {
                        error("CHROME_NODES must be a positive integer. Current value: ${params.CHROME_NODES}")
                    }
                    env.TEST_RUNNER_CONTAINER = ("test-runner-${env.JOB_BASE_NAME}-${env.BUILD_NUMBER}").replaceAll('[^A-Za-z0-9_.-]', '-')
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
                if [ -n "${TEST_RUNNER_CONTAINER}" ]; then
                    docker cp ${TEST_RUNNER_CONTAINER}:/workspace/build ./build
                    docker rm -f ${TEST_RUNNER_CONTAINER} >/dev/null 2>&1
                fi
                docker compose -f ${GRID_COMPOSE_FILE} down --remove-orphans
                exit 0
            '''

            archiveArtifacts artifacts: 'build/cucumber-reports/**,build/first-run-report/**,build/reports/tests/**,build/**/*.json,build/rerun/**', allowEmptyArchive: true
            junit testResults: 'build/test-results/**/*.xml', allowEmptyResults: true
        }
    }
}
