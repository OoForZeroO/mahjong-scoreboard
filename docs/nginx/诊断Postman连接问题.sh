#!/bin/bash

# Postman 连接问题诊断脚本

echo "=========================================="
echo "Postman 连接问题诊断"
echo "=========================================="

TESTING_PORT="8082"
DOMAIN="test.yaohufox.com"

# 1. 检查服务状态
echo -e "\n1. 检查测试环境服务："
if ps aux | grep java | grep "$TESTING_PORT" | grep -v grep > /dev/null 2>&1; then
    echo "✅ 服务运行中"
    ps aux | grep java | grep "$TESTING_PORT" | grep -v grep | head -1
else
    echo "❌ 服务未运行"
    echo "   请先启动测试环境服务"
fi

# 2. 检查端口监听
echo -e "\n2. 检查端口监听："
if netstat -tln 2>/dev/null | grep -q ":$TESTING_PORT " || ss -tln 2>/dev/null | grep -q ":$TESTING_PORT "; then
    echo "✅ 端口 $TESTING_PORT 正在监听"
    LISTEN_INFO=$(netstat -tlnp 2>/dev/null | grep ":$TESTING_PORT " || ss -tlnp 2>/dev/null | grep ":$TESTING_PORT ")
    echo "$LISTEN_INFO"
    
    # 检查监听地址
    LISTEN_ADDR=$(echo "$LISTEN_INFO" | awk '{print $4}' | head -1)
    if echo "$LISTEN_ADDR" | grep -q "0.0.0.0\|::"; then
        echo "✅ 监听所有网络接口（0.0.0.0），外部可访问"
    elif echo "$LISTEN_ADDR" | grep -q "127.0.0.1"; then
        echo "❌ 只监听本地（127.0.0.1），外部无法访问！"
        echo "   需要修改应用配置，监听 0.0.0.0"
    fi
else
    echo "❌ 端口 $TESTING_PORT 未监听"
fi

# 3. 测试本地访问
echo -e "\n3. 测试本地访问："
if curl -f -s "http://localhost:$TESTING_PORT/api/test/hello" > /dev/null 2>&1; then
    echo "✅ 本地访问成功"
    curl -s "http://localhost:$TESTING_PORT/api/test/hello" | head -3
else
    echo "❌ 本地访问失败"
    echo "   服务可能未正常运行或配置错误"
fi

# 4. 检查 DNS
echo -e "\n4. 检查 DNS 配置："
if nslookup "$DOMAIN" > /dev/null 2>&1; then
    echo "✅ DNS 解析成功"
    RESOLVED_IP=$(nslookup "$DOMAIN" | grep -A 2 "Name:" | grep "Address:" | tail -1 | awk '{print $2}')
    SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || curl -s ipinfo.io/ip 2>/dev/null || echo "")
    echo "解析的 IP: $RESOLVED_IP"
    if [ -n "$SERVER_IP" ]; then
        echo "服务器 IP: $SERVER_IP"
        if [ "$RESOLVED_IP" = "$SERVER_IP" ]; then
            echo "✅ DNS 解析的 IP 与服务器 IP 一致"
        else
            echo "⚠️  DNS 解析的 IP 与服务器 IP 不一致"
        fi
    fi
else
    echo "❌ DNS 解析失败"
    echo "   需要配置 DNS A 记录：$DOMAIN → 服务器IP"
fi

# 5. 测试域名访问
echo -e "\n5. 测试域名访问："
echo "测试: http://${DOMAIN}:${TESTING_PORT}/api/test/hello"
if curl -f -s --connect-timeout 5 "http://${DOMAIN}:${TESTING_PORT}/api/test/hello" > /dev/null 2>&1; then
    echo "✅ 域名访问成功"
    curl -s "http://${DOMAIN}:${TESTING_PORT}/api/test/hello" | head -3
else
    ERROR=$?
    echo "❌ 域名访问失败（错误代码: $ERROR）"
    case $ERROR in
        6)
            echo "   原因：DNS 解析失败"
            ;;
        7)
            echo "   原因：连接被拒绝（可能是防火墙或服务未监听外部 IP）"
            ;;
        28)
            echo "   原因：连接超时（可能是防火墙阻止）"
            ;;
        56)
            echo "   原因：连接重置（ECONNRESET）"
            echo "   可能原因："
            echo "     - 防火墙或安全组规则阻止"
            echo "     - 服务只监听本地，不监听外部 IP"
            echo "     - 云服务商安全组未配置"
            ;;
        *)
            echo "   未知错误"
            ;;
    esac
fi

# 6. 检查防火墙
echo -e "\n6. 检查防火墙："
if command -v ufw > /dev/null 2>&1; then
    echo "检测到 UFW 防火墙："
    if ufw status | grep -q "$TESTING_PORT"; then
        echo "✅ 端口 $TESTING_PORT 已在防火墙规则中"
        ufw status | grep "$TESTING_PORT"
    else
        echo "❌ 端口 $TESTING_PORT 未在防火墙规则中"
        echo "   执行: sudo ufw allow $TESTING_PORT/tcp"
    fi
elif command -v firewall-cmd > /dev/null 2>&1; then
    echo "检测到 Firewalld 防火墙："
    if firewall-cmd --list-ports 2>/dev/null | grep -q "$TESTING_PORT"; then
        echo "✅ 端口 $TESTING_PORT 已开放"
    else
        echo "❌ 端口 $TESTING_PORT 未开放"
        echo "   执行: sudo firewall-cmd --permanent --add-port=$TESTING_PORT/tcp && sudo firewall-cmd --reload"
    fi
else
    echo "⚠️  未检测到常见的防火墙工具"
    echo "   请检查云服务商安全组规则"
fi

# 7. 检查云服务商安全组（提示）
echo -e "\n7. 云服务商安全组检查："
echo "⚠️  重要：请检查云服务商（阿里云/腾讯云等）安全组规则"
echo "   需要确保入方向规则允许端口 $TESTING_PORT"
echo "   协议：TCP"
echo "   源：0.0.0.0/0（或特定 IP）"

# 8. Postman 配置建议
echo -e "\n8. Postman 配置建议："
echo "在 Postman 中设置 base_url 为："
if nslookup "$DOMAIN" > /dev/null 2>&1; then
    echo "  http://${DOMAIN}:${TESTING_PORT}"
else
    echo "  ⚠️  DNS 未配置，临时使用："
    SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || echo "服务器IP")
    echo "  http://${SERVER_IP}:${TESTING_PORT}"
    echo "  或配置 DNS 后使用：http://${DOMAIN}:${TESTING_PORT}"
fi

# 9. 总结和建议
echo -e "\n=========================================="
echo "诊断总结"
echo "=========================================="

ISSUES=0

if ! ps aux | grep java | grep "$TESTING_PORT" | grep -v grep > /dev/null 2>&1; then
    echo "❌ 问题1：服务未运行"
    ISSUES=$((ISSUES + 1))
fi

LISTEN_ADDR=$(netstat -tlnp 2>/dev/null | grep ":$TESTING_PORT " | awk '{print $4}' | head -1 || \
              ss -tlnp 2>/dev/null | grep ":$TESTING_PORT " | awk '{print $4}' | head -1)
if echo "$LISTEN_ADDR" | grep -q "127.0.0.1"; then
    echo "❌ 问题2：服务只监听本地，外部无法访问"
    ISSUES=$((ISSUES + 1))
fi

if ! nslookup "$DOMAIN" > /dev/null 2>&1; then
    echo "❌ 问题3：DNS 未配置或未生效"
    ISSUES=$((ISSUES + 1))
fi

if [ $ISSUES -eq 0 ]; then
    echo "✅ 所有检查通过"
    echo "如果 Postman 仍无法访问，请检查："
    echo "  1. 云服务商安全组规则"
    echo "  2. Postman base_url 配置"
    echo "  3. 网络连接（是否在公司网络，有代理等）"
else
    echo "发现 $ISSUES 个问题，请先解决这些问题"
fi

echo "=========================================="

