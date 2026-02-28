# Jenkins 构建成功但应用未启动 — 排查指南

## 问题现象

- ✅ Jenkins 构建日志显示"构建成功"
- ❌ 服务器上 `lsof -i :8082` 无结果
- ❌ `ss -tlnp | grep 8082` 无结果
- ❌ `curl http://test.yaohufox.com:8082/actuator/health` 无响应
- ❌ 外部调用返回 502 Bad Gateway

## 可能原因

### 1. 应用启动后立即崩溃（最常见）

**现象**：
- Jenkins 检查进程时进程存在（刚启动）
- 但检查端口时进程已退出（启动失败）
- 或进程存在但应用内部错误导致端口未监听

**排查步骤**：

```bash
# 1. 查看应用启动日志（最重要）
tail -200 /opt/yaohufox/testing/app.log

# 2. 检查是否有错误信息
grep -i "error\|exception\|failed\|fatal" /opt/yaohufox/testing/app.log

# 3. 检查进程状态
ps aux | grep java | grep mahjong

# 4. 检查 PID 文件
cat /opt/yaohufox/testing/app.pid 2>/dev/null
```

**常见错误**：
- 数据库连接失败
- 端口被占用
- 配置文件错误
- 依赖缺失

### 2. Jenkins 验证逻辑时机问题

**现象**：
- 应用启动需要时间，但 Jenkins 等待时间不够
- 或应用启动过程中进程存在但端口还未监听

**排查**：
检查 Jenkins 构建日志中的时间戳，看进程检查和端口检查之间的时间间隔。

### 3. 健康检查误判

**现象**：
- 健康检查可能通过了某个条件（如 localhost 访问），但实际上应用未启动
- 或健康检查访问的是旧进程

**排查**：
检查 Jenkins 构建日志中的健康检查部分，看具体通过了哪个条件。

---

## 立即排查步骤（在服务器上执行）

### 步骤1：检查应用日志（最重要）

```bash
cd /opt/yaohufox/testing

# 查看完整启动日志
cat app.log

# 查看最后200行
tail -200 app.log

# 查找错误
grep -i "error\|exception\|failed\|fatal" app.log | tail -50
```

### 步骤2：检查进程和端口

```bash
# 检查进程
ps aux | grep java | grep -v grep

# 检查端口
ss -tlnp | grep 8082
netstat -tlnp | grep 8082

# 检查 PID 文件
if [ -f /opt/yaohufox/testing/app.pid ]; then
    PID=$(cat /opt/yaohufox/testing/app.pid)
    echo "PID文件中的进程ID: $PID"
    ps -p $PID || echo "进程 $PID 不存在"
else
    echo "PID文件不存在"
fi
```

### 步骤3：手动启动测试

```bash
cd /opt/yaohufox/testing

# 停止可能存在的旧进程
if [ -f app.pid ]; then
    kill $(cat app.pid) 2>/dev/null || true
    rm -f app.pid
fi

# 前台启动（可以看到实时输出）
java -jar app.jar \
  --spring.profiles.active=testing \
  -Dserver.port=8082 \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test \
  -Dspring.datasource.username=yaohu \
  -Dspring.datasource.password=cch815566
```

观察启动过程中的错误信息。

### 步骤4：检查数据库连接

```bash
# 测试数据库连接
PGPASSWORD=cch815566 psql -h localhost -U yaohu -d mahjong_scoreboard_system_test -c "SELECT 1;"
```

如果连接失败，参考 `docs/测试环境数据库用户认证失败-修复.md`。

### 步骤5：检查端口占用

```bash
# 检查8082端口是否被占用
lsof -i :8082
ss -tlnp | grep 8082

# 如果被占用，查看是什么进程
netstat -tlnp | grep 8082
```

---

## 修复方案

### 方案1：根据日志错误修复

根据 `app.log` 中的错误信息，常见修复：

**数据库连接失败**：
```bash
# 创建/修复数据库用户
sudo -u postgres psql <<EOF
DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'yaohu') THEN
        CREATE USER yaohu WITH PASSWORD 'cch815566';
    ELSE
        ALTER USER yaohu WITH PASSWORD 'cch815566';
    END IF;
END
\$\$;
GRANT ALL PRIVILEGES ON DATABASE mahjong_scoreboard_system_test TO yaohu;
\c mahjong_scoreboard_system_test
GRANT ALL ON SCHEMA public TO yaohu;
EOF
```

**端口被占用**：
```bash
# 查找并停止占用8082的进程
lsof -i :8082 | grep LISTEN | awk '{print $2}' | xargs kill -9
```

### 方案2：增强 Jenkins 验证逻辑

已更新 Jenkinsfile，添加了更严格的验证：

1. **端口检查必须成功**：端口未监听直接失败
2. **进程状态检查**：检查进程是否真的在运行
3. **详细错误输出**：失败时输出完整的应用日志

### 方案3：增加启动等待时间

如果应用启动较慢，可以增加等待时间（在 Jenkinsfile 中）：

```groovy
// 等待应用启动
echo "等待应用启动..."
sh 'sleep 30'  // 从15秒增加到30秒
```

---

## 验证修复

修复后，重新构建并验证：

```bash
# 1. 检查进程
ps aux | grep java | grep mahjong

# 2. 检查端口
ss -tlnp | grep 8082

# 3. 测试健康检查
curl http://localhost:8082/actuator/health

# 4. 测试接口
curl http://localhost:8082/api/test/hello

# 5. 通过域名测试（如果配置了Nginx）
curl http://test.yaohufox.com:8082/actuator/health
```

---

## 预防措施

### 1. 在 Jenkinsfile 中添加启动验证脚本

创建一个启动验证脚本，确保应用真正启动成功：

```bash
#!/bin/bash
# verify_startup.sh

APP_DIR="/opt/yaohufox/testing"
APP_PORT=8082
MAX_WAIT=60
WAIT_INTERVAL=3

echo "等待应用启动（最多等待 ${MAX_WAIT} 秒）..."

for i in $(seq 1 $((MAX_WAIT/WAIT_INTERVAL))); do
    # 检查端口是否监听
    if ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
        # 检查健康检查端点
        if curl -f -s --connect-timeout 2 "http://localhost:$APP_PORT/actuator/health" > /dev/null 2>&1; then
            echo "✅ 应用启动成功，端口 $APP_PORT 正在监听"
            exit 0
        fi
    fi
    sleep $WAIT_INTERVAL
done

echo "❌ 应用启动失败，端口 $APP_PORT 未监听或健康检查失败"
echo "查看应用日志："
tail -100 "$APP_DIR/app.log"
exit 1
```

### 2. 监控应用状态

定期检查应用状态：

```bash
#!/bin/bash
# check_app_status.sh

APP_DIR="/opt/yaohufox/testing"
APP_PORT=8082

if [ -f "$APP_DIR/app.pid" ]; then
    PID=$(cat "$APP_DIR/app.pid")
    if ps -p $PID > /dev/null 2>&1; then
        if ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
            echo "✅ 应用运行正常"
        else
            echo "⚠️  进程存在但端口未监听"
        fi
    else
        echo "❌ 进程不存在"
    fi
else
    echo "❌ PID文件不存在"
fi
```

---

## 总结

**立即行动**：
1. ✅ 查看 `/opt/yaohufox/testing/app.log` 找到启动失败原因
2. ✅ 根据错误信息修复（数据库、端口、配置等）
3. ✅ 手动启动测试，确认修复有效
4. ✅ 重新通过 Jenkins 部署

**长期改进**：
1. ✅ 增强 Jenkins 验证逻辑（已更新）
2. ✅ 添加启动验证脚本
3. ✅ 设置应用监控告警
