# Jenkins 测试环境部署问题排查

## 问题1：日志显示使用生产数据库而非测试数据库

### 现象

Jenkins 构建日志中显示：
```
数据库: jdbc:postgresql://localhost:5432/mahjong_scoreboard_system
```

但测试环境应该使用：`mahjong_scoreboard_system_test`

### 原因分析

Jenkinsfile 中的配置顺序问题：

1. **第183-205行**：先加载 `.env` 文件或系统环境变量
   ```bash
   if [ -f "$ENV_FILE" ]; then
       . "$ENV_FILE"  # 如果 .env 中有 SPRING_DATASOURCE_URL，会被设置
   fi
   ```

2. **第211行**：使用默认值语法
   ```bash
   export SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test}
   ```
   如果 `.env` 或系统环境变量中已经设置了 `SPRING_DATASOURCE_URL`（比如设置为生产库），这个默认值就不会生效。

### 修复方案

**已修复**：强制设置测试环境数据库 URL，忽略 `.env` 或系统环境变量中的值：

```bash
# 强制设置测试环境数据库连接（覆盖 .env 或系统环境变量中的值）
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test
```

### 验证方法

重新构建后，日志应该显示：
```
数据库URL: jdbc:postgresql://localhost:5432/mahjong_scoreboard_system_test
```

---

## 问题2：8082端口未启动但构建显示成功

### 现象

- Jenkins 构建日志显示"构建成功"
- 但在服务器上执行 `ss -tlnp | grep 8082` 发现端口未监听
- 应用实际未启动或启动失败

### 原因分析

Jenkinsfile 中的验证逻辑不够严格：

**第280-284行（修复前）**：
```bash
if netstat -tln 2>/dev/null | grep -q ":$APP_PORT " || ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
    echo "✅ 端口 $APP_PORT 正在监听"
else
    echo "⚠️  端口 $APP_PORT 未监听，应用可能还在启动中..."  # 只是警告，没有 exit 1
fi
```

**问题**：
- 端口未监听时只打印警告，**没有 `exit 1`**
- 构建会继续执行，如果后续的健康检查因为某种原因通过（比如旧进程还在），构建就会成功
- 或者健康检查失败，但错误信息不够明确

### 修复方案

**已修复**：端口未监听时直接失败，并输出详细的诊断信息：

```bash
# 2. 检查端口是否监听（必须成功，否则构建失败）
APP_PORT=${TESTING_PORT}
PORT_LISTENING=false
if netstat -tln 2>/dev/null | grep -q ":$APP_PORT " || ss -tln 2>/dev/null | grep -q ":$APP_PORT "; then
    echo "✅ 端口 $APP_PORT 正在监听"
    PORT_LISTENING=true
else
    echo "❌ 端口 $APP_PORT 未监听，应用启动失败"
    echo "查看应用启动日志："
    tail -100 ${TESTING_DIR}/app.log
    echo ""
    echo "检查进程状态："
    if [ -f ${TESTING_DIR}/app.pid ]; then
        PID=$(cat ${TESTING_DIR}/app.pid)
        if ps -p $PID > /dev/null 2>&1; then
            echo "进程 $PID 仍在运行，但端口未监听，可能应用启动失败"
        else
            echo "进程 $PID 已退出"
        fi
    fi
    exit 1  # 直接失败
fi
```

### 验证方法

重新构建后：
- 如果端口未监听，构建会**立即失败**，并显示详细的错误信息
- 如果端口正常监听，构建会继续执行健康检查

---

## 常见启动失败原因

### 1. 数据库连接失败

**错误日志**：
```
FATAL: password authentication failed for user "yaohu"
或
FATAL: database "mahjong_scoreboard_system_test" does not exist
```

**解决方法**：参考 `docs/测试环境数据库用户认证失败-修复.md`

### 2. 端口被占用

**错误日志**：
```
Web server failed to start. Port 8082 was already in use.
```

**解决方法**：
```bash
# 查找占用端口的进程
lsof -i :8082
# 或
ss -tlnp | grep 8082

# 停止旧进程
kill <PID>
```

### 3. 应用启动后立即崩溃

**检查方法**：
```bash
# 查看应用日志
tail -100 /opt/yaohufox/testing/app.log

# 检查进程
ps aux | grep java | grep mahjong
```

---

## 修复后的构建流程

1. ✅ **强制设置测试数据库**：忽略 `.env` 中的数据库配置
2. ✅ **启动应用**：使用 `testing` profile 和 8082 端口
3. ✅ **检查进程**：确认进程存在
4. ✅ **检查端口**：**必须监听，否则构建失败**
5. ✅ **健康检查**：通过 HTTP 请求验证应用可用性

---

## 手动验证步骤

在服务器上手动验证：

```bash
# 1. 检查进程
ps aux | grep java | grep mahjong

# 2. 检查端口
ss -tlnp | grep 8082

# 3. 测试健康检查
curl http://localhost:8082/actuator/health

# 4. 查看日志
tail -f /opt/yaohufox/testing/app.log
```

---

## 总结

| 问题 | 原因 | 修复状态 |
|------|------|----------|
| 使用生产数据库 | `.env` 或环境变量覆盖了默认值 | ✅ 已修复：强制设置测试库 |
| 端口未监听但构建成功 | 端口检查只警告不失败 | ✅ 已修复：端口未监听直接失败 |

修复后，Jenkins 构建会：
- ✅ 强制使用测试数据库 `mahjong_scoreboard_system_test`
- ✅ 端口未监听时立即失败，不会误报成功
- ✅ 提供详细的错误诊断信息
