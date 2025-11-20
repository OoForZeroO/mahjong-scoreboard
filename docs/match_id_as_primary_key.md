# 使用match_id作为match_results表主键的设计说明

## 设计决策

将`match_results`表的主键从独立的`result_id`改为`match_id`，这是一个更好的设计选择。

## 优势分析

### 1. **业务逻辑清晰**
- 每个对局只能有一个结果记录（一对一关系）
- 对局ID直接对应结果ID，逻辑关系明确
- 避免了额外的ID字段，减少数据冗余

### 2. **查询效率更高**
- 通过对局ID直接查询结果，无需JOIN操作
- 主键查询是最快的查询方式
- 减少了索引维护的开销

### 3. **数据一致性更好**
- 天然防止重复的结果记录
- 对局删除时，结果记录自动级联删除
- 避免了数据不一致的问题

### 4. **代码简化**
- 不需要生成额外的ID
- 保存和查询逻辑更简单
- 减少了null identifier错误的可能性

## 数据库表结构

```sql
CREATE TABLE match_results (
    match_id BIGINT PRIMARY KEY,  -- 对局ID（主键）
    winner_id BIGINT,             -- 获胜者ID
    highest_score INTEGER,        -- 最高分
    lowest_score INTEGER,         -- 最低分
    completion_time BIGINT,       -- 完成时间
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES match_participants(id)
);
```

## 实体类定义

```java
@Entity
@Table(name = "match_results")
public class MatchResult {
    @Id
    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @OneToOne
    @JoinColumn(name = "match_id", insertable = false, updatable = false)
    private Match match;

    // ... 其他字段
}
```

## 使用方式

### 保存结果
```java
MatchResult result = new MatchResult();
result.setMatchId(matchId);  // 直接设置对局ID作为主键
result.setWinner(winner);
// ... 设置其他字段
matchResultRepository.save(result);
```

### 查询结果
```java
// 通过对局ID查询结果
Optional<MatchResult> result = matchResultRepository.findById(matchId);

// 或者通过方法查询
Optional<MatchResult> result = matchResultRepository.findByMatchId(matchId);
```

## 与之前设计的对比

| 方面 | 使用result_id | 使用match_id |
|------|---------------|--------------|
| 主键 | 独立的result_id | match_id |
| 唯一性 | 需要额外约束 | 天然唯一 |
| 查询效率 | 需要JOIN | 直接查询 |
| 数据一致性 | 可能重复 | 天然防重复 |
| 代码复杂度 | 较复杂 | 更简单 |
| 存储空间 | 多一个字段 | 节省空间 |

## 注意事项

1. **外键约束**：确保`match_id`有正确的外键约束
2. **级联删除**：对局删除时，结果记录会自动删除
3. **查询优化**：不需要为`match_id`创建额外索引（已经是主键）
4. **数据迁移**：如果从旧结构迁移，需要删除`result_id`字段

## 总结

使用`match_id`作为`match_results`表的主键是一个更优雅、更高效的设计选择。它简化了代码逻辑，提高了查询效率，并确保了数据的一致性。这种设计符合业务逻辑，是数据库设计的最佳实践。
