def call(Map cfg) {
    def tag = env.BUILD_NUMBER
    def localImage = "${cfg.imageName}:${tag}"

    env.LOCAL_IMAGE = localImage

    echo "Building local Docker image: ${env.LOCAL_IMAGE}"

    sh """
      set -euxo pipefail
      docker version
      docker build -t ${env.LOCAL_IMAGE} -f ${cfg.dockerfile ?: 'Dockerfile'} .
      docker images | grep ${cfg.imageName} || true
    """
}
