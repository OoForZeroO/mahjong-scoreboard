# 麻将对局记录核心API快速参考

## 基础URL

```
http://localhost:8080/api/v1
```

## 核心接口列表

### 1. 对局管理

| 接口 | 方法 | 功能描述 |
|------|------|----------|
| `/matches` | POST | **创建新对局** |
| `/matches` | GET | **获取对局列表** |
| `/matches/{matchId}` | GET | **获取对局详情** |

### 2. 参与者管理

| 接口 | 方法 | 功能描述 |
|------|------|----------|
| `/matches/{matchId}/participants` | POST | **添加参与者** |
| `/matches/{matchId}/participants` | GET | **获取参与者列表** |
| `/matches/{matchId}/participants/{participantId}` | PUT | **更新参与者状态** |

### 3. 轮次计分

| 接口 | 方法 | 功能描述 |
|------|------|----------|
| `/matches/{matchId}/rounds/{roundNumber}/scores` | POST | **记录轮次分数** |
| `/matches/{matchId}/rounds` | GET | **获取所有轮次记录** |

### 4. 收盘结算

| 接口 | 方法 | 功能描述 |
|------|------|----------|
| `/matches/{matchId}/settlement` | POST | **执行对局结算** |
| `/matches/{matchId}/settlement` | GET | **获取结算信息** |

## 关键请求/响应示例

### 1. 创建对局

**请求**:
```json
POST /matches
{
  "roomId": 1,
  "roomName": "快乐棋牌室",
  "startTime": 1716700800000
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建对局成功",
  "data": {
    "matchId": 1,
    "status": "进行中"
    // 其他字段...
  }
}
```

### 2. 添加参与者

**请求**:
```json
POST /matches/1/participants
[
  {
    "userId": 1,
    "nickname": "玩家1",
    "avatar": "https://example.com/avatar1.jpg"
  }
]
```

**响应**:
```json
{
  "code": 200,
  "message": "添加参与者成功",
  "data": [
    {
      "participantId": 1,
      "nickname": "玩家1"
      // 其他字段...
    }
  ]
}
```

### 3. 记录轮次分数

**请求**:
```json
POST /matches/1/rounds/1/scores
[
  {
    "participantId": 1,
    "score": 50,
    "cumulativeScore": 50
  }
]
```

**响应**:
```json
{
  "code": 200,
  "message": "记录轮次分数成功",
  "data": {
    "roundNumber": 1,
    "scores": [...] // 分数列表
  }
}
```

### 4. 执行收盘结算

**请求**:
```json
POST /matches/1/settlement
{
  "multiplier": 2.0,
  "notes": "周末双倍积分"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "对局结算成功",
  "data": {
    "multiplier": 2.0,
    "finalScores": [...] // 最终得分列表
  }
}
```

## 错误码速查

| 错误码 | 说明 |
|-------|------|
| 400 | 请求参数错误 |
| 404 | 资源不存在（对局/参与者不存在） |
| 409 | 资源冲突（如重复添加参与者） |
| 500 | 服务器内部错误 |

## 微信小程序调用示例

```javascript
// 统一请求方法
const api = (path, method = 'GET', data = {}) => {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `http://localhost:8080/api/v1${path}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.data.code === 200) {
          resolve(res.data.data);
        } else {
          wx.showToast({title: res.data.message, icon: 'none'});
          reject(new Error(res.data.message));
        }
      },
      fail: (err) => {
        wx.showToast({title: '网络错误', icon: 'none'});
        reject(err);
      }
    });
  });
};

// 创建对局
const createMatch = async (roomInfo) => {
  return await api('/matches', 'POST', roomInfo);
};

// 记录分数
const recordScores = async (matchId, roundNum, scores) => {
  return await api(`/matches/${matchId}/rounds/${roundNum}/scores`, 'POST', scores);
};

// 结算对局
const settleMatch = async (matchId, multiplier, notes) => {
  return await api(`/matches/${matchId}/settlement`, 'POST', {multiplier, notes});
};
```

## 注意事项

1. 所有时间戳使用毫秒级Unix时间戳
2. 轮次编号从1开始递增
3. 结算后对局状态变为"已完成"，不可再修改
4. 参与者分数支持正负值，总分为所有轮次分数累加

*详细接口说明请参考完整API文档*