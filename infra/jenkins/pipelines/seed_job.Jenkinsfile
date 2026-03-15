pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    parameters {
        string(name: 'JOB_CONFIG_GLOB', defaultValue: 'infra/jenkins/jobs/*.yml', description: 'YAML files that define Jenkins jobs.')
        string(name: 'DSL_SCRIPT', defaultValue: 'infra/jenkins/job-dsl/jobs_from_yaml.groovy', description: 'Job DSL script path in repository.')
        string(name: 'PIPELINE_TEMPLATE', defaultValue: 'infra/jenkins/pipelines/cucumber-job-template.groovy', description: 'Pipeline template path in repository.')
    }

    stages {
        stage('Checkout') {
            steps {
                deleteDir()
                checkout scm
            }
        }

        stage('Run Job DSL') {
            steps {
                script {
                    if (!scm?.userRemoteConfigs || scm.userRemoteConfigs.isEmpty()) {
                        error('SCM configuration is missing on seed-jobs. Configure this job as Pipeline script from SCM.')
                    }

                    String repoUrl = scm.userRemoteConfigs[0].url ?: ''
                    String credentialsId = scm.userRemoteConfigs[0].credentialsId ?: ''
                    String repoBranch = 'master'

                    if (scm.branches && !scm.branches.isEmpty() && scm.branches[0]?.name) {
                        repoBranch = scm.branches[0].name.replaceFirst('^[*/]+', '')
                    }

                    jobDsl(
                        targets: params.DSL_SCRIPT,
                        removedJobAction: 'DELETE',
                        removedViewAction: 'DELETE',
                        lookupStrategy: 'JENKINS_ROOT',
                        additionalParameters: [
                            repoUrl: repoUrl,
                            repoBranch: repoBranch,
                            credentialsId: credentialsId,
                            jobConfigGlob: params.JOB_CONFIG_GLOB,
                            pipelineTemplatePath: params.PIPELINE_TEMPLATE,
                            workspacePath: pwd()
                        ]
                    )
                }
            }
        }
    }
}
