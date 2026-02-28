pipeline {
    agent any
    
    environment {
        PRODUCTION_DIR = '/opt/yaohufox/production'
        TESTING_DIR = '/opt/yaohufox/testing'
        PRODUCTION_PORT = '8081'  // 生产环境端口
        TESTING_PORT = '8082'     // 测试环境端口
        PRODUCTION_DOMAIN = 'yaohufox.com'  // 生产环境域名
        TESTING_DOMAIN = 'test.yaohufox.com'  // 测试环境域名
        TESTING_DOMAIN_PORT = '8082'  // 测试环境端口（使用8082端口访问）
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk'  // 根据实际 Java 路径调整
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // 获取当前分支名（多种方法尝试）
                    def branchName = env.GIT_BRANCH ?: env.BRANCH_NAME
                    if (!branchName) {
                        // 从 Git 命令获取
                        branchName = sh(
                            script: 'git rev-parse --abbrev-ref HEAD || git branch --show-current',
                            returnStdout: true
                        ).trim()
                    }
                    // 移除 origin/ 前缀（如果有）
                    branchName = branchName.replaceAll('^origin/', '')
                    env.BRANCH_NAME = branchName
                echo "代码检出完成，分支：${env.BRANCH_NAME}"
                }
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
            // 部署方式：复制 JAR 后通过 systemctl restart 重启服务。服务器上需预先配置 systemd 单元 mahjong-testing.service（见项目说明）。
            when {
                anyOf {
                    branch 'develop'
                    branch 'test'
                    expression { 
                        def branch = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceAll('^origin/', '')
                        return branch == 'develop' || branch == 'test'
                    }
                }
            }
            steps {
                script {
                    // 再次确认分支信息
                    def currentBranch = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceAll('^origin/', '') ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    echo "当前分支: ${currentBranch}"
                        echo "开始部署到测试环境..."
                        
                        // 复制新的 JAR 文件
                        sh '''
                        echo "开始复制 JAR 文件到测试环境..."
                            mkdir -p ${TESTING_DIR}
                        
                        # 显示工作空间信息
                        echo "工作空间: ${WORKSPACE}"
                        echo "目标目录: ${TESTING_DIR}"
                        
                        # 查找 JAR 文件（排除 sources 和 javadoc）
                        echo "查找 JAR 文件..."
                        JAR_FILE=$(find ${WORKSPACE}/mahjong-scoreboard-start/target -name "mahjong-scoreboard-start-*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
                        
                        if [ -z "$JAR_FILE" ]; then
                            echo "❌ 错误: 找不到 JAR 文件"
                            echo "检查构建目录："
                            ls -la ${WORKSPACE}/mahjong-scoreboard-start/target/ 2>/dev/null || echo "target 目录不存在"
                            echo ""
                            echo "查找所有 JAR 文件："
                            find ${WORKSPACE} -name "*.jar" -type f 2>/dev/null | head -10
                            exit 1
                        fi
                        
                        if [ ! -f "$JAR_FILE" ]; then
                            echo "❌ 错误: JAR 文件不存在: $JAR_FILE"
                            exit 1
                        fi
                        
                        echo "✅ 找到 JAR 文件: $JAR_FILE"
                        ls -lh "$JAR_FILE"
                        
                        # 复制文件
                        echo "复制 JAR 文件: $JAR_FILE -> ${TESTING_DIR}/app.jar"
                        cp "$JAR_FILE" ${TESTING_DIR}/app.jar
                        
                        # 验证复制是否成功
                        if [ ! -f "${TESTING_DIR}/app.jar" ]; then
                            echo "❌ 错误: 复制失败，目标文件不存在"
                            echo "检查目标目录权限："
                            ls -ld ${TESTING_DIR}
                            exit 1
                        fi
                        
                        echo "✅ JAR 文件复制成功"
                        ls -lh ${TESTING_DIR}/app.jar
                        '''
                        
                        // 通过 systemd 重启测试环境服务
                        sh '''
                        echo "重启 systemd 服务: mahjong-testing"
                        if ! systemctl restart mahjong-testing; then
                            echo "❌ systemctl restart mahjong-testing 失败"
                            systemctl status mahjong-testing --no-pager || true
                            journalctl -u mahjong-testing -n 50 --no-pager || true
                            exit 1
                        fi
                        echo "✅ mahjong-testing 已执行 restart"
                        sleep 3
                        '''
                        
                        // 等待应用启动（按端口与 systemd 状态检查）
                        echo "等待应用启动..."
                        sh '''
                        APP_PORT=${TESTING_PORT}
                        MAX_WAIT=60
                        WAIT_INTERVAL=3
                        WAIT_COUNT=0
                        
                        echo "等待应用启动（最多等待 ${MAX_WAIT} 秒）..."
                        while [ $WAIT_COUNT -lt $MAX_WAIT ]; do
                            sleep $WAIT_INTERVAL
                            WAIT_COUNT=$((WAIT_COUNT + WAIT_INTERVAL))
                            
                            if systemctl is-active --quiet mahjong-testing && ( netstat -tln 2>/dev/null | grep -q ":$APP_PORT " || ss -tln 2>/dev/null | grep -q ":$APP_PORT " ); then
                                echo "✅ 服务已就绪，端口 $APP_PORT 正在监听（${WAIT_COUNT} 秒）"
                                break
                            fi
                            echo "等待端口 $APP_PORT 监听... (${WAIT_COUNT}/${MAX_WAIT}秒)"
                        done
                        
                        if ! systemctl is-active --quiet mahjong-testing; then
                            echo "❌ mahjong-testing 未在运行"
                            systemctl status mahjong-testing --no-pager || true
                            echo "--- 最近日志 ---"
                            journalctl -u mahjong-testing -n 100 --no-pager || true
                            exit 1
                        fi
                        if ! netstat -tln 2>/dev/null | grep -q ":$APP_PORT " && ! ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
                            echo "❌ 端口 $APP_PORT 未监听"
                            echo "--- 应用日志 ---"
                            tail -200 ${TESTING_DIR}/app.log 2>/dev/null || journalctl -u mahjong-testing -n 100 --no-pager
                            exit 1
                        fi
                        '''
                        
                        // 验证部署
                        sh '''
                        APP_PORT=${TESTING_PORT}
                        
                        if ! systemctl is-active --quiet mahjong-testing; then
                            echo "❌ mahjong-testing 未在运行"
                            systemctl status mahjong-testing --no-pager || true
                            journalctl -u mahjong-testing -n 80 --no-pager || true
                            exit 1
                        fi
                        if ! netstat -tln 2>/dev/null | grep -q ":$APP_PORT " && ! ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
                            echo "❌ 端口 $APP_PORT 未监听"
                            tail -150 ${TESTING_DIR}/app.log 2>/dev/null || journalctl -u mahjong-testing -n 80 --no-pager
                            exit 1
                        fi
                        
                        echo "检查应用健康状态..."
                        LOCALHOST_URL="http://localhost:${TESTING_PORT}"
                        MAX_RETRIES=10
                        RETRY_COUNT=0
                        HEALTH_CHECK_SUCCESS=false
                        while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
                            sleep 2
                            RETRY_COUNT=$((RETRY_COUNT + 1))
                            if curl -f -s --connect-timeout 3 "${LOCALHOST_URL}/actuator/health" > /dev/null 2>&1 || curl -f -s --connect-timeout 3 "${LOCALHOST_URL}/api/test/hello" > /dev/null 2>&1; then
                                echo "✅ 健康检查通过"
                                curl -s "${LOCALHOST_URL}/actuator/health" | head -5
                                HEALTH_CHECK_SUCCESS=true
                                break
                            fi
                            echo "健康检查尝试 $RETRY_COUNT/$MAX_RETRIES..."
                        done
                        
                        if [ "$HEALTH_CHECK_SUCCESS" != "true" ]; then
                            echo "❌ 健康检查失败"
                            tail -200 ${TESTING_DIR}/app.log 2>/dev/null || journalctl -u mahjong-testing -n 100 --no-pager
                            exit 1
                        fi
                        
                        echo "验证域名访问（可选）..."
                        if [ -n "${TESTING_DOMAIN_PORT}" ]; then
                            DOMAIN_URL="http://${TESTING_DOMAIN}:${TESTING_DOMAIN_PORT}"
                            if curl -f -s --connect-timeout 5 "${DOMAIN_URL}/actuator/health" > /dev/null 2>&1; then
                                echo "✅ 域名访问成功: ${DOMAIN_URL}/actuator/health"
                            else
                                echo "⚠️  域名访问失败（localhost 已通过）"
                            fi
                        fi
                        '''
                        
                        echo "测试环境部署完成！"
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                anyOf {
                branch 'main'
                    branch 'master'
                    expression { 
                        def branch = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceAll('^origin/', '')
                        return branch == 'main' || branch == 'master'
                    }
                }
            }
            steps {
                script {
                    // 再次确认分支信息
                    def currentBranch = env.BRANCH_NAME ?: env.GIT_BRANCH?.replaceAll('^origin/', '') ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    echo "当前分支: ${currentBranch}"
                        echo "开始部署到生产环境..."
                        
                        // 复制新的 JAR 文件
                        sh '''
                        echo "开始复制 JAR 文件到生产环境..."
                            mkdir -p ${PRODUCTION_DIR}
                        
                        # 显示工作空间信息
                        echo "工作空间: ${WORKSPACE}"
                        echo "目标目录: ${PRODUCTION_DIR}"
                        
                        # 查找 JAR 文件（排除 sources 和 javadoc）
                        echo "查找 JAR 文件..."
                        JAR_FILE=$(find ${WORKSPACE}/mahjong-scoreboard-start/target -name "mahjong-scoreboard-start-*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" | head -1)
                        
                        if [ -z "$JAR_FILE" ]; then
                            echo "❌ 错误: 找不到 JAR 文件"
                            echo "检查构建目录："
                            ls -la ${WORKSPACE}/mahjong-scoreboard-start/target/ 2>/dev/null || echo "target 目录不存在"
                            echo ""
                            echo "查找所有 JAR 文件："
                            find ${WORKSPACE} -name "*.jar" -type f 2>/dev/null | head -10
                            exit 1
                        fi
                        
                        if [ ! -f "$JAR_FILE" ]; then
                            echo "❌ 错误: JAR 文件不存在: $JAR_FILE"
                            exit 1
                        fi
                        
                        echo "✅ 找到 JAR 文件: $JAR_FILE"
                        ls -lh "$JAR_FILE"
                        
                        # 复制文件
                        echo "复制 JAR 文件: $JAR_FILE -> ${PRODUCTION_DIR}/app.jar"
                        cp "$JAR_FILE" ${PRODUCTION_DIR}/app.jar
                        
                        # 验证复制是否成功
                        if [ ! -f "${PRODUCTION_DIR}/app.jar" ]; then
                            echo "❌ 错误: 复制失败，目标文件不存在"
                            echo "检查目标目录权限："
                            ls -ld ${PRODUCTION_DIR}
                            exit 1
                        fi
                        
                        echo "✅ JAR 文件复制成功"
                        ls -lh ${PRODUCTION_DIR}/app.jar
                        '''
                        
                        // 通过 systemd 重启生产环境服务
                        sh '''
                        echo "重启 systemd 服务: mahjong-production"
                        if ! systemctl restart mahjong-production; then
                            echo "❌ systemctl restart mahjong-production 失败"
                            systemctl status mahjong-production --no-pager || true
                            journalctl -u mahjong-production -n 50 --no-pager || true
                            exit 1
                        fi
                        echo "✅ mahjong-production 已执行 restart"
                        sleep 3
                        '''
                        
                        // 等待应用启动
                        echo "等待应用启动..."
                        sh '''
                        APP_PORT=${PRODUCTION_PORT}
                        MAX_WAIT=60
                        WAIT_INTERVAL=3
                        WAIT_COUNT=0
                        while [ $WAIT_COUNT -lt $MAX_WAIT ]; do
                            sleep $WAIT_INTERVAL
                            WAIT_COUNT=$((WAIT_COUNT + WAIT_INTERVAL))
                            if systemctl is-active --quiet mahjong-production && ( netstat -tln 2>/dev/null | grep -q ":$APP_PORT " || ss -tln 2>/dev/null | grep -q ":$APP_PORT " ); then
                                echo "✅ 服务已就绪，端口 $APP_PORT 正在监听"
                                break
                            fi
                            echo "等待端口 $APP_PORT... (${WAIT_COUNT}/${MAX_WAIT}秒)"
                        done
                        if ! systemctl is-active --quiet mahjong-production; then
                            echo "❌ mahjong-production 未在运行"
                            systemctl status mahjong-production --no-pager || true
                            journalctl -u mahjong-production -n 80 --no-pager || true
                            exit 1
                        fi
                        if ! netstat -tln 2>/dev/null | grep -q ":$APP_PORT " && ! ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
                            echo "❌ 端口 $APP_PORT 未监听"
                            journalctl -u mahjong-production -n 100 --no-pager || true
                            exit 1
                        fi
                        '''
                        
                        // 验证部署
                        sh '''
                        APP_PORT=${PRODUCTION_PORT}
                        if ! systemctl is-active --quiet mahjong-production; then
                            echo "❌ mahjong-production 未在运行"
                            systemctl status mahjong-production --no-pager || true
                            journalctl -u mahjong-production -n 80 --no-pager || true
                            exit 1
                        fi
                        if ! netstat -tln 2>/dev/null | grep -q ":$APP_PORT " && ! ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
                            echo "❌ 端口 $APP_PORT 未监听"
                            journalctl -u mahjong-production -n 80 --no-pager || true
                            exit 1
                        fi
                        echo "检查应用健康状态..."
                        MAX_RETRIES=8
                        RETRY_COUNT=0
                        HEALTH_CHECK_SUCCESS=false
                        LOCALHOST_URL="http://localhost:${PRODUCTION_PORT}"
                        HEALTH_CHECK_URL="https://${PRODUCTION_DOMAIN}"
                        HEALTH_CHECK_HTTP_URL="http://${PRODUCTION_DOMAIN}"
                        while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
                            sleep 3
                            RETRY_COUNT=$((RETRY_COUNT + 1))
                            if curl -f -s --connect-timeout 5 -k "${HEALTH_CHECK_URL}/actuator/health" > /dev/null 2>&1 || \
                               curl -f -s --connect-timeout 5 "${HEALTH_CHECK_HTTP_URL}/actuator/health" > /dev/null 2>&1 || \
                               curl -f -s --connect-timeout 5 "${LOCALHOST_URL}/actuator/health" > /dev/null 2>&1 || \
                               curl -f -s --connect-timeout 5 "${LOCALHOST_URL}/api/test/hello" > /dev/null 2>&1; then
                                echo "✅ 健康检查通过"
                                HEALTH_CHECK_SUCCESS=true
                                break
                            fi
                            echo "健康检查尝试 $RETRY_COUNT/$MAX_RETRIES..."
                        done
                        if [ "$HEALTH_CHECK_SUCCESS" != "true" ]; then
                            echo "❌ 健康检查失败"
                            journalctl -u mahjong-production -n 100 --no-pager || true
                            exit 1
                        fi
                        '''
                        
                        echo "生产环境部署完成！"
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

