# 麻将计分系统完整API接口文档

## 1. 概述

本文档提供了麻将计分系统的完整API接口定义，包括用户管理、微信用户管理、棋牌室管理、对局管理、参与者管理、轮次计分和收盘结算等所有功能接口。

## 2. 基础信息

- **API 基础路径**: `http://localhost:8080/api`
- **响应格式**: 统一 JSON 格式
- **状态码**: 遵循 HTTP 标准状态码

### 2.1 通用响应格式

成功响应:
```json
{
  "code": 200,              // 状态码
  "message": "success",    // 状态消息
  "data": {}               // 返回的数据
}
```

失败响应:
```json
{
  "code": 错误码,           // 错误状态码
  "message": "错误信息",    // 错误描述
  "data": null             // 数据为null
}
```

## 3. 测试接口 (TestController)

### 3.1 健康检查接口

**URL**: `/api/test/hello`
**方法**: `GET`
**描述**: 简单的健康检查接口，返回Hello World
**响应**: `200 OK`
```
Hello World!
```

### 3.2 测试用户创建接口

**URL**: `/api/test/user`
**方法**: `POST`
**描述**: 测试创建用户的接口，用于开发调试
**请求体**:
```json
{
  "username": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com",
  "password": "123456",
  "role": "user",
  "status": "active",
  "avatar": null
}
```
**响应**: `200 OK`
```
用户创建成功: 测试用户 (13800138000)
```

## 4. 用户管理接口 (UserController)

### 4.1 创建用户

**URL**: `/api/users`
**方法**: `POST`
**描述**: 创建新用户
**请求体**:
```json
{
  "username": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "password": "123456"
}
```
**响应**: `201 Created`

### 4.2 获取所有用户

**URL**: `/api/users`
**方法**: `GET`
**描述**: 获取所有用户列表
**响应**: `200 OK`

### 4.3 根据ID获取用户

**URL**: `/api/users/{id}`
**方法**: `GET`
**描述**: 根据用户ID获取用户信息
**路径参数**: `id` - 用户ID
**响应**: `200 OK` 或 `404 Not Found`

### 4.4 更新用户

**URL**: `/api/users/{id}`
**方法**: `PUT`
**描述**: 更新用户信息
**路径参数**: `id` - 用户ID
**请求体**:
```json
{
  "username": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "password": "654321"
}
```
**响应**: `200 OK`

### 4.5 删除用户

**URL**: `/api/users/{id}`
**方法**: `DELETE`
**描述**: 删除指定ID的用户
**路径参数**: `id` - 用户ID
**响应**: `204 No Content`

### 4.6 检查手机号是否存在

**URL**: `/api/users/exists/phone/{phone}`
**方法**: `GET`
**描述**: 检查指定手机号是否已被注册
**路径参数**: `phone` - 手机号
**响应**: `200 OK` - 返回布尔值

## 5. 版本化用户管理接口 (UserApiController)

### 5.1 创建用户 (V1)

**URL**: `/api/v1/users`
**方法**: `POST`
**描述**: 创建新用户，支持前端页面和微信小程序调用
**请求体**:
```json
{
  "username": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "password": "123456"
}
```
**响应**: `201 Created`
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "张三",
    "phone": "13800138000",
    "email": "zhangsan@example.com",
    "role": "user",
    "status": "active",
    "avatar": null
  }
}
```

### 5.2 获取用户列表 (V1)

**URL**: `/api/v1/users?page=1&limit=10`
**方法**: `GET`
**描述**: 获取用户列表，支持分页
**请求参数**:
- `page`: 页码，默认为1
- `limit`: 每页数量，默认为10
**响应**: `200 OK`
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "username": "张三",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "role": "user",
      "status": "active",
      "avatar": null
    }
  ]
}
```

## 6. 微信小程序用户管理接口 (WechatMiniController)

### 6.1 创建微信用户

**URL**: `/api/v1/wechat-users`
**方法**: `POST`
**描述**: 创建新的微信用户，用于微信小程序用户注册或首次登录
**请求体**:
```json
{
  "userId": "wx_user_123456",
  "nickname": "微信用户",
  "username": "wxuser",
  "avatar": "http://example.com/avatar.jpg"
}
```
**响应**: `200 OK`
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "userId": "wx_user_123456",
    "nickname": "微信用户",
    "username": "wxuser",
    "avatar": "http://example.com/avatar.jpg"
  }
}
```

### 6.2 获取所有微信用户

**URL**: `/api/v1/wechat-users`
**方法**: `GET`
**描述**: 获取所有微信用户列表
**响应**: `200 OK`
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": "wx_user_123456",
      "nickname": "微信用户",
      "username": "wxuser",
      "avatar": "http://example.com/avatar.jpg"
    }
  ]
}
```

### 6.3 根据系统ID获取微信用户

**URL**: `/api/v1/wechat-users/{id}`
**方法**: `GET`
**描述**: 根据系统自增ID获取微信用户信息
**路径参数**: `id` - 系统用户ID
**响应**: `200 OK` 或 `404 Not Found`

### 6.4 根据微信用户ID获取用户

**URL**: `/api/v1/wechat-users/user-id/{userId}`
**方法**: `GET`
**描述**: 根据微信用户唯一标识获取用户信息
**路径参数**: `userId` - 微信用户唯一标识
**响应**: `200 OK` 或 `404 Not Found`

### 6.5 更新微信用户信息

**URL**: `/api/v1/wechat-users/{id}`
**方法**: `PUT`
**描述**: 更新微信用户信息
**路径参数**: `id` - 系统用户ID
**请求体**:
```json
{
  "userId": "wx_user_123456",
  "nickname": "更新后的昵称",
  "username": "updateduser",
  "avatar": "http://example.com/new_avatar.jpg"
}
```
**响应**: `200 OK`

### 6.6 删除微信用户

**URL**: `/api/v1/wechat-users/{id}`
**方法**: `DELETE`
**描述**: 删除指定ID的微信用户
**路径参数**: `id` - 系统用户ID
**响应**: `200 OK`

## 7. 棋牌室管理接口 (RoomController)

### 7.1 创建棋牌室

**URL**: `/api/rooms`
**方法**: `POST`
**描述**: 创建新的棋牌室
**请求体**:
```json
{
  "name": "快乐棋牌室",
  "address": "北京市朝阳区",
  "contactPhone": "010-12345678"
}
```
**响应**: `201 Created`

### 7.2 获取所有棋牌室

**URL**: `/api/rooms`
**方法**: `GET`
**描述**: 获取所有棋牌室列表
**响应**: `200 OK`

### 7.3 根据ID获取棋牌室

**URL**: `/api/rooms/{id}`
**方法**: `GET`
**描述**: 根据ID获取棋牌室详细信息
**路径参数**: `id` - 棋牌室ID
**响应**: `200 OK` 或 `404 Not Found`

### 7.4 更新棋牌室

**URL**: `/api/rooms/{id}`
**方法**: `PUT`
**描述**: 更新棋牌室信息
**路径参数**: `id` - 棋牌室ID
**请求体**:
```json
{
  "name": "新棋牌室",
  "address": "北京市海淀区",
  "contactPhone": "010-87654321"
}
```
**响应**: `200 OK`

### 7.5 删除棋牌室

**URL**: `/api/rooms/{id}`
**方法**: `DELETE`
**描述**: 删除指定ID的棋牌室
**路径参数**: `id` - 棋牌室ID
**响应**: `204 No Content`

### 7.6 检查棋牌室名称是否存在

**URL**: `/api/rooms/exists/name/{name}`
**方法**: `GET`
**描述**: 检查指定棋牌室名称是否已存在
**路径参数**: `name` - 棋牌室名称
**响应**: `200 OK` - 返回布尔值

## 8. 对局管理接口 (MatchController)

### 8.1 创建对局

**URL**: `/api/v1/matches`
**方法**: `POST`
**描述**: 创建一个新的麻将对局
**请求体**:
```json
{
  "room": {
    "id": 1
  },
  "roomName": "快乐棋牌室",
  "startTime": 1716700800000,
  "totalRounds": 4
}
```
**请求参数说明**:
- `room.id`: 房间ID（必填），必须是数据库中存在的房间
- `roomName`: 房间名称（可选），如果不提供会自动从room对象获取
- `startTime`: 开始时间（可选），毫秒级时间戳，不提供则使用当前时间
- `totalRounds`: 总轮数（可选），默认为0
- `status`: 对局状态（可选），默认为"进行中"

**成功响应**: `200 OK`
```json
{
  "success": true,
  "data": {
    "matchId": 1,
    "room": {
      "id": 1,
      "name": "快乐棋牌室"
    },
    "roomName": "快乐棋牌室",
    "startTime": 1716700800000,
    "status": "进行中",
    "totalRounds": 4,
    "currentRound": 0,
    "createTime": 1716700800000,
    "updateTime": 1716700800000
  }
}
```

**错误响应**: `400 Bad Request`
```json
{
  "success": false,
  "error": "对局必须关联一个房间，请提供room信息"
}
```

**常见错误**:
- `400 Bad Request`: 请求体为空或room信息缺失
- `400 Bad Request`: 指定的房间不存在
- `500 Internal Server Error`: 服务器内部错误

### 8.2 获取对局列表

**URL**: `/api/v1/matches`
**方法**: `GET`
**描述**: 获取所有对局列表
**响应**: `200 OK`
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "matchId": 1,
      "roomId": 1,
      "roomName": "快乐棋牌室",
      "startTime": 1716700800000,
      "status": "进行中"
    }
  ]
}
```

### 8.3 获取对局详情

**URL**: `/api/v1/matches/{matchId}`
**方法**: `GET`
**描述**: 根据ID获取对局详细信息
**路径参数**: `matchId` - 对局ID
**响应**: `200 OK` 或 `404 Not Found`

### 8.4 获取指定房间的对局

**URL**: `/api/v1/matches/room/{roomId}`
**方法**: `GET`
**描述**: 获取指定房间的所有对局
**路径参数**: `roomId` - 房间ID
**响应**: `200 OK`

### 8.5 更新对局信息

**URL**: `/api/v1/matches/{matchId}`
**方法**: `PUT`
**描述**: 更新对局信息
**路径参数**: `matchId` - 对局ID
**请求体**:
```json
{
  "roomName": "新棋牌室名称"
}
```
**响应**: `200 OK` 或 `404 Not Found`

### 8.6 结束对局

**URL**: `/api/v1/matches/{matchId}/end`
**方法**: `PUT`
**描述**: 结束指定的对局
**路径参数**: `matchId` - 对局ID
**响应**: `200 OK` 或 `404 Not Found`

### 8.7 删除对局

**URL**: `/api/v1/matches/{matchId}`
**方法**: `DELETE`
**描述**: 删除指定的对局
**路径参数**: `matchId` - 对局ID
**响应**: `200 OK`

## 9. 参与者管理接口

### 9.1 添加参与者

**URL**: `/api/v1/matches/{matchId}/participants`
**方法**: `POST`
**描述**: 为指定对局添加参与者
**路径参数**: `matchId` - 对局ID
**请求体**:
```json
{
  "userId": 1,
  "nickname": "玩家1",
  "avatar": "http://example.com/avatar1.jpg"
}
```
**响应**: `200 OK` 或 `404 Not Found`

### 9.2 获取对局参与者列表

**URL**: `/api/v1/matches/{matchId}/participants`
**方法**: `GET`
**描述**: 获取指定对局的所有参与者
**路径参数**: `matchId` - 对局ID
**响应**: `200 OK`

### 9.3 更新参与者信息

**URL**: `/api/v1/participants/{participantId}`
**方法**: `PUT`
**描述**: 更新参与者信息
**路径参数**: `participantId` - 参与者ID
**请求体**:
```json
{
  "nickname": "更新后的昵称",
  "avatar": "http://example.com/new_avatar.jpg"
}
```
**响应**: `200 OK` 或 `404 Not Found`

### 9.4 参与者退出对局

**URL**: `/api/v1/participants/{participantId}/quit`
**方法**: `PUT`
**描述**: 让指定参与者退出对局
**路径参数**: `participantId` - 参与者ID
**响应**: `200 OK` 或 `404 Not Found`

### 9.5 获取参与者排名

**URL**: `/api/v1/matches/{matchId}/participants/ranking`
**方法**: `GET`
**描述**: 获取对局参与者的排名信息
**路径参数**: `matchId` - 对局ID
**响应**: `200 OK`

### 9.6 获取参与者总分

**URL**: `/api/v1/participants/{participantId}/total-score`
**方法**: `GET`
**描述**: 获取指定参与者的总分
**路径参数**: `participantId` - 参与者ID
**响应**: `200 OK`

### 9.7 获取参与者最终得分

**URL**: `/api/v1/participants/{participantId}/final-score`
**方法**: `GET`
**描述**: 获取指定参与者的最终得分（考虑结算倍率）
**路径参数**: `participantId` - 参与者ID
**响应**: `200 OK`

### 9.8 获取参与者轮次记录

**URL**: `/api/v1/participants/{participantId}/rounds`
**方法**: `GET`
**描述**: 获取指定参与者的所有轮次记录
**路径参数**: `participantId` - 参与者ID
**响应**: `200 OK`

## 10. 轮次得分记录接口

### 10.1 记录轮次得分

**URL**: `/api/v1/matches/{matchId}/rounds/{roundNumber}`
**方法**: `POST`
**描述**: 记录指定对局和轮次的得分信息
**路径参数**:
- `matchId` - 对局ID
- `roundNumber` - 轮次编号
**请求体**:
```json
[
  {
    "participantId": 1,
    "score": 10,
    "description": "自摸"
  },
  {
    "participantId": 2,
    "score": -5,
    "description": "点炮"
  }
]
```
**响应**: `200 OK`

### 10.2 获取对局轮次记录

**URL**: `/api/v1/matches/{matchId}/rounds`
**方法**: `GET`
**描述**: 获取指定对局的所有轮次记录
**路径参数**: `matchId` - 对局ID
**响应**: `200 OK`

### 10.3 获取当前轮次编号

**URL**: `/api/v1/matches/{matchId}/rounds/current-number`
**方法**: `GET`
**描述**: 获取指定对局的当前轮次编号
**路径参数**: `matchId` - 对局ID
**响应**: `200 OK`

### 10.4 获取指定轮次详情

**URL**: `/api/v1/matches/{matchId}/rounds/{roundNumber}`
**方法**: `GET`
**描述**: 获取指定对局和轮次的详细信息
**路径参数**:
- `matchId` - 对局ID
- `roundNumber` - 轮次编号
**响应**: `200 OK`

## 11. 对局结算接口

### 11.1 对局结算

**URL**: `/api/v1/matches/{matchId}/settle`
**方法**: `POST`
**描述**: 对指定对局进行结算
**路径参数**: `matchId` - 对局ID
**请求体**:
```json
{
  "multiplier": 2.0,
  "notes": "常规结算"
}
```
**响应**: `200 OK` 或 `404 Not Found` 或 `400 Bad Request`

### 11.2 获取结算信息

**URL**: `/api/v1/matches/{matchId}/settlement`
**方法**: `GET`
**描述**: 获取指定对局的结算信息
**路径参数**: `matchId` - 对局ID
**响应**: `200 OK` 或 `404 Not Found`

### 11.3 检查对局是否已结算

**URL**: `/api/v1/matches/{matchId}/is-settled`
**方法**: `GET`
**描述**: 检查指定对局是否已完成结算
**路径参数**: `matchId` - 对局ID
**响应**: `200 OK` - 返回布尔值

## 12. 计分记录接口 (ScoreRecordController)

### 12.1 创建计分记录

**URL**: `/api/score-records`
**方法**: `POST`
**描述**: 创建新的计分记录
**请求体**:
```json
{
  "matchId": 1,
  "participantId": 1,
  "score": 10,
  "description": "自摸"
}
```
**响应**: `201 Created`

## 13. 错误码说明

| 错误码 | 说明 |
|-------|------|
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如手机号已存在） |
| 500 | 服务器内部错误 |

## 14. 接口使用注意事项

1. 所有接口均支持JSON格式的数据交换
2. 部分接口支持跨域访问（使用了@CrossOrigin注解）
3. 日期时间使用Unix时间戳（毫秒级）
4. 分页接口默认返回第一页，每页10条数据
5. 建议使用HTTPS进行接口调用（生产环境）
6. 敏感操作建议添加身份验证机制

## 15. 数据完整性说明

### 15.1 对局创建数据完整性要求

创建对局时必须满足以下数据完整性要求：

1. **房间关联**: 对局必须关联一个有效的房间
   - `room.id` 必须提供且不能为空
   - 指定的房间ID必须在数据库中存在
   - 如果`roomName`未提供，系统会自动从房间对象获取

2. **时间戳格式**: 所有时间字段使用毫秒级Unix时间戳
   - `startTime`: 对局开始时间
   - `endTime`: 对局结束时间（可选）
   - `createTime`: 记录创建时间（自动生成）
   - `updateTime`: 记录更新时间（自动生成）

3. **状态管理**: 对局状态遵循预定义的状态值
   - 默认状态: "进行中"
   - 可选状态: "已完成", "已取消"

### 15.2 常见数据完整性错误

| 错误类型 | 错误信息 | 解决方案 |
|---------|---------|---------|
| `DataIntegrityViolationException` | not-null property references a null or transient value | 确保所有必填字段都有值 |
| `IllegalArgumentException` | 对局必须关联一个房间 | 在请求体中提供room对象 |
| `IllegalArgumentException` | 指定的房间不存在 | 使用数据库中存在的房间ID |

### 15.3 最佳实践

1. **创建对局前先创建房间**: 确保房间已存在于数据库中
2. **使用完整的请求体**: 提供所有必要的字段信息
3. **验证数据有效性**: 客户端应验证房间ID的有效性
4. **错误处理**: 实现适当的错误处理机制来处理数据完整性错误

