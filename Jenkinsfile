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
                        
                        // 停止旧应用
                        sh '''
                        if [ -f ${TESTING_DIR}/app.pid ]; then
                            PID=$(cat ${TESTING_DIR}/app.pid)
                                if ps -p $PID > /dev/null 2>&1; then
                                    kill $PID || true
                                    sleep 5
                                    kill -9 $PID 2>/dev/null || true
                                fi
                            rm -f ${TESTING_DIR}/app.pid
                            fi
                        '''
                        
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
                        
                        // 启动新应用
                        sh '''
                            cd ${TESTING_DIR}
                        
                        # 加载环境变量（从 .env 文件或系统环境变量）
                        ENV_FILE="${TESTING_DIR}/.env"
                        if [ -f "$ENV_FILE" ]; then
                            echo "从 .env 文件加载环境变量: $ENV_FILE"
                            # 读取 .env 文件并设置环境变量（兼容 sh）
                            set -a
                            . "$ENV_FILE" 2>/dev/null || {
                                echo "⚠️  加载 .env 文件失败，尝试手动读取..."
                                # 手动读取 .env 文件
                                while IFS='=' read -r key value; do
                                    # 跳过注释和空行
                                    case "$key" in
                                        '#'*) continue ;;
                                        '') continue ;;
                                    esac
                                    # 移除引号
                                    value=$(echo "$value" | sed "s/^['\\\"]//; s/['\\\"]$//")
                                    export "$key=$value"
                                done < "$ENV_FILE"
                            }
                            set +a
                        else
                            echo "⚠️  .env 文件不存在: $ENV_FILE，使用系统环境变量或默认值"
                        fi
                        
                        # 显式设置端口
                        export SERVER_PORT=${TESTING_PORT}
                        
                        # 强制设置测试环境数据库连接（覆盖 .env 或系统环境变量中的值）
                        export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test
                        export SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-yaohu}
                        
                        # 输出确认信息（用于调试）
                        echo "⚠️  强制使用测试环境数据库配置（忽略 .env 中的 SPRING_DATASOURCE_URL）"
                        echo "   数据库URL: ${SPRING_DATASOURCE_URL}"
                        
                        # 数据库密码必须设置（从 POSTGRES_PASSWORD 或 SPRING_DATASOURCE_PASSWORD）
                        if [ -z "$SPRING_DATASOURCE_PASSWORD" ] && [ -n "$POSTGRES_PASSWORD" ]; then
                            export SPRING_DATASOURCE_PASSWORD="$POSTGRES_PASSWORD"
                        fi
                        
                        # 如果仍然没有密码，使用默认值（仅用于测试，生产环境应该设置）
                        if [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
                            echo "⚠️  警告: SPRING_DATASOURCE_PASSWORD 未设置，使用默认值"
                            export SPRING_DATASOURCE_PASSWORD="cch815566"
                        fi
                        
                        # 设置微信配置（如果存在）
                        export WECHAT_APPID=${WECHAT_APPID_TEST:-${WECHAT_APPID:-}}
                        export WECHAT_APPSECRET=${WECHAT_APPSECRET_TEST:-${WECHAT_APPSECRET:-}}
                        
                        echo "=========================================="
                        echo "启动测试环境应用"
                        echo "=========================================="
                        echo "端口: ${TESTING_PORT}"
                        echo "数据库URL: ${SPRING_DATASOURCE_URL}"
                        echo "数据库用户: ${SPRING_DATASOURCE_USERNAME}"
                        echo "Profile: testing"
                        echo "=========================================="
                        if [ -n "$SPRING_DATASOURCE_PASSWORD" ]; then
                            echo "数据库密码: 已设置（隐藏）"
                        else
                            echo "⚠️  警告: 数据库密码未设置"
                        fi
                        
                        # 使用 env 命令确保环境变量被传递，并通过 -D 参数显式传递数据库配置
                        nohup env SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}" \
                            SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME}" \
                            SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD}" \
                            WECHAT_APPID="${WECHAT_APPID}" \
                            WECHAT_APPSECRET="${WECHAT_APPSECRET}" \
                            SERVER_PORT="${TESTING_PORT}" \
                            java -jar \
                            -Dspring.profiles.active=testing \
                            -Dserver.port=${TESTING_PORT} \
                            -Dspring.datasource.url="${SPRING_DATASOURCE_URL}" \
                            -Dspring.datasource.username="${SPRING_DATASOURCE_USERNAME}" \
                            -Dspring.datasource.password="${SPRING_DATASOURCE_PASSWORD}" \
                            app.jar > app.log 2>&1 &
                            echo $! > app.pid
                        echo "应用已启动，PID: $(cat app.pid)"
                        '''
                        
                        // 等待应用启动（增加等待时间，并分阶段检查）
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
                            
                            # 检查进程是否存在
                            if [ -f ${TESTING_DIR}/app.pid ]; then
                                PID=$(cat ${TESTING_DIR}/app.pid)
                                if ! ps -p $PID > /dev/null 2>&1; then
                                    echo "❌ 进程 $PID 已退出（启动失败）"
                                    echo "查看应用启动日志："
                                    tail -100 ${TESTING_DIR}/app.log
                                    exit 1
                                fi
                            else
                                echo "⚠️  PID文件不存在，等待中... (${WAIT_COUNT}/${MAX_WAIT}秒)"
                                continue
                            fi
                            
                            # 检查端口是否监听
                            if netstat -tln 2>/dev/null | grep -q ":$APP_PORT " || ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
                                echo "✅ 端口 $APP_PORT 正在监听（等待 ${WAIT_COUNT} 秒后检测到）"
                                break
                            fi
                            
                            echo "等待端口 $APP_PORT 监听... (${WAIT_COUNT}/${MAX_WAIT}秒)"
                        done
                        
                        # 最终检查：端口必须监听
                        if ! netstat -tln 2>/dev/null | grep -q ":$APP_PORT " && ! ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
                            echo "❌ 端口 $APP_PORT 未监听，应用启动失败"
                            echo "查看应用启动日志："
                            tail -200 ${TESTING_DIR}/app.log
                            echo ""
                            echo "检查进程状态："
                            if [ -f ${TESTING_DIR}/app.pid ]; then
                                PID=$(cat ${TESTING_DIR}/app.pid)
                                if ps -p $PID > /dev/null 2>&1; then
                                    echo "进程 $PID 仍在运行，但端口未监听，可能应用启动失败"
                                else
                                    echo "进程 $PID 已退出"
                                fi
                            fi
                            exit 1
                        fi
                        '''
                        
                        // 验证部署
                        sh '''
                        APP_PORT=${TESTING_PORT}
                        
                        # 1. 再次确认进程和端口
                        if [ -f ${TESTING_DIR}/app.pid ]; then
                            PID=$(cat ${TESTING_DIR}/app.pid)
                            if ps -p $PID > /dev/null 2>&1; then
                                echo "✅ 应用进程运行中，PID: $PID"
                            else
                                echo "❌ 应用进程不存在，查看日志："
                                tail -100 ${TESTING_DIR}/app.log
                                exit 1
                            fi
                        else
                            echo "❌ PID 文件不存在，查看日志："
                            tail -100 ${TESTING_DIR}/app.log
                            exit 1
                        fi
                        
                        # 2. 再次确认端口监听
                        if netstat -tln 2>/dev/null | grep -q ":$APP_PORT " || ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
                            echo "✅ 端口 $APP_PORT 正在监听"
                        else
                            echo "❌ 端口 $APP_PORT 未监听，应用启动失败"
                            echo "查看应用启动日志："
                            tail -200 ${TESTING_DIR}/app.log
                            exit 1
                        fi
                        
                        # 3. 检查 HTTP 健康检查端点（优先检查 localhost，确保应用真正启动）
                        echo "检查应用健康状态..."
                        MAX_RETRIES=10
                        RETRY_COUNT=0
                        HEALTH_CHECK_SUCCESS=false
                        LOCALHOST_URL="http://localhost:${TESTING_PORT}"
                        
                        echo "优先检查 localhost 健康检查端点（确保应用真正启动）..."
                        echo "健康检查地址: ${LOCALHOST_URL}/actuator/health"
                        
                        while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
                            sleep 2
                            RETRY_COUNT=$((RETRY_COUNT + 1))
                            echo "健康检查尝试 $RETRY_COUNT/$MAX_RETRIES..."
                            
                            # 优先检查 localhost（最可靠）
                            if curl -f -s --connect-timeout 3 "${LOCALHOST_URL}/actuator/health" > /dev/null 2>&1; then
                                echo "✅ 健康检查通过（localhost 访问）"
                                curl -s "${LOCALHOST_URL}/actuator/health" | head -5
                                HEALTH_CHECK_SUCCESS=true
                                break
                            elif curl -f -s --connect-timeout 3 "${LOCALHOST_URL}/api/test/hello" > /dev/null 2>&1; then
                                echo "✅ 测试接口可访问（localhost 访问）"
                                curl -s "${LOCALHOST_URL}/api/test/hello"
                                HEALTH_CHECK_SUCCESS=true
                                break
                            fi
                        done
                        
                        # 如果 localhost 检查失败，构建必须失败
                        if [ "$HEALTH_CHECK_SUCCESS" != "true" ]; then
                            echo "❌ 健康检查失败，应用未完全启动或无法访问"
                            echo ""
                            echo "查看应用启动日志："
                            tail -200 ${TESTING_DIR}/app.log
                            echo ""
                            echo "检查进程状态："
                            if [ -f ${TESTING_DIR}/app.pid ]; then
                                PID=$(cat ${TESTING_DIR}/app.pid)
                                ps -p $PID > /dev/null 2>&1 && echo "进程 $PID 仍在运行" || echo "进程 $PID 已退出"
                            fi
                            echo ""
                            echo "检查端口监听状态："
                            netstat -tln 2>/dev/null | grep ${TESTING_PORT} || ss -tln 2>/dev/null | grep ${TESTING_PORT} || echo "端口 ${TESTING_PORT} 未监听"
                            echo ""
                            echo "诊断命令："
                            echo "  curl ${LOCALHOST_URL}/actuator/health"
                            echo "  curl ${LOCALHOST_URL}/api/test/hello"
                            exit 1
                        fi
                        
                        # localhost 检查成功后，尝试域名访问（用于验证 Nginx 配置，但不影响构建结果）
                        echo ""
                        echo "验证域名访问（不影响构建结果）..."
                        if [ -n "${TESTING_DOMAIN_PORT}" ]; then
                            DOMAIN_URL="http://${TESTING_DOMAIN}:${TESTING_DOMAIN_PORT}"
                            if curl -f -s --connect-timeout 5 "${DOMAIN_URL}/actuator/health" > /dev/null 2>&1; then
                                echo "✅ 域名访问成功: ${DOMAIN_URL}/actuator/health"
                            else
                                echo "⚠️  域名访问失败（但 localhost 访问成功）"
                                echo "   可能原因：Nginx 未配置或防火墙阻止外部访问"
                                echo "   建议：检查 Nginx 配置和防火墙规则"
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
                        
                        // 停止旧应用
                        sh '''
                        if [ -f ${PRODUCTION_DIR}/app.pid ]; then
                            PID=$(cat ${PRODUCTION_DIR}/app.pid)
                                if ps -p $PID > /dev/null 2>&1; then
                                    kill $PID || true
                                    sleep 5
                                    kill -9 $PID 2>/dev/null || true
                                fi
                            rm -f ${PRODUCTION_DIR}/app.pid
                            fi
                        '''
                        
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
                        
                        // 启动新应用
                        sh '''
                            cd ${PRODUCTION_DIR}
                        
                        # 加载环境变量（从 .env 文件或系统环境变量）
                        ENV_FILE="${PRODUCTION_DIR}/.env"
                        if [ -f "$ENV_FILE" ]; then
                            echo "从 .env 文件加载环境变量: $ENV_FILE"
                            # 读取 .env 文件并设置环境变量（兼容 sh）
                            set -a
                            . "$ENV_FILE" 2>/dev/null || {
                                echo "⚠️  加载 .env 文件失败，尝试手动读取..."
                                # 手动读取 .env 文件
                                while IFS='=' read -r key value; do
                                    # 跳过注释和空行
                                    case "$key" in
                                        '#'*) continue ;;
                                        '') continue ;;
                                    esac
                                    # 移除引号
                                    value=$(echo "$value" | sed "s/^['\\\"]//; s/['\\\"]$//")
                                    export "$key=$value"
                                done < "$ENV_FILE"
                            }
                            set +a
                        else
                            echo "⚠️  .env 文件不存在: $ENV_FILE，使用系统环境变量或默认值"
                        fi
                        
                        # 显式设置端口
                        export SERVER_PORT=${PRODUCTION_PORT}
                        
                        # 设置数据库连接（如果环境变量未设置，使用默认值）
                        export SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/mahjong_scoreboard_system}
                        export SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-yaohu}
                        
                        # 数据库密码必须设置（从 POSTGRES_PASSWORD 或 SPRING_DATASOURCE_PASSWORD）
                        if [ -z "$SPRING_DATASOURCE_PASSWORD" ] && [ -n "$POSTGRES_PASSWORD" ]; then
                            export SPRING_DATASOURCE_PASSWORD="$POSTGRES_PASSWORD"
                        fi
                        
                        # 如果仍然没有密码，使用默认值（仅用于测试，生产环境应该设置）
                        if [ -z "$SPRING_DATASOURCE_PASSWORD" ]; then
                            echo "⚠️  警告: SPRING_DATASOURCE_PASSWORD 未设置，使用默认值"
                            export SPRING_DATASOURCE_PASSWORD="cch815566"
                        fi
                        
                        # 设置微信配置（如果存在）
                        export WECHAT_APPID=${WECHAT_APPID:-}
                        export WECHAT_APPSECRET=${WECHAT_APPSECRET:-}
                        
                        echo "启动应用，端口: ${PRODUCTION_PORT}"
                        echo "数据库: ${SPRING_DATASOURCE_URL}"
                        echo "数据库用户: ${SPRING_DATASOURCE_USERNAME}"
                        if [ -n "$SPRING_DATASOURCE_PASSWORD" ]; then
                            echo "数据库密码: 已设置（隐藏）"
                        else
                            echo "⚠️  警告: 数据库密码未设置"
                        fi
                        
                        # 使用 env 命令确保环境变量被传递，并通过 -D 参数显式传递数据库配置
                        nohup env SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}" \
                            SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME}" \
                            SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD}" \
                            WECHAT_APPID="${WECHAT_APPID}" \
                            WECHAT_APPSECRET="${WECHAT_APPSECRET}" \
                            SERVER_PORT="${PRODUCTION_PORT}" \
                            java -jar \
                            -Dspring.profiles.active=production \
                            -Dserver.port=${PRODUCTION_PORT} \
                            -Dspring.datasource.url="${SPRING_DATASOURCE_URL}" \
                            -Dspring.datasource.username="${SPRING_DATASOURCE_USERNAME}" \
                            -Dspring.datasource.password="${SPRING_DATASOURCE_PASSWORD}" \
                            app.jar > app.log 2>&1 &
                            echo $! > app.pid
                        echo "应用已启动，PID: $(cat app.pid)"
                        '''
                        
                        // 等待应用启动
                        echo "等待应用启动..."
                        sh 'sleep 15'
                        
                        // 验证部署
                        sh '''
                        # 1. 检查进程是否存在
                        if [ -f ${PRODUCTION_DIR}/app.pid ]; then
                            PID=$(cat ${PRODUCTION_DIR}/app.pid)
                                if ps -p $PID > /dev/null 2>&1; then
                                echo "✅ 应用进程运行中，PID: $PID"
                                else
                                echo "❌ 应用进程不存在，查看日志："
                                tail -50 ${PRODUCTION_DIR}/app.log
                                exit 1
                            fi
                        else
                            echo "❌ PID 文件不存在，查看日志："
                            tail -50 ${PRODUCTION_DIR}/app.log
                                    exit 1
                                fi
                        
                        # 2. 检查端口是否监听
                        APP_PORT=${PRODUCTION_PORT}
                        if netstat -tln 2>/dev/null | grep -q ":$APP_PORT " || ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
                            echo "✅ 端口 $APP_PORT 正在监听"
                        else
                            echo "⚠️  端口 $APP_PORT 未监听，应用可能还在启动中..."
                        fi
                        
                        # 3. 检查 HTTP 健康检查端点（使用域名，最多重试 5 次）
                        echo "检查应用健康状态（通过域名访问）..."
                        MAX_RETRIES=5
                        RETRY_COUNT=0
                        HEALTH_CHECK_SUCCESS=false
                        
                        # 构建健康检查 URL（生产环境使用 HTTPS）
                        HEALTH_CHECK_URL="https://${PRODUCTION_DOMAIN}"
                        HEALTH_CHECK_HTTP_URL="http://${PRODUCTION_DOMAIN}"
                        TEST_API_URL="https://${PRODUCTION_DOMAIN}/api/test/hello"
                        
                        echo "健康检查地址（HTTPS）: ${HEALTH_CHECK_URL}"
                        echo "健康检查地址（HTTP）: ${HEALTH_CHECK_HTTP_URL}"
                        echo "测试接口地址: ${TEST_API_URL}"
                        
                        while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
                            sleep 3
                            RETRY_COUNT=$((RETRY_COUNT + 1))
                            echo "健康检查尝试 $RETRY_COUNT/$MAX_RETRIES..."
                            
                            # 优先使用 HTTPS 域名访问健康检查端点
                            if curl -f -s --connect-timeout 5 -k "${HEALTH_CHECK_URL}/actuator/health" > /dev/null 2>&1; then
                                echo "✅ 健康检查通过（HTTPS 域名访问）"
                                curl -s -k "${HEALTH_CHECK_URL}/actuator/health" | head -3
                                HEALTH_CHECK_SUCCESS=true
                                break
                            elif curl -f -s --connect-timeout 5 "${HEALTH_CHECK_HTTP_URL}/actuator/health" > /dev/null 2>&1; then
                                echo "✅ 健康检查通过（HTTP 域名访问）"
                                curl -s "${HEALTH_CHECK_HTTP_URL}/actuator/health" | head -3
                                HEALTH_CHECK_SUCCESS=true
                                break
                            elif curl -f -s --connect-timeout 5 -k "${TEST_API_URL}" > /dev/null 2>&1; then
                                echo "✅ 测试接口可访问（HTTPS 域名访问）"
                                curl -s -k "${TEST_API_URL}"
                                HEALTH_CHECK_SUCCESS=true
                                break
                            elif curl -f -s --connect-timeout 5 "${HEALTH_CHECK_HTTP_URL}/api/test/hello" > /dev/null 2>&1; then
                                echo "✅ 测试接口可访问（HTTP 域名访问）"
                                curl -s "${HEALTH_CHECK_HTTP_URL}/api/test/hello"
                                HEALTH_CHECK_SUCCESS=true
                                break
                            # 如果域名访问失败，回退到 localhost（用于诊断）
                            elif curl -f -s --connect-timeout 5 "http://localhost:${PRODUCTION_PORT}/actuator/health" > /dev/null 2>&1; then
                                echo "⚠️  域名访问失败，但 localhost 访问成功"
                                echo "   可能原因：Nginx 未配置或防火墙阻止外部访问"
                                echo "   建议：检查 Nginx 配置和防火墙规则"
                                HEALTH_CHECK_SUCCESS=true
                                break
                            elif curl -f -s --connect-timeout 5 "http://localhost:${PRODUCTION_PORT}/api/test/hello" > /dev/null 2>&1; then
                                echo "⚠️  域名访问失败，但 localhost 访问成功"
                                echo "   可能原因：Nginx 未配置或防火墙阻止外部访问"
                                HEALTH_CHECK_SUCCESS=true
                                break
                            fi
                        done
                        
                        if [ "$HEALTH_CHECK_SUCCESS" != "true" ]; then
                            echo "❌ 健康检查失败，应用可能未完全启动"
                            echo "查看应用日志："
                            tail -100 ${PRODUCTION_DIR}/app.log
                            echo ""
                            echo "检查端口监听状态："
                            netstat -tln 2>/dev/null | grep ${PRODUCTION_PORT} || ss -tln 2>/dev/null | grep ${PRODUCTION_PORT} || echo "端口 ${PRODUCTION_PORT} 未监听"
                            echo ""
                            echo "诊断信息："
                            echo "  HTTPS域名访问: curl -k ${HEALTH_CHECK_URL}/actuator/health"
                            echo "  HTTP域名访问: curl ${HEALTH_CHECK_HTTP_URL}/actuator/health"
                            echo "  本地访问: curl http://localhost:${PRODUCTION_PORT}/actuator/health"
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

