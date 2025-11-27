# 修复match_settlements表不存在的错误

## 问题描述

错误信息：`关系 "match_settlements" 不存在`

这是因为代码中使用了`MatchSettlementRepository`，但数据库中没有`match_settlements`表。

## 解决方案

### 临时解决方案
暂时注释掉所有使用`MatchSettlementRepository`的代码，因为：
1. 数据库中没有`match_settlements`表
2. 我们现在主要使用`MatchResult`来存储对局结果
3. 可以通过对局状态来判断是否已结算

### 修改内容

#### 1. 注释掉MatchSettlementRepository注入
```java
// 暂时注释掉MatchSettlementRepository，因为数据库中没有match_settlements表
// @Autowired
// private MatchSettlementRepository sdao;
```

#### 2. 修改settleMatch方法
```java
@Override
@Transactional
public MatchSettlement settleMatch(Long id, Double multiplier, String notes) {
    // 暂时注释掉，因为数据库中没有match_settlements表
    throw new RuntimeException("结算功能暂时不可用，因为数据库中没有match_settlements表");
}
```

#### 3. 修改getMatchSettlement方法
```java
@Override
public Optional<MatchSettlement> getMatchSettlement(Long id) {
    // 暂时注释掉，因为数据库中没有match_settlements表
    return Optional.empty();
}
```

#### 4. 修改isMatchSettled方法
```java
@Override
public boolean isMatchSettled(Long id) {
    // 通过检查对局状态来判断是否已结算
    Optional<Match> match = dao.findById(id);
    return match.isPresent() && match.get().getStatus() == 1; // 状态为1表示已完成
}
```

#### 5. 注释掉deleteMatch中的结算记录删除
```java
// 4. 删除对局结算记录（如果存在）
// 暂时注释掉，因为数据库中没有match_settlements表
```

## 当前状态

- ✅ `GET /api/v1/matches/{matchId}/settlement` 接口现在返回`MatchResult`数据
- ✅ 不再依赖`match_settlements`表
- ✅ 通过`match_results`表获取对局结果
- ⚠️ 结算功能暂时不可用（需要创建`match_settlements`表）

## 长期解决方案

如果需要完整的结算功能，可以：

1. **创建match_settlements表**：
```sql
CREATE TABLE IF NOT EXISTS match_settlements (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    multiplier DECIMAL(10,2) NOT NULL,
    settlement_time BIGINT NOT NULL,
    notes TEXT,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id)
);
```

2. **恢复相关代码**：
   - 取消注释`MatchSettlementRepository`注入
   - 恢复`settleMatch`、`getMatchSettlement`等方法

## 测试建议

1. 重启应用程序
2. 测试`GET /api/v1/matches/{matchId}/settlement`接口
3. 确认不再出现`match_settlements`表不存在的错误
4. 验证接口返回`MatchResult`数据
