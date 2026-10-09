// Change NEXUS_URL to your Nexus/SonarQube server IP if required

pipeline {
    agent { label 'built-in' }

    tools {
        maven 'maven-3.10'
    }

    environment {
        SCANNER_HOME = tool 'sonar-scanner'

        NEXUS_URL    = '18.61.41.151:8081'
        NEXUS_REPO   = 'devops-repo'

        APP_GROUP    = 'com.javatpoint'
        APP_ARTIFACT = 'SimpleCustomerApp'
        APP_VERSION  = "${BUILD_NUMBER}-SNAPSHOT"
    }

    stages {

        stage('Git Clone') {
            steps {
                git branch: 'feature-1.1',
                    url: 'https://github.com/betawins/sabear_simplecutomerapp.git'
            }
        }

        stage('SonarQube Integration') {
            steps {
                withSonarQubeEnv('sonarqube-server') {
                    sh """
                        ${SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.projectKey=sabear-declarative \
                        -Dsonar.projectName=sabear-declarative \
                        -Dsonar.projectVersion=1.0 \
                        -Dsonar.sources=src \
                        -Dsonar.java.binaries=. \
                        -Dsonar.scanner.skipJreProvisioning=true
                    """
                }
            }
        }

        stage('Maven Compilation') {
            steps {
                sh '''
                    rm -rf src/main/webapp
                    mkdir -p src/main
                    cp -r WebContent src/main/webapp
                '''

                sh 'mvn -DBUILD_NUMBER=${BUILD_NUMBER} clean package'
            }
        }

        stage('Nexus Artifactory') {
            steps {
                nexusArtifactUploader(
                    nexusVersion: 'nexus3',
                    protocol: 'http',
                    nexusUrl: "${NEXUS_URL}",
                    groupId: "${APP_GROUP}",
                    version: "${APP_VERSION}",
                    repository: "${NEXUS_REPO}",
                    credentialsId: 'nexus-credentials',
                    artifacts: [
                        [
                            artifactId: "${APP_ARTIFACT}",
                            classifier: '',
                            file: "target/${APP_ARTIFACT}-${APP_VERSION}.war",
                            type: 'war'
                        ]
                    ]
                )
            }
        }

        stage('Slack Notification') {
            steps {
                slackSend(
                    channel: '#jenkins-builds',
                    tokenCredentialId: 'slack-token',
                    botUser: true,
                    color: 'good',
                    message: "${env.JOB_NAME} #${env.BUILD_NUMBER} built and uploaded to Nexus: ${env.BUILD_URL}"
                )
            }
        }

        stage('Deploy On Tomcat') {
            steps {
                sh '''
                    docker cp target/*.war tomcat:/usr/local/tomcat/webapps/simplecustomerapp.war
                    sleep 15
                    docker logs --tail 5 tomcat
                '''
            }
        }
    }

    post {
        failure {
            slackSend(
                channel: '#jenkins-builds',
                tokenCredentialId: 'slack-token',
                botUser: true,
                color: 'danger',
                message: "${env.JOB_NAME} #${env.BUILD_NUMBER} FAILED: ${env.BUILD_URL}"
            )
        }
    }
}
