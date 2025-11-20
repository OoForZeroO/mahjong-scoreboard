# round_scores 批量接口使用示例（更新版）

## 概述

为 `round_scores` 表提供了完整的批量操作接口，支持批量创建、更新、查询和删除轮次得分记录。所有接口都支持自动计算累计得分和更新参与者总分。

## 接口列表

### 1. 批量创建轮次得分
- **接口**: `POST /api/v1/matches/rounds/batch`
- **功能**: 批量创建轮次得分记录
- **特点**: 自动计算累计得分，更新参与者总分

### 2. 批量更新轮次得分
- **接口**: `PUT /api/v1/matches/rounds/batch`
- **功能**: 批量更新轮次得分记录
- **特点**: 重新计算累计得分，更新参与者总分

### 3. 批量查询轮次得分
- **接口**: `GET /api/v1/matches/rounds/batch?scoreIds=1,2,3`
- **功能**: 根据ID列表批量查询轮次得分记录

### 4. 批量删除轮次得分
- **接口**: `DELETE /api/v1/matches/rounds/batch`
- **功能**: 批量删除轮次得分记录

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
    "score": 200,
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
      "roundTime": 1716700800000,
      "score": 100,
      "cumulativeScore": 100,
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
      "roundTime": 1716700800000,
      "score": -50,
      "cumulativeScore": -50,
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
        "userName": "游客小王"
      },
      "roundNumber": 1,
      "roundTime": 1716700800000,
      "score": 200,
      "cumulativeScore": 200,
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

### 示例3: 批量查询轮次得分

**请求**:
```bash
GET /api/v1/matches/rounds/batch?scoreIds=1,2,3
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
      "roundTime": 1716700800000,
      "score": 150,
      "cumulativeScore": 150,
      "createTime": 1716700800000,
      "updateTime": 1716700900000
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
      "roundTime": 1716700800000,
      "score": -30,
      "cumulativeScore": -30,
      "createTime": 1716700800000,
      "updateTime": 1716700900000
    },
    {
      "id": 3,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "participant": {
        "id": 3,
        "userName": "游客小王"
      },
      "roundNumber": 1,
      "roundTime": 1716700800000,
      "score": 200,
      "cumulativeScore": 200,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
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
[1, 2, 3]
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

**轮次得分列表不能为空**:
```json
{
  "code": 400,
  "message": "轮次得分列表不能为空",
  "data": null
}
```

**轮次得分必须包含对局、参与者和轮次号信息**:
```json
{
  "code": 500,
  "message": "批量创建轮次得分失败: 轮次得分必须包含对局、参与者和轮次号信息",
  "data": null
}
```

**对局不存在**:
```json
{
  "code": 500,
  "message": "批量创建轮次得分失败: 对局不存在，matchId: 999",
  "data": null
}
```

**参与者不存在**:
```json
{
  "code": 500,
  "message": "批量创建轮次得分失败: 参与者不存在，participantId: 999",
  "data": null
}
```

**轮次得分ID列表不能为空**:
```json
{
  "code": 400,
  "message": "轮次得分ID列表不能为空",
  "data": null
}
```

## 字段说明

### 请求字段

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| match | Object | 是 | 对局信息（创建时必填） |
| match.matchId | Long | 是 | 对局ID |
| participant | Object | 是 | 参与者信息（创建时必填） |
| participant.id | Long | 是 | 参与者ID |
| roundNumber | Integer | 是 | 轮次编号 |
| score | Integer | 是 | 本轮得分 |
| roundTime | Long | 否 | 轮次时间（不提供则使用当前时间） |
| id | Long | 否 | 轮次得分ID（更新时必填） |

### 响应字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 轮次得分ID |
| match | Object | 对局信息 |
| match.matchId | Long | 对局ID |
| match.roomName | String | 房间名称 |
| participant | Object | 参与者信息 |
| participant.id | Long | 参与者ID |
| participant.userName | String | 参与者名称 |
| roundNumber | Integer | 轮次编号 |
| roundTime | Long | 轮次时间 |
| score | Integer | 本轮得分 |
| cumulativeScore | Integer | 累计得分（自动计算） |
| createTime | Long | 创建时间 |
| updateTime | Long | 更新时间 |

## 自动计算功能

### 累计得分计算
- 系统会自动计算每个参与者的累计得分
- 累计得分 = 该参与者之前所有轮次得分的总和 + 当前轮次得分
- 每次创建或更新轮次得分后，会自动更新参与者的 `totalScore`

### 参与者总分更新
- 每次轮次得分变更后，系统会自动更新对应参与者的 `totalScore`
- 确保参与者总分与轮次得分记录保持一致

## 注意事项

1. **事务性**: 所有批量操作都在事务中执行，要么全部成功，要么全部回滚
2. **自动计算**: 系统会自动计算累计得分和更新参与者总分
3. **对象查找**: 创建时会根据 `matchId` 和 `participantId` 自动查找对应的对象
4. **时间设置**: 如果不提供 `roundTime`，系统会自动使用当前时间
5. **数据一致性**: 确保轮次得分与参与者总分保持同步
6. **数据库字段**: 需要确保数据库表包含 `round_time` 字段

## 性能建议

1. **批量大小**: 建议单次批量操作不超过100个轮次得分
2. **网络超时**: 大批量操作可能需要较长时间，注意设置合适的超时时间
3. **错误处理**: 建议在客户端实现重试机制
4. **数据验证**: 建议在客户端预先验证数据完整性

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
      "score": 100,
      "roundTime": 1716700800000
    },
    {
      "match": {"matchId": 1},
      "participant": {"id": 2},
      "roundNumber": 1,
      "score": -50,
      "roundTime": 1716700800000
    }
  ]'

# 2. 批量更新轮次得分
curl -X PUT "http://localhost:8080/api/v1/matches/rounds/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {"id": 1, "score": 150, "roundTime": 1716700900000},
    {"id": 2, "score": -30, "roundTime": 1716700900000}
  ]'

# 3. 批量查询轮次得分
curl -X GET "http://localhost:8080/api/v1/matches/rounds/batch?scoreIds=1,2,3"

# 4. 批量删除轮次得分
curl -X DELETE "http://localhost:8080/api/v1/matches/rounds/batch" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

## 数据库要求

### 必需字段
确保 `round_scores` 表包含以下字段：
- `id` (BIGINT, PRIMARY KEY)
- `match_id` (BIGINT, NOT NULL)
- `participant_id` (BIGINT, NOT NULL)
- `round_number` (INT, NOT NULL)
- `round_time` (BIGINT, NOT NULL) - **需要添加**
- `score` (INT, NOT NULL)
- `cumulative_score` (INT, NOT NULL)
- `create_time` (BIGINT, NOT NULL)
- `update_time` (BIGINT, NOT NULL)

### 添加 round_time 字段的SQL
```sql
ALTER TABLE round_scores ADD COLUMN round_time BIGINT;
UPDATE round_scores SET round_time = create_time WHERE round_time IS NULL;
ALTER TABLE round_scores ALTER COLUMN round_time SET NOT NULL;
```

---

**文档版本**: v2.0  
**更新时间**: 2024年12月  
**更新内容**: 完善字段说明，添加自动计算功能说明，更新错误处理
