pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        GRADLE_USER_HOME = "${WORKSPACE}/.gradle"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Basic checks') {
            steps {
                sh './gradlew --no-daemon clean testDebugUnitTest lintDebug'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/build/reports/**', allowEmptyArchive: true
            junit testResults: '**/build/test-results/testDebugUnitTest/*.xml', allowEmptyResults: true
        }
    }
}
