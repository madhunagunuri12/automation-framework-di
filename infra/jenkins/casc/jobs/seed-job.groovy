pipelineJob('seed-jobs') {
    description('Seeds/updates all Jenkins jobs from YAML definitions using Job DSL.')

    logRotator {
        daysToKeep(14)
        numToKeep(30)
    }

    definition {
        cps {
            sandbox(true)
            script('''
pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    environment {
        REPO_URL = "${SEED_REPO_URL}"
        REPO_BRANCH = "${SEED_REPO_BRANCH}"
        CREDENTIALS_ID = "${SEED_CREDENTIALS_ID}"
        JOB_CONFIG_GLOB = "${SEED_JOB_CONFIG_GLOB}"
        DSL_SCRIPT = "${SEED_DSL_SCRIPT}"
        PIPELINE_TEMPLATE = "${SEED_PIPELINE_TEMPLATE}"
    }

    stages {
        stage('Validate Config') {
            steps {
                script {
                    if (!env.REPO_URL?.trim()) {
                        error('SEED_REPO_URL is missing. Set it in Docker env/.env and restart Jenkins container.')
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                deleteDir()
                script {
                    String branch = env.REPO_BRANCH?.trim() ? env.REPO_BRANCH.trim() : 'master'
                    String creds = env.CREDENTIALS_ID?.trim()

                    if (creds) {
                        checkout([
                            $class: 'GitSCM',
                            branches: [[name: "*/${branch}"]],
                            userRemoteConfigs: [[url: env.REPO_URL, credentialsId: creds]]
                        ])
                    } else {
                        git branch: branch, url: env.REPO_URL
                    }
                }
            }
        }

        stage('Run Job DSL') {
            steps {
                script {
                    String jobConfigGlob = env.JOB_CONFIG_GLOB?.trim() ? env.JOB_CONFIG_GLOB.trim() : 'infra/jenkins/jobs/*.yml'
                    String dslScript = env.DSL_SCRIPT?.trim() ? env.DSL_SCRIPT.trim() : 'infra/jenkins/job-dsl/jobs_from_yaml.groovy'
                    String pipelineTemplate = env.PIPELINE_TEMPLATE?.trim() ? env.PIPELINE_TEMPLATE.trim() : 'infra/jenkins/pipelines/cucumber-job-template.groovy'
                    String branch = env.REPO_BRANCH?.trim() ? env.REPO_BRANCH.trim() : 'master'
                    String creds = env.CREDENTIALS_ID?.trim() ? env.CREDENTIALS_ID.trim() : ''

                    jobDsl(
                        targets: dslScript,
                        removedJobAction: 'DELETE',
                        removedViewAction: 'DELETE',
                        lookupStrategy: 'JENKINS_ROOT',
                        additionalParameters: [
                            repoUrl: env.REPO_URL,
                            repoBranch: branch,
                            credentialsId: creds,
                            jobConfigGlob: jobConfigGlob,
                            pipelineTemplatePath: pipelineTemplate
                        ]
                    )
                }
            }
        }
    }
}
''')
        }
    }
}
