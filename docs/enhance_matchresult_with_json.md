# 增强MatchResult记录，添加参与者JSON数据

## 需求描述

在给结果表创建数据时，需要：
1. 将参与者的所有最终得分（倍率之后的分数）、参与者信息（头像、名称）以JSON数据格式存入total_scores字段
2. 将当前时间与match表的创建时间相减得出的时间存入total_duration表

## 实现方案

### 1. 创建参与者得分信息DTO类

创建了`ParticipantScoreInfo`类来封装参与者信息：

```java
public class ParticipantScoreInfo {
    @JsonProperty("participantId")
    private Long participantId;
    
    @JsonProperty("nickname")
    private String nickname;
    
    @JsonProperty("avatar")
    private String avatar;
    
    @JsonProperty("totalScore")
    private Integer totalScore;
    
    @JsonProperty("finalScore")
    private Integer finalScore; // 倍率后的最终得分
    
    @JsonProperty("isWinner")
    private Boolean isWinner;
    
    @JsonProperty("wechatUserId")
    private String wechatUserId;
}
```

### 2. 修改endMatch方法

在两个endMatch方法中都添加了JSON序列化功能：

```java
// 生成参与者得分信息的JSON数据
try {
    List<ParticipantScoreInfo> participantScores = new ArrayList<>();
    for (MatchParticipant p : ps) {
        // 计算倍率后的最终得分
        Integer finalScore = p.getTotalScore();
        if (u.getSettlementMultiplier() != null && u.getSettlementMultiplier() > 0) {
            finalScore = (int) Math.round(p.getTotalScore() * u.getSettlementMultiplier());
        }
        
        // 获取用户信息
        String nickname = p.getUserName();
        String avatar = null;
        String wechatUserId = p.getWechatUserId();
        
        if (p.getUser() != null) {
            nickname = p.getUser().getNickname();
            avatar = p.getUser().getAvatar();
            wechatUserId = p.getUser().getUserId();
        }
        
        ParticipantScoreInfo scoreInfo = new ParticipantScoreInfo(
            p.getId(),
            nickname,
            avatar,
            p.getTotalScore(),
            finalScore,
            p.equals(w), // 是否为获胜者
            wechatUserId
        );
        participantScores.add(scoreInfo);
    }
    
    // 序列化为JSON
    String totalScoresJson = objectMapper.writeValueAsString(participantScores);
    r.setTotalScores(totalScoresJson);
    logger.info("Generated participant scores JSON: {}", totalScoresJson);
    
} catch (Exception ex) {
    logger.error("Failed to generate participant scores JSON", ex);
    r.setTotalScores("[]"); // 设置空数组作为默认值
}
```

### 3. 修复total_duration计算逻辑

根据需求，total_duration应该是当前时间与match表的创建时间相减：

```java
// 计算总时长（毫秒）- 当前时间与match表的创建时间相减
if (u.getCreateTime() != null) {
    long totalDuration = completionTime - u.getCreateTime();
    r.setTotalDuration(totalDuration);
    logger.info("Match total duration: {} ms (from create time)", totalDuration);
} else if (u.getStartTime() != null) {
    // 如果createTime不存在，使用startTime作为备选
    long totalDuration = completionTime - u.getStartTime();
    r.setTotalDuration(totalDuration);
    logger.info("Match total duration: {} ms (from start time)", totalDuration);
}
```

## JSON数据结构示例

生成的total_scores字段将包含如下JSON数据：

```json
[
  {
    "participantId": 1,
    "nickname": "小张",
    "avatar": "https://example.com/avatar1.jpg",
    "totalScore": 100,
    "finalScore": 150,
    "isWinner": true,
    "wechatUserId": "wx_user_123"
  },
  {
    "participantId": 2,
    "nickname": "小李",
    "avatar": "https://example.com/avatar2.jpg",
    "totalScore": -50,
    "finalScore": -75,
    "isWinner": false,
    "wechatUserId": "wx_user_456"
  }
]
```

## 功能特点

### 1. 完整参与者信息
- **participantId**: 参与者ID
- **nickname**: 用户昵称（从WechatUser获取）
- **avatar**: 用户头像（从WechatUser获取）
- **wechatUserId**: 微信用户ID

### 2. 得分信息
- **totalScore**: 原始总得分
- **finalScore**: 倍率后的最终得分
- **isWinner**: 是否为获胜者

### 3. 倍率计算
- 如果设置了settlementMultiplier，会计算倍率后的得分
- 使用四舍五入确保得分为整数

### 4. 错误处理
- 如果JSON序列化失败，会设置空数组作为默认值
- 添加了详细的日志记录便于调试

### 5. 时间计算
- 优先使用createTime计算总时长
- 如果createTime不存在，使用startTime作为备选

## 优势

1. **数据完整性**: 包含所有参与者的详细信息
2. **倍率支持**: 正确计算倍率后的最终得分
3. **用户信息**: 包含头像、昵称等用户信息
4. **JSON格式**: 便于前端解析和显示
5. **错误处理**: 健壮的错误处理机制
6. **日志记录**: 详细的日志便于调试

## 验证步骤

1. 重启应用
2. 测试收盘接口
3. 检查数据库中的match_results表
4. 验证total_scores字段是否包含正确的JSON数据
5. 验证total_duration字段是否正确计算

现在MatchResult记录将包含完整的参与者信息和正确的时长计算！
