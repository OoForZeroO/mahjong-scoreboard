#!/bin/bash

# 正确启动测试环境服务

TESTING_DIR="/opt/yaohufox/testing"
TESTING_PORT="8082"

echo "=========================================="
echo "启动测试环境服务"
echo "=========================================="

cd "$TESTING_DIR" || {
    echo "❌ 无法进入目录: $TESTING_DIR"
    exit 1
}

# 1. 停止旧进程
echo -e "\n1. 停止旧进程..."
if [ -f app.pid ]; then
    OLD_PID=$(cat app.pid)
    if ps -p "$OLD_PID" > /dev/null 2>&1; then
        echo "停止进程 $OLD_PID"
        kill "$OLD_PID" 2>/dev/null
        sleep 3
        if ps -p "$OLD_PID" > /dev/null 2>&1; then
            echo "强制停止进程"
            kill -9 "$OLD_PID" 2>/dev/null
        fi
    fi
    rm -f app.pid
fi

# 2. 检查 JAR 文件
echo -e "\n2. 检查 JAR 文件..."
if [ ! -f app.jar ]; then
    echo "❌ JAR 文件不存在: app.jar"
    echo "   请先通过 Jenkins 部署或复制 JAR 文件"
    exit 1
fi
echo "✅ JAR 文件存在"
ls -lh app.jar

# 3. 检查数据库连接
echo -e "\n3. 检查数据库连接..."
if psql -h localhost -p 5432 -U yaohu -d mahjong_scoreboard_system_test -c "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ 数据库连接正常（端口 5432）"
else
    echo "❌ 数据库连接失败（端口 5432）"
    echo "   请检查："
    echo "   1. PostgreSQL 是否运行"
    echo "   2. 数据库是否存在：createdb -h localhost -p 5432 -U yaohu mahjong_scoreboard_system_test"
    echo "   3. 用户权限是否正确"
    read -p "是否继续启动？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 4. 检查端口占用
echo -e "\n4. 检查端口占用..."
if netstat -tln 2>/dev/null | grep -q ":$TESTING_PORT " || ss -tln 2>/dev/null | grep -q ":$TESTING_PORT "; then
    echo "⚠️  端口 $TESTING_PORT 被占用"
    OCCUPIED_PID=$(lsof -ti :${TESTING_PORT} 2>/dev/null || \
                   netstat -tlnp 2>/dev/null | grep ":${TESTING_PORT}" | awk '{print $7}' | cut -d'/' -f1 | head -1)
    if [ -n "$OCCUPIED_PID" ]; then
        echo "占用进程: $OCCUPIED_PID"
        read -p "是否停止占用端口的进程？(y/n) " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            kill "$OCCUPIED_PID" 2>/dev/null || kill -9 "$OCCUPIED_PID" 2>/dev/null
            sleep 2
        else
            echo "取消启动"
            exit 1
        fi
    fi
else
    echo "✅ 端口 $TESTING_PORT 可用"
fi

# 5. 启动服务
echo -e "\n5. 启动服务..."
echo "启动参数："
echo "  端口: $TESTING_PORT"
echo "  监听地址: 0.0.0.0（所有网络接口）"
echo "  数据库: jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test"
echo "  数据库用户: yaohu"
echo ""

# 关键：使用正确的数据库端口 5432，并监听 0.0.0.0
nohup java -jar \
  -Dspring.profiles.active=testing \
  -Dserver.port=${TESTING_PORT} \
  -Dserver.address=0.0.0.0 \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test \
  -Dspring.datasource.username=yaohu \
  -Dspring.datasource.password=cch815566 \
  app.jar > app.log 2>&1 &

echo $! > app.pid
echo "✅ 服务已启动，PID: $(cat app.pid)"

# 6. 等待启动
echo -e "\n6. 等待服务启动（15秒）..."
sleep 15

# 7. 验证启动
echo -e "\n7. 验证服务状态..."

# 检查进程
if ps -p "$(cat app.pid)" > /dev/null 2>&1; then
    echo "✅ 进程运行中"
    ps -fp "$(cat app.pid)" | tail -1
else
    echo "❌ 进程已退出"
    echo "查看日志："
    tail -30 app.log
    exit 1
fi

# 检查端口
if netstat -tln 2>/dev/null | grep -q ":$TESTING_PORT " || ss -tln 2>/dev/null | grep -q ":$TESTING_PORT "; then
    echo "✅ 端口正在监听"
    LISTEN_INFO=$(netstat -tlnp 2>/dev/null | grep ":$TESTING_PORT " || ss -tlnp 2>/dev/null | grep ":$TESTING_PORT ")
    echo "$LISTEN_INFO"
    
    # 检查监听地址
    if echo "$LISTEN_INFO" | grep -q "0.0.0.0\|::"; then
        echo "✅ 监听所有网络接口（外部可访问）"
    else
        echo "⚠️  只监听本地接口（外部无法访问）"
    fi
else
    echo "⚠️  端口未监听，服务可能还在启动中"
fi

# 测试接口
echo -e "\n8. 测试接口访问..."
MAX_RETRIES=5
RETRY_COUNT=0
SUCCESS=false

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    sleep 3
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "尝试 $RETRY_COUNT/$MAX_RETRIES..."
    
    if curl -f -s "http://localhost:${TESTING_PORT}/api/test/hello" > /dev/null 2>&1; then
        echo "✅ 接口访问成功"
        curl -s "http://localhost:${TESTING_PORT}/api/test/hello"
        SUCCESS=true
        break
    fi
done

if [ "$SUCCESS" != "true" ]; then
    echo "❌ 接口无法访问"
    echo "查看日志："
    tail -50 app.log
    exit 1
fi

echo -e "\n=========================================="
echo "✅ 服务启动成功！"
echo "=========================================="
echo "访问地址:"
echo "  本地: http://localhost:${TESTING_PORT}"
echo "  外部: http://yaohufox.com:${TESTING_PORT}"
echo ""
echo "测试接口:"
echo "  http://localhost:${TESTING_PORT}/api/test/hello"
echo "  http://yaohufox.com:${TESTING_PORT}/api/test/hello"
echo ""
echo "PID: $(cat app.pid)"
echo "日志: ${TESTING_DIR}/app.log"
echo "=========================================="

