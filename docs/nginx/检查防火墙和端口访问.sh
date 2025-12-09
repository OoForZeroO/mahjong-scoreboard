#!/bin/bash

# 检查防火墙和端口访问配置

TESTING_PORT="8082"
PRODUCTION_PORT="8081"
DOMAIN="yaohufox.com"

echo "=========================================="
echo "防火墙和端口访问检查"
echo "=========================================="

# 1. 检查端口监听状态
echo -e "\n1. 检查端口监听状态："
echo "测试环境端口 $TESTING_PORT："
if netstat -tln 2>/dev/null | grep -q ":$TESTING_PORT " || ss -tln 2>/dev/null | grep -q ":$TESTING_PORT "; then
    echo "✅ 端口正在监听"
    netstat -tln 2>/dev/null | grep ":$TESTING_PORT " || ss -tln 2>/dev/null | grep ":$TESTING_PORT "
    
    # 检查监听地址
    LISTEN_ADDR=$(netstat -tln 2>/dev/null | grep ":$TESTING_PORT " | awk '{print $4}' || \
                  ss -tln 2>/dev/null | grep ":$TESTING_PORT " | awk '{print $4}')
    if echo "$LISTEN_ADDR" | grep -q "0.0.0.0\|::"; then
        echo "✅ 监听所有网络接口（0.0.0.0），外部可访问"
    elif echo "$LISTEN_ADDR" | grep -q "127.0.0.1"; then
        echo "⚠️  只监听本地（127.0.0.1），外部无法访问"
        echo "   需要修改应用配置，监听 0.0.0.0"
    fi
else
    echo "❌ 端口未监听"
fi

echo -e "\n生产环境端口 $PRODUCTION_PORT："
if netstat -tln 2>/dev/null | grep -q ":$PRODUCTION_PORT " || ss -tln 2>/dev/null | grep -q ":$PRODUCTION_PORT "; then
    echo "✅ 端口正在监听"
    netstat -tln 2>/dev/null | grep ":$PRODUCTION_PORT " || ss -tln 2>/dev/null | grep ":$PRODUCTION_PORT "
else
    echo "❌ 端口未监听"
fi

# 2. 检查防火墙状态
echo -e "\n2. 检查防火墙状态："

# UFW (Ubuntu/Debian)
if command -v ufw > /dev/null 2>&1; then
    echo "检测到 UFW 防火墙："
    UFW_STATUS=$(ufw status | head -1)
    echo "$UFW_STATUS"
    
    if echo "$UFW_STATUS" | grep -q "inactive\|未激活"; then
        echo "✅ 防火墙未激活，端口应该可以访问"
    else
        echo "检查端口规则："
        if ufw status | grep -q "$TESTING_PORT"; then
            echo "✅ 测试环境端口 $TESTING_PORT 已在防火墙规则中"
            ufw status | grep "$TESTING_PORT"
        else
            echo "❌ 测试环境端口 $TESTING_PORT 未在防火墙规则中"
            echo "   需要执行：ufw allow $TESTING_PORT/tcp"
        fi
        
        if ufw status | grep -q "$PRODUCTION_PORT"; then
            echo "✅ 生产环境端口 $PRODUCTION_PORT 已在防火墙规则中"
        else
            echo "⚠️  生产环境端口 $PRODUCTION_PORT 未在防火墙规则中"
        fi
    fi
# Firewalld (CentOS/RHEL)
elif command -v firewall-cmd > /dev/null 2>&1; then
    echo "检测到 Firewalld 防火墙："
    if firewall-cmd --state 2>/dev/null | grep -q "running"; then
        echo "✅ 防火墙运行中"
        echo "开放的端口："
        firewall-cmd --list-ports
        
        if firewall-cmd --list-ports 2>/dev/null | grep -q "$TESTING_PORT"; then
            echo "✅ 测试环境端口 $TESTING_PORT 已开放"
        else
            echo "❌ 测试环境端口 $TESTING_PORT 未开放"
            echo "   需要执行：firewall-cmd --permanent --add-port=$TESTING_PORT/tcp && firewall-cmd --reload"
        fi
    else
        echo "⚠️  防火墙未运行"
    fi
# iptables
elif command -v iptables > /dev/null 2>&1; then
    echo "检测到 iptables："
    if iptables -L -n | grep -q "$TESTING_PORT"; then
        echo "✅ 找到端口 $TESTING_PORT 的规则"
        iptables -L -n | grep "$TESTING_PORT"
    else
        echo "⚠️  未找到端口 $TESTING_PORT 的规则"
        echo "   检查 iptables 规则："
        iptables -L -n | head -20
    fi
else
    echo "⚠️  未检测到常见的防火墙工具"
fi

# 3. 测试本地访问
echo -e "\n3. 测试本地访问："
echo "测试环境："
if curl -f -s --connect-timeout 3 "http://localhost:$TESTING_PORT/api/test/hello" > /dev/null 2>&1; then
    echo "✅ 本地访问成功"
    curl -s "http://localhost:$TESTING_PORT/api/test/hello"
else
    echo "❌ 本地访问失败"
fi

# 4. 测试域名访问
echo -e "\n4. 测试域名访问："
echo "测试环境（域名+端口）："
if curl -f -s --connect-timeout 5 "http://${DOMAIN}:$TESTING_PORT/api/test/hello" > /dev/null 2>&1; then
    echo "✅ 域名访问成功"
    curl -s "http://${DOMAIN}:$TESTING_PORT/api/test/hello"
else
    ERROR_CODE=$?
    echo "❌ 域名访问失败（错误代码: $ERROR_CODE）"
    
    if [ $ERROR_CODE -eq 6 ]; then
        echo "   原因：DNS 解析失败"
    elif [ $ERROR_CODE -eq 7 ]; then
        echo "   原因：连接被拒绝，可能是防火墙阻止"
    elif [ $ERROR_CODE -eq 28 ]; then
        echo "   原因：连接超时，可能是防火墙阻止或服务未监听外部 IP"
    fi
fi

# 5. 获取服务器公网 IP
echo -e "\n5. 服务器网络信息："
echo "公网 IP："
PUBLIC_IP=$(curl -s ifconfig.me 2>/dev/null || curl -s ipinfo.io/ip 2>/dev/null || echo "无法获取")
echo "$PUBLIC_IP"

echo -e "\n内网 IP："
hostname -I 2>/dev/null || ip addr show | grep "inet " | grep -v "127.0.0.1"

# 6. 提供解决方案
echo -e "\n=========================================="
echo "解决方案建议"
echo "=========================================="

if curl -f -s --connect-timeout 3 "http://localhost:$TESTING_PORT/api/test/hello" > /dev/null 2>&1; then
    if ! curl -f -s --connect-timeout 5 "http://${DOMAIN}:$TESTING_PORT/api/test/hello" > /dev/null 2>&1; then
        echo ""
        echo "✅ 服务正常运行，但外部无法访问"
        echo ""
        echo "执行以下命令开放端口："
        echo ""
        if command -v ufw > /dev/null 2>&1; then
            echo "  # Ubuntu/Debian"
            echo "  sudo ufw allow $TESTING_PORT/tcp"
            echo "  sudo ufw reload"
        elif command -v firewall-cmd > /dev/null 2>&1; then
            echo "  # CentOS/RHEL"
            echo "  sudo firewall-cmd --permanent --add-port=$TESTING_PORT/tcp"
            echo "  sudo firewall-cmd --reload"
        fi
        echo ""
        echo "然后再次测试："
        echo "  curl http://${DOMAIN}:$TESTING_PORT/api/test/hello"
    else
        echo "✅ 服务正常运行且外部可以访问"
    fi
else
    echo "❌ 服务未运行，请先启动服务"
fi

echo -e "\n=========================================="

