# 麻将对局记录系统 API 接口文档

## 1. 概述

本文档描述了麻将对局记录系统的API接口，包括对局管理、参与者管理、轮次得分记录和对局结算等功能。系统支持微信小程序和前端页面调用，用于记录和管理麻将对局数据。

## 2. 基础信息

- **API 基础路径**: `/api/v1`
- **响应格式**: 统一 JSON 格式
- **状态码**: 遵循 HTTP 标准状态码

### 2.1 响应格式

成功响应:
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

失败响应:
```json
{
  "code": 状态码,
  "message": "错误信息",
  "data": null
}
```

## 3. 对局相关接口

### 3.1 创建对局

**URL**: `/api/v1/matches`
**方法**: `POST`
**描述**: 创建一个新的麻将对局

**请求体**:
```json
{
  "roomId": 1,
  "roomName": "1号房间",
  "status": "进行中"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomId": 1,
    "roomName": "1号房间",
    "startTime": 1725934800000,
    "status": "进行中",
    "totalRounds": 0,
    "currentRound": 0
  }
}
```

### 3.2 获取对局列表

**URL**: `/api/v1/matches`
**方法**: `GET`
**描述**: 获取所有对局列表

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "matchId": 1,
      "roomId": 1,
      "roomName": "1号房间",
      "startTime": 1725934800000,
      "status": "进行中"
    }
  ]
}
```

### 3.3 获取对局详情

**URL**: `/api/v1/matches/{matchId}`
**方法**: `GET`
**描述**: 根据ID获取对局详细信息

**参数**:
- `matchId`: 对局ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomId": 1,
    "roomName": "1号房间",
    "startTime": 1725934800000,
    "status": "进行中",
    "totalRounds": 3,
    "currentRound": 3
  }
}
```

### 3.4 更新对局

**URL**: `/api/v1/matches/{matchId}`
**方法**: `PUT`
**描述**: 更新对局信息

**参数**:
- `matchId`: 对局ID

**请求体**:
```json
{
  "roomName": "更新后的房间名",
  "status": "进行中"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomId": 1,
    "roomName": "更新后的房间名",
    "status": "进行中"
  }
}
```

### 3.5 删除对局

**URL**: `/api/v1/matches/{matchId}`
**方法**: `DELETE`
**描述**: 删除指定对局

**参数**:
- `matchId`: 对局ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

## 4. 参与者相关接口

### 4.1 添加参与者

**URL**: `/api/v1/matches/{matchId}/participants`
**方法**: `POST`
**描述**: 向指定对局添加参与者

**参数**:
- `matchId`: 对局ID

**请求体**:
```json
{
  "userId": 1,
  "userName": "张三",
  "totalScore": 0
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "participantId": 1,
    "matchId": 1,
    "userId": 1,
    "userName": "张三",
    "totalScore": 0,
    "isQuit": false
  }
}
```

### 4.2 获取对局参与者列表

**URL**: `/api/v1/matches/{matchId}/participants`
**方法**: `GET`
**描述**: 获取指定对局的所有参与者

**参数**:
- `matchId`: 对局ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "participantId": 1,
      "matchId": 1,
      "userId": 1,
      "userName": "张三",
      "totalScore": 150,
      "isQuit": false
    },
    {
      "participantId": 2,
      "matchId": 1,
      "userId": 2,
      "userName": "李四",
      "totalScore": -50,
      "isQuit": false
    }
  ]
}
```

### 4.3 更新参与者信息

**URL**: `/api/v1/matches/participants/{participantId}`
**方法**: `PUT`
**描述**: 更新参与者信息

**参数**:
- `participantId`: 参与者ID

**请求体**:
```json
{
  "userName": "新用户名"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "participantId": 1,
    "userName": "新用户名",
    "totalScore": 150
  }
}
```

### 4.4 参与者退出对局

**URL**: `/api/v1/matches/participants/{participantId}/quit`
**方法**: `PUT`
**描述**: 参与者退出当前对局

**参数**:
- `participantId`: 参与者ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "participantId": 1,
    "isQuit": true,
    "quitTime": 1725936000000
  }
}
```

## 5. 轮次得分相关接口

### 5.1 记录轮次得分

**URL**: `/api/v1/matches/{matchId}/rounds/{roundNumber}`
**方法**: `POST`
**描述**: 记录指定轮次的得分情况

**参数**:
- `matchId`: 对局ID
- `roundNumber`: 轮次编号

**请求体**:
```json
[
  {
    "participantId": 1,
    "score": 50,
    "cumulativeScore": 150
  },
  {
    "participantId": 2,
    "score": -50,
    "cumulativeScore": -50
  }
]
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "roundScoreId": 1,
      "matchId": 1,
      "participantId": 1,
      "roundNumber": 1,
      "score": 50,
      "cumulativeScore": 150
    },
    {
      "roundScoreId": 2,
      "matchId": 1,
      "participantId": 2,
      "roundNumber": 1,
      "score": -50,
      "cumulativeScore": -50
    }
  ]
}
```

### 5.2 获取对局所有轮次

**URL**: `/api/v1/matches/{matchId}/rounds`
**方法**: `GET`
**描述**: 获取指定对局的所有轮次得分记录

**参数**:
- `matchId`: 对局ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "roundScoreId": 1,
      "matchId": 1,
      "participantId": 1,
      "roundNumber": 1,
      "score": 50,
      "cumulativeScore": 150
    },
    {
      "roundScoreId": 2,
      "matchId": 1,
      "participantId": 2,
      "roundNumber": 1,
      "score": -50,
      "cumulativeScore": -50
    }
  ]
}
```

### 5.3 获取当前轮次编号

**URL**: `/api/v1/matches/{matchId}/rounds/current-number`
**方法**: `GET`
**描述**: 获取指定对局的当前轮次编号

**参数**:
- `matchId`: 对局ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": 3
}
```

### 5.4 获取指定轮次详情

**URL**: `/api/v1/matches/{matchId}/rounds/{roundNumber}`
**方法**: `GET`
**描述**: 获取指定轮次的详细得分情况

**参数**:
- `matchId`: 对局ID
- `roundNumber`: 轮次编号

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "roundScoreId": 1,
      "matchId": 1,
      "participantId": 1,
      "roundNumber": 1,
      "score": 50,
      "cumulativeScore": 150
    }
  ]
}
```

## 6. 对局结算相关接口

### 6.1 对局结算

**URL**: `/api/v1/matches/{matchId}/settle`
**方法**: `POST`
**描述**: 对指定对局进行结算，应用倍率并计算最终得分

**参数**:
- `matchId`: 对局ID

**请求体**:
```json
{
  "multiplier": 2.5,
  "notes": "常规对局结算"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "settlementId": 1,
    "matchId": 1,
    "settlementMultiplier": 2.5,
    "settlementTime": 1725937200000,
    "notes": "常规对局结算"
  }
}
```

### 6.2 获取结算记录

**URL**: `/api/v1/matches/{matchId}/settlement`
**方法**: `GET`
**描述**: 获取指定对局的结算记录

**参数**:
- `matchId`: 对局ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "settlementId": 1,
    "matchId": 1,
    "settlementMultiplier": 2.5,
    "settlementTime": 1725937200000,
    "notes": "常规对局结算"
  }
}
```

### 6.3 检查对局是否已结算

**URL**: `/api/v1/matches/{matchId}/is-settled`
**方法**: `GET`
**描述**: 检查指定对局是否已经结算

**参数**:
- `matchId`: 对局ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

## 7. 统计相关接口

### 7.1 获取参与者排名

**URL**: `/api/v1/matches/{matchId}/participants/ranking`
**方法**: `GET`
**描述**: 获取指定对局参与者的排名（按最终得分降序排列）

**参数**:
- `matchId`: 对局ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "participantId": 1,
      "userName": "张三",
      "totalScore": 150,
      "finalScore": 375
    },
    {
      "participantId": 2,
      "userName": "李四",
      "totalScore": -50,
      "finalScore": -125
    }
  ]
}
```

### 7.2 获取参与者总分

**URL**: `/api/v1/matches/participants/{participantId}/total-score`
**方法**: `GET`
**描述**: 获取指定参与者的总分

**参数**:
- `participantId`: 参与者ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": 150
}
```

### 7.3 获取参与者最终得分

**URL**: `/api/v1/matches/participants/{participantId}/final-score`
**方法**: `GET`
**描述**: 获取指定参与者的最终得分（应用倍率后）

**参数**:
- `participantId`: 参与者ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": 375.0
}
```

## 8. 错误码说明

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 9. 使用流程示例

### 9.1 创建对局并记录多轮得分

1. 创建对局
   ```
   POST /api/v1/matches
   ```

2. 添加参与者
   ```
   POST /api/v1/matches/{matchId}/participants
   ```

3. 记录第1轮得分
   ```
   POST /api/v1/matches/{matchId}/rounds/1
   ```

4. 记录第2轮得分
   ```
   POST /api/v1/matches/{matchId}/rounds/2
   ```

5. 进行对局结算（应用倍率）
   ```
   POST /api/v1/matches/{matchId}/settle
   ```

6. 获取最终排名
   ```
   GET /api/v1/matches/{matchId}/participants/ranking
   ```

## 10. 注意事项

1. 一个对局可以有多轮得分记录，每轮需要记录所有参与者的得分
2. 对局结算后，不能再添加新的轮次得分记录
3. 结算时，系统会自动计算每个参与者的最终得分（总分 × 倍率）
4. 参与者退出对局后，仍然可以查看其历史得分记录
5. 所有时间戳均为毫秒级时间戳