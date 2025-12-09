#!/bin/bash

# 快速检查测试环境服务状态

TESTING_DIR="/opt/yaohufox/testing"
TESTING_PORT="8082"

echo "=========================================="
echo "测试环境服务快速检查"
echo "=========================================="

# 检查进程
echo -e "\n1. 进程状态："
if [ -f "${TESTING_DIR}/app.pid" ]; then
    PID=$(cat "${TESTING_DIR}/app.pid")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "✅ 服务运行中，PID: $PID"
        ps -fp "$PID" | tail -1
    else
        echo "❌ 进程不存在（PID 文件存在但进程已退出）"
    fi
else
    echo "❌ 服务未运行（PID 文件不存在）"
fi

# 检查端口
echo -e "\n2. 端口状态："
if netstat -tln 2>/dev/null | grep -q ":$TESTING_PORT " || ss -tln 2>/dev/null | grep -q ":$TESTING_PORT "; then
    echo "✅ 端口 $TESTING_PORT 正在监听"
    netstat -tln 2>/dev/null | grep ":$TESTING_PORT " || ss -tln 2>/dev/null | grep ":$TESTING_PORT "
else
    echo "❌ 端口 $TESTING_PORT 未监听"
fi

# 检查文件
echo -e "\n3. 应用文件："
if [ -f "${TESTING_DIR}/app.jar" ]; then
    echo "✅ JAR 文件存在"
    ls -lh "${TESTING_DIR}/app.jar"
else
    echo "❌ JAR 文件不存在"
fi

# 测试接口
echo -e "\n4. 接口测试："
if curl -f -s "http://localhost:${TESTING_PORT}/actuator/health" > /dev/null 2>&1; then
    echo "✅ 健康检查通过"
    curl -s "http://localhost:${TESTING_PORT}/actuator/health" | head -3
elif curl -f -s "http://localhost:${TESTING_PORT}/api/test/hello" > /dev/null 2>&1; then
    echo "✅ 测试接口可访问"
    curl -s "http://localhost:${TESTING_PORT}/api/test/hello"
else
    echo "❌ 接口无法访问"
    echo "   错误: Connection refused"
fi

# 查看日志（最后5行）
echo -e "\n5. 最近日志（最后5行）："
if [ -f "${TESTING_DIR}/app.log" ]; then
    tail -5 "${TESTING_DIR}/app.log"
else
    echo "日志文件不存在"
fi

echo -e "\n=========================================="
echo "如需启动服务，请执行："
echo "  bash docs/nginx/检查并启动测试环境.sh"
echo "=========================================="

