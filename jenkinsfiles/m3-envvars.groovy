// jenkinsfiles/m3-envvars.groovy
pipeline {
    agent any
    environment {
        DEPLOY_ENV = 'none'
    }
    stages {
        stage('Set Environment') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'main') {
                        env.DEPLOY_ENV = 'production'
                    } else if (env.BRANCH_NAME == 'develop' || env.BRANCH_NAME == 'staging') {
                        env.DEPLOY_ENV = 'staging'
                    } else if (env.BRANCH_NAME.startsWith('feature/')) {
                        env.DEPLOY_ENV = 'feature'
                    }
                }
            }
        }
        stage('Build') {
            steps {
                echo "Building branch ${env.BRANCH_NAME}"
                echo "Target environment: ${env.DEPLOY_ENV}"
            }
        }
        stage('Deploy') {
            steps {
                script {
                    switch (env.DEPLOY_ENV) {
                        case 'production':
                            echo 'Deploying to production...'
                            break
                        case 'staging':
                            echo 'Deploying to staging...'
                            break
                        case 'feature':
                            echo 'Running feature branch checks...'
                            break
                        default:
                            echo 'No deployment required'
                    }
                }
            }
        }
    }
}
