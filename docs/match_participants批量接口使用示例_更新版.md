# match_participants 批量接口使用示例（更新版）

## 概述

为 `match_participants` 表提供了完整的批量操作接口，支持批量添加、更新、退出和删除参与者。所有接口都支持游客模式和注册用户模式。

## 接口列表

### 1. 批量添加参与者
- **接口**: `POST /api/v1/matches/{matchId}/participants/batch`
- **功能**: 一次性为指定对局添加多个参与者
- **支持**: 游客模式和注册用户模式

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

### 示例1: 批量添加参与者

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
    "userName": "张三"
  },
  {
    "user": {
      "id": 102
    },
    "userName": "李四"
  },
  {
    "user": null,
    "userName": "游客小王"
  },
  {
    "user": {
      "id": 103
    },
    "userName": "王五"
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
      "user": {
        "id": 101,
        "nickname": "张三"
      },
      "userName": "张三",
      "totalScore": 0,
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
      "totalScore": 0,
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
      "totalScore": 0,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
    },
    {
      "id": 4,
      "match": {
        "matchId": 1,
        "roomName": "快乐棋牌室"
      },
      "user": {
        "id": 103,
        "nickname": "王五"
      },
      "userName": "王五",
      "totalScore": 0,
      "createTime": 1716700800000,
      "updateTime": 1716700800000
    }
  ]
}
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

**参与者用户名不能为空**:
```json
{
  "code": 500,
  "message": "批量添加参与者失败: 参与者用户名不能为空",
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

## 字段说明

### 请求字段

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| user | Object | 否 | 微信用户对象，为null表示游客 |
| user.id | Long | 否 | 微信用户ID |
| userName | String | 是 | 用户名称（显示名称） |
| id | Long | 否 | 参与者ID（更新时必填） |
| totalScore | Integer | 否 | 总得分（更新时可选） |

### 响应字段

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 参与者ID |
| match | Object | 对局信息 |
| match.matchId | Long | 对局ID |
| match.roomName | String | 房间名称 |
| user | Object | 微信用户信息（游客时为null） |
| user.id | Long | 微信用户ID |
| user.nickname | String | 微信用户昵称 |
| userName | String | 用户名称 |
| totalScore | Integer | 总得分 |
| createTime | Long | 创建时间 |
| updateTime | Long | 更新时间 |

## 注意事项

1. **事务性**: 所有批量操作都在事务中执行，要么全部成功，要么全部回滚
2. **验证**: 批量添加时会验证对局是否存在和用户名是否为空
3. **游客支持**: 支持添加游客参与者（user 为 null）
4. **部分更新**: 批量更新时，只更新提供的字段，未提供的字段保持不变
5. **删除操作**: 批量退出和删除都会永久删除参与者记录
6. **字段映射**: 数据库字段 `nickname` 对应API字段 `userName`

## 性能建议

1. **批量大小**: 建议单次批量操作不超过100个参与者
2. **网络超时**: 大批量操作可能需要较长时间，注意设置合适的超时时间
3. **错误处理**: 建议在客户端实现重试机制

## 测试用例

### cURL 测试命令

```bash
# 1. 批量添加参与者
curl -X POST "http://localhost:8080/api/v1/matches/1/participants/batch" \
  -H "Content-Type: application/json" \
  -d '[
    {"user": {"id": 101}, "userName": "张三"},
    {"user": null, "userName": "游客小王"}
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
**更新内容**: 修复字段映射问题，完善错误处理，支持游客模式
