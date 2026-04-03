def call(Map cfg) {
    def javaVersion = cfg.javaVersion ?: '17'

    sh """
      set -euxo pipefail
      docker run --rm \
        -v "\$PWD:/workspace" -w /workspace \
        maven:3.9.9-eclipse-temurin-${javaVersion} \
        mvn -B clean test package
    """
}
