pipeline {
    agent any
    tools {
        maven 'LocalMVN'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                configFileProvider([configFile(fileId: '5e6f5fb5-ba7c-41d7-9f6a-634d1b059982', variable: 'MAVEN_SETTINGS')]) {
                    sh 'mvn clean install --settings $MAVEN_SETTINGS'
                }
            }
        }
        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}