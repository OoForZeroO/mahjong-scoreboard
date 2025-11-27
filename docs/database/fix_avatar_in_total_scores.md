# 修复total_scores字段中缺少头像数据的问题

## 问题描述

在`total_scores`字段的JSON数据中缺少头像字段的数据。

## 问题原因

在收盘接口中，获取用户信息时只从`p.getUser()`对象中获取头像，但如果`p.getUser()`为null，就没有设置头像。实际上`MatchParticipant`实体类中已经有`avatar`字段。

## 修复方案

### 修改前的问题代码
```java
// 获取用户信息
String nickname = p.getUserName();
String avatar = null;  // 初始化为null
String wechatUserId = p.getWechatUserId();

if (p.getUser() != null) {
    nickname = p.getUser().getNickname();
    avatar = p.getUser().getAvatar();
    wechatUserId = p.getUser().getUserId();
    logger.info("从User对象获取信息: 昵称={}, 头像={}", nickname, avatar);
} else {
    logger.info("使用MatchParticipant中的信息: 昵称={}", nickname);
    // 这里没有设置avatar，导致头像为null
}
```

### 修改后的正确代码
```java
// 获取用户信息
String nickname = p.getUserName();
String avatar = p.getAvatar(); // 直接从MatchParticipant获取头像
String wechatUserId = p.getWechatUserId();

if (p.getUser() != null) {
    // 如果User对象存在，优先使用User中的信息
    nickname = p.getUser().getNickname();
    if (p.getUser().getAvatar() != null && !p.getUser().getAvatar().trim().isEmpty()) {
        avatar = p.getUser().getAvatar();
    }
    wechatUserId = p.getUser().getUserId();
    logger.info("从User对象获取信息: 昵称={}, 头像={}", nickname, avatar);
} else {
    logger.info("使用MatchParticipant中的信息: 昵称={}, 头像={}", nickname, avatar);
}
```

## 修复内容

1. **直接从MatchParticipant获取头像**：`String avatar = p.getAvatar();`
2. **优先使用User中的头像**：如果User对象存在且头像不为空，则使用User中的头像
3. **添加头像日志**：在日志中显示头像信息，便于调试

## 数据流程

1. **MatchParticipant表**：存储参与者的基本信息，包括头像
2. **收盘接口**：从MatchParticipant中获取头像信息
3. **ParticipantScoreInfo**：接收头像数据
4. **JSON序列化**：将包含头像的完整信息序列化为JSON
5. **total_scores字段**：存储完整的参与者信息JSON

## 预期结果

现在`total_scores`字段的JSON数据应该包含完整的参与者信息：

```json
[
  {
    "participantId": 123,
    "nickname": "张三",
    "avatar": "https://example.com/avatar1.jpg",
    "totalScore": 1200,
    "finalScore": 1200,
    "isWinner": true,
    "wechatUserId": "wx123456"
  },
  {
    "participantId": 124,
    "nickname": "李四",
    "avatar": "https://example.com/avatar2.jpg",
    "totalScore": 800,
    "finalScore": 800,
    "isWinner": false,
    "wechatUserId": "wx789012"
  }
]
```

## 测试建议

1. 重启应用程序
2. 调用收盘接口
3. 检查日志中的头像信息
4. 查看`total_scores`字段的JSON数据是否包含头像
