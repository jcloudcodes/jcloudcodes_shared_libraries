def call(Map cfg) {
    if (!env.LOCAL_IMAGE?.trim()) {
        error("LOCAL_IMAGE is empty. Run dockerBuild first.")
    }

    def tag = env.BUILD_NUMBER
    def hubImage = "${cfg.dockerhubNamespace}/${cfg.imageName}"

    sh """
      set -euxo pipefail
      docker tag ${env.LOCAL_IMAGE} ${hubImage}:${tag}
    """

    docker.withRegistry("https://index.docker.io/v1/", cfg.dockerhubCredId) {
        sh """
          set -euxo pipefail
          docker push ${hubImage}:${tag}
        """
    }
}
