#!/bin/bash

# 修复测试环境数据库连接问题

echo "=========================================="
echo "修复测试环境数据库连接问题"
echo "=========================================="

TEST_DB_PORT="5433"
TEST_DB_NAME="mahjong_scoreboard_system_test"
TEST_DB_USER="yaohu"

# 1. 检查 PostgreSQL 服务状态
echo -e "\n1. 检查 PostgreSQL 服务状态："
if systemctl is-active --quiet postgresql || systemctl is-active --quiet postgresql@*; then
    echo "✅ PostgreSQL 服务运行中"
    systemctl status postgresql --no-pager -l | head -10 || systemctl status postgresql@* --no-pager -l | head -10
else
    echo "❌ PostgreSQL 服务未运行"
    echo "   尝试启动 PostgreSQL..."
    sudo systemctl start postgresql || sudo systemctl start postgresql@*
    sleep 3
    if systemctl is-active --quiet postgresql || systemctl is-active --quiet postgresql@*; then
        echo "✅ PostgreSQL 已启动"
    else
        echo "❌ PostgreSQL 启动失败"
        echo "   请手动检查 PostgreSQL 安装和配置"
    fi
fi

# 2. 检查 PostgreSQL 监听的端口
echo -e "\n2. 检查 PostgreSQL 监听的端口："
if netstat -tln 2>/dev/null | grep -q ":543" || ss -tln 2>/dev/null | grep -q ":543"; then
    echo "✅ PostgreSQL 正在监听端口："
    netstat -tln 2>/dev/null | grep ":543" || ss -tln 2>/dev/null | grep ":543"
    
    # 检查是否监听 5433
    if netstat -tln 2>/dev/null | grep -q ":5433 " || ss -tln 2>/dev/null | grep -q ":5433 "; then
        echo "✅ 端口 5433 正在监听"
    else
        echo "❌ 端口 5433 未监听"
        echo "   检查 PostgreSQL 配置..."
    fi
else
    echo "❌ PostgreSQL 未监听任何端口"
fi

# 3. 检查 PostgreSQL 配置
echo -e "\n3. 检查 PostgreSQL 配置："

# 查找 PostgreSQL 配置目录
PG_CONFIG_DIR=$(sudo -u postgres psql -t -c "SHOW config_file;" 2>/dev/null | xargs dirname 2>/dev/null || echo "")
if [ -z "$PG_CONFIG_DIR" ]; then
    # 尝试常见位置
    for dir in /etc/postgresql/*/main /var/lib/pgsql/*/data /usr/local/pgsql/data; do
        if [ -d "$dir" ]; then
            PG_CONFIG_DIR="$dir"
            break
        fi
    done
fi

if [ -n "$PG_CONFIG_DIR" ] && [ -d "$PG_CONFIG_DIR" ]; then
    echo "PostgreSQL 配置目录: $PG_CONFIG_DIR"
    
    # 检查 postgresql.conf
    if [ -f "$PG_CONFIG_DIR/postgresql.conf" ]; then
        echo "检查端口配置："
        grep -E "^port\s*=" "$PG_CONFIG_DIR/postgresql.conf" || echo "未找到端口配置"
        
        echo "检查监听地址："
        grep -E "^listen_addresses\s*=" "$PG_CONFIG_DIR/postgresql.conf" || echo "未找到监听地址配置"
    fi
    
    # 检查 pg_hba.conf
    if [ -f "$PG_CONFIG_DIR/pg_hba.conf" ]; then
        echo "检查访问控制配置（pg_hba.conf）："
        grep -v "^#" "$PG_CONFIG_DIR/pg_hba.conf" | grep -v "^$" | tail -5 || echo "未找到访问控制配置"
    fi
else
    echo "⚠️  无法找到 PostgreSQL 配置目录"
fi

# 4. 测试数据库连接
echo -e "\n4. 测试数据库连接："

# 测试默认端口 5432
echo "测试端口 5432："
if psql -h localhost -p 5432 -U "$TEST_DB_USER" -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ 端口 5432 连接成功"
    DEFAULT_PORT_WORKS=true
else
    echo "❌ 端口 5432 连接失败"
    DEFAULT_PORT_WORKS=false
fi

# 测试端口 5433
echo "测试端口 5433："
if psql -h localhost -p 5433 -U "$TEST_DB_USER" -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ 端口 5433 连接成功"
    PORT_5433_WORKS=true
else
    echo "❌ 端口 5433 连接失败"
    PORT_5433_WORKS=false
fi

# 5. 检查数据库是否存在
echo -e "\n5. 检查测试数据库："
if [ "$DEFAULT_PORT_WORKS" = true ]; then
    if psql -h localhost -p 5432 -U "$TEST_DB_USER" -d "$TEST_DB_NAME" -c "SELECT 1;" > /dev/null 2>&1; then
        echo "✅ 测试数据库存在（端口 5432）"
    else
        echo "❌ 测试数据库不存在（端口 5432）"
        echo "   需要创建数据库："
        echo "   createdb -h localhost -p 5432 -U $TEST_DB_USER $TEST_DB_NAME"
    fi
fi

# 6. 提供解决方案
echo -e "\n=========================================="
echo "解决方案"
echo "=========================================="

if [ "$PORT_5433_WORKS" = false ]; then
    if [ "$DEFAULT_PORT_WORKS" = true ]; then
        echo "✅ 解决方案：使用默认端口 5432"
        echo ""
        echo "PostgreSQL 运行在默认端口 5432，但应用配置使用 5433。"
        echo "有两个选择："
        echo ""
        echo "方案1：修改应用配置使用端口 5432（推荐）"
        echo "  修改 application-testing.yml："
        echo "    spring.datasource.url: jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test"
        echo ""
        echo "方案2：配置 PostgreSQL 监听端口 5433"
        echo "  1. 编辑 postgresql.conf："
        echo "     port = 5433"
        echo "  2. 重启 PostgreSQL："
        echo "     sudo systemctl restart postgresql"
    else
        echo "❌ PostgreSQL 服务未正常运行"
        echo ""
        echo "请执行："
        echo "  1. 启动 PostgreSQL："
        echo "     sudo systemctl start postgresql"
        echo "  2. 检查 PostgreSQL 状态："
        echo "     sudo systemctl status postgresql"
        echo "  3. 如果启动失败，查看日志："
        echo "     sudo journalctl -u postgresql -n 50"
    fi
else
    echo "✅ 端口 5433 连接正常"
    echo "   问题可能是数据库不存在或用户权限问题"
fi

echo "=========================================="

