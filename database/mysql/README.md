# MySQL数据库配置指南

本目录提供MySQL数据库的安装、配置和基本使用指南。

## 步骤1：下载并安装MySQL

### Windows平台安装

1. 访问MySQL官方下载页面（https://dev.mysql.com/downloads/installer/）
2. 下载MySQL Installer
3. 运行安装程序，选择「Developer Default」或「Server Only」选项
4. 按照安装向导完成安装，设置root用户密码

### 使用MySQL Community Server

如果需要单独的MySQL服务器，可以：
1. 下载ZIP格式的MySQL Community Server
2. 解压到指定目录
3. 按照步骤2中的说明进行配置

## 步骤2：配置MySQL

### 基本配置

1. 创建MySQL配置文件（my.ini）在MySQL安装目录下
2. 基本配置示例：

```ini
[mysqld]
# 设置端口
port=3306
# 设置MySQL的安装目录
basedir=C:\Program Files\MySQL\MySQL Server 8.0
# 设置MySQL数据库的数据的存放目录
datadir=C:\Program Files\MySQL\MySQL Server 8.0\data
# 允许最大连接数
max_connections=200
# 允许连接失败的次数。这是为了防止有人从该主机试图攻击数据库系统
max_connect_errors=10
# 服务端使用的字符集默认为UTF8
character-set-server=utf8mb4
# 创建新表时将使用的默认存储引擎
default-storage-engine=INNODB
# 默认使用“mysql_native_password”插件认证
default_authentication_plugin=mysql_native_password
[mysql]
# 设置mysql客户端默认字符集
default-character-set=utf8mb4
[client]
# 设置mysql客户端连接服务端时默认使用的端口
port=3306
default-character-set=utf8mb4
```

## 步骤3：启动MySQL服务

### 通过Windows服务启动

1. 按下Win+R，输入`services.msc`
2. 找到「MySQL80」服务（或相应版本）
3. 右键点击，选择「启动」

### 通过命令行启动

1. 以管理员身份运行命令提示符
2. 启动MySQL服务：
   ```bash
   net start MySQL80
   ```
3. 停止MySQL服务：
   ```bash
   net stop MySQL80
   ```

## 步骤4：连接MySQL

### 使用命令行工具

1. 打开命令提示符
2. 输入以下命令连接MySQL：
   ```bash
   mysql -u root -p
   ```
3. 输入安装时设置的root密码

### 使用MySQL Workbench

1. 下载并安装MySQL Workbench（https://dev.mysql.com/downloads/workbench/）
2. 打开MySQL Workbench，点击「+」添加连接
3. 填写连接信息，点击「Test Connection」测试连接
4. 连接成功后，双击连接名称即可打开数据库

## 步骤5：创建数据库和用户

### 创建新数据库

```sql
CREATE DATABASE test_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 创建用户并授权

```sql
-- 创建用户
CREATE USER 'test_user'@'localhost' IDENTIFIED BY 'password123';

-- 授权
GRANT ALL PRIVILEGES ON test_db.* TO 'test_user'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

## 步骤6：Java连接MySQL

### 添加依赖

#### Maven项目

在pom.xml中添加MySQL连接器依赖：

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.30</version>
</dependency>
```

#### Gradle项目

在build.gradle中添加：

```groovy
dependencies {
    implementation 'mysql:mysql-connector-java:8.0.30'
}
```

### 连接示例

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC";
        String username = "test_user";
        String password = "password123";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            System.out.println("数据库连接成功！");
        } catch (SQLException e) {
            System.err.println("数据库连接失败！");
            e.printStackTrace();
        }
    }
}
```

## 数据备份与恢复

### 备份数据库

```bash
mysqldump -u root -p test_db > test_db_backup.sql
```

### 恢复数据库

```bash
mysql -u root -p test_db < test_db_backup.sql
```

## 常见问题解决

1. **端口冲突**：如果3306端口被占用，可以在my.ini中修改端口号
2. **字符集问题**：确保配置文件中设置了正确的字符集（utf8mb4）
3. **连接失败**：检查防火墙设置，确保MySQL服务正在运行

## 推荐的MySQL客户端工具

- MySQL Workbench（官方工具）
- Navicat for MySQL
- DBeaver（开源）
- HeidiSQL（轻量级）