pipeline {
    agent any
    
    environment {
        PRODUCTION_DIR = '/opt/yaohufox/production'
        TESTING_DIR = '/opt/yaohufox/testing'
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk'  // 根据实际 Java 路径调整3
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "代码检出完成，分支：${env.BRANCH_NAME}"
            }
        }
        
        stage('Build Application') {
            steps {
                script {
                    echo "开始构建应用..."
                    
                    // 使用 Maven 构建应用（不构建 Docker 镜像）
                    sh '''
                        # 设置 JAVA_HOME（如果环境变量未设置或路径不存在，尝试自动查找）
                        if [ -z "$JAVA_HOME" ] || [ ! -d "$JAVA_HOME" ]; then
                            # 尝试查找 Java 21
                            if [ -d "/usr/lib/jvm/java-21-openjdk" ]; then
                                export JAVA_HOME="/usr/lib/jvm/java-21-openjdk"
                            elif [ -d "/usr/lib/jvm/java-21" ]; then
                                export JAVA_HOME="/usr/lib/jvm/java-21"
                            else
                                # 尝试从 java 命令查找
                                JAVA_PATH=$(readlink -f $(which java) 2>/dev/null || echo "")
                                if [ -n "$JAVA_PATH" ]; then
                                    JAVA_HOME=$(dirname $(dirname "$JAVA_PATH"))
                                    export JAVA_HOME
                                fi
                            fi
                        fi
                        
                        # 验证 JAVA_HOME
                        echo "JAVA_HOME: $JAVA_HOME"
                        if [ -z "$JAVA_HOME" ] || [ ! -d "$JAVA_HOME" ]; then
                            echo "错误: JAVA_HOME 未设置或路径不存在"
                            echo "请检查 Java 安装路径，或修改 Jenkinsfile 中的 JAVA_HOME"
                            exit 1
                        fi
                        
                        # 验证 Java 版本
                        $JAVA_HOME/bin/java -version || {
                            echo "错误: 无法执行 Java，请检查 JAVA_HOME 路径"
                            exit 1
                        }
                        
                        # 设置 PATH
                        export PATH="$JAVA_HOME/bin:$PATH"
                        
                        # 构建整个多模块项目（从根目录构建，确保所有依赖都被构建）
                        echo "构建整个项目（多模块）..."
                        mvn clean package -DskipTests
                        
                        # 验证构建产物
                        JAR_FILE=$(find mahjong-scoreboard-start/target -name "mahjong-scoreboard-start-*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
                        if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
                            echo "错误: JAR 文件未找到，构建可能失败"
                            echo "检查 target 目录："
                            ls -la mahjong-scoreboard-start/target/ || true
                            exit 1
                        fi
                        
                        echo "构建成功，JAR 文件位置："
                        ls -lh "$JAR_FILE"
                    '''
                    
                    echo "应用构建完成"
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
                        
                        // 停止旧应用
                        sh '''
                            if [ -f app.pid ]; then
                                PID=$(cat app.pid)
                                if ps -p $PID > /dev/null 2>&1; then
                                    kill $PID || true
                                    sleep 5
                                    kill -9 $PID 2>/dev/null || true
                                fi
                                rm -f app.pid
                            fi
                        '''
                        
                        // 复制新的 JAR 文件
                        sh '''
                            mkdir -p ${TESTING_DIR}
                            # 查找 JAR 文件（排除 sources 和 javadoc）
                            JAR_FILE=$(find ${WORKSPACE}/mahjong-scoreboard-start/target -name "mahjong-scoreboard-start-*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
                            if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
                                echo "错误: 找不到 JAR 文件"
                                exit 1
                            fi
                            echo "复制 JAR 文件: $JAR_FILE -> ${TESTING_DIR}/app.jar"
                            cp "$JAR_FILE" ${TESTING_DIR}/app.jar
                        '''
                        
                        // 启动新应用
                        sh '''
                            cd ${TESTING_DIR}
                            nohup java -jar -Dspring.profiles.active=testing app.jar > app.log 2>&1 &
                            echo $! > app.pid
                        '''
                        
                        // 等待应用启动
                        echo "等待应用启动..."
                        sh 'sleep 15'
                        
                        // 验证部署
                        sh '''
                            if [ -f app.pid ]; then
                                PID=$(cat app.pid)
                                if ps -p $PID > /dev/null 2>&1; then
                                    echo "应用运行中，PID: $PID"
                                else
                                    echo "应用启动失败，查看日志："
                                    tail -50 app.log
                                    exit 1
                                fi
                            fi
                        '''
                        
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
                        
                        // 停止旧应用
                        sh '''
                            if [ -f app.pid ]; then
                                PID=$(cat app.pid)
                                if ps -p $PID > /dev/null 2>&1; then
                                    kill $PID || true
                                    sleep 5
                                    kill -9 $PID 2>/dev/null || true
                                fi
                                rm -f app.pid
                            fi
                        '''
                        
                        // 复制新的 JAR 文件
                        sh '''
                            mkdir -p ${PRODUCTION_DIR}
                            # 查找 JAR 文件（排除 sources 和 javadoc）
                            JAR_FILE=$(find ${WORKSPACE}/mahjong-scoreboard-start/target -name "mahjong-scoreboard-start-*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
                            if [ -z "$JAR_FILE" ] || [ ! -f "$JAR_FILE" ]; then
                                echo "错误: 找不到 JAR 文件"
                                exit 1
                            fi
                            echo "复制 JAR 文件: $JAR_FILE -> ${PRODUCTION_DIR}/app.jar"
                            cp "$JAR_FILE" ${PRODUCTION_DIR}/app.jar
                        '''
                        
                        // 启动新应用
                        sh '''
                            cd ${PRODUCTION_DIR}
                            nohup java -jar -Dspring.profiles.active=production app.jar > app.log 2>&1 &
                            echo $! > app.pid
                        '''
                        
                        // 等待应用启动
                        echo "等待应用启动..."
                        sh 'sleep 15'
                        
                        // 验证部署
                        sh '''
                            if [ -f app.pid ]; then
                                PID=$(cat app.pid)
                                if ps -p $PID > /dev/null 2>&1; then
                                    echo "应用运行中，PID: $PID"
                                else
                                    echo "应用启动失败，查看日志："
                                    tail -50 app.log
                                    exit 1
                                fi
                            fi
                        '''
                        
                        echo "生产环境部署完成！"
                    }
                }
            }
        }
    }
    
    post {
        always {
            // 清理旧的构建产物（保留最近 5 个）
            sh '''
                find ${PRODUCTION_DIR} -name "app-*.jar" -type f -mtime +5 -delete 2>/dev/null || true
                find ${TESTING_DIR} -name "app-*.jar" -type f -mtime +5 -delete 2>/dev/null || true
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

