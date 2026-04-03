def call() {
    def cfg = [:]

    pipeline {
        agent none

        options {
            timestamps()
            disableConcurrentBuilds()
            timeout(time: 45, unit: 'MINUTES')
            skipDefaultCheckout(true)
            buildDiscarder(logRotator(
                daysToKeepStr: '2',
                numToKeepStr: '2',
                artifactDaysToKeepStr: '2',
                artifactNumToKeepStr: '2'
            ))
        }

        parameters {
            choice(name: 'DEPLOY_ENV', choices: ['lab2', 'qa', 'prod'], description: 'Target environment')
            booleanParam(name: 'PUSH_ARTIFACT', defaultValue: true, description: 'Upload build artifact to Nexus')
            booleanParam(name: 'PUSH_DOCKER', defaultValue: true, description: 'Build and push Docker image')
            booleanParam(name: 'GITOPS_DEPLOY', defaultValue: true, description: 'Trigger GitOps deploy')
            booleanParam(name: 'ARGO_WAIT', defaultValue: true, description: 'Wait for ArgoCD sync/health')
        }

        stages {
            stage('Checkout') {
                agent { label 'jslave-inbound' }
                steps {
                    commonCheckout()
                }
            }

            stage('Load Config') {
                agent { label 'jslave-inbound' }
                steps {
                    script {
                        cfg = loadProjectConfig('ci/project.yaml')
                    }
                }
            }

            stage('Init') {
                agent { label "${cfg.agentLabel ?: 'jslave-inbound'}" }
                steps {
                    script {
                        commonInit(cfg)
                    }
                }
            }

            stage('Build + Test') {
                agent { label "${cfg.agentLabel ?: 'jslave-inbound'}" }
                steps {
                    script {
                        if (cfg.projectType == 'django') {
                            djangoCi(cfg)
                        } else if (cfg.projectType == 'java-maven') {
                            javaMavenCi(cfg)
                        } else if (cfg.projectType == 'java-gradle') {
                            javaGradleCi(cfg)
                        } else {
                            error("Unsupported projectType: ${cfg.projectType}")
                        }
                    }
                }
            }

            stage('SonarQube Scan') {
                agent { label "${cfg.agentLabel ?: 'jslave-inbound'}" }
                steps {
                    script {
                        sonarScan(cfg)
                    }
                }
            }

            stage('Package Artifact') {
                agent { label "${cfg.agentLabel ?: 'jslave-inbound'}" }
                steps {
                    script {
                        packageArtifact(cfg)
                    }
                }
            }
        }

        post {
            always {
                script {
                    if (cfg) {
                        commonPost(cfg)
                    }
                }
            }
        }
    }
}
