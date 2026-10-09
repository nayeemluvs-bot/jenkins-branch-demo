pipeline {
    agent any

    environment {
        APP_NAME = 'simplecustomerapp'
        SONAR_SERVER = 'SonarQube-102026'
        NEXUS_URL = 'http://<NEXUS-IP>:8081/repository/maven-releases/'
        TOMCAT_URL = 'http://<TOMCAT-IP>:8080'
    }

    stages {
        stage('Git Clone') {
            steps {
                checkout scm
                sh 'git rev-parse --short HEAD'
            }
        }

        stage('SonarQube Integration') {
            steps {
                withSonarQubeEnv("${SONAR_SERVER}") {
                    sh '''
                        mvn -B sonar:sonar \
                          -Dsonar.projectKey=simplecustomerapp \
                          -Dsonar.projectName=simplecustomerapp
                    '''
                }
            }
        }

        stage('Maven Compilation') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('Nexus Artifactory') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'nexus-creds',
                    usernameVariable: 'NEXUS_USER',
                    passwordVariable: 'NEXUS_PASS'
                )]) {
                    sh 'mvn -B deploy -DskipTests'
                }
            }
        }

        stage('Slack Notification') {
            steps {
                slackSend(
                    channel: '#jenkins-alerts',
                    color: 'good',
                    message: "SUCCESS ${env.JOB_NAME} #${env.BUILD_NUMBER} ${env.BUILD_URL}"
                )
            }
        }

        stage('Deploy on Tomcat') {
            steps {
                sh 'echo "Deploy target: ${TOMCAT_URL}"'
                // Put approved Tomcat deployment command/script here.
            }
        }
    }

    post {
        failure {
            slackSend(
                channel: '#jenkins-alerts',
                color: 'danger',
                message: "FAILED ${env.JOB_NAME} #${env.BUILD_NUMBER} ${env.BUILD_URL}"
            )
        }
    }
}
