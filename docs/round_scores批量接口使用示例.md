# round_scores 批量接口使用示例

## 概述

为 `round_scores` 表提供了完整的批量操作接口，支持批量创建、更新、获取和删除轮次得分记录。

## 接口列表

### 1. 批量创建轮次得分
- **接口**: `POST /api/v1/matches/rounds/batch`
- **功能**: 一次性创建多个轮次得分记录

### 2. 批量更新轮次得分
- **接口**: `PUT /api/v1/matches/rounds/batch`
- **功能**: 批量更新多个轮次得分记录

### 3. 批量获取轮次得分
- **接口**: `GET /api/v1/matches/rounds/batch`
- **功能**: 根据ID列表批量获取轮次得分记录

### 4. 批量删除轮次得分
- **接口**: `DELETE /api/v1/matches/rounds/batch`
- **功能**: 批量删除多个轮次得分记录

## 使用示例

### 示例1: 批量创建轮次得分

**请求**:
```bash
POST /api/v1/matches/rounds/batch
Content-Type: application/json
```

**请求体**:
```json
[
  {
    "match": {
      "matchId": 1
    },
    "participant": {
      "id": 1
    },
    "roundNumber": 1,
    "score": 100,
    "roundTime": 1716700800000
  },
  {
    "match": {
      "matchId": 1
    },
    "participant": {
      "id": 2
    },
    "roundNumber": 1,
    "score": -50,
    "roundTime": 1716700800000
  },
  {
    "match": {
      "matchId": 1
    },
    "participant": {
      "id": 3
    },
    "roundNumber": 1,
    "score": 0,
    "roundTime": 1716700800000
  },
  {
    "match": {
      "matchId": 1
    },
    "participant": {
      "id": 4
    },
    "roundNumber": 1,
    "score": -50,
    "roundTime": 1716700800000
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
      "id": 1,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "participant": {
        "id": 1,
        "userName": "张三"
      },
      "roundNumber": 1,
      "score": 100,
      "cumulativeScore": 100,
      "roundTime": 1716700800000,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
    },
    {
      "id": 2,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "participant": {
        "id": 2,
        "userName": "李四"
      },
      "roundNumber": 1,
      "score": -50,
      "cumulativeScore": -50,
      "roundTime": 1716700800000,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
    },
    {
      "id": 3,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "participant": {
        "id": 3,
        "userName": "王五"
      },
      "roundNumber": 1,
      "score": 0,
      "cumulativeScore": 0,
      "roundTime": 1716700800000,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
    },
    {
      "id": 4,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "participant": {
        "id": 4,
        "userName": "赵六"
      },
      "roundNumber": 1,
      "score": -50,
      "cumulativeScore": -50,
      "roundTime": 1716700800000,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
    }
  ]
}
```

### 示例2: 批量更新轮次得分

**请求**:
```bash
PUT /api/v1/matches/rounds/batch
Content-Type: application/json
```

**请求体**:
```json
[
  {
    "id": 1,
    "score": 150,
    "roundTime": 1716700900000
  },
  {
    "id": 2,
    "score": -30,
    "roundTime": 1716700900000
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
      "id": 1,
      "score": 150,
      "cumulativeScore": 150,
      "roundTime": 1716700900000,
      "updateTime": 1716700900000
    },
    {
      "id": 2,
      "score": -30,
      "cumulativeScore": -30,
      "roundTime": 1716700900000,
      "updateTime": 1716700900000
    }
  ]
}
```

### 示例3: 批量获取轮次得分

**请求**:
```bash
GET /api/v1/matches/rounds/batch?scoreIds=1,2,3,4
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "participant": {
        "id": 1,
        "userName": "张三"
      },
      "roundNumber": 1,
      "score": 150,
      "cumulativeScore": 150,
      "roundTime": 1716700900000
    },
    {
      "id": 2,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "participant": {
        "id": 2,
        "userName": "李四"
      },
      "roundNumber": 1,
      "score": -30,
      "cumulativeScore": -30,
      "roundTime": 1716700900000
    },
    {
      "id": 3,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "participant": {
        "id": 3,
        "userName": "王五"
      },
      "roundNumber": 1,
      "score": 0,
      "cumulativeScore": 0,
      "roundTime": 1716700800000
    },
    {
      "id": 4,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "participant": {
        "id": 4,
        "userName": "赵六"
      },
      "roundNumber": 1,
      "score": -50,
      "cumulativeScore": -50,
      "roundTime": 1716700800000
    }
  ]
}
```

### 示例4: 批量删除轮次得分

**请求**:
```bash
DELETE /api/v1/matches/rounds/batch
Content-Type: application/json
```

**请求体**:
```json
[1, 2]
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

## 错误处理

### 常见错误响应

**轮次得分列表为空**:
```json
{
  "code": 400,
  "message": "轮次得分列表不能为空",
  "data": null
}
```

**缺少必要字段**:
```json
{
  "code": 400,
  "message": "轮次得分必须包含对局、参与者和轮次号信息",
  "data": null
}
```

**轮次得分ID列表为空**:
```json
{
  "code": 400,
  "message": "轮次得分ID列表不能为空",
  "data": null
}
```

## 注意事项

1. **事务性**: 所有批量操作都在事务中执行，要么全部成功，要么全部回滚
2. **验证**: 批量创建时会验证对局、参与者和轮次号信息
3. **自动计算**: 系统会自动计算累计得分并更新参与者总分
4. **时间设置**: 如果未提供轮次时间，系统会自动设置为当前时间
5. **部分更新**: 批量更新时，只更新提供的字段，未提供的字段保持不变

## 性能建议

1. **批量大小**: 建议单次批量操作不超过100个轮次得分
2. **网络超时**: 大批量操作可能需要较长时间，注意设置合适的超时时间
3. **错误处理**: 建议在客户端实现重试机制
4. **数据一致性**: 批量操作会自动维护参与者总分的一致性

## 测试用例

### cURL 测试命令

```bash
# 1. 批量创建轮次得分
curl -X POST "http://localhost:8080/api/v1/matches/rounds/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "match": {"matchId": 1},
      "participant": {"id": 1},
      "roundNumber": 1,
      "score": 100
    },
    {
      "match": {"matchId": 1},
      "participant": {"id": 2},
      "roundNumber": 1,
      "score": -50
    }
  ]'

# 2. 批量更新轮次得分
curl -X PUT "http://localhost:8080/api/v1/matches/rounds/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {"id": 1, "score": 150},
    {"id": 2, "score": -30}
  ]'

# 3. 批量获取轮次得分
curl -X GET "http://localhost:8080/api/v1/matches/rounds/batch?scoreIds=1,2,3"

# 4. 批量删除轮次得分
curl -X DELETE "http://localhost:8080/api/v1/matches/rounds/batch" \
  -H "Content-Type: application/json" \
  -d '[1, 2]'
```

## 业务场景示例

### 场景1: 一局麻将的完整计分

```json
// 第1轮计分
POST /api/v1/matches/rounds/batch
[
  {
    "match": {"matchId": 1},
    "participant": {"id": 1},
    "roundNumber": 1,
    "score": 100
  },
  {
    "match": {"matchId": 1},
    "participant": {"id": 2},
    "roundNumber": 1,
    "score": -50
  },
  {
    "match": {"matchId": 1},
    "participant": {"id": 3},
    "roundNumber": 1,
    "score": 0
  },
  {
    "match": {"matchId": 1},
    "participant": {"id": 4},
    "roundNumber": 1,
    "score": -50
  }
]

// 第2轮计分
POST /api/v1/matches/rounds/batch
[
  {
    "match": {"matchId": 1},
    "participant": {"id": 1},
    "roundNumber": 2,
    "score": -30
  },
  {
    "match": {"matchId": 1},
    "participant": {"id": 2},
    "roundNumber": 2,
    "score": 80
  },
  {
    "match": {"matchId": 1},
    "participant": {"id": 3},
    "roundNumber": 2,
    "score": -20
  },
  {
    "match": {"matchId": 1},
    "participant": {"id": 4},
    "roundNumber": 2,
    "score": -30
  }
]
```

### 场景2: 批量修正得分

```json
// 发现第1轮得分有误，批量修正
PUT /api/v1/matches/rounds/batch
[
  {
    "id": 1,
    "score": 120  // 张三从100改为120
  },
  {
    "id": 2,
    "score": -40  // 李四从-50改为-40
  }
]
```

---

**文档版本**: v1.0  
**更新时间**: 2024年12月
