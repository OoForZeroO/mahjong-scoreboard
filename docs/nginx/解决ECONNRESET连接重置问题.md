# 解决 ECONNRESET 连接重置问题

## 问题描述

Postman 调用返回 **Error: read ECONNRESET**，表示连接被重置。

**错误信息**：
- `Error: read ECONNRESET`
- 连接建立后立即被关闭

## 可能原因

1. **服务未运行**：测试环境服务未启动或已崩溃
2. **服务只监听 localhost**：服务只监听 `127.0.0.1:8082`，外部无法访问
3. **防火墙阻止**：服务器防火墙或云服务商安全组阻止连接
4. **端口未监听**：服务未在 8082 端口监听
5. **服务启动失败**：服务启动后立即退出

## 立即诊断步骤

### 步骤1：检查服务是否运行

在服务器上执行：

```bash
# 检查进程
ps aux | grep java | grep 8082

# 检查端口监听
netstat -tlnp | grep 8082
# 或
ss -tlnp | grep 8082
```

**关键检查**：监听地址应该是 `0.0.0.0:8082` 或 `:::8082`，而不是 `127.0.0.1:8082`

### 步骤2：检查服务日志

```bash
cd /opt/yaohufox/testing
tail -50 app.log
```

查看是否有错误信息，特别是：
- 数据库连接错误
- 端口占用错误
- 启动失败错误

### 步骤3：测试本地访问

```bash
# 在服务器上测试
curl http://localhost:8082/api/test/hello
```

如果成功，说明服务正常，问题在外部访问。

### 步骤4：测试外部访问

```bash
# 从服务器测试外部访问
curl http://yaohufox.com:8082/api/test/hello

# 或使用服务器 IP
curl http://服务器IP:8082/api/test/hello
```

### 步骤5：检查防火墙

```bash
# Ubuntu/Debian
sudo ufw status
sudo ufw allow 8082/tcp

# CentOS/RHEL
sudo firewall-cmd --list-ports
sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --reload
```

### 步骤6：检查云服务商安全组

**阿里云安全组配置**（重要！）：
1. 登录阿里云控制台
2. 进入 ECS 实例 → 安全组
3. 检查入方向规则是否有 8082 端口
4. 如果没有，添加规则：
   - 端口：`8082`
   - 协议：`TCP`
   - 源：`0.0.0.0/0`

## 解决方案

### 方案1：确保服务运行并监听正确地址

如果服务未运行或只监听 localhost：

```bash
cd /opt/yaohufox/testing

# 停止旧进程（如果有）
if [ -f app.pid ]; then
    PID=$(cat app.pid)
    kill $PID 2>/dev/null
    rm -f app.pid
fi

# 检查 JAR 文件
ls -lh app.jar

# 启动服务（确保监听 0.0.0.0）
nohup java -jar \
  -Dspring.profiles.active=testing \
  -Dserver.port=8082 \
  -Dserver.address=0.0.0.0 \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test \
  -Dspring.datasource.username=yaohu \
  -Dspring.datasource.password=cch815566 \
  app.jar > app.log 2>&1 &

echo $! > app.pid
echo "PID: $(cat app.pid)"

# 等待启动
sleep 15

# 验证
ps aux | grep java | grep 8082
netstat -tlnp | grep 8082
curl http://localhost:8082/api/test/hello
```

**关键**：添加 `-Dserver.address=0.0.0.0` 确保监听所有网络接口

### 方案2：检查并修复应用配置

如果 Spring Boot 默认只监听 localhost，检查配置：

**检查 `application-testing.yml`**：
```yaml
server:
  port: 8082
  address: 0.0.0.0  # 确保监听所有接口
```

### 方案3：开放防火墙和安全组

**服务器防火墙**：
```bash
# Ubuntu/Debian
sudo ufw allow 8082/tcp
sudo ufw reload

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=8082/tcp
sudo firewall-cmd --reload
```

**云服务商安全组**：
- 在阿里云控制台配置安全组规则
- 允许 8082 端口的入方向流量

### 方案4：使用诊断脚本

```bash
cd /path/to/mahjong-scoreboard
chmod +x docs/nginx/诊断服务未运行问题.sh
bash docs/nginx/诊断服务未运行问题.sh
```

## 常见问题排查

### Q1: 服务运行但外部无法访问？

**检查项**：
1. 监听地址是否为 `0.0.0.0:8082`（不是 `127.0.0.1:8082`）
2. 防火墙是否开放端口
3. 云服务商安全组是否配置

**解决**：
- 添加 `-Dserver.address=0.0.0.0` 启动参数
- 开放防火墙端口
- 配置安全组规则

### Q2: 服务启动后立即退出？

**检查日志**：
```bash
tail -100 /opt/yaohufox/testing/app.log
```

**常见原因**：
- 数据库连接失败（已修复，使用端口 5432）
- 端口被占用
- 配置文件错误

### Q3: 本地可以访问，外部不行？

**原因**：服务只监听 localhost

**解决**：
1. 修改启动参数添加 `-Dserver.address=0.0.0.0`
2. 或修改配置文件添加 `server.address: 0.0.0.0`

## 完整启动命令（推荐）

```bash
cd /opt/yaohufox/testing

# 停止旧进程
if [ -f app.pid ]; then
    kill $(cat app.pid) 2>/dev/null
    rm -f app.pid
fi

# 启动服务
nohup java -jar \
  -Dspring.profiles.active=testing \
  -Dserver.port=8082 \
  -Dserver.address=0.0.0.0 \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test \
  -Dspring.datasource.username=yaohu \
  -Dspring.datasource.password=cch815566 \
  app.jar > app.log 2>&1 &

echo $! > app.pid

# 等待启动
sleep 15

# 验证
echo "检查进程："
ps aux | grep java | grep 8082

echo "检查端口监听："
netstat -tlnp | grep 8082

echo "测试接口："
curl http://localhost:8082/api/test/hello
```

## 验证清单

- [ ] 服务进程运行中
- [ ] 端口 8082 正在监听
- [ ] 监听地址是 `0.0.0.0:8082`（不是 `127.0.0.1:8082`）
- [ ] 本地访问成功：`curl http://localhost:8082/api/test/hello`
- [ ] 防火墙开放 8082 端口
- [ ] 云服务商安全组配置 8082 端口
- [ ] 外部访问成功：`curl http://yaohufox.com:8082/api/test/hello`
- [ ] Postman 调用成功

## 下一步

1. **执行诊断步骤**，确认问题原因
2. **根据问题选择对应方案**修复
3. **验证修复**：在 Postman 中重新测试

如果问题仍然存在，请提供：
- `ps aux | grep java | grep 8082` 的输出
- `netstat -tlnp | grep 8082` 的输出
- `tail -50 /opt/yaohufox/testing/app.log` 的输出

