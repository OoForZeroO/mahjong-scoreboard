# PostgreSQL快速入门指南

## 概述

PostgreSQL是一个功能强大的开源关系型数据库系统，具有可靠性、稳定性和丰富的功能集。本文档将帮助您快速开始使用PostgreSQL进行项目开发。

## 当前环境状态

根据检测，您已在本地安装了PostgreSQL 9.3版本，服务名称为`postgresql-x64-9.3`，并且服务状态为运行中。

## PostgreSQL基本使用

### 1. 连接到PostgreSQL

使用我们提供的示例程序连接并初始化数据库：

```bash
# 运行PostgreSQL示例程序
scripts\run_postgresql_example.bat
```

该脚本将自动：
- 检查Java环境
- 编译项目
- 尝试连接到PostgreSQL
- 创建开发数据库`devdb`
- 创建开发用户`devuser`
- 创建测试表和数据
- 验证数据交互功能

### 2. 使用psql命令行工具

```bash
# 连接到默认数据库
psql -U postgres

# 连接到开发数据库
psql -U devuser -d devdb
```

### 3. 基本SQL命令

```sql
-- 列出所有数据库
\l

-- 切换数据库
\c devdb

-- 列出当前数据库中的所有表
\dt

-- 查看表结构
\d 表名

-- 创建表
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL
);

-- 插入数据
INSERT INTO users (username, email) VALUES ('admin', 'admin@example.com');

-- 查询数据
SELECT * FROM users;

-- 退出psql
\q
```

## 数据库配置信息

### 开发环境配置

- **数据库名称**：devdb
- **用户名**：devuser
- **密码**：devpassword
- **主机**：localhost
- **端口**：5432

### Java连接配置

连接字符串格式：
```
jdbc:postgresql://localhost:5432/devdb
```

驱动类：
```
org.postgresql.Driver
```

## 环境配置文件

我们提供了以下配置文件，您可以根据需要进行修改：

1. **PostgreSQL配置文件**：`config/postgresql_config.properties`
   - 包含详细的数据库连接和连接池配置

2. **Spring Boot应用配置**：`java/demo/src/main/resources/application.properties`
   - 已配置好PostgreSQL连接参数

## 常见问题排查

### 连接问题

1. **PostgreSQL服务未启动**
   - 检查服务状态：`Get-Service | Where-Object {$_.Name -like "*PostgreSQL*"}`
   - 启动服务：在Windows服务管理器中启动`postgresql-x64-9.3`服务

2. **密码错误**
   - 默认尝试的超级用户密码是`postgres`
   - 如果连接失败，请检查并修改正确的密码

3. **权限问题**
   - 可能需要修改`pg_hba.conf`文件以允许密码认证
   - PostgreSQL 9.3的配置文件通常位于：`C:\Program Files\PostgreSQL\9.3\data\pg_hba.conf`

### 修改pg_hba.conf文件

1. 使用管理员权限打开`pg_hba.conf`文件
2. 找到以下行并修改：
   ```
   # IPv4 local connections:
   host    all             all             127.0.0.1/32            md5
   ```
   确保认证方法是`md5`（密码认证）而不是`peer`或`ident`
3. 保存文件
4. 重启PostgreSQL服务：
   ```bash
   net stop postgresql-x64-9.3
   net start postgresql-x64-9.3
   ```

## 重置超级用户密码

如果忘记了PostgreSQL超级用户密码，可以按照以下步骤重置：

1. 停止PostgreSQL服务：
   ```bash
   net stop postgresql-x64-9.3
   ```

2. 以单用户模式启动PostgreSQL：
   ```bash
   "C:\Program Files\PostgreSQL\9.3\bin\postgres" --single -D "C:\Program Files\PostgreSQL\9.3\data" postgres
   ```

3. 在单用户模式下执行：
   ```
   ALTER USER postgres WITH PASSWORD 'newpassword';
   \q
   ```

4. 重启PostgreSQL服务：
   ```bash
   net start postgresql-x64-9.3
   ```

## 备份和恢复

### 备份数据库

```bash
# 备份整个数据库
dump -U postgres -d devdb -f devdb_backup.sql
```

### 恢复数据库

```bash
# 恢复数据库
psql -U postgres -d devdb -f devdb_backup.sql
```

## 资源链接

- [PostgreSQL官方文档](https://www.postgresql.org/docs/9.3/)
- [PostgreSQL JDBC驱动文档](https://jdbc.postgresql.org/documentation/)
- [PostgreSQL教程](https://www.postgresqltutorial.com/)

## 总结

您的PostgreSQL环境已基本配置完成，现在可以使用我们提供的示例程序测试连接，并开始进行项目开发。如果您遇到任何问题，请参考本文档的常见问题排查部分，或根据错误提示进行相应的调整。