# 修复收盘时没有插入最终轮次的问题

## 问题描述

收盘后插入结果表时没有插入最终轮次，缺少completionTime和totalRounds字段的设置。

## 修复内容

### 1. 添加completionTime设置
在两个endMatch方法中都添加了completionTime的设置：

**修复前**：
```java
// 第一个endMatch方法中没有设置completionTime
mdao.save(r);
```

**修复后**：
```java
// 设置完成时间和总时长
long completionTime = System.currentTimeMillis();
r.setCompletionTime(completionTime);

// 计算总时长（毫秒）
if (u.getStartTime() != null) {
    long totalDuration = completionTime - u.getStartTime();
    r.setTotalDuration(totalDuration);
    logger.info("Match total duration: {} ms", totalDuration);
}

// 设置总轮次
r.setTotalRounds(u.getTotalRounds());
logger.info("Match total rounds: {}", u.getTotalRounds());

mdao.save(r);
```

### 2. 添加totalRounds字段到MatchResult实体类
在MatchResult实体类中添加了totalRounds字段：

```java
@Column(name = "total_rounds")
private Integer totalRounds;

// 添加getter和setter方法
public Integer getTotalRounds() {
    return totalRounds;
}

public void setTotalRounds(Integer totalRounds) {
    this.totalRounds = totalRounds;
}
```

### 3. 完善MatchResult记录
现在MatchResult记录包含以下完整信息：

- **matchId**: 对局ID（主键）
- **winnerId**: 获胜者ID
- **highestScore**: 最高分
- **lowestScore**: 最低分
- **totalDuration**: 对局总时长（毫秒）
- **completionTime**: 完成时间戳
- **totalRounds**: 总轮次
- **createTime**: 创建时间
- **updateTime**: 更新时间

## 修复后的endMatch方法逻辑

### 1. 简单结束对局
```java
@Override
@Transactional
public Match endMatch(Long id) {
    // 1. 更新对局状态
    u.setStatus(1); // 1:已完成
    u.setEndTime(System.currentTimeMillis());
    
    // 2. 删除现有结果记录
    mdao.deleteByMatchId(id);
    
    // 3. 创建新的结果记录
    MatchResult r = new MatchResult();
    r.setMatchId(id);
    r.setWinner(w);
    r.setHighestScore(h);
    r.setLowestScore(l);
    
    // 4. 设置时间和轮次信息
    long completionTime = System.currentTimeMillis();
    r.setCompletionTime(completionTime);
    r.setTotalRounds(u.getTotalRounds());
    
    // 5. 计算总时长
    if (u.getStartTime() != null) {
        long totalDuration = completionTime - u.getStartTime();
        r.setTotalDuration(totalDuration);
    }
    
    // 6. 保存记录
    mdao.save(r);
    return dao.save(u);
}
```

### 2. 带参数结束对局
```java
@Override
@Transactional
public Match endMatch(Long id, EndMatchRequest request) {
    // 1. 更新对局状态和参数
    u.setStatus(1);
    u.setEndTime(System.currentTimeMillis());
    if (request.getRoomName() != null) {
        u.setRoomName(request.getRoomName());
    }
    if (request.getMultiplier() != null) {
        u.setSettlementMultiplier(request.getMultiplier());
    }
    
    // 2. 创建结果记录（同简单结束对局）
    // ... 相同的逻辑
}
```

## 优势

1. **完整记录**：MatchResult现在包含所有必要的信息
2. **时间统计**：准确记录对局完成时间和总时长
3. **轮次统计**：记录对局的总轮次
4. **日志记录**：添加了详细的日志便于调试
5. **数据一致性**：确保所有endMatch方法都设置相同的字段

## 验证步骤

1. 重启应用
2. 测试收盘接口
3. 检查数据库中的match_results表
4. 验证completionTime、totalDuration、totalRounds字段是否正确设置

现在收盘接口应该能够正确插入包含最终轮次和完成时间的完整MatchResult记录。
