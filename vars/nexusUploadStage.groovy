echo "cfg.appName=${cfg.appName}"
echo "cfg.nexusUrl=${cfg.nexusUrl}"
echo "cfg.nexusRawRepo=${cfg.nexusRawRepo}"
echo "cfg.rawRepo=${cfg.rawRepo}"
echo "env.PACKAGED_ARTIFACT=${env.PACKAGED_ARTIFACT}"
echo "env.BRANCH=${env.BRANCH}"
echo "env.BUILD_NUMBER=${env.BUILD_NUMBER}"

def call(Map cfg = [:]) {
    if (!env.PACKAGED_ARTIFACT?.trim()) {
        error("PACKAGED_ARTIFACT is empty. Run packageArtifact first.")
    }

    def nexusUrl      = cfg.nexusUrl
    def rawRepo       = cfg.nexusRawRepo
    def credentialsId = cfg.nexusCredId
    def filePath      = env.PACKAGED_ARTIFACT

    if (!nexusUrl || !rawRepo || !credentialsId) {
        error("nexusUploadStage missing required args: nexusUrl, nexusRawRepo, nexusCredId")
    }

    def branch = env.BRANCH ?: env.BRANCH_NAME ?: 'main'
    def fileName = filePath.tokenize('/').last()
    def targetPath = "${cfg.appName ?: 'app'}/${branch}/${env.BUILD_NUMBER ?: '0'}"
    def uploadUrl = "${nexusUrl}/repository/${rawRepo}/${targetPath}/${fileName}".replaceAll('/+', '/')
    uploadUrl = uploadUrl.replaceFirst(':/', '://')

    echo "cfg.appName=${cfg.appName}"
    echo "cfg.nexusRawRepo=${cfg.nexusRawRepo}"
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
