# 为收盘接口添加详细日志输出

## 修改内容

为`MatchServiceImpl`中的两个`endMatch`方法添加了详细的日志输出，帮助调试问题。

## 添加的日志内容

### 1. 方法开始日志
```java
logger.info("=== 开始收盘对局 ===");
logger.info("对局ID: {}", id);
```

### 2. 对局记录查找日志
```java
logger.info("找到对局记录: matchId={}, status={}, totalRounds={}", 
           u.getMatchId(), u.getStatus(), u.getTotalRounds());
```

### 3. 状态更新日志
```java
logger.info("更新对局状态为已完成，设置结束时间: {}", u.getEndTime());
```

### 4. 获胜者计算日志
```java
logger.info("开始计算对局结果...");
MatchParticipant w = getMatchWinner(id);
logger.info("获胜者: {}", w != null ? w.getId() : "无");
```

### 5. 数据库操作日志
```java
logger.info("删除现有的对局结果记录...");
mdao.deleteByMatchId(id);
logger.info("已删除现有对局结果记录，对局ID: {}", id);

logger.info("创建新的对局结果记录...");
MatchResult r = new MatchResult();
r.setMatchId(id);
logger.info("设置MatchResult的matchId: {}", id);
```

### 6. 得分计算日志
```java
logger.info("开始计算最高分和最低分...");
List<MatchParticipant> ps = pdao.findByMatch(u);
logger.info("找到参与者数量: {}", ps.size());

for (MatchParticipant p : ps) {
    logger.info("参与者ID: {}, 总得分: {}", p.getId(), p.getTotalScore());
    // ... 计算逻辑
}

logger.info("计算完成 - 最高分: {}, 最低分: {}", h, l);
```

### 7. 时间计算日志
```java
logger.info("开始设置时间和轮次信息...");
long completionTime = System.currentTimeMillis();
r.setCompletionTime(completionTime);
logger.info("设置完成时间: {}", completionTime);

if (u.getCreateTime() != null) {
    long totalDuration = completionTime - u.getCreateTime();
    r.setTotalDuration(totalDuration);
    logger.info("计算总时长: {} ms (基于创建时间)", totalDuration);
}
```

### 8. JSON生成日志
```java
logger.info("开始生成参与者得分信息JSON...");
for (MatchParticipant p : ps) {
    logger.info("处理参与者: ID={}, 总得分={}", p.getId(), p.getTotalScore());
    
    // 计算最终得分
    if (u.getSettlementMultiplier() != null && u.getSettlementMultiplier() > 0) {
        finalScore = (int) Math.round(p.getTotalScore() * u.getSettlementMultiplier());
        logger.info("应用结算倍率: {} -> {}", p.getTotalScore(), finalScore);
    }
    
    // 用户信息获取
    if (p.getUser() != null) {
        logger.info("从User对象获取信息: 昵称={}, 头像={}", nickname, avatar);
    } else {
        logger.info("使用MatchParticipant中的信息: 昵称={}", nickname);
    }
    
    logger.info("添加参与者得分信息: ID={}, 昵称={}, 最终得分={}, 是否获胜={}", 
               p.getId(), nickname, finalScore, p.equals(w));
}

logger.info("成功生成参与者得分信息JSON，长度: {}", totalScoresJson.length());
logger.debug("JSON内容: {}", totalScoresJson);
```

### 9. 数据库保存日志
```java
logger.info("开始保存MatchResult到数据库...");
mdao.save(r);
logger.info("MatchResult保存成功，matchId: {}", r.getMatchId());

logger.info("开始保存Match到数据库...");
Match savedMatch = dao.save(u);
logger.info("Match保存成功，matchId: {}", savedMatch.getMatchId());

logger.info("=== 收盘对局完成 ===");
```

## 日志级别

- **INFO**: 正常流程信息
- **DEBUG**: 详细的调试信息（如JSON内容）
- **WARN**: 警告信息（如无法计算总时长）
- **ERROR**: 错误信息（如序列化失败）

## 使用方法

1. 确保日志级别设置为INFO或DEBUG
2. 调用收盘接口
3. 查看控制台输出，跟踪每一步的执行情况
4. 根据日志信息定位问题所在

## 预期效果

通过这些详细的日志，可以：
1. 跟踪整个收盘流程的执行
2. 快速定位问题发生的具体步骤
3. 验证数据是否正确计算和保存
4. 调试数据库操作是否成功

现在当您调用收盘接口时，会看到详细的执行日志，帮助您快速定位`total_rounds`字段不存在的问题。
