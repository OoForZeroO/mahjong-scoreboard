# match_participants 批量接口使用示例

## 概述

为 `match_participants` 表提供了完整的批量操作接口，支持批量添加、更新、退出和删除参与者。

**接口说明**: 批量添加参与者接口支持传入轮次计分数据字段，但**不会自动创建轮次计分记录**，需要单独调用轮次计分接口。

## 接口列表

### 1. 批量添加参与者（支持轮次计分字段）
- **接口**: `POST /api/v1/matches/{matchId}/participants/batch`
- **功能**: 一次性为指定对局添加多个参与者
- **说明**: 支持传入轮次计分数据字段，但不会自动创建轮次计分记录

### 2. 批量更新参与者
- **接口**: `PUT /api/v1/participants/batch`
- **功能**: 批量更新多个参与者的信息

### 3. 批量退出对局
- **接口**: `PUT /api/v1/participants/batch/quit`
- **功能**: 批量让多个参与者退出对局

### 4. 批量删除参与者
- **接口**: `DELETE /api/v1/participants/batch`
- **功能**: 批量删除多个参与者

## 使用示例

### 示例1: 批量添加参与者（基础版本）

**请求**:
```bash
POST /api/v1/matches/1/participants/batch
Content-Type: application/json
```

**请求体**:
```json
[
  {
    "wechatUserId": "wx_openid_zhangsan",
    "nickName": "张三",
    "avatarUrl": "https://example.com/avatar1.jpg"
  },
  {
    "wechatUserId": "wx_openid_lisi",
    "nickName": "李四",
    "avatarUrl": "https://example.com/avatar2.jpg"
  },
  {
    "nickName": "游客小王",
    "avatarUrl": "https://example.com/guest-avatar.jpg"
  },
  {
    "wechatUserId": "wx_openid_wangwu",
    "nickName": "王五",
    "avatarUrl": "https://example.com/avatar3.jpg"
  }
]
```

### 示例1.1: 批量添加参与者（包含轮次计分数据）⭐ 新功能

**请求**:
```bash
POST /api/v1/matches/1/participants/batch
Content-Type: application/json
```

**请求体**:
```json
[
  {
    "user": {
      "id": 101
    },
    "userName": "张三",
    "avatar": "https://example.com/avatar1.jpg",
    "roundScore": {
      "roundNumber": 1,
      "score": 10,
      "roundTime": 1716700800000
    }
  },
  {
    "user": {
      "id": 102
    },
    "userName": "李四",
    "avatar": "https://example.com/avatar2.jpg",
    "roundScore": {
      "roundNumber": 1,
      "score": -5,
      "roundTime": 1716700800000
    }
  },
  {
    "user": null,
    "userName": "游客小王",
    "avatar": "https://example.com/guest-avatar.jpg",
    "roundScore": {
      "score": 15
    }
  }
]
```

**请求字段说明**:
- `wechatUserId`: 微信用户OpenID（可选，为空表示游客）
- `nickName`: 参与者昵称（必填）
- `avatarUrl`: 参与者头像URL（可选，最大长度500字符）
- `roundScore`: 单个轮次计分对象（必填）
  - `roundNumber`: 轮次编号（可选，默认为1）
  - `score`: 该轮次得分（必填）
  - `roundTime`: 轮次时间戳（可选，不传则使用当前时间）

**响应字段说明**:
- `wechatUserId`: 微信用户OpenID
- `userName`: 参与者昵称（内部存储字段）
- `avatar`: 参与者头像URL（内部存储字段）
- 游客用户的`wechatUserId`为空字符串

**重要说明**:
- 每个参与者只能包含一个轮次计分数据
- `roundNumber` 默认为1，可以不传
- **注意**: 传入的轮次计分数据仅用于数据传输，**不会自动创建轮次计分记录**
- 如需创建轮次计分记录，请单独调用轮次计分接口

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
      "user": {
        "id": 101,
        "nickname": "张三"
      },
      "userName": "张三",
      "wechatUserId": "wx_openid_zhangsan",
      "avatar": "https://example.com/avatar1.jpg",
      "totalScore": 15,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
    },
    {
      "id": 2,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "user": {
        "id": 102,
        "nickname": "李四"
      },
      "userName": "李四",
      "wechatUserId": "wx_openid_lisi",
      "avatar": "https://example.com/avatar2.jpg",
      "totalScore": 3,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
    },
    {
      "id": 3,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "user": null,
      "userName": "游客小王",
      "wechatUserId": "",
      "avatar": "https://example.com/guest-avatar.jpg",
      "totalScore": 12,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
    }
  ]
}
```

**轮次计分记录创建**:
调用成功后，**不会自动创建轮次计分记录**。如需创建轮次计分记录，请使用轮次计分接口：
```bash
POST /api/v1/matches/{matchId}/rounds
```

### 示例2: 批量更新参与者

**请求**:
```bash
PUT /api/v1/participants/batch
Content-Type: application/json
```

**请求体**:
```json
[
  {
    "id": 1,
    "userName": "张三（更新）",
    "totalScore": 100
  },
  {
    "id": 2,
    "userName": "李四（更新）",
    "totalScore": 200
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
      "userName": "张三（更新）",
      "totalScore": 100,
      "updateTime": 1716700900000
    },
    {
      "id": 2,
      "userName": "李四（更新）",
      "totalScore": 200,
      "updateTime": 1716700900000
    }
  ]
}
```

### 示例3: 批量退出对局

**请求**:
```bash
PUT /api/v1/participants/batch/quit
Content-Type: application/json
```

**请求体**:
```json
[3, 4]
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 3,
      "userName": "游客小王",
      "totalScore": 0
    },
    {
      "id": 4,
      "userName": "王五",
      "totalScore": 0
    }
  ]
}
```

### 示例4: 批量删除参与者

**请求**:
```bash
DELETE /api/v1/participants/batch
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

**重要说明**:
- 删除参与者时会**自动级联删除**相关的轮次计分记录
- 确保数据一致性，避免孤立的轮次计分数据

## 错误处理

### 常见错误响应

**对局不存在**:
```json
{
  "code": 400,
  "message": "对局不存在，matchId: 999",
  "data": null
}
```

**参与者列表为空**:
```json
{
  "code": 400,
  "message": "参与者列表不能为空",
  "data": null
}
```

**参与者ID列表为空**:
```json
{
  "code": 400,
  "message": "参与者ID列表不能为空",
  "data": null
}
```

## 注意事项

1. **事务性**: 所有批量操作都在事务中执行，要么全部成功，要么全部回滚
2. **验证**: 批量添加时会验证对局是否存在
3. **游客支持**: 支持添加游客参与者（user 为 null）
4. **部分更新**: 批量更新时，只更新提供的字段，未提供的字段保持不变
5. **删除操作**: 批量退出和删除都会永久删除参与者记录
6. **级联删除**: 删除参与者时会自动删除相关的轮次计分记录，确保数据一致性
7. **轮次计分数据**: 当提供 `roundScore` 数据时，仅用于数据传输，**不会自动创建轮次计分记录**
8. **轮次计分字段**: `roundScore` 字段为可选，如果提供则必须包含 `score` 字段
9. **单轮次限制**: 每个参与者只能包含一个轮次计分数据，`roundNumber` 默认为1
10. **手动创建**: 如需创建轮次计分记录，请单独调用轮次计分接口

## 性能建议

1. **批量大小**: 建议单次批量操作不超过100个参与者
2. **网络超时**: 大批量操作可能需要较长时间，注意设置合适的超时时间
3. **错误处理**: 建议在客户端实现重试机制

## 测试用例

### cURL 测试命令

```bash
# 1. 批量添加参与者（基础版本）
curl -X POST "http://localhost:8080/api/v1/matches/1/participants/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {"user": {"id": 101}, "userName": "张三"},
    {"user": null, "userName": "游客小王"}
  ]'

# 1.1. 批量添加参与者（包含轮次计分数据）⭐ 新功能
curl -X POST "http://localhost:8080/api/v1/matches/1/participants/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "user": {"id": 101}, 
      "userName": "张三",
      "roundScore": {"roundNumber": 1, "score": 10}
    },
    {
      "user": null, 
      "userName": "游客小王",
      "roundScore": {"score": 15}
    }
  ]'

# 2. 批量更新参与者
curl -X PUT "http://localhost:8080/api/v1/participants/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {"id": 1, "userName": "张三（更新）", "totalScore": 100},
    {"id": 2, "userName": "李四（更新）", "totalScore": 200}
  ]'

# 3. 批量退出对局
curl -X PUT "http://localhost:8080/api/v1/participants/batch/quit" \
  -H "Content-Type: application/json" \
  -d '[1, 2]'

# 4. 批量删除参与者
curl -X DELETE "http://localhost:8080/api/v1/participants/batch" \
  -H "Content-Type: application/json" \
  -d '[1, 2]'
```

---

**文档版本**: v2.0  
**更新时间**: 2024年12月  
**更新内容**: 新增轮次计分数据支持，支持一次调用完成参与者创建和轮次计分录入
