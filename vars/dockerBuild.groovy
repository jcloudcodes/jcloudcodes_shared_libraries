def call(Map cfg) {
    def tag = env.BUILD_NUMBER
    def localImage = "${cfg.imageName}:${tag}"
    def nexusImage = "${cfg.dockerRegistry}/${cfg.dockerRepo}/${cfg.imageName}"

    echo "Local image: ${localImage}"
    echo "Nexus image: ${nexusImage}:${tag}"

    sh """
      set -euxo pipefail
      docker build -t ${localImage} -f ${cfg.dockerfile ?: 'Dockerfile'} .
    """

    dockerBuild(
        build: false,
        sourceImage: localImage,
        registry: "http://${cfg.dockerRegistry}",
        credentialsId: cfg.dockerCredId,
        image: nexusImage,
        tag: tag
    )
}
