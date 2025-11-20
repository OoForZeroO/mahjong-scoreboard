# IntelliJ IDEA 运行配置说明

## 问题说明

如果遇到以下错误：
- `Activation.main: 警告: sun.rmi.activation.execPolicy`
- `Port already in use: 1098`

说明 IDEA 的运行配置指向了错误的类（RMI 激活类而不是 Spring Boot 应用）。

## 解决方案

### 方法一：删除旧配置并创建新配置（推荐）

1. **删除旧的运行配置**
   - 点击 IDEA 顶部菜单：`Run` → `Edit Configurations...`
   - 找到所有名为 `mahjongScoreboard` 或类似的配置
   - 选中后点击左上角的 `-` 号删除

2. **创建新的运行配置**
   - 在 `Run` → `Edit Configurations...` 中点击左上角 `+`
   - 选择 `Spring Boot` 或 `Application`
   - 配置如下：
     - **Name**: `Mahjong Scoreboard Application`
     - **Main class**: `com.mahjong.start.MahjongScoreboardApplication`
     - **Module**: `mahjong-scoreboard-start`
     - **Working directory**: `$PROJECT_DIR$`
     - **Use classpath of module**: `mahjong-scoreboard-start`

3. **保存并运行**
   - 点击 `OK` 保存配置
   - 点击运行按钮启动应用

### 方法二：使用 Maven 运行

1. 在 IDEA 右侧 Maven 面板中
2. 展开 `mahjong-scoreboard-start` → `Plugins` → `spring-boot`
3. 双击 `spring-boot:run` 运行

### 方法三：使用命令行

```bash
# 在项目根目录执行
mvn -pl mahjong-scoreboard-start -am spring-boot:run
```

或使用提供的启动脚本：

```bash
# macOS/Linux
./scripts/unix/start.sh
```

## 验证配置

启动成功后，应该看到类似以下输出：

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.5)
```

而不是 `Activation.main` 相关的错误信息。

## 常见问题

### 端口 1098 被占用

如果提示端口被占用，执行以下命令终止进程：

```bash
# 查找占用端口的进程
lsof -ti:1098

# 终止进程（替换 PID 为实际进程号）
kill -9 <PID>
```

### 找不到主类

确保：
1. Maven 项目已正确导入（右键项目 → `Maven` → `Reload Project`）
2. `mahjong-scoreboard-start` 模块已正确编译
3. 主类路径为：`com.mahjong.start.MahjongScoreboardApplication`

### 无效的源发行版错误（Java 版本不匹配）

如果遇到 `错误: 无效的源发行版: 18` 或类似错误：

1. **检查 IDEA 项目 JDK 设置**
   - `File` → `Project Structure` → `Project`
   - 确保 `SDK` 设置为 **JDK 21**
   - 确保 `Language level` 设置为 **21 - Record patterns, pattern matching for switch**

2. **检查模块 JDK 设置**
   - `File` → `Project Structure` → `Modules`
   - 选择 `mahjong-scoreboard-service` 和 `mahjong-scoreboard-start`
   - 确保 `Language level` 都设置为 **21**

3. **检查 Maven 设置**
   - `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven` → `Runner`
   - 确保 `JRE` 设置为 **JDK 21**

4. **重新导入 Maven 项目**
   - 右键项目根目录 → `Maven` → `Reload Project`
   - 或者在 Maven 面板点击刷新按钮

5. **清理并重新构建**
   - `Build` → `Rebuild Project`
   - 或在 Maven 面板执行 `Lifecycle` → `clean` → `compile`

