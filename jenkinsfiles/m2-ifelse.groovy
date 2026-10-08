//jenkinsfiles/m2-ifelse.groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps { echo "Building branch ${env.BRANCH_NAME}" }
        }
        stage('Deploy to Production') {
            when { branch 'main' }
            steps { echo 'Deploying to production...' }
        }
        stage('Deploy to Staging') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'staging'
                }
            }
            steps { echo 'Deploying to staging...' }
        }
        stage('Feature checks') {
            when { branch 'feature/*' }
            steps { echo 'Quick checks for a feature branch' }
        }
    }
}
