# 麻将对局记录完整API接口文档

## 1. 接口概述

本文档提供了麻将对局记录系统的完整API接口定义，包括对局管理、参与者管理、轮次计分和收盘结算等功能接口，支持前端和微信小程序进行接口对接。

### 1.1 基础URL

```
http://localhost:8080/api/v1
```

### 1.2 数据格式

- 所有接口使用JSON格式进行数据交换
- 时间戳使用Unix时间戳（毫秒级）
- 响应格式统一包含：
  ```json
  {
    "code": 200, // 状态码，200表示成功
    "message": "操作成功", // 状态消息
    "data": {} // 返回的数据
  }
  ```

### 1.3 错误码说明

| 错误码 | 说明 |
|-------|------|
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 2. 对局管理接口

### 2.1 创建对局

**接口路径**：`/matches`
**请求方法**：POST
**请求参数**：

```json
{
  "roomId": 1, // 房间ID
  "roomName": "快乐棋牌室", // 房间名称
  "startTime": 1716700800000 // 对局开始时间戳
}
```

**响应数据**：

```json
{
  "code": 200,
  "message": "创建对局成功",
  "data": {
    "matchId": 1,
    "roomId": 1,
    "roomName": "快乐棋牌室",
    "startTime": 1716700800000,
    "status": "进行中",
    "totalRounds": 0,
    "currentRound": 0,
    "createTime": 1716700800000,
    "updateTime": 1716700800000
  }
}
```

### 2.2 获取对局列表

**接口路径**：`/matches`
**请求方法**：GET
**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| limit | Integer | 否 | 每页数量，默认10 |
| status | String | 否 | 对局状态筛选（进行中/已完成） |
| roomId | Long | 否 | 房间ID筛选 |
| startTime | Long | 否 | 开始时间戳（大于等于） |
| endTime | Long | 否 | 结束时间戳（小于等于） |

**响应数据**：

```json
{
  "code": 200,
  "message": "获取对局列表成功",
  "data": {
    "total": 25,
    "pages": 3,
    "page": 1,
    "limit": 10,
    "list": [
      {
        "matchId": 1,
        "roomId": 1,
        "roomName": "快乐棋牌室",
        "startTime": 1716700800000,
        "endTime": 1716707200000,
        "status": "已完成",
        "totalRounds": 8,
        "participantCount": 4,
        "settlementMultiplier": 2.0
      }
    ]
  }
}
```

### 2.3 获取对局详情

**接口路径**：`/matches/{matchId}`
**请求方法**：GET
**响应数据**：

```json
{
  "code": 200,
  "message": "获取对局详情成功",
  "data": {
    "matchId": 1,
    "roomId": 1,
    "roomName": "快乐棋牌室",
    "startTime": 1716700800000,
    "endTime": 1716707200000,
    "status": "已完成",
    "totalRounds": 8,
    "currentRound": 8,
    "settlementMultiplier": 2.0,
    "participantCount": 4,
    "totalMatchScore": 0,
    "createTime": 1716700800000,
    "updateTime": 1716707200000
  }
}
```

### 2.4 更新对局信息

**接口路径**：`/matches/{matchId}`
**请求方法**：PUT
**请求参数**：

```json
{
  "roomName": "新棋牌室名称" // 可选参数
}
```

**响应数据**：

```json
{
  "code": 200,
  "message": "更新对局信息成功",
  "data": {
    "matchId": 1,
    "roomName": "新棋牌室名称",
    "updateTime": 1716708000000
  }
}
```

### 2.5 删除对局

**接口路径**：`/matches/{matchId}`
**请求方法**：DELETE
**响应数据**：

```json
{
  "code": 200,
  "message": "删除对局成功",
  "data": null
}
```

## 3. 参与者管理接口

### 3.1 添加参与者

**接口路径**：`/matches/{matchId}/participants`
**请求方法**：POST
**请求参数**：

```json
[
  {
    "userId": 1, // 用户ID（可选，没有则为匿名用户）
    "nickname": "玩家1", // 昵称（必填）
    "avatar": "https://example.com/avatar1.jpg" // 头像URL（可选）
  },
  {
    "userId": null,
    "nickname": "玩家2",
    "avatar": "https://example.com/avatar2.jpg"
  }
]
```

**响应数据**：

```json
{
  "code": 200,
  "message": "添加参与者成功",
  "data": [
    {
      "participantId": 1,
      "matchId": 1,
      "userId": 1,
      "nickname": "玩家1",
      "avatar": "https://example.com/avatar1.jpg",
      "totalScore": 0,
      "isQuit": false,
      "createTime": 1716700800000
    },
    {
      "participantId": 2,
      "matchId": 1,
      "userId": null,
      "nickname": "玩家2",
      "avatar": "https://example.com/avatar2.jpg",
      "totalScore": 0,
      "isQuit": false,
      "createTime": 1716700800000
    }
  ]
}
```

### 3.2 获取对局参与者列表

**接口路径**：`/matches/{matchId}/participants`
**请求方法**：GET
**响应数据**：

```json
{
  "code": 200,
  "message": "获取参与者列表成功",
  "data": [
    {
      "participantId": 1,
      "matchId": 1,
      "userId": 1,
      "participantName": "玩家1",
      "avatar": "https://example.com/avatar1.jpg",
      "totalScore": 150,
      "finalScore": 300.0,
      "isQuit": false,
      "quitTime": null,
      "createTime": 1716700800000
    }
  ]
}
```

### 3.3 更新参与者状态

**接口路径**：`/matches/{matchId}/participants/{participantId}`
**请求方法**：PUT
**请求参数**：

```json
{
  "isQuit": true, // 设置参与者退出
  "quitTime": 1716705000000 // 退出时间
}
```

**响应数据**：

```json
{
  "code": 200,
  "message": "更新参与者状态成功",
  "data": {
    "participantId": 1,
    "isQuit": true,
    "quitTime": 1716705000000,
    "updateTime": 1716705000000
  }
}
```

## 4. 轮次计分接口

### 4.1 记录轮次分数

**接口路径**：`/matches/{matchId}/rounds/{roundNumber}/scores`
**请求方法**：POST
**请求参数**：

```json
[
  {
    "participantId": 1,
    "score": 50,
    "cumulativeScore": 50
  },
  {
    "participantId": 2,
    "score": -50,
    "cumulativeScore": -50
  }
]
```

**响应数据**：

```json
{
  "code": 200,
  "message": "记录轮次分数成功",
  "data": {
    "roundNumber": 1,
    "matchId": 1,
    "scores": [
      {
        "scoreId": 1,
        "participantId": 1,
        "score": 50,
        "cumulativeScore": 50
      },
      {
        "scoreId": 2,
        "participantId": 2,
        "score": -50,
        "cumulativeScore": -50
      }
    ],
    "createTime": 1716701000000
  }
}
```

### 4.2 获取轮次分数记录

**接口路径**：`/matches/{matchId}/rounds/{roundNumber}/scores`
**请求方法**：GET
**响应数据**：

```json
{
  "code": 200,
  "message": "获取轮次分数记录成功",
  "data": {
    "matchId": 1,
    "roundNumber": 1,
    "scores": [
      {
        "scoreId": 1,
        "participantId": 1,
        "participantName": "玩家1",
        "score": 50,
        "cumulativeScore": 50,
        "createTime": 1716701000000
      }
    ]
  }
}
```

### 4.3 获取对局所有轮次记录

**接口路径**：`/matches/{matchId}/rounds`
**请求方法**：GET
**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| participantId | Long | 否 | 筛选特定参与者 |
| startRound | Integer | 否 | 开始轮次 |
| endRound | Integer | 否 | 结束轮次 |

**响应数据**：

```json
{
  "code": 200,
  "message": "获取轮次记录成功",
  "data": {
    "matchId": 1,
    "totalRounds": 8,
    "rounds": [
      {
        "roundNumber": 1,
        "scores": [
          {
            "scoreId": 1,
            "participantId": 1,
            "participantName": "玩家1",
            "score": 50,
            "cumulativeScore": 50
          }
        ],
        "createTime": 1716701000000
      }
    ]
  }
}
```

### 4.4 更新轮次分数

**接口路径**：`/matches/{matchId}/rounds/{roundNumber}/scores/{scoreId}`
**请求方法**：PUT
**请求参数**：

```json
{
  "score": 60,
  "cumulativeScore": 60
}
```

**响应数据**：

```json
{
  "code": 200,
  "message": "更新轮次分数成功",
  "data": {
    "scoreId": 1,
    "score": 60,
    "cumulativeScore": 60,
    "updateTime": 1716701200000
  }
}
```

## 5. 收盘结算接口

### 5.1 执行对局结算

**接口路径**：`/matches/{matchId}/settlement`
**请求方法**：POST
**请求参数**：

```json
{
  "multiplier": 2.0, // 结算倍率
  "notes": "周末双倍积分" // 结算备注（可选）
}
```

**响应数据**：

```json
{
  "code": 200,
  "message": "对局结算成功",
  "data": {
    "settlementId": 1,
    "matchId": 1,
    "multiplier": 2.0,
    "settlementTime": 1716707200000,
    "notes": "周末双倍积分",
    "finalScores": [
      {
        "participantId": 1,
        "participantName": "玩家1",
        "totalScore": 150,
        "finalScore": 300.0
      },
      {
        "participantId": 2,
        "participantName": "玩家2",
        "totalScore": -150,
        "finalScore": -300.0
      }
    ]
  }
}
```

### 5.2 获取结算信息

**接口路径**：`/matches/{matchId}/settlement`
**请求方法**：GET
**响应数据**：

```json
{
  "code": 200,
  "message": "获取结算信息成功",
  "data": {
    "settlementId": 1,
    "matchId": 1,
    "multiplier": 2.0,
    "settlementTime": 1716707200000,
    "notes": "周末双倍积分",
    "finalScores": [
      {
        "participantId": 1,
        "participantName": "玩家1",
        "totalScore": 150,
        "finalScore": 300.0,
        "ranking": 1
      }
    ]
  }
}
```

## 6. 统计查询接口

### 6.1 获取参与者统计数据

**接口路径**：`/statistics/participants/{userId}`
**请求方法**：GET
**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| startTime | Long | 否 | 开始时间 |
| endTime | Long | 否 | 结束时间 |
| roomId | Long | 否 | 房间ID |

**响应数据**：

```json
{
  "code": 200,
  "message": "获取统计数据成功",
  "data": {
    "totalMatches": 25,
    "winMatches": 12,
    "winRate": 0.48,
    "totalScore": 5000,
    "averageScore": 200,
    "highestScore": 1000,
    "totalRounds": 200
  }
}
```

### 6.2 获取房间统计数据

**接口路径**：`/statistics/rooms/{roomId}`
**请求方法**：GET
**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| startTime | Long | 否 | 开始时间 |
| endTime | Long | 否 | 结束时间 |

**响应数据**：

```json
{
  "code": 200,
  "message": "获取房间统计数据成功",
  "data": {
    "totalMatches": 50,
    "completedMatches": 48,
    "totalParticipants": 200,
    "activeDays": 30,
    "averageParticipants": 4
  }
}
```

### 6.3 获取排行榜数据

**接口路径**：`/statistics/rankings`
**请求方法**：GET
**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| type | String | 是 | 排行榜类型（score:总分, win_rate:胜率） |
| limit | Integer | 否 | 返回数量，默认10 |
| period | String | 否 | 统计周期（day:日, week:周, month:月, all:全部） |

**响应数据**：

```json
{
  "code": 200,
  "message": "获取排行榜成功",
  "data": {
    "type": "score",
    "period": "month",
    "rankings": [
      {
        "rank": 1,
        "userId": 1,
        "nickname": "玩家1",
        "avatar": "https://example.com/avatar1.jpg",
        "totalScore": 15000,
        "matches": 20
      }
    ]
  }
}
```

## 7. 数据导出接口

### 7.1 导出对局记录

**接口路径**：`/export/matches/{matchId}`
**请求方法**：GET
**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|-------|------|------|------|
| format | String | 否 | 导出格式（excel/csv/pdf），默认excel |

**响应**：文件流下载

### 7.2 批量导出对局记录

**接口路径**：`/export/matches/batch`
**请求方法**：POST
**请求参数**：

```json
{
  "matchIds": [1, 2, 3, 4, 5],
  "format": "excel"
}
```

**响应**：文件流下载

## 8. 接口使用示例

### 8.1 创建对局并添加参与者

**Step 1: 创建对局**

```javascript
// 前端示例代码
fetch('http://localhost:8080/api/v1/matches', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    roomId: 1,
    roomName: '快乐棋牌室',
    startTime: Date.now()
  })
})
.then(response => response.json())
.then(data => {
  const matchId = data.data.matchId;
  // 继续添加参与者
  addParticipants(matchId);
});
```

**Step 2: 添加参与者**

```javascript
function addParticipants(matchId) {
  fetch(`http://localhost:8080/api/v1/matches/${matchId}/participants`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify([
      {
        userId: 1,
        nickname: '张三',
        avatar: 'https://example.com/avatar1.jpg'
      },
      {
        userId: null,
        nickname: '李四',
        avatar: 'https://example.com/avatar2.jpg'
      }
    ])
  })
  .then(response => response.json())
  .then(data => {
    console.log('参与者添加成功', data);
  });
}
```

### 8.2 记录轮次分数

```javascript
function recordRoundScore(matchId, roundNumber, scores) {
  fetch(`http://localhost:8080/api/v1/matches/${matchId}/rounds/${roundNumber}/scores`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(scores)
  })
  .then(response => response.json())
  .then(data => {
    console.log('轮次分数记录成功', data);
    // 更新UI显示
  });
}

// 使用示例
const scores = [
  { participantId: 1, score: 50, cumulativeScore: 50 },
  { participantId: 2, score: -50, cumulativeScore: -50 }
];
recordRoundScore(1, 1, scores);
```

### 8.3 执行收盘结算

```javascript
function settleMatch(matchId, multiplier, notes) {
  fetch(`http://localhost:8080/api/v1/matches/${matchId}/settlement`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      multiplier: multiplier,
      notes: notes
    })
  })
  .then(response => response.json())
  .then(data => {
    console.log('对局结算成功', data);
    // 显示结算结果
  });
}

// 使用示例
settleMatch(1, 2.0, '周末双倍积分');
```

## 9. 微信小程序接口调用注意事项

1. **域名配置**：请确保在微信小程序后台配置了正确的服务器域名
2. **会话管理**：建议使用token进行接口鉴权
3. **请求封装**：可以封装统一的请求方法处理错误和响应
4. **数据缓存**：对于频繁使用的数据可以考虑使用本地缓存
5. **网络请求**：注意处理网络异常和超时情况

```javascript
// 微信小程序示例代码
const request = (url, method, data) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `http://localhost:8080/api/v1${url}`,
      method: method,
      data: data,
      header: {
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data.data);
        } else {
          reject(new Error(res.data.message));
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
};

// 使用示例
request('/matches', 'GET')
  .then(data => {
    console.log('获取对局列表成功', data);
  })
  .catch(err => {
    wx.showToast({
      title: err.message || '请求失败',
      icon: 'none'
    });
  });
```

## 10. 安全考虑

1. **参数验证**：所有接口都需要对输入参数进行验证
2. **权限控制**：根据用户角色和权限控制接口访问
3. **SQL注入防护**：使用参数化查询防止SQL注入
4. **XSS防护**：对用户输入进行过滤和转义
5. **请求限流**：防止接口被恶意调用
6. **数据加密**：敏感数据传输应使用HTTPS加密

## 11. 常见问题解答

1. **Q**: 如何处理参与者中途退出的情况？
   **A**: 调用参与者状态更新接口，设置`isQuit`为true并记录退出时间，后续轮次不计分。

2. **Q**: 收盘后是否可以修改分数？
   **A**: 收盘后不建议修改分数，如必须修改，需先取消结算，修改后重新结算。

3. **Q**: 如何处理多轮计分的数据一致性？
   **A**: 系统使用触发器自动维护累计分数和总分，确保数据一致性。

4. **Q**: 接口是否支持分页？
   **A**: 是的，所有列表查询接口都支持分页参数，默认页码1，每页10条。

5. **Q**: 如何处理网络异常导致的数据不一致？
   **A**: 建议前端实现数据本地缓存和重试机制，服务端使用事务确保操作原子性。