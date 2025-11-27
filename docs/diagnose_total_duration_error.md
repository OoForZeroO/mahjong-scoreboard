# 诊断total_duration字段不存在的错误

## 问题描述

错误信息：`字段 mr1_0.total_duration 不存在`

虽然您确认数据库有这个字段，但应用仍然报错。这可能是以下原因之一：

## 诊断方法

### 方法1：使用SQL脚本检查
执行以下SQL脚本检查数据库表结构：

```bash
psql -U postgres -d mahjong_score_system -f test_database_connection.sql
```

### 方法2：使用Java程序检查
编译并运行Java测试程序：

```bash
# 编译
javac -cp "lib/postgresql-42.5.4.jar" TestDatabaseConnection.java

# 运行
java -cp ".;lib/postgresql-42.5.4.jar" TestDatabaseConnection
```

### 方法3：手动检查
连接到数据库并手动检查：

```sql
-- 连接到正确的数据库
\c mahjong_score_system

-- 检查表结构
\d match_results

-- 检查total_duration字段
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name = 'total_duration';
```

## 可能的原因和解决方案

### 1. 数据库连接问题
**问题**：应用连接的不是您检查的数据库
**解决**：确认应用配置中的数据库名称

检查 `application.properties`：
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mahjong_score_system
```

### 2. 表名大小写问题
**问题**：PostgreSQL对表名大小写敏感
**解决**：确保表名完全匹配

检查实体类：
```java
@Table(name = "match_results")  // 确保与数据库表名完全一致
```

### 3. 字段名大小写问题
**问题**：字段名可能不匹配
**解决**：检查字段名是否完全一致

检查实体类：
```java
@Column(name = "total_duration")  // 确保与数据库字段名完全一致
```

### 4. 数据库权限问题
**问题**：应用用户没有访问该字段的权限
**解决**：检查数据库用户权限

```sql
-- 检查用户权限
SELECT * FROM information_schema.role_table_grants 
WHERE table_name = 'match_results';

-- 授予权限
GRANT SELECT, INSERT, UPDATE, DELETE ON match_results TO postgres;
```

### 5. 缓存问题
**问题**：Hibernate缓存了旧的表结构
**解决**：清除缓存并重启应用

在 `application.properties` 中添加：
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.hbm2ddl.auto=validate
```

## 快速修复方案

如果确认数据库有该字段，可以尝试以下修复：

### 方案1：强制刷新Hibernate
```properties
# 在application.properties中添加
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.hbm2ddl.auto=update
```

### 方案2：重启数据库服务
```bash
# Windows
net stop postgresql-x64-9.3
net start postgresql-x64-9.3

# Linux/Mac
sudo systemctl restart postgresql
```

### 方案3：重新创建表
如果表结构有问题，可以重新创建：

```sql
-- 备份数据
CREATE TABLE match_results_backup AS SELECT * FROM match_results;

-- 删除表
DROP TABLE match_results CASCADE;

-- 重新创建表（使用simplified_database_schema.sql）
-- 然后恢复数据
```

## 验证步骤

1. 运行诊断脚本确认表结构
2. 检查应用日志中的SQL语句
3. 确认数据库连接配置正确
4. 重启应用测试

请先运行诊断脚本，然后告诉我结果，我可以根据具体情况提供更精确的解决方案。
