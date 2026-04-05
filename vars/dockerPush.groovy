def call(Map cfg) {
    if (!env.LOCAL_IMAGE?.trim()) {
        error("LOCAL_IMAGE is empty. Run dockerBuild first.")
    }

    def tag = env.BUILD_NUMBER
    def nexusImage = "${cfg.dockerRegistry}/${cfg.dockerRepo}/${cfg.imageName}"

    sh """
      set -euxo pipefail
      docker tag ${env.LOCAL_IMAGE} ${nexusImage}:${tag}
    """

    docker.withRegistry("http://${cfg.dockerRegistry}", cfg.dockerCredId) {
        sh """
          set -euxo pipefail
          docker push ${nexusImage}:${tag}
        """
    }
}
