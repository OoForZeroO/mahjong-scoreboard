#!/bin/bash

# 诊断服务未运行问题

TESTING_DIR="/opt/yaohufox/testing"
TESTING_PORT="8082"

echo "=========================================="
echo "诊断服务未运行问题"
echo "=========================================="

# 1. 检查 PID 文件
echo -e "\n1. 检查 PID 文件："
if [ -f "${TESTING_DIR}/app.pid" ]; then
    PID=$(cat "${TESTING_DIR}/app.pid")
    echo "PID 文件存在: ${TESTING_DIR}/app.pid"
    echo "PID: $PID"
    
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "✅ 进程存在"
        ps -fp "$PID"
    else
        echo "❌ 进程不存在（PID 文件存在但进程已退出）"
        echo "   这说明服务启动后立即崩溃了"
    fi
else
    echo "❌ PID 文件不存在: ${TESTING_DIR}/app.pid"
    echo "   服务可能从未启动成功"
fi

# 2. 检查应用日志
echo -e "\n2. 检查应用日志（最后50行）："
if [ -f "${TESTING_DIR}/app.log" ]; then
    echo "日志文件: ${TESTING_DIR}/app.log"
    echo "文件大小: $(ls -lh "${TESTING_DIR}/app.log" | awk '{print $5}')"
    echo ""
    echo "=== 最后50行日志 ==="
    tail -50 "${TESTING_DIR}/app.log"
    echo ""
    
    # 检查错误
    echo "=== 错误信息 ==="
    grep -i "error\|exception\|failed\|fatal" "${TESTING_DIR}/app.log" | tail -20 || echo "未找到错误信息"
else
    echo "❌ 日志文件不存在: ${TESTING_DIR}/app.log"
fi

# 3. 检查 JAR 文件
echo -e "\n3. 检查 JAR 文件："
if [ -f "${TESTING_DIR}/app.jar" ]; then
    echo "✅ JAR 文件存在"
    ls -lh "${TESTING_DIR}/app.jar"
    
    # 检查文件是否可执行
    if [ -x "${TESTING_DIR}/app.jar" ]; then
        echo "✅ JAR 文件可执行"
    else
        echo "⚠️  JAR 文件不可执行（这通常不影响，因为用 java -jar 运行）"
    fi
else
    echo "❌ JAR 文件不存在: ${TESTING_DIR}/app.jar"
fi

# 4. 检查 Java 环境
echo -e "\n4. 检查 Java 环境："
if command -v java > /dev/null 2>&1; then
    echo "✅ Java 已安装"
    java -version 2>&1 | head -3
else
    echo "❌ Java 未安装或不在 PATH 中"
fi

# 5. 检查端口占用
echo -e "\n5. 检查端口占用："
if netstat -tln 2>/dev/null | grep -q ":$TESTING_PORT " || ss -tln 2>/dev/null | grep -q ":$TESTING_PORT "; then
    echo "⚠️  端口 $TESTING_PORT 被占用"
    echo "占用端口的进程："
    lsof -i :${TESTING_PORT} 2>/dev/null || netstat -tlnp 2>/dev/null | grep ":${TESTING_PORT}" || ss -tlnp 2>/dev/null | grep ":${TESTING_PORT}"
else
    echo "✅ 端口 $TESTING_PORT 未被占用"
fi

# 6. 检查环境变量文件
echo -e "\n6. 检查环境变量文件："
ENV_FILE="${TESTING_DIR}/.env"
if [ -f "$ENV_FILE" ]; then
    echo "✅ 环境变量文件存在: $ENV_FILE"
    echo "环境变量内容（隐藏敏感信息）："
    grep -v "PASSWORD\|SECRET" "$ENV_FILE" || echo "（文件为空或只包含敏感信息）"
else
    echo "⚠️  环境变量文件不存在: $ENV_FILE"
    echo "   服务可能使用默认配置或系统环境变量"
fi

# 7. 尝试手动启动（诊断模式）
echo -e "\n7. 尝试诊断启动："
echo "检查是否可以手动启动服务..."
cd "${TESTING_DIR}" || {
    echo "❌ 无法进入目录: ${TESTING_DIR}"
    exit 1
}

# 检查是否有旧的进程
OLD_PID=$(cat app.pid 2>/dev/null)
if [ -n "$OLD_PID" ] && ps -p "$OLD_PID" > /dev/null 2>&1; then
    echo "发现旧进程 $OLD_PID，先停止..."
    kill "$OLD_PID" 2>/dev/null
    sleep 2
fi

# 尝试启动并立即检查
echo "尝试启动服务（前台模式，5秒后检查）..."
timeout 5 java -jar \
    -Dspring.profiles.active=testing \
    -Dserver.port=${TESTING_PORT} \
    app.jar 2>&1 | head -30 &
START_PID=$!
sleep 5

if ps -p "$START_PID" > /dev/null 2>&1; then
    echo "✅ 服务可以启动"
    kill "$START_PID" 2>/dev/null
else
    echo "❌ 服务启动失败"
    echo "查看启动输出..."
fi

# 8. 检查数据库连接
echo -e "\n8. 检查数据库连接："
echo "测试数据库连接（测试环境）..."
if command -v psql > /dev/null 2>&1; then
    if psql -h localhost -p 5433 -U yaohu -d mahjong_scoreboard_system_test -c "SELECT 1;" > /dev/null 2>&1; then
        echo "✅ 数据库连接正常"
    else
        echo "❌ 数据库连接失败"
        echo "   可能原因：数据库未运行、密码错误、数据库不存在"
    fi
else
    echo "⚠️  psql 未安装，无法测试数据库连接"
fi

# 9. 检查磁盘空间
echo -e "\n9. 检查磁盘空间："
df -h "${TESTING_DIR}" | tail -1

# 10. 检查文件权限
echo -e "\n10. 检查文件权限："
ls -ld "${TESTING_DIR}"
ls -l "${TESTING_DIR}/app.jar" 2>/dev/null || echo "JAR 文件不存在"
ls -l "${TESTING_DIR}/app.log" 2>/dev/null || echo "日志文件不存在"

# 11. 总结和建议
echo -e "\n=========================================="
echo "诊断总结"
echo "=========================================="

if [ -f "${TESTING_DIR}/app.pid" ]; then
    PID=$(cat "${TESTING_DIR}/app.pid")
    if ! ps -p "$PID" > /dev/null 2>&1; then
        echo "❌ 问题确认：服务启动后立即退出"
        echo ""
        echo "可能原因："
        echo "  1. 应用启动时发生错误（查看日志）"
        echo "  2. 数据库连接失败"
        echo "  3. 端口被占用"
        echo "  4. 配置文件错误"
        echo "  5. 内存不足"
        echo ""
        echo "建议操作："
        echo "  1. 查看完整日志: tail -100 ${TESTING_DIR}/app.log"
        echo "  2. 检查数据库连接"
        echo "  3. 手动启动服务查看错误: cd ${TESTING_DIR} && java -jar app.jar"
        echo "  4. 检查 Jenkins 部署日志中的错误信息"
    fi
else
    echo "❌ 问题确认：服务从未启动"
    echo ""
    echo "可能原因："
    echo "  1. Jenkins 部署失败但误报成功"
    echo "  2. 启动脚本执行失败"
    echo "  3. 权限问题"
    echo ""
    echo "建议操作："
    echo "  1. 检查 Jenkins 部署日志"
    echo "  2. 手动执行启动命令测试"
fi

echo "=========================================="

