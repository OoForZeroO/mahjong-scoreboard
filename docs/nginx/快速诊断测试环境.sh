#!/bin/bash

# 快速诊断测试环境访问问题

echo "=========================================="
echo "测试环境访问诊断"
echo "=========================================="

# 1. 检查服务是否运行
echo -e "\n1. 检查测试环境服务（端口8082）："
if curl -f -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo "✅ 测试环境服务正常运行"
    curl -s http://localhost:8082/actuator/health
else
    echo "❌ 测试环境服务未运行或无法访问"
fi

# 2. 检查 Nginx 配置目录
echo -e "\n2. 检查 Nginx 配置："
NGINX_CONFIG_DIR="/opt/yaohufox/config/nginx"
if [ -d "$NGINX_CONFIG_DIR" ]; then
    echo "✅ 找到 Nginx 配置目录：$NGINX_CONFIG_DIR"
    echo "已启用的配置："
    ls -la "$NGINX_CONFIG_DIR/sites-enabled/" 2>/dev/null || echo "sites-enabled 目录不存在"
else
    echo "❌ 未找到 Nginx 配置目录：$NGINX_CONFIG_DIR"
fi

# 3. 检查测试环境配置是否存在
echo -e "\n3. 检查测试环境 Nginx 配置："
if [ -f "$NGINX_CONFIG_DIR/sites-enabled/test.yaohufox.com" ]; then
    echo "✅ 找到测试环境配置"
    echo "配置内容："
    cat "$NGINX_CONFIG_DIR/sites-enabled/test.yaohufox.com"
elif [ -f "$NGINX_CONFIG_DIR/sites-available/test.yaohufox.com" ]; then
    echo "⚠️  配置存在但未启用"
    echo "执行：ln -s $NGINX_CONFIG_DIR/sites-available/test.yaohufox.com $NGINX_CONFIG_DIR/sites-enabled/"
else
    echo "❌ 未找到测试环境配置"
fi

# 4. 检查 DNS 解析
echo -e "\n4. 检查 DNS 解析："
if nslookup test.yaohufox.com > /dev/null 2>&1; then
    echo "✅ DNS 解析正常"
    nslookup test.yaohufox.com | grep -A 2 "Name:"
else
    echo "❌ DNS 解析失败：test.yaohufox.com 无法解析"
    echo "   需要配置 DNS A 记录：test.yaohufox.com → 服务器IP"
fi

# 5. 测试本地访问
echo -e "\n5. 测试本地访问："
echo "测试 localhost:8082："
curl -s http://localhost:8082/api/test/hello || echo "访问失败"

# 6. 测试通过生产域名+端口访问
echo -e "\n6. 测试通过生产域名+端口访问："
echo "测试 http://yaohufox.com:8082："
curl -s -m 5 http://yaohufox.com:8082/api/test/hello 2>&1 | head -3 || echo "访问失败（可能需要配置防火墙）"

# 7. 检查防火墙
echo -e "\n7. 检查防火墙端口："
if command -v ufw > /dev/null; then
    echo "UFW 状态："
    ufw status | grep 8082 || echo "端口 8082 未在 UFW 规则中"
elif command -v firewall-cmd > /dev/null; then
    echo "Firewalld 状态："
    firewall-cmd --list-ports | grep 8082 || echo "端口 8082 未在防火墙规则中"
else
    echo "未检测到防火墙管理工具"
fi

# 8. 推荐解决方案
echo -e "\n=========================================="
echo "推荐解决方案："
echo "=========================================="
echo ""
echo "方案1（立即可用）：使用生产域名+端口"
echo "  Postman base_url: http://yaohufox.com:8082"
echo "  测试：curl http://yaohufox.com:8082/api/test/hello"
echo ""
echo "方案2（需要配置）：配置 DNS + Nginx"
echo "  1. 配置 DNS：test.yaohufox.com → 服务器IP"
echo "  2. 创建 Nginx 配置（参考配置脚本）"
echo "  3. 重载 Nginx"
echo "  4. 测试：curl http://test.yaohufox.com/api/test/hello"
echo ""
echo "=========================================="

