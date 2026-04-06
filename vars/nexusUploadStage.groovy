def call(Map cfg = [:]) {
    if (!env.PACKAGED_ARTIFACT?.trim()) {
        error("PACKAGED_ARTIFACT is empty. Run packageArtifact first.")
    }

    def nexusUrl      = cfg.nexusUrl
    def rawRepo       = cfg.nexusRawRepo ?: cfg.rawRepo
    def credentialsId = cfg.nexusCredId ?: cfg.credentialsId
    def filePath      = env.PACKAGED_ARTIFACT

    if (!nexusUrl || !rawRepo || !credentialsId) {
        error "nexusUploadStage missing required args: nexusUrl, nexusRawRepo/rawRepo, nexusCredId/credentialsId"
    }

    def fileName = filePath.tokenize('/').last()
    def targetPath = "${cfg.appName ?: 'app'}/${env.BRANCH ?: 'main'}/${env.BUILD_NUMBER ?: '0'}"
    def uploadUrl = "${nexusUrl}/repository/${rawRepo}/${targetPath}/${fileName}".replaceAll('/+', '/')
    uploadUrl = uploadUrl.replaceFirst(':/', '://')

    echo "Uploading ${filePath} to ${uploadUrl}"

    withCredentials([usernamePassword(
        credentialsId: credentialsId,
        usernameVariable: 'NEXUS_USER',
        passwordVariable: 'NEXUS_PASS'
    )]) {
        sh """#!/usr/bin/env bash
          set -euxo pipefail
          curl -u "\$NEXUS_USER:\$NEXUS_PASS" --upload-file "${filePath}" "${uploadUrl}"
        """
    }
}
