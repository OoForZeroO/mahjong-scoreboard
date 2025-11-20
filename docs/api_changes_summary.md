# API接口变更总结

## 主要变更

### 1. 接口URL变更

#### 原接口 → 新接口
- `GET /api/v1/matches/{matchId}/settlement` → `GET /api/v1/matches/{matchId}/result`
- `GET /api/v1/matches/{matchId}/is-settled` → `GET /api/v1/matches/{matchId}/is-completed`

#### 暂时禁用的接口
- `POST /api/v1/matches/{matchId}/settle` - 暂时注释掉（需要match_settlements表）

### 2. 接口功能变更

#### GET /api/v1/matches/{matchId}/result
- **原功能**: 返回结算记录（MatchSettlement）
- **新功能**: 返回对局结果数据（MatchResultResponse）
- **包含数据**: 结果表的所有字段，包括参与者得分信息

#### GET /api/v1/matches/{matchId}/is-completed
- **原功能**: 检查是否已结算
- **新功能**: 检查对局是否已完成
- **判断依据**: 对局状态（status = 1表示已完成）

### 3. 响应数据结构

#### MatchResultResponse 包含字段
```json
{
  "matchId": 123,
  "winnerId": 456,
  "winnerNickname": "张三",
  "winnerAvatar": "https://example.com/avatar1.jpg",
  "highestScore": 1200,
  "lowestScore": 800,
  "totalDuration": 3600000,
  "totalScores": "[JSON字符串]",
  "participantScores": [
    {
      "participantId": 456,
      "nickname": "张三",
      "avatar": "https://example.com/avatar1.jpg",
      "totalScore": 1200,
      "finalScore": 1200,
      "isWinner": true,
      "wechatUserId": "wx123456"
    }
  ],
  "completionTime": 1701234567890,
  "createTime": 1701234567890,
  "updateTime": 1701234567890
}
```

### 4. 数据库变更

#### 移除依赖
- 不再依赖`match_settlements`表
- 主要使用`match_results`表

#### 字段变更
- `match_results`表移除了`total_rounds`字段
- 添加了`total_scores`字段存储JSON数据

### 5. 代码变更

#### 控制器变更
- `getMatchSettlement()` → `getMatchResult()`
- `isMatchSettled()` → `isMatchCompleted()`
- 注释掉`settleMatch()`方法

#### 服务层变更
- 添加`getMatchResult()`方法
- 修改`isMatchSettled()`逻辑（通过状态判断）
- 注释掉`settleMatch()`方法

### 6. 使用建议

#### 获取对局结果
```bash
GET /api/v1/matches/{matchId}/result
```

#### 检查对局完成状态
```bash
GET /api/v1/matches/{matchId}/is-completed
```

#### 对局结算
目前对局结算功能暂时不可用，需要：
1. 创建`match_settlements`表
2. 恢复相关代码

### 7. 注意事项

1. **向后兼容性**: 原`settlement`接口已更改为`result`接口
2. **数据完整性**: 结果数据现在包含完整的参与者信息
3. **性能优化**: 减少了数据库表依赖
4. **功能简化**: 移除了复杂的结算逻辑，专注于结果展示

### 8. 测试建议

1. 测试`GET /api/v1/matches/{matchId}/result`接口
2. 验证返回的数据结构
3. 检查参与者得分信息是否完整
4. 确认头像字段是否正确显示
