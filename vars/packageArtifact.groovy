def call(Map cfg) {
    if (cfg.projectType == 'django') {
        sh """
          set -euxo pipefail
          rm -rf dist
          mkdir -p dist

          zip -r "dist/${env.ARTIFACT_NAME}" . \
            -x ".git/*" ".venv/*" "venv/*" "__pycache__/*" "*.pyc" \
               "staticfiles/*" "media/*" "*.log" ".DS_Store" ".idea/*" ".vscode/*"

          ls -lh dist
        """
    } else if (cfg.projectType == 'java-maven') {
        sh """
          set -euxo pipefail
          ls -lh target
          ls -lh target/*.war || true
          ls -lh target/*.jar || true
        """
    } else if (cfg.projectType == 'java-gradle') {
        sh """
          set -euxo pipefail
          ls -lh build/libs
          ls -lh build/libs/*.jar || true
          ls -lh build/libs/*.war || true
        """
    } else {
        error("Unsupported projectType for packaging: ${cfg.projectType}")
    }
}
