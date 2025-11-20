# 修改后的GET /api/v1/matches/{matchId}/result接口

## 接口说明

**URL**: `GET /api/v1/matches/{matchId}/result`  
**描述**: 获取对局结果数据，包含结果表（match_results）的所有数据

## 修改内容

### 修改前
- 返回`MatchSettlement`数据（结算记录）
- 只包含结算相关信息

### 修改后
- 返回`MatchResultResponse`数据（对局结果）
- 包含结果表的所有数据

## 请求参数

- `matchId` (路径参数): 对局ID

## 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 123,
    "winnerId": 456,
    "winnerNickname": "张三",
    "winnerAvatar": "https://example.com/avatar1.jpg",
    "highestScore": 1200,
    "lowestScore": 800,
    "totalDuration": 3600000,
    "totalScores": "[{\"participantId\":456,\"nickname\":\"张三\",\"avatar\":\"https://example.com/avatar1.jpg\",\"totalScore\":1200,\"finalScore\":1200,\"isWinner\":true,\"wechatUserId\":\"wx123456\"}]",
    "participantScores": [
      {
        "participantId": 456,
        "nickname": "张三",
        "avatar": "https://example.com/avatar1.jpg",
        "totalScore": 1200,
        "finalScore": 1200,
        "isWinner": true,
        "wechatUserId": "wx123456"
      },
      {
        "participantId": 789,
        "nickname": "李四",
        "avatar": "https://example.com/avatar2.jpg",
        "totalScore": 800,
        "finalScore": 800,
        "isWinner": false,
        "wechatUserId": "wx789012"
      }
    ],
    "completionTime": 1701234567890,
    "createTime": 1701234567890,
    "updateTime": 1701234567890
  }
}
```

## 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| matchId | Long | 对局ID |
| winnerId | Long | 获胜者ID |
| winnerNickname | String | 获胜者昵称 |
| winnerAvatar | String | 获胜者头像 |
| highestScore | Integer | 最高分 |
| lowestScore | Integer | 最低分 |
| totalDuration | Long | 对局总时长（毫秒） |
| totalScores | String | 参与者得分信息JSON字符串 |
| participantScores | Array | 解析后的参与者得分信息数组 |
| completionTime | Long | 完成时间戳 |
| createTime | Long | 创建时间戳 |
| updateTime | Long | 更新时间戳 |

## participantScores数组字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| participantId | Long | 参与者ID |
| nickname | String | 参与者昵称 |
| avatar | String | 参与者头像 |
| totalScore | Integer | 总得分 |
| finalScore | Integer | 最终得分（倍率后） |
| isWinner | Boolean | 是否为获胜者 |
| wechatUserId | String | 微信用户ID |

## 错误响应

### 对局结果不存在
```json
{
  "code": 404,
  "message": "对局结果不存在",
  "data": null
}
```

### 服务器错误
```json
{
  "code": 500,
  "message": "获取对局结果失败: 错误详情",
  "data": null
}
```

## 使用场景

1. **对局结束后查看结果**: 获取完整的对局结果信息
2. **历史记录查询**: 查看已完成对局的详细结果
3. **数据统计**: 获取对局统计数据用于分析
4. **前端展示**: 为前端提供完整的对局结果数据

## 注意事项

1. 只有已完成的对局才会有结果数据
2. `totalScores`字段包含JSON字符串格式的原始数据
3. `participantScores`字段是解析后的结构化数据，便于前端使用
4. 如果对局未完成，将返回404错误
