def call(Map cfg) {
    sh '''
      set +e
      docker image prune -f || true
      true
    '''

    archiveArtifacts artifacts: 'dist/*.zip, target/*.jar, target/*.war, build/libs/*.jar', fingerprint: true, onlyIfSuccessful: false
    junit allowEmptyResults: true, testResults: '**/test-results/**/*.xml, **/pytest-report.xml, **/surefire-reports/*.xml'
    cleanWs(deleteDirs: true, notFailBuild: true)
}
