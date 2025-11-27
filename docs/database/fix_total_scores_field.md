# 修复total_scores字段缺失问题

## 问题描述

错误信息：`字段 mr1_0.total_rounds 不存在`

经过分析发现，真正的问题是：
1. 数据库表`match_results`缺少`total_scores`字段
2. 实体类`MatchResult`中定义了`total_scores`字段用于存储JSON数据
3. 但数据库表中没有这个字段

## 解决方案

### 1. 添加数据库字段

执行以下SQL脚本添加`total_scores`字段：

```sql
-- 添加total_scores字段
ALTER TABLE match_results 
ADD COLUMN IF NOT EXISTS total_scores TEXT;

-- 添加注释
COMMENT ON COLUMN match_results.total_scores IS '参与者得分信息JSON数据';
```

### 2. 更新数据库表结构文件

已更新`simplified_database_schema.sql`文件，在`match_results`表中添加了`total_scores`字段：

```sql
CREATE TABLE IF NOT EXISTS match_results (
    match_id BIGINT PRIMARY KEY,  -- 对局ID（主键）
    winner_id BIGINT,             -- 获胜者ID
    highest_score INTEGER,        -- 最高分
    lowest_score INTEGER,         -- 最低分
    total_duration BIGINT,        -- 对局总时长（毫秒）
    total_scores TEXT,            -- 参与者得分信息JSON数据  ← 新增字段
    total_rounds INTEGER,         -- 总轮次
    completion_time BIGINT,       -- 完成时间
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES match_participants(id)
);
```

### 3. 实体类字段映射

`MatchResult.java`中的字段映射是正确的：

```java
@Column(name = "total_scores", columnDefinition = "TEXT")
private String totalScores;
```

## 执行步骤

1. **执行数据库更新脚本**：
   ```bash
   psql -U postgres -d mahjong_score_system -f add_total_scores_field.sql
   ```

2. **验证字段添加成功**：
   ```sql
   SELECT column_name, data_type, is_nullable
   FROM information_schema.columns 
   WHERE table_name = 'match_results' 
   AND column_name = 'total_scores';
   ```

3. **重启应用程序**：
   重启Spring Boot应用以加载新的数据库结构

4. **测试收盘接口**：
   调用收盘接口，查看详细日志输出

## 预期结果

添加`total_scores`字段后，收盘接口应该能够：
1. 成功保存`MatchResult`记录
2. 将参与者得分信息序列化为JSON并存储到`total_scores`字段
3. 不再出现字段不存在的错误

## 相关文件

- `add_total_scores_field.sql` - 数据库更新脚本
- `simplified_database_schema.sql` - 更新的表结构定义
- `MatchResult.java` - 实体类（无需修改）
- `MatchServiceImpl.java` - 服务实现（已添加详细日志）
