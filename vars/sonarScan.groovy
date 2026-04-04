def call(Map cfg) {
    withSonarQubeEnv(cfg.sonarServer ?: 'jcloudcodes-sonarqube') {
        def scannerHome = tool 'jcloudcodes-sonarqube-scanner'

        if (cfg.projectType == 'java-maven') {
            sh """
              set -euxo pipefail
              ls -la target
              ls -la target/classes

              ${scannerHome}/bin/sonar-scanner \
                -Dsonar.projectKey=${cfg.sonarProjectKey} \
                -Dsonar.projectName=${cfg.sonarProjectName} \
                -Dsonar.sources=src/main/java \
                -Dsonar.java.binaries=target/classes
            """
        } else if (cfg.projectType == 'django') {
            sh """
              set -euxo pipefail
              ${scannerHome}/bin/sonar-scanner \
                -Dsonar.projectKey=${cfg.sonarProjectKey} \
                -Dsonar.projectName=${cfg.sonarProjectName} \
                -Dsonar.sources=. \
                -Dsonar.python.version=${cfg.pythonVersion ?: '3.12'} \
                -Dsonar.exclusions=**/migrations/**,**/static/**,**/staticfiles/**,**/media/**,**/.venv/**,**/venv/**,**/__pycache__/**
            """
        } else {
            error("Unsupported projectType for sonar scan: ${cfg.projectType}")
        }
    }
}
