def call(String file = 'ci/project.yaml') {
    if (!fileExists(file)) {
        error("Project config file not found: ${file}")
    }

    def cfg = readYaml file: file
    if (!cfg?.projectType) {
        error("projectType is required in ${file}")
    }

    return cfg
}
