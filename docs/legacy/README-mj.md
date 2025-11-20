# Java + MySQL 开发环境搭建指南

本目录包含搭建Java和MySQL服务器端开发环境的必要文件和说明，支持传统安装方式。

## 目录结构

- `java/` - Java应用和配置
- `mysql/` - MySQL配置和初始化脚本
- `config/` - 环境配置文件
- `scripts/` - 自动化脚本

## 环境搭建步骤

我们提供以下几种方案：

### 方案1: 使用已准备的MySQL安装脚本（推荐）

以管理员身份运行安装脚本：

```bash
scripts\install_mysql.bat
```

按照安装向导完成MySQL安装，设置root密码为`rootpassword`以保持配置一致。

### 方案2: 手动配置MySQL

1. 下载并安装MySQL（参考`mysql/README.md`）
2. 启动MySQL服务
3. 使用以下命令登录并创建数据库：

```bash
mysql -u root -p
```

输入密码后执行：

```sql
CREATE DATABASE devdb;
CREATE USER 'devuser'@'%' IDENTIFIED BY 'devpassword';
GRANT ALL PRIVILEGES ON devdb.* TO 'devuser'@'%';
FLUSH PRIVILEGES;
```

4. 导入初始化数据：

```bash
mysql -u root -p devdb < mysql\init\init.sql
```


## 运行Java应用

### 方案A: 本地运行Java应用

1. 确保MySQL服务已启动
2. 进入Java项目目录：

```bash
cd java\demo
```

3. 使用Maven构建并运行应用：

```bash
mvn clean install
mvn spring-boot:run
```

### 方案B: 直接运行连接示例

运行简单的MySQL连接示例：

```bash
cd java\demo
mvn exec:java -Dexec.mainClass="com.example.demo.MySQLConnectionExample"
```

## 环境管理

### 管理MySQL服务

使用以下脚本管理MySQL服务：

```bash
scripts\mysql_service_manager.bat
```

### 检查Java环境

```bash
scripts\check_java_env.bat
```

## 数据库连接信息

- 数据库名称: `devdb`
- 用户名: `devuser`
- 密码: `devpassword`
- 端口: `3306`

## 常见问题

### MySQL连接失败

- 检查MySQL服务是否正在运行
- 验证连接参数是否正确
- 确保防火墙允许3306端口通信

### Java应用启动失败

- 检查数据库连接配置
- 确保MySQL Connector/J依赖已添加
- 查看详细错误日志

## 后续步骤

1. 创建您的Java项目
2. 配置数据库连接
3. 开始开发您的应用程序

## 更多信息

- Java环境配置详细说明：[java/README.md](java/README.md)
- MySQL配置详细说明：[mysql/README.md](mysql/README.md)# mj
