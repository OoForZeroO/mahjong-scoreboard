#!/bin/bash

# 配置测试环境 Nginx 脚本
# 在服务器上执行此脚本

NGINX_CONFIG_DIR="/opt/yaohufox/config/nginx"
SITES_AVAILABLE="$NGINX_CONFIG_DIR/sites-available"
SITES_ENABLED="$NGINX_CONFIG_DIR/sites-enabled"
TEST_CONFIG_FILE="$SITES_AVAILABLE/test.yaohufox.com"

echo "=========================================="
echo "配置测试环境 Nginx"
echo "=========================================="

# 1. 查看当前配置
echo -e "\n1. 当前已启用的站点配置："
ls -la "$SITES_ENABLED/" 2>/dev/null || echo "目录不存在"

echo -e "\n2. 所有可用站点配置："
ls -la "$SITES_AVAILABLE/" 2>/dev/null || echo "目录不存在"

# 2. 查看生产环境配置作为参考
echo -e "\n3. 查看生产环境配置（参考）："
if [ -f "$SITES_ENABLED/yaohufox.com" ]; then
    echo "找到生产环境配置："
    cat "$SITES_ENABLED/yaohufox.com"
elif [ -f "$SITES_ENABLED/default" ]; then
    echo "找到默认配置："
    cat "$SITES_ENABLED/default"
else
    echo "未找到参考配置"
fi

# 3. 创建测试环境配置
echo -e "\n4. 创建测试环境配置..."
mkdir -p "$SITES_AVAILABLE"
mkdir -p "$SITES_ENABLED"

cat > "$TEST_CONFIG_FILE" << 'EOF'
server {
    listen 80;
    listen 443 ssl http2;
    server_name test.yaohufox.com;

    # SSL 证书配置（如果使用HTTPS）
    # 如果有SSL证书，取消下面的注释并配置证书路径
    # ssl_certificate /opt/yaohufox/config/nginx/ssl/test.yaohufox.com.crt;
    # ssl_certificate_key /opt/yaohufox/config/nginx/ssl/test.yaohufox.com.key;
    
    # 如果没有SSL证书，可以暂时只使用HTTP（端口80）
    # 或者使用 Let's Encrypt 免费证书：
    # certbot --nginx -d test.yaohufox.com

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

echo "配置文件已创建：$TEST_CONFIG_FILE"

# 4. 创建软链接
echo -e "\n5. 启用配置..."
ln -sf "$TEST_CONFIG_FILE" "$SITES_ENABLED/test.yaohufox.com"
echo "软链接已创建"

# 5. 测试配置
echo -e "\n6. 测试 Nginx 配置..."
nginx -t

if [ $? -eq 0 ]; then
    echo -e "\n✅ 配置测试通过！"
    echo -e "\n下一步操作："
    echo "1. 配置 DNS：在域名管理中添加 A 记录 test.yaohufox.com → 服务器IP"
    echo "2. 重载 Nginx：nginx -s reload 或 systemctl reload nginx"
    echo "3. 验证：curl http://test.yaohufox.com/api/test/hello"
else
    echo -e "\n❌ 配置测试失败，请检查配置文件"
fi

echo -e "\n=========================================="

