pipeline {
    agent any
    
    environment {
        PRODUCTION_DIR = '/opt/yaohufox/production'
        TESTING_DIR = '/opt/yaohufox/testing'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "代码检出完成，分支：${env.BRANCH_NAME}"
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    // 构建 Docker 镜像
                    def imageTag = "mahjong-scoreboard:${BUILD_NUMBER}"
                    sh "docker build -t ${imageTag} ."
                    sh "docker tag ${imageTag} mahjong-scoreboard:latest"
                    
                    echo "Docker 镜像构建完成：${imageTag}"
                    
                    // 如果是测试环境分支，也打标签
                    if (env.BRANCH_NAME == 'develop' || env.BRANCH_NAME == 'test') {
                        sh "docker tag ${imageTag} mahjong-scoreboard:test"
                        echo "已标记为测试环境镜像"
                    }
                }
            }
        }
        
        stage('Deploy to Testing') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'test'
                }
            }
            steps {
                script {
                    dir(TESTING_DIR) {
                        echo "开始部署到测试环境..."
                        
                        // 停止旧容器
                        sh 'docker-compose down || true'
                        
                        // 确保使用最新镜像
                        sh "docker tag mahjong-scoreboard:${BUILD_NUMBER} mahjong-scoreboard:test"
                        
                        // 启动新容器
                        sh 'docker-compose up -d'
                        
                        // 等待健康检查
                        echo "等待应用启动..."
                        sh 'sleep 15'
                        
                        // 验证部署
                        sh 'docker-compose ps'
                        
                        echo "测试环境部署完成！"
                    }
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    dir(PRODUCTION_DIR) {
                        echo "开始部署到生产环境..."
                        
                        // 停止旧容器
                        sh 'docker-compose down || true'
                        
                        // 确保使用最新镜像
                        sh "docker tag mahjong-scoreboard:${BUILD_NUMBER} mahjong-scoreboard:latest"
                        
                        // 启动新容器
                        sh 'docker-compose up -d'
                        
                        // 等待健康检查
                        echo "等待应用启动..."
                        sh 'sleep 15'
                        
                        // 验证部署
                        sh 'docker-compose ps'
                        
                        echo "生产环境部署完成！"
                    }
                }
            }
        }
    }
    
    post {
        always {
            // 清理旧镜像（保留最近 5 个构建）
            sh '''
                docker images mahjong-scoreboard --format "{{.ID}} {{.Tag}}" | \
                grep -E "^[a-f0-9]+ [0-9]+$" | \
                tail -n +6 | awk '{print $1}' | \
                xargs -r docker rmi 2>/dev/null || true
            '''
        }
        success {
            echo '✅ 部署成功！'
        }
        failure {
            echo '❌ 部署失败！请检查日志。'
        }
    }
}

