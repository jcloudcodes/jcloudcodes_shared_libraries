def call() {
    def cfg = [:]

    pipeline {
        agent { label 'jslave-inbound' }

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
            booleanParam(name: 'PUSH_JFROG', defaultValue: false, description: 'Upload artifact/image to JFrog')
            booleanParam(name: 'GITOPS_DEPLOY', defaultValue: true, description: 'Trigger GitOps deploy')
            booleanParam(name: 'ARGO_WAIT', defaultValue: true, description: 'Wait for ArgoCD sync/health')
        }

        stages {
            stage('Checkout') {
                steps {
                    commonCheckout()
                }
            }

            stage('Load Config') {
                steps {
                    script {
                        cfg = loadProjectConfig('ci/project.yaml')
                    }
                }
            }

            stage('Init') {
                steps {
                    script {
                        commonInit(cfg)
                    }
                }
            }

            stage('Build + Test') {
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
                steps {
                    script {
                        sonarScan(cfg)
                    }
                }
            }

            stage('Package Artifact') {
                steps {
                    script {
                        packageArtifact(cfg)
                    }
                }
            }

            stage('Create Docker Image') {
                when { expression { return params.PUSH_DOCKER } }
                steps {
                    script {
                        dockerBuild(cfg)
                    }
                }
            }

            stage('Publish Artifacts and Images') {
                parallel {
                    stage('Push Docker Image to Nexus') {
                        when { expression { return params.PUSH_DOCKER } }
                        steps {
                            script {
                                dockerPush(cfg)
                            }
                        }
                    }

                    stage('Push Docker Image to Docker Hub') {
                        when { expression { return params.PUSH_DOCKER } }
                        steps {
                            script {
                                dockerHubPush(cfg)
                            }
                        }
                    }

                    stage('Upload Artifact to Nexus') {
                        when { expression { return params.PUSH_ARTIFACT } }
                        steps {
                            script {
                                nexusUploadStage(cfg)
                            }
                        }
                    }
                }
            }
        }

        post {
            always {
                script {
                    sh """
                      set +e
                      echo "Docker cleanup on Jenkins agent"

                      echo "Remove local image built by this job if present"
                      docker rmi -f "${env.LOCAL_IMAGE ?: ''}" 2>/dev/null || true

                      echo "Remove app-related images if present"
                      docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" | \
                        awk '/${cfg.imageName ?: ""}/ {print \$2}' | sort -u | \
                        xargs -r docker rmi -f || true

                      echo "Prune dangling images"
                      docker image prune -f || true

                      echo "Prune builder cache"
                      docker builder prune -af || true

                      true
                    """

                    if (cfg) {
                        commonPost(cfg)
                    }
                }
            }
        }
    }
}
