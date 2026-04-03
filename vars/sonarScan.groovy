def call(Map cfg) {
    withSonarQubeEnv(cfg.sonarServer ?: 'jcloudcodes-sonarqube') {
        sh """
          set -euxo pipefail
          ${tool 'jcloudcodes-sonarqube-scanner'}/bin/sonar-scanner \
            -Dsonar.projectKey=${cfg.sonarProjectKey} \
            -Dsonar.projectName=${cfg.sonarProjectName} \
            -Dsonar.sources=.
        """
    }
}
