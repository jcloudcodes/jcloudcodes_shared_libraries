def call(Map cfg) {
    if (!env.LOCAL_IMAGE?.trim()) {
        error("LOCAL_IMAGE is empty. Run dockerBuild first.")
    }

    def tag = env.BUILD_NUMBER
    def jfrogImage = "${cfg.jfrogDockerRegistry}/${cfg.jfrogDockerRepo}/${cfg.imageName}"

    sh """
      set -euxo pipefail
      docker tag ${env.LOCAL_IMAGE} ${jfrogImage}:${tag}
    """

    docker.withRegistry("https://${cfg.jfrogDockerRegistry}", cfg.jfrogCredId) {
        sh """
          set -euxo pipefail
          docker push ${jfrogImage}:${tag}
        """
    }
}
