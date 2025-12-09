# 配置 test.yaohufox.com 完整指南

## 概述

本指南将帮助您配置 `test.yaohufox.com` 子域名，使其能够访问测试环境服务（端口 8082）。

## 步骤1：配置 DNS

### 1.1 登录域名管理平台

登录您的域名管理平台（如阿里云、腾讯云、GoDaddy 等），找到 `yaohufox.com` 域名的 DNS 管理页面。

### 1.2 添加 A 记录

添加一条 **A 记录**：

| 记录类型 | 主机记录 | 记录值 | TTL |
|---------|---------|--------|-----|
| A | test | 服务器公网IP | 600（或默认） |

**说明**：
- **主机记录**：填写 `test`（会自动生成 `test.yaohufox.com`）
- **记录值**：填写您的服务器公网 IP 地址
- **TTL**：建议 600 秒（10分钟），DNS 生效更快

### 1.3 获取服务器公网 IP

如果不知道服务器公网 IP，在服务器上执行：

```bash
# 方法1
curl ifconfig.me

# 方法2
curl ipinfo.io/ip

# 方法3
hostname -I | awk '{print $1}'
```

### 1.4 验证 DNS 配置

配置完成后，等待几分钟让 DNS 生效，然后验证：

```bash
# 在服务器上验证
nslookup test.yaohufox.com

# 或使用 dig
dig test.yaohufox.com

# 或使用 ping（如果服务器允许 ping）
ping test.yaohufox.com
```

**预期结果**：应该返回您的服务器 IP 地址。

**如果 DNS 未生效**：
- 等待 5-30 分钟（DNS 传播需要时间）
- 清除本地 DNS 缓存：`sudo systemd-resolve --flush-caches`（Linux）或 `ipconfig /flushdns`（Windows）
- 使用公共 DNS：`nslookup test.yaohufox.com 8.8.8.8`

## 步骤2：配置 Nginx 反向代理

### 2.1 创建 Nginx 配置文件

在服务器上创建配置文件：

```bash
sudo nano /opt/yaohufox/config/nginx/sites-available/test.yaohufox.com
```

### 2.2 配置内容

**基础配置（HTTP）**：

```nginx
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
```

**完整配置（HTTP + HTTPS）**：

```nginx
# HTTP 服务器（可选：重定向到 HTTPS）
server {
    listen 80;
    server_name test.yaohufox.com;

    # 如果使用 HTTPS，取消下面的注释以重定向
    # return 301 https://$server_name$request_uri;

    # 如果只使用 HTTP，保留以下配置
    access_log /var/log/nginx/test.yaohufox.com.access.log;
    error_log /var/log/nginx/test.yaohufox.com.error.log;

    location / {
        proxy_pass http://localhost:8082;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    location /actuator/health {
        proxy_pass http://localhost:8082/actuator/health;
        access_log off;
    }
}

# HTTPS 服务器（如果配置了 SSL 证书）
# server {
#     listen 443 ssl http2;
#     server_name test.yaohufox.com;
#
#     # SSL 证书配置
#     ssl_certificate /path/to/ssl/test.yaohufox.com.crt;
#     ssl_certificate_key /path/to/ssl/test.yaohufox.com.key;
#
#     # SSL 配置优化
#     ssl_protocols TLSv1.2 TLSv1.3;
#     ssl_ciphers HIGH:!aNULL:!MD5;
#     ssl_prefer_server_ciphers on;
#
#     access_log /var/log/nginx/test.yaohufox.com.access.log;
#     error_log /var/log/nginx/test.yaohufox.com.error.log;
#
#     location / {
#         proxy_pass http://localhost:8082;
#         proxy_set_header Host $host;
#         proxy_set_header X-Real-IP $remote_addr;
#         proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
#         proxy_set_header X-Forwarded-Proto $scheme;
#         
#         proxy_connect_timeout 60s;
#         proxy_send_timeout 60s;
#         proxy_read_timeout 60s;
#         
#         proxy_http_version 1.1;
#         proxy_set_header Upgrade $http_upgrade;
#         proxy_set_header Connection "upgrade";
#     }
#
#     location /actuator/health {
#         proxy_pass http://localhost:8082/actuator/health;
#         access_log off;
#     }
# }
```

### 2.3 启用配置

```bash
# 创建软链接到 sites-enabled
sudo ln -s /opt/yaohufox/config/nginx/sites-available/test.yaohufox.com \
          /opt/yaohufox/config/nginx/sites-enabled/test.yaohufox.com

# 测试 Nginx 配置
sudo nginx -t

# 如果测试通过，重载 Nginx
sudo nginx -s reload
# 或
sudo systemctl reload nginx
```

## 步骤3：配置 SSL 证书（可选，推荐）

### 3.1 使用 Let's Encrypt 免费证书

```bash
# 安装 certbot（如果未安装）
sudo apt-get update
sudo apt-get install certbot python3-certbot-nginx

# 获取证书并自动配置 Nginx
sudo certbot --nginx -d test.yaohufox.com

# 测试自动续期
sudo certbot renew --dry-run
```

### 3.2 手动配置 SSL 证书

如果您有自己的 SSL 证书：

1. 将证书文件上传到服务器
2. 修改 Nginx 配置，取消 HTTPS server 块的注释
3. 更新证书路径
4. 重载 Nginx

## 步骤4：验证配置

### 4.1 验证 DNS

```bash
# 检查 DNS 解析
nslookup test.yaohufox.com
dig test.yaohufox.com

# 应该返回服务器 IP
```

### 4.2 验证 Nginx 配置

```bash
# 测试配置语法
sudo nginx -t

# 检查配置是否加载
sudo nginx -T | grep test.yaohufox.com
```

### 4.3 验证服务访问

```bash
# 测试 HTTP 访问
curl http://test.yaohufox.com/api/test/hello

# 测试健康检查
curl http://test.yaohufox.com/actuator/health

# 如果配置了 HTTPS
curl -k https://test.yaohufox.com/api/test/hello
```

### 4.4 从外部验证

在浏览器中访问：
- `http://test.yaohufox.com/api/test/hello`
- `https://test.yaohufox.com/api/test/hello`（如果配置了 SSL）

## 步骤5：更新 Jenkins 配置

配置完成后，Jenkins 部署时会自动使用 `test.yaohufox.com` 进行健康检查。

**当前 Jenkinsfile 配置**：
```groovy
TESTING_DOMAIN = 'test.yaohufox.com'
TESTING_DOMAIN_PORT = ''  // 使用标准端口
```

健康检查会尝试：
1. `https://test.yaohufox.com/actuator/health`
2. `http://test.yaohufox.com/actuator/health`
3. `https://test.yaohufox.com/api/test/hello`
4. `http://test.yaohufox.com/api/test/hello`
5. 如果都失败，回退到 `localhost:8082`

## 故障排除

### 问题1：DNS 解析失败

**症状**：`nslookup test.yaohufox.com` 返回 `NXDOMAIN`

**解决**：
1. 检查 DNS 配置是否正确
2. 等待 DNS 传播（最多 24 小时）
3. 清除本地 DNS 缓存
4. 使用公共 DNS 服务器验证

### 问题2：Nginx 502 Bad Gateway

**症状**：访问域名返回 502 错误

**解决**：
1. 检查测试环境服务是否运行：`curl http://localhost:8082/api/test/hello`
2. 检查 Nginx 错误日志：`sudo tail -f /var/log/nginx/test.yaohufox.com.error.log`
3. 检查端口是否正确：`netstat -tln | grep 8082`

### 问题3：连接超时

**症状**：访问域名连接超时

**解决**：
1. 检查防火墙是否开放 80/443 端口
2. 检查服务器安全组规则
3. 检查 Nginx 是否正常运行：`sudo systemctl status nginx`

### 问题4：SSL 证书错误

**症状**：HTTPS 访问显示证书错误

**解决**：
1. 检查证书路径是否正确
2. 检查证书是否过期
3. 使用 Let's Encrypt 自动续期

## 快速配置脚本

已创建自动化配置脚本：`docs/nginx/配置test.yaohufox.com.sh`

执行脚本可以自动完成大部分配置步骤。

## 配置检查清单

- [ ] DNS A 记录已添加（test → 服务器IP）
- [ ] DNS 解析正常（nslookup 返回正确 IP）
- [ ] Nginx 配置文件已创建
- [ ] Nginx 配置已启用（软链接到 sites-enabled）
- [ ] Nginx 配置测试通过（nginx -t）
- [ ] Nginx 已重载（nginx -s reload）
- [ ] 测试环境服务运行正常（localhost:8082）
- [ ] HTTP 访问正常（curl http://test.yaohufox.com/api/test/hello）
- [ ] HTTPS 访问正常（如果配置了 SSL）
- [ ] Jenkins 健康检查通过

## 完成后的效果

配置完成后：
- ✅ 可以通过 `http://test.yaohufox.com` 访问测试环境
- ✅ Jenkins 部署时会自动使用域名进行健康检查
- ✅ 生产环境 `yaohufox.com` 不受影响
- ✅ 测试环境和生产环境完全隔离

