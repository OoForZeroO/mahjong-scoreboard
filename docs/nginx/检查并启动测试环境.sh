#!/bin/bash

# 检查并启动测试环境服务

TESTING_DIR="/opt/yaohufox/testing"
TESTING_PORT="8082"

echo "=========================================="
echo "测试环境服务诊断和启动"
echo "=========================================="

# 1. 检查服务是否运行
echo -e "\n1. 检查服务状态："
if [ -f "${TESTING_DIR}/app.pid" ]; then
    PID=$(cat "${TESTING_DIR}/app.pid")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "✅ 服务正在运行，PID: $PID"
        echo "进程信息："
        ps -fp "$PID"
        
        # 检查端口
        if netstat -tln 2>/dev/null | grep -q ":$TESTING_PORT " || ss -tln 2>/dev/null | grep -q ":$TESTING_PORT "; then
            echo "✅ 端口 $TESTING_PORT 正在监听"
        else
            echo "⚠️  端口 $TESTING_PORT 未监听"
        fi
        
        # 测试接口
        echo -e "\n测试接口访问："
        if curl -f -s "http://localhost:${TESTING_PORT}/actuator/health" > /dev/null 2>&1; then
            echo "✅ 健康检查通过"
            curl -s "http://localhost:${TESTING_PORT}/actuator/health"
        elif curl -f -s "http://localhost:${TESTING_PORT}/api/test/hello" > /dev/null 2>&1; then
            echo "✅ 测试接口可访问"
            curl -s "http://localhost:${TESTING_PORT}/api/test/hello"
        else
            echo "❌ 接口无法访问"
        fi
        
        exit 0
    else
        echo "⚠️  PID 文件存在但进程不存在，清理 PID 文件"
        rm -f "${TESTING_DIR}/app.pid"
    fi
else
    echo "❌ 服务未运行（PID 文件不存在）"
fi

# 2. 检查 JAR 文件是否存在
echo -e "\n2. 检查应用文件："
if [ ! -f "${TESTING_DIR}/app.jar" ]; then
    echo "❌ JAR 文件不存在: ${TESTING_DIR}/app.jar"
    echo "请先通过 Jenkins 部署应用"
    exit 1
else
    echo "✅ JAR 文件存在"
    ls -lh "${TESTING_DIR}/app.jar"
fi

# 3. 检查端口是否被占用
echo -e "\n3. 检查端口占用："
if netstat -tln 2>/dev/null | grep -q ":$TESTING_PORT " || ss -tln 2>/dev/null | grep -q ":$TESTING_PORT "; then
    echo "⚠️  端口 $TESTING_PORT 已被占用"
    echo "占用端口的进程："
    lsof -i :${TESTING_PORT} 2>/dev/null || netstat -tlnp 2>/dev/null | grep ":${TESTING_PORT}" || ss -tlnp 2>/dev/null | grep ":${TESTING_PORT}"
    echo ""
    read -p "是否要停止占用端口的进程？(y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        PID_TO_KILL=$(lsof -ti :${TESTING_PORT} 2>/dev/null || netstat -tlnp 2>/dev/null | grep ":${TESTING_PORT}" | awk '{print $7}' | cut -d'/' -f1 | head -1)
        if [ -n "$PID_TO_KILL" ]; then
            echo "停止进程 $PID_TO_KILL"
            kill "$PID_TO_KILL" 2>/dev/null || kill -9 "$PID_TO_KILL" 2>/dev/null
            sleep 2
        fi
    else
        echo "取消启动"
        exit 1
    fi
else
    echo "✅ 端口 $TESTING_PORT 可用"
fi

# 4. 检查环境变量文件
echo -e "\n4. 检查环境配置："
ENV_FILE="${TESTING_DIR}/.env"
if [ -f "$ENV_FILE" ]; then
    echo "✅ 找到环境变量文件: $ENV_FILE"
    echo "环境变量内容（隐藏敏感信息）："
    grep -v "PASSWORD\|SECRET" "$ENV_FILE" || cat "$ENV_FILE"
else
    echo "⚠️  环境变量文件不存在: $ENV_FILE"
    echo "将使用默认配置"
fi

# 5. 查看最近的日志
echo -e "\n5. 最近的日志（最后20行）："
if [ -f "${TESTING_DIR}/app.log" ]; then
    tail -20 "${TESTING_DIR}/app.log"
else
    echo "日志文件不存在"
fi

# 6. 启动服务
echo -e "\n6. 启动服务..."
cd "${TESTING_DIR}" || exit 1

# 加载环境变量
if [ -f "$ENV_FILE" ]; then
    echo "加载环境变量..."
    set -a
    . "$ENV_FILE" 2>/dev/null || {
        # 手动读取 .env 文件
        while IFS='=' read -r key value; do
            case "$key" in
                '#'*) continue ;;
                '') continue ;;
            esac
            value=$(echo "$value" | sed "s/^['\"]//; s/['\"]$//")
            export "$key=$value"
        done < "$ENV_FILE"
    }
    set +a
fi

# 设置默认值
export SERVER_PORT=${TESTING_PORT}
export SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5433/mahjong_scoreboard_system_test}
export SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-yaohu}
export SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-cch815566}

echo "启动参数："
echo "  端口: ${TESTING_PORT}"
echo "  数据库: ${SPRING_DATASOURCE_URL}"
echo "  数据库用户: ${SPRING_DATASOURCE_USERNAME}"
echo "  数据库密码: 已设置（隐藏）"

# 启动应用
echo -e "\n正在启动应用..."
nohup env SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL}" \
    SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME}" \
    SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD}" \
    WECHAT_APPID="${WECHAT_APPID:-}" \
    WECHAT_APPSECRET="${WECHAT_APPSECRET:-}" \
    SERVER_PORT="${TESTING_PORT}" \
    java -jar \
    -Dspring.profiles.active=testing \
    -Dserver.port=${TESTING_PORT} \
    -Dspring.datasource.url="${SPRING_DATASOURCE_URL}" \
    -Dspring.datasource.username="${SPRING_DATASOURCE_USERNAME}" \
    -Dspring.datasource.password="${SPRING_DATASOURCE_PASSWORD}" \
    app.jar > app.log 2>&1 &

echo $! > app.pid
echo "✅ 应用已启动，PID: $(cat app.pid)"

# 7. 等待并验证
echo -e "\n7. 等待应用启动（15秒）..."
sleep 15

# 检查进程
if ps -p "$(cat app.pid)" > /dev/null 2>&1; then
    echo "✅ 进程运行中"
else
    echo "❌ 进程已退出，查看日志："
    tail -50 app.log
    exit 1
fi

# 检查端口
if netstat -tln 2>/dev/null | grep -q ":$TESTING_PORT " || ss -tln 2>/dev/null | grep -q ":$TESTING_PORT "; then
    echo "✅ 端口正在监听"
else
    echo "⚠️  端口未监听，应用可能还在启动中"
fi

# 测试接口
echo -e "\n8. 测试接口访问："
MAX_RETRIES=5
RETRY_COUNT=0
SUCCESS=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    sleep 3
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "尝试 $RETRY_COUNT/$MAX_RETRIES..."
    
    if curl -f -s "http://localhost:${TESTING_PORT}/actuator/health" > /dev/null 2>&1; then
        echo "✅ 健康检查通过"
        curl -s "http://localhost:${TESTING_PORT}/actuator/health"
        SUCCESS=true
        break
    elif curl -f -s "http://localhost:${TESTING_PORT}/api/test/hello" > /dev/null 2>&1; then
        echo "✅ 测试接口可访问"
        curl -s "http://localhost:${TESTING_PORT}/api/test/hello"
        SUCCESS=true
        break
    fi
done

if [ "$SUCCESS" != "true" ]; then
    echo "❌ 接口无法访问，查看日志："
    tail -50 app.log
    exit 1
fi

echo -e "\n=========================================="
echo "✅ 测试环境服务启动成功！"
echo "=========================================="
echo "访问地址: http://localhost:${TESTING_PORT}"
echo "健康检查: http://localhost:${TESTING_PORT}/actuator/health"
echo "测试接口: http://localhost:${TESTING_PORT}/api/test/hello"
echo "PID: $(cat app.pid)"
echo "日志文件: ${TESTING_DIR}/app.log"
echo "=========================================="

