// jenkinsfiles/m2-ifelse.groovy
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                echo "Building branch ${env.BRANCH_NAME}"
            }
        }
        stage('Deploy') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'main') {
                        echo 'Deploying to production...'
                    } else if (env.BRANCH_NAME == 'develop' || env.BRANCH_NAME == 'staging') {
                        echo 'Deploying to staging...'
                    } else if (env.BRANCH_NAME.startsWith('feature/')) {
                        echo 'Quick checks for a feature branch'
                    } else {
                        echo "No deployment rule for ${env.BRANCH_NAME}"
                    }
                }
            }
        }
    }
}
