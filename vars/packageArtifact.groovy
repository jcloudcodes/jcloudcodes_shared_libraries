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
    } else if (cfg.projectType in ['java-maven', 'java-gradle']) {
        sh """
          set -euxo pipefail
          ls -lh target || true
          ls -lh build/libs || true
        """
    } else {
        error("Unsupported projectType for packaging: ${cfg.projectType}")
    }
}
