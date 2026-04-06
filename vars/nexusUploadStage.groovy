def call(Map cfg) {
    if (!env.PACKAGED_ARTIFACT?.trim()) {
        error("PACKAGED_ARTIFACT is empty. Run packageArtifact first.")
    }

    def artifactFile = env.PACKAGED_ARTIFACT
    def artifactName = artifactFile.tokenize('/').last()
    def artifactType = artifactName.tokenize('.').last()

    echo "Uploading artifact to Nexus: ${artifactFile}"

    nexusArtifactUploader(
        nexusVersion: 'nexus3',
        protocol: 'http',
        nexusUrl: cfg.nexusUrl.replaceFirst('^https?://', ''),
        repository: cfg.nexusRawRepo,
        credentialsId: cfg.nexusCredId,
        groupId: cfg.groupId ?: 'com.jcloudcodes',
        version: env.BUILD_NUMBER,
        artifacts: [[
            artifactId: cfg.appName,
            classifier: '',
            file: artifactFile,
            type: artifactType
        ]]
    )
}
