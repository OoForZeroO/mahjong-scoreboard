# 从结果表中移除total_rounds字段

## 修改内容

根据您的要求，从`match_results`表中移除了`total_rounds`字段，因为结果表不需要存储总轮次信息。

## 修改的文件

### 1. MatchResult.java
- 注释掉了`total_rounds`字段定义
- 注释掉了相关的getter和setter方法

```java
// 移除total_rounds字段，不需要在结果表中存储
// @Column(name = "total_rounds")
// private Integer totalRounds;

// 移除totalRounds字段的getter和setter方法
// public Integer getTotalRounds() {
//     return totalRounds;
// }

// public void setTotalRounds(Integer totalRounds) {
//     this.totalRounds = totalRounds;
// }
```

### 2. MatchServiceImpl.java
- 移除了收盘接口中对`setTotalRounds`的调用
- 添加了日志说明跳过total_rounds字段设置

```java
// 不需要设置total_rounds字段
logger.info("跳过total_rounds字段设置");
```

### 3. simplified_database_schema.sql
- 从`match_results`表定义中移除了`total_rounds`字段

```sql
CREATE TABLE IF NOT EXISTS match_results (
    match_id BIGINT PRIMARY KEY,  -- 对局ID（主键）
    winner_id BIGINT,             -- 获胜者ID
    highest_score INTEGER,        -- 最高分
    lowest_score INTEGER,         -- 最低分
    total_duration BIGINT,        -- 对局总时长（毫秒）
    total_scores TEXT,            -- 参与者得分信息JSON数据
    completion_time BIGINT,       -- 完成时间
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES match_participants(id)
);
```

## 数据库更新

执行以下SQL脚本来更新现有数据库：

```bash
psql -U postgres -d mahjong_score_system -f remove_total_rounds_field.sql
```

## 现在的字段结构

`match_results`表现在只包含以下字段：
- `match_id` - 对局ID（主键）
- `winner_id` - 获胜者ID
- `highest_score` - 最高分
- `lowest_score` - 最低分
- `total_duration` - 对局总时长
- `total_scores` - 参与者得分信息JSON数据
- `completion_time` - 完成时间
- `create_time` - 创建时间
- `update_time` - 更新时间

## 预期效果

1. 收盘接口不再尝试设置`total_rounds`字段
2. 数据库表结构与实体类完全匹配
3. 不再出现字段不存在的错误
4. 收盘接口应该能够正常工作

## 测试建议

1. 执行数据库更新脚本
2. 重启应用程序
3. 调用收盘接口测试
4. 查看详细日志确认没有字段相关错误
