# Postman 连接重置（ECONNRESET）问题诊断

## 错误说明

**错误信息**：`Error: read ECONNRESET`

**含义**：连接已建立，但被服务器或网络设备主动关闭。

## 可能原因

1. **服务未运行**：测试环境服务未启动或已崩溃
2. **DNS 未配置**：`test.yaohufox.com` DNS 未配置或未生效
3. **防火墙阻止**：服务器防火墙或云服务商安全组阻止了连接
4. **Nginx 未配置**：如果使用标准端口，Nginx 反向代理未配置
5. **端口未监听**：服务只监听 localhost，不监听外部 IP
6. **SSL/TLS 问题**：如果使用 HTTPS，证书配置错误

## 诊断步骤

### 步骤1：检查服务状态

在服务器上执行：

```bash
# 检查服务是否运行
ps aux | grep java | grep 8082

# 检查端口是否监听
netstat -tln | grep 8082
# 或
ss -tln | grep 8082

# 检查监听地址（重要！）
netstat -tlnp | grep 8082
# 应该显示 0.0.0.0:8082 或 :::8082，而不是 127.0.0.1:8082
```

**如果端口只监听 127.0.0.1**：
- 服务只能从本地访问，外部无法访问
- 需要修改应用配置，监听 `0.0.0.0:8082`

### 步骤2：检查 DNS 配置

```bash
# 在服务器上检查 DNS
nslookup test.yaohufox.com

# 应该返回服务器 IP 地址
# 如果返回 NXDOMAIN，说明 DNS 未配置
```

### 步骤3：检查防火墙

```bash
# Ubuntu/Debian
sudo ufw status
sudo ufw allow 8082/tcp

# CentOS/RHEL
sudo firewall-cmd --list-ports
sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --reload

# 检查云服务商安全组（阿里云、腾讯云等）
# 需要在控制台配置安全组规则，开放 8082 端口
```

### 步骤4：测试本地访问

```bash
# 在服务器上测试
curl http://localhost:8082/api/test/hello

# 如果成功，说明服务正常
# 如果失败，说明服务有问题
```

### 步骤5：测试域名访问

```bash
# 在服务器上测试（如果 DNS 已配置）
curl http://test.yaohufox.com:8082/api/test/hello

# 从外部测试
curl http://test.yaohufox.com:8082/api/test/hello
```

### 步骤6：检查 Postman 环境变量

在 Postman 中：
1. 点击右上角环境选择器
2. 选择"测试环境"
3. 检查 `base_url` 的值：
   - 应该是：`http://test.yaohufox.com:8082`
   - 或：`https://test.yaohufox.com:8082`（如果配置了 SSL）

## 解决方案

### 方案1：确保服务监听所有网络接口

如果服务只监听 `127.0.0.1`，需要修改配置：

**检查应用配置**：
```bash
# 查看应用启动参数
ps aux | grep java | grep 8082

# 应该包含：-Dserver.address=0.0.0.0 或类似配置
```

**修改 Spring Boot 配置**（如果需要）：
在 `application-testing.yml` 中添加：
```yaml
server:
  address: 0.0.0.0  # 监听所有网络接口
  port: 8082
```

### 方案2：配置 DNS（如果未配置）

参考：`docs/nginx/配置test.yaohufox.com完整指南.md`

快速配置：
1. 登录域名管理平台
2. 添加 A 记录：`test` → `服务器IP`
3. 等待 DNS 生效（5-30分钟）

### 方案3：开放防火墙端口

```bash
# Ubuntu/Debian
sudo ufw allow 8082/tcp
sudo ufw reload

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --reload

# 云服务商安全组（重要！）
# 在阿里云/腾讯云控制台配置安全组规则：
# - 入方向规则
# - 端口：8082
# - 协议：TCP
# - 源：0.0.0.0/0（或特定 IP）
```

### 方案4：使用 Nginx 反向代理（推荐）

如果不想直接暴露 8082 端口，可以配置 Nginx：

```bash
# 使用配置脚本
cd /path/to/mahjong-scoreboard
chmod +x docs/nginx/配置test.yaohufox.com.sh
sudo bash docs/nginx/配置test.yaohufox.com.sh
```

然后更新 Postman `base_url` 为：`http://test.yaohufox.com`（使用标准端口 80）

### 方案5：临时使用生产域名+端口

如果 `test.yaohufox.com` 暂时无法配置，可以临时使用：

在 Postman 中设置 `base_url` 为：`http://yaohufox.com:8082`

**注意**：这只是临时方案，需要确保防火墙开放 8082 端口。

## 快速诊断脚本

创建诊断脚本 `diagnose_postman_connection.sh`：

```bash
#!/bin/bash

echo "=========================================="
echo "Postman 连接问题诊断"
echo "=========================================="

# 1. 检查服务
echo -e "\n1. 检查测试环境服务："
if ps aux | grep java | grep 8082 | grep -v grep > /dev/null; then
    echo "✅ 服务运行中"
    ps aux | grep java | grep 8082 | grep -v grep
else
    echo "❌ 服务未运行"
fi

# 2. 检查端口监听
echo -e "\n2. 检查端口监听："
if netstat -tln 2>/dev/null | grep -q ":8082 " || ss -tln 2>/dev/null | grep -q ":8082 "; then
    echo "✅ 端口 8082 正在监听"
    LISTEN_ADDR=$(netstat -tln 2>/dev/null | grep ":8082 " | awk '{print $4}' || \
                  ss -tln 2>/dev/null | grep ":8082 " | awk '{print $4}')
    echo "监听地址: $LISTEN_ADDR"
    if echo "$LISTEN_ADDR" | grep -q "127.0.0.1"; then
        echo "⚠️  只监听本地，外部无法访问！"
    fi
else
    echo "❌ 端口 8082 未监听"
fi

# 3. 检查 DNS
echo -e "\n3. 检查 DNS："
if nslookup test.yaohufox.com > /dev/null 2>&1; then
    echo "✅ DNS 解析成功"
    nslookup test.yaohufox.com | grep -A 2 "Name:"
else
    echo "❌ DNS 解析失败"
fi

# 4. 测试本地访问
echo -e "\n4. 测试本地访问："
if curl -f -s http://localhost:8082/api/test/hello > /dev/null 2>&1; then
    echo "✅ 本地访问成功"
else
    echo "❌ 本地访问失败"
fi

# 5. 测试域名访问
echo -e "\n5. 测试域名访问："
if curl -f -s --connect-timeout 5 http://test.yaohufox.com:8082/api/test/hello > /dev/null 2>&1; then
    echo "✅ 域名访问成功"
else
    ERROR=$?
    echo "❌ 域名访问失败（错误代码: $ERROR）"
    if [ $ERROR -eq 6 ]; then
        echo "   原因：DNS 解析失败"
    elif [ $ERROR -eq 7 ]; then
        echo "   原因：连接被拒绝（可能是防火墙）"
    elif [ $ERROR -eq 28 ]; then
        echo "   原因：连接超时"
    fi
fi

# 6. 检查防火墙
echo -e "\n6. 检查防火墙："
if command -v ufw > /dev/null 2>&1; then
    if ufw status | grep -q "8082"; then
        echo "✅ 防火墙规则已配置"
    else
        echo "⚠️  防火墙未开放 8082 端口"
        echo "   执行: sudo ufw allow 8082/tcp"
    fi
fi

echo -e "\n=========================================="
```

## 常见问题

### Q1: 为什么本地可以访问，但 Postman 不行？

**原因**：
1. 服务只监听 `127.0.0.1`，不监听外部 IP
2. 防火墙阻止外部访问
3. DNS 未配置，Postman 无法解析域名

**解决**：
1. 确保服务监听 `0.0.0.0:8082`
2. 开放防火墙端口
3. 配置 DNS

### Q2: 连接建立但立即被重置？

**原因**：
1. 服务器主动关闭连接（可能是安全策略）
2. 云服务商安全组规则阻止
3. Nginx 配置错误

**解决**：
1. 检查云服务商安全组规则
2. 检查服务器防火墙日志
3. 检查 Nginx 配置

### Q3: DNS 已配置但仍无法访问？

**原因**：
1. DNS 未完全生效（等待时间不够）
2. 本地 DNS 缓存
3. DNS 配置错误

**解决**：
1. 等待 5-30 分钟
2. 清除本地 DNS 缓存
3. 使用 `nslookup test.yaohufox.com 8.8.8.8` 验证

## 推荐操作顺序

1. **检查服务状态**：确保服务运行且监听正确地址
2. **检查 DNS**：确保 DNS 已配置并生效
3. **检查防火墙**：确保端口已开放（包括云服务商安全组）
4. **测试访问**：先在服务器上测试，再在外部测试
5. **检查 Postman 配置**：确保环境变量正确

## 如果问题仍然存在

请提供以下信息以便进一步诊断：

1. 服务器上 `curl http://localhost:8082/api/test/hello` 的结果
2. 服务器上 `netstat -tln | grep 8082` 的输出
3. 服务器上 `nslookup test.yaohufox.com` 的输出
4. Postman 中 `base_url` 的配置值
5. 云服务商和服务器类型（阿里云/腾讯云等）

