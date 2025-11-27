# 修复total_rounds字段不存在的错误

## 问题描述

错误信息：`字段 mr1_0.total_rounds 不存在`

虽然数据库已经有这个字段，但应用仍然报错。

## 可能的原因

### 1. Hibernate缓存问题
Hibernate可能缓存了旧的表结构，需要清除缓存。

### 2. 数据库连接问题
应用可能连接的不是您检查的数据库实例。

### 3. 字段名大小写问题
PostgreSQL对字段名大小写敏感，可能存在大小写不匹配。

### 4. 表名问题
可能连接的是不同的表或schema。

## 诊断步骤

### 步骤1：检查数据库连接
确认应用连接的是正确的数据库：

1. 检查`application.properties`中的数据库配置：
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/mahjong_score_system
   spring.datasource.username=postgres
   spring.datasource.password=cch815566
   ```

2. 执行诊断脚本：
   ```bash
   psql -U postgres -d mahjong_score_system -f diagnose_total_rounds_issue.sql
   ```

### 步骤2：检查字段名
确认数据库中的字段名与实体类中的完全匹配：

```sql
-- 检查字段名（不区分大小写）
SELECT column_name, data_type
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name ILIKE '%total_rounds%';
```

### 步骤3：清除Hibernate缓存
在`application.properties`中添加：

```properties
# 强制Hibernate重新检查表结构
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.hbm2ddl.auto=validate

# 清除Hibernate缓存
spring.jpa.properties.hibernate.cache.use_second_level_cache=false
spring.jpa.properties.hibernate.cache.use_query_cache=false
```

### 步骤4：重启应用
完全重启应用以清除所有缓存。

## 解决方案

### 方案1：强制刷新Hibernate
```properties
# 在application.properties中临时添加
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.hbm2ddl.auto=update
```

### 方案2：检查字段名大小写
如果数据库字段名是`Total_Rounds`或`TOTAL_ROUNDS`，修改实体类：

```java
@Column(name = "Total_Rounds")  // 或 "TOTAL_ROUNDS"
private Integer totalRounds;
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

1. 执行诊断脚本确认数据库状态
2. 检查应用日志中的SQL语句
3. 确认数据库连接配置正确
4. 重启应用测试

## 注意事项

1. **不要使用临时解决方案**：应该找到根本原因并解决
2. **检查数据库连接**：确保应用连接的是正确的数据库
3. **字段名匹配**：确保实体类中的字段名与数据库完全匹配
4. **清除缓存**：重启应用以清除Hibernate缓存

请先执行诊断脚本，然后告诉我结果，我可以根据具体情况提供更精确的解决方案。
