#!/bin/bash

# 自动配置 test.yaohufox.com 的脚本

set -e

NGINX_CONFIG_DIR="/opt/yaohufox/config/nginx"
SITES_AVAILABLE="$NGINX_CONFIG_DIR/sites-available"
SITES_ENABLED="$NGINX_CONFIG_DIR/sites-enabled"
CONFIG_FILE="$SITES_AVAILABLE/test.yaohufox.com"
TESTING_PORT="8082"

echo "=========================================="
echo "配置 test.yaohufox.com"
echo "=========================================="

# 1. 检查 DNS 配置
echo -e "\n1. 检查 DNS 配置..."
SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || curl -s ipinfo.io/ip 2>/dev/null || echo "")

if [ -z "$SERVER_IP" ]; then
    echo "⚠️  无法自动获取服务器 IP，请手动配置 DNS"
    read -p "请输入服务器公网 IP: " SERVER_IP
fi

echo "服务器公网 IP: $SERVER_IP"
echo ""
echo "请确保已在域名管理平台添加以下 DNS 记录："
echo "  类型: A"
echo "  主机记录: test"
echo "  记录值: $SERVER_IP"
echo "  TTL: 600"
echo ""
read -p "DNS 记录已添加？(y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "请先配置 DNS 记录，然后重新运行此脚本"
    exit 1
fi

# 2. 验证 DNS 解析
echo -e "\n2. 验证 DNS 解析..."
if nslookup test.yaohufox.com > /dev/null 2>&1; then
    RESOLVED_IP=$(nslookup test.yaohufox.com | grep -A 2 "Name:" | grep "Address:" | tail -1 | awk '{print $2}')
    echo "✅ DNS 解析成功: test.yaohufox.com -> $RESOLVED_IP"
    if [ "$RESOLVED_IP" != "$SERVER_IP" ]; then
        echo "⚠️  警告: DNS 解析的 IP ($RESOLVED_IP) 与服务器 IP ($SERVER_IP) 不一致"
        echo "   请检查 DNS 配置或等待 DNS 传播"
    fi
else
    echo "⚠️  DNS 解析失败，请检查 DNS 配置"
    echo "   继续配置 Nginx，但域名可能暂时无法访问"
fi

# 3. 检查测试环境服务
echo -e "\n3. 检查测试环境服务..."
if curl -f -s "http://localhost:$TESTING_PORT/api/test/hello" > /dev/null 2>&1; then
    echo "✅ 测试环境服务运行正常"
else
    echo "⚠️  测试环境服务未运行或无法访问"
    echo "   请确保服务在 localhost:$TESTING_PORT 上运行"
    read -p "是否继续配置 Nginx？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# 4. 创建目录
echo -e "\n4. 创建 Nginx 配置目录..."
mkdir -p "$SITES_AVAILABLE"
mkdir -p "$SITES_ENABLED"

# 5. 创建 Nginx 配置文件
echo -e "\n5. 创建 Nginx 配置文件..."
cat > "$CONFIG_FILE" << 'EOF'
server {
    listen 80;
    server_name test.yaohufox.com;

    # 日志配置
    access_log /var/log/nginx/test.yaohufox.com.access.log;
    error_log /var/log/nginx/test.yaohufox.com.error.log;

    # 反向代理到测试环境（端口8082）
    location / {
        proxy_pass http://localhost:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # WebSocket 支持（如果需要）
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 健康检查端点
    location /actuator/health {
        proxy_pass http://localhost:8082/actuator/health;
        access_log off;
    }

    # API 测试接口
    location /api/test/ {
        proxy_pass http://localhost:8082/api/test/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
EOF

echo "✅ 配置文件已创建: $CONFIG_FILE"

# 6. 启用配置
echo -e "\n6. 启用 Nginx 配置..."
if [ -L "$SITES_ENABLED/test.yaohufox.com" ]; then
    echo "⚠️  配置已存在，删除旧链接..."
    rm -f "$SITES_ENABLED/test.yaohufox.com"
fi

ln -sf "$CONFIG_FILE" "$SITES_ENABLED/test.yaohufox.com"
echo "✅ 配置已启用"

# 7. 测试 Nginx 配置
echo -e "\n7. 测试 Nginx 配置..."
if nginx -t 2>/dev/null; then
    echo "✅ Nginx 配置测试通过"
else
    echo "❌ Nginx 配置测试失败"
    echo "请检查配置文件: $CONFIG_FILE"
    exit 1
fi

# 8. 重载 Nginx
echo -e "\n8. 重载 Nginx..."
if nginx -s reload 2>/dev/null || systemctl reload nginx 2>/dev/null; then
    echo "✅ Nginx 已重载"
else
    echo "⚠️  Nginx 重载失败，请手动执行: sudo nginx -s reload"
fi

# 9. 验证访问
echo -e "\n9. 验证访问..."
sleep 2

echo "测试 HTTP 访问..."
if curl -f -s --connect-timeout 5 "http://test.yaohufox.com/api/test/hello" > /dev/null 2>&1; then
    echo "✅ HTTP 访问成功"
    curl -s "http://test.yaohufox.com/api/test/hello"
elif curl -f -s --connect-timeout 5 "http://localhost/api/test/hello" > /dev/null 2>&1; then
    echo "⚠️  通过 localhost 访问成功，但域名访问失败"
    echo "   可能原因：DNS 未生效或防火墙阻止"
else
    echo "❌ 访问失败"
    echo "   请检查："
    echo "   1. DNS 是否已配置并生效"
    echo "   2. 测试环境服务是否运行"
    echo "   3. Nginx 是否正常运行"
fi

# 10. SSL 证书配置提示
echo -e "\n10. SSL 证书配置（可选）..."
echo "如需配置 HTTPS，可以执行："
echo "  sudo certbot --nginx -d test.yaohufox.com"
echo ""
read -p "是否现在配置 SSL 证书？(y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if command -v certbot > /dev/null 2>&1; then
        sudo certbot --nginx -d test.yaohufox.com
    else
        echo "⚠️  certbot 未安装，请先安装："
        echo "  sudo apt-get install certbot python3-certbot-nginx"
    fi
fi

echo -e "\n=========================================="
echo "配置完成！"
echo "=========================================="
echo ""
echo "访问地址:"
echo "  HTTP:  http://test.yaohufox.com"
echo "  HTTPS: https://test.yaohufox.com (如果配置了 SSL)"
echo ""
echo "测试接口:"
echo "  http://test.yaohufox.com/api/test/hello"
echo "  http://test.yaohufox.com/actuator/health"
echo ""
echo "Jenkins 健康检查将自动使用此域名"
echo "=========================================="

