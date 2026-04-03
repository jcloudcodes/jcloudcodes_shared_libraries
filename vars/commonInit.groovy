def call(Map cfg) {
    env.GIT_SHA  = sh(script: "git rev-parse --short=12 HEAD", returnStdout: true).trim()
    env.BRANCH   = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
    env.BUILD_TS = sh(script: "date -u +%Y%m%d%H%M%S", returnStdout: true).trim()

    env.APP_NAME = cfg.appName ?: 'app'
    env.IMAGE_NAME = cfg.imageName ?: env.APP_NAME

    env.ARTIFACT_NAME = "${env.APP_NAME}-${env.BRANCH}-${env.GIT_SHA}-${env.BUILD_TS}.zip"
    env.DOCKER_TAG = "${env.BUILD_NUMBER}"
}
