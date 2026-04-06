def call(Map cfg) {
    if (cfg.projectType == 'django') {
        env.PACKAGED_ARTIFACT = "dist/${env.ARTIFACT_NAME}"

        sh """
          set -euxo pipefail
          rm -rf dist
          mkdir -p dist

          zip -r "${env.PACKAGED_ARTIFACT}" . \
            -x ".git/*" ".venv/*" "venv/*" "__pycache__/*" "*.pyc" \
               "staticfiles/*" "media/*" "*.log" ".DS_Store" ".idea/*" ".vscode/*"

          ls -lh dist
        """

    } else if (cfg.projectType == 'java-maven') {
        env.PACKAGED_ARTIFACT = sh(
            script: "ls -1 target/*.war 2>/dev/null | head -1 || ls -1 target/*.jar 2>/dev/null | head -1",
            returnStdout: true
        ).trim()

        if (!env.PACKAGED_ARTIFACT) {
            error("No Maven artifact found in target/")
        }

        sh """
          set -euxo pipefail
          ls -lh "${env.PACKAGED_ARTIFACT}"
        """

    } else if (cfg.projectType == 'java-gradle') {
        env.PACKAGED_ARTIFACT = sh(
            script: "ls -1 build/libs/*.jar 2>/dev/null | head -1 || ls -1 build/libs/*.war 2>/dev/null | head -1",
            returnStdout: true
        ).trim()

        if (!env.PACKAGED_ARTIFACT) {
            error("No Gradle artifact found in build/libs/")
        }

        sh """
          set -euxo pipefail
          ls -lh "${env.PACKAGED_ARTIFACT}"
        """

    } else {
        error("Unsupported projectType for packaging: ${cfg.projectType}")
    }

    echo "PACKAGED_ARTIFACT=${env.PACKAGED_ARTIFACT}"
}
