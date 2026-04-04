def call(Map cfg) {
    withSonarQubeEnv(cfg.sonarServer ?: 'jcloudcodes-sonarqube') {
        if (cfg.projectType == 'java-maven') {
            sh """
              set -euxo pipefail
              ls -la target || true
              ls -la target/classes || true

              ${tool 'jcloudcodes-sonarqube-scanner'}/bin/sonar-scanner \
                -Dsonar.projectKey=${cfg.sonarProjectKey} \
                -Dsonar.projectName=${cfg.sonarProjectName} \
                -Dsonar.sources=src/main/java \
                -Dsonar.tests=src/test/java \
                -Dsonar.java.binaries=target/classes
            """
        } else if (cfg.projectType == 'django') {
            sh """
              set -euxo pipefail
              ${tool 'jcloudcodes-sonarqube-scanner'}/bin/sonar-scanner \
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
