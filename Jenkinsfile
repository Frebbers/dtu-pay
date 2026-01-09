pipeline {
  agent { label 'linux' }

  options {
    timestamps()
    ansiColor('xterm')
  }

  environment {
    MAVEN_OPTS = '-Dmaven.test.failure.ignore=false'
    BANK_API_KEY = credentials('bank-api-key-id-amber')
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Prepare') {
      steps {
        sh 'chmod +x mvnw'
        sh 'java -version'
      }
    }

    stage('Test') {
      steps {
        sh 'bash build_and_test.sh'
      }
    }

    stage('Build & Run') {
      steps {
        sh 'bash build_and_run.sh'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'target/surefire-reports/**/*,target/cucumber-reports/**/*', allowEmptyArchive: true
      junit 'target/surefire-reports/*.xml'
    }
  }
}

