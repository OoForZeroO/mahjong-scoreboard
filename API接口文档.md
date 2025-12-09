# 麻将计分系统 API 接口文档

## 目录

1. [基础信息](#基础信息)
2. [通用响应格式](#通用响应格式)
3. [测试接口](#测试接口)
4. [用户管理接口](#用户管理接口)
5. [微信用户管理接口](#微信用户管理接口)
6. [房间管理接口](#房间管理接口)
7. [对局管理接口](#对局管理接口)
8. [参与者管理接口](#参与者管理接口)
9. [轮次得分接口](#轮次得分接口)
10. [对局结算接口](#对局结算接口)
11. [统计查询接口](#统计查询接口)
12. [计分记录接口](#计分记录接口)

---

## 基础信息

### 基础URL

- **生产环境**: `https://yaohufox.com` 或 `http://localhost:8081`
- **测试环境**: `http://localhost:8082`
- **API版本**: v1

### 请求头

所有接口请求需要设置以下请求头：

```
Content-Type: application/json
```

### 认证

当前版本无需认证，后续版本可能添加Token认证。

---

## 通用响应格式

### 成功响应

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 错误响应

```json
{
  "code": 错误码,
  "message": "错误信息",
  "data": null
}
```

### HTTP状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如手机号已存在） |
| 500 | 服务器内部错误 |

---

## 测试接口

### 1. 健康检查

**接口**: `GET /api/test/hello`

**描述**: 简单的健康检查接口，用于验证服务是否正常运行

**请求参数**: 无

**响应示例**:
```
Hello World!
```

**调用示例**:
```bash
curl https://yaohufox.com/api/test/hello
```

---

## 用户管理接口

### 1. 创建用户

**接口**: `POST /api/v1/users`

**描述**: 创建新用户，支持前端页面和微信小程序调用

**请求体**:
```json
{
  "username": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "password": "123456",
  "role": "user",
  "status": "active",
  "avatar": "https://example.com/avatar.jpg"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| phone | String | 是 | 手机号，格式：1[3-9]xxxxxxxxx |
| email | String | 否 | 邮箱地址 |
| password | String | 是 | 密码 |
| role | String | 否 | 角色，默认：user |
| status | String | 否 | 状态，默认：active |
| avatar | String | 否 | 头像URL |

**成功响应** (201):
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
    "avatar": "https://example.com/avatar.jpg"
  }
}
```

**错误响应** (409 - 手机号已存在):
```json
{
  "code": 409,
  "message": "手机号已被注册",
  "data": null
}
```

### 2. 获取用户列表

**接口**: `GET /api/v1/users`

**描述**: 获取用户列表，支持分页

**请求参数**:
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码 |
| limit | Integer | 否 | 10 | 每页数量 |

**响应示例**:
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
      "avatar": "https://example.com/avatar.jpg"
    }
  ]
}
```

### 3. 根据手机号查询用户

**接口**: `GET /api/v1/users/phone/{phone}`

**描述**: 根据手机号查询用户信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |

**响应示例**:
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
    "status": "active"
  }
}
```

### 4. 检查手机号是否存在

**接口**: `GET /api/v1/users/exists/phone/{phone}`

**描述**: 检查指定手机号是否已被注册

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "exists": true
  }
}
```

---

## 微信用户管理接口

### 1. 创建微信用户

**接口**: `POST /api/v1/wechat-users`

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

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 微信用户唯一标识（openid） |
| nickname | String | 是 | 用户昵称 |
| username | String | 否 | 用户名称 |
| avatar | String | 否 | 用户头像URL |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": "wx_user_123456",
    "nickname": "微信用户",
    "username": "wxuser",
    "avatar": "http://example.com/avatar.jpg",
    "createTime": 1701234567890,
    "updateTime": 1701234567890
  }
}
```

**错误响应** (500):
```json
{
  "code": 500,
  "message": "创建微信用户失败: xxx",
  "data": null
}
```

### 2. 获取所有微信用户

**接口**: `GET /api/v1/wechat-users`

**描述**: 获取所有微信用户列表

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
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

### 3. 根据ID获取微信用户

**接口**: `GET /api/v1/wechat-users/{id}`

**描述**: 根据系统ID获取微信用户信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 微信用户系统ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": "wx_user_123456",
    "nickname": "微信用户",
    "username": "wxuser",
    "avatar": "http://example.com/avatar.jpg"
  }
}
```

### 4. 根据微信用户ID获取用户

**接口**: `GET /api/v1/wechat-users/user-id/{userId}`

**描述**: 根据微信用户唯一标识（openid）获取用户信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 微信用户唯一标识（openid） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": "wx_user_123456",
    "nickname": "微信用户",
    "username": "wxuser",
    "avatar": "http://example.com/avatar.jpg"
  }
}
```

### 5. 更新微信用户

**接口**: `PUT /api/v1/wechat-users/{id}`

**描述**: 更新微信用户信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 微信用户系统ID |

**请求体**:
```json
{
  "nickname": "新昵称",
  "username": "newname",
  "avatar": "http://example.com/new-avatar.jpg"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": "wx_user_123456",
    "nickname": "新昵称",
    "username": "newname",
    "avatar": "http://example.com/new-avatar.jpg"
  }
}
```

### 6. 删除微信用户

**接口**: `DELETE /api/v1/wechat-users/{id}`

**描述**: 删除指定ID的微信用户

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 微信用户系统ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "用户删除成功"
}
```

---

## 房间管理接口

### 1. 创建房间

**接口**: `POST /api/rooms`

**描述**: 创建新的棋牌室

**请求体**:
```json
{
  "name": "快乐棋牌室",
  "logo": "https://example.com/logo.jpg"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 房间名称 |
| logo | String | 否 | 房间Logo URL |

**成功响应** (201):
```json
{
  "id": 1,
  "name": "快乐棋牌室",
  "logo": "https://example.com/logo.jpg",
  "createTime": 1701234567890,
  "updateTime": 1701234567890
}
```

### 2. 获取所有房间

**接口**: `GET /api/rooms`

**描述**: 获取所有棋牌室列表

**请求参数**: 无

**响应示例**:
```json
[
  {
    "id": 1,
    "name": "快乐棋牌室",
    "logo": "https://example.com/logo.jpg",
    "createTime": 1701234567890,
    "updateTime": 1701234567890
  }
]
```

### 3. 根据ID获取房间

**接口**: `GET /api/rooms/{id}`

**描述**: 根据ID获取棋牌室详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 房间ID |

**响应示例**:
```json
{
  "id": 1,
  "name": "快乐棋牌室",
  "logo": "https://example.com/logo.jpg",
  "createTime": 1701234567890,
  "updateTime": 1701234567890
}
```

### 4. 更新房间

**接口**: `PUT /api/rooms/{id}`

**描述**: 更新棋牌室信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 房间ID |

**请求体**:
```json
{
  "name": "新棋牌室",
  "logo": "https://example.com/new-logo.jpg"
}
```

**响应示例**:
```json
{
  "id": 1,
  "name": "新棋牌室",
  "logo": "https://example.com/new-logo.jpg",
  "createTime": 1701234567890,
  "updateTime": 1701234567891
}
```

### 5. 删除房间

**接口**: `DELETE /api/rooms/{id}`

**描述**: 删除指定ID的棋牌室

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 房间ID |

**响应**: 204 No Content

### 6. 检查房间名称是否存在

**接口**: `GET /api/rooms/exists/name/{name}`

**描述**: 检查指定房间名称是否已存在

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 是 | 房间名称 |

**响应示例**:
```json
true
```

---

## 对局管理接口

### 1. 创建对局

**接口**: `POST /api/v1/matches`

**描述**: 创建一个新的麻将对局

**请求体**:
```json
{
  "room": {
    "id": 1
  },
  "roomName": "默认棋牌室",
  "startTime": 1765016628196,
  "status": 0,
  "totalRounds": 0
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| room | Object | 否 | 房间对象，包含id字段 |
| room.id | Long | 否 | 房间ID |
| roomName | String | 否 | 房间名称，如果不提供则使用"默认房间" |
| startTime | Long | 否 | 开始时间戳（毫秒），不提供则使用当前时间 |
| status | Integer | 否 | 对局状态，0:进行中，1:已完成，默认0 |
| totalRounds | Integer | 否 | 总轮数，默认0 |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomName": "默认棋牌室",
    "startTime": 1765016628196,
    "endTime": null,
    "status": 0,
    "totalRounds": 0,
    "settlementMultiplier": null,
    "createTime": 1765016628196,
    "updateTime": 1765016628196
  }
}
```

**错误响应** (400):
```json
{
  "code": 400,
  "message": "请求体不能为空",
  "data": null
}
```

### 2. 获取所有对局

**接口**: `GET /api/v1/matches`

**描述**: 获取所有对局列表

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "matchId": 1,
      "roomName": "默认棋牌室",
      "startTime": 1765016628196,
      "status": 0,
      "totalRounds": 0
    }
  ]
}
```

### 3. 根据ID获取对局

**接口**: `GET /api/v1/matches/{matchId}`

**描述**: 根据ID获取对局详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomName": "默认棋牌室",
    "startTime": 1765016628196,
    "endTime": null,
    "status": 0,
    "totalRounds": 0
  }
}
```

### 4. 获取对局详情

**接口**: `GET /api/v1/matches/{matchId}/detail`

**描述**: 获取对局详细信息，包含参与者、轮次得分等完整信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomName": "默认棋牌室",
    "startTime": 1765016628196,
    "status": 0,
    "participants": [],
    "rounds": []
  }
}
```

### 5. 根据房间ID获取对局列表

**接口**: `GET /api/v1/matches/room/{roomId}`

**描述**: 获取指定房间的所有对局列表

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 房间ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "matchId": 1,
      "roomName": "默认棋牌室",
      "startTime": 1765016628196,
      "status": 0
    }
  ]
}
```

### 6. 根据状态查询对局

**接口**: `GET /api/v1/matches/status/{status}`

**描述**: 根据对局状态查询对局列表，支持按微信用户ID筛选

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 是 | 对局状态，0:进行中，1:已完成 |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| wechat_user_id | String | 否 | 微信用户ID，用于筛选该用户参与的对局 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "matchId": 1,
      "roomName": "默认棋牌室",
      "startTime": 1765016628196,
      "status": 0
    }
  ]
}
```

### 7. 更新对局

**接口**: `PUT /api/v1/matches/{matchId}`

**描述**: 更新对局信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**请求体**:
```json
{
  "roomName": "新房间名",
  "status": 0,
  "totalRounds": 4
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomName": "新房间名",
    "status": 0,
    "totalRounds": 4
  }
}
```

### 8. 结束对局（简单）

**接口**: `PUT /api/v1/matches/{matchId}/end`

**描述**: 结束对局，将状态设置为已完成

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**请求体**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "status": 1,
    "endTime": 1765016628196
  }
}
```

### 9. 结束对局（带详情）

**接口**: `PUT /api/v1/matches/{matchId}/end-details`

**描述**: 结束对局，可设置房间名称和结算倍率

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**请求体**:
```json
{
  "roomName": "默认棋牌室",
  "multiplier": 1.5
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomName | String | 否 | 房间名称 |
| multiplier | Double | 否 | 结算倍率 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomName": "默认棋牌室",
    "status": 1,
    "endTime": 1765016628196,
    "settlementMultiplier": 1.5
  }
}
```

### 10. 删除对局

**接口**: `DELETE /api/v1/matches/{matchId}`

**描述**: 删除指定ID的对局

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 11. 获取对局二维码

**接口**: `GET /api/v1/matches/{matchId}/qrcode`

**描述**: 生成对局二维码，用于分享对局信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**说明**: 
- 调用微信小程序API生成不限制数量的二维码
- 二维码的`scene`参数为对局ID
- 二维码以Base64格式返回，可直接用于前端显示

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 141,
    "qrcodeData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
  }
}
```

**失败响应** (404):
```json
{
  "code": 404,
  "message": "对局不存在",
  "data": null
}
```

---

## 参与者管理接口

### 1. 添加参与者

**接口**: `POST /api/v1/matches/{matchId}/participants`

**描述**: 向对局中添加一个参与者

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**请求体**:
```json
{
  "user": {
    "id": 1
  },
  "wechatUserId": "wx_user_123456",
  "userName": "玩家1",
  "nickName": "玩家1",
  "avatar": "https://example.com/avatar.jpg",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| user | Object | 否 | 用户对象，包含id字段 |
| user.id | Long | 否 | 用户ID（系统用户） |
| wechatUserId | String | 否 | 微信用户ID（openid） |
| userName | String | 是 | 用户名称/昵称 |
| nickName | String | 否 | 昵称（兼容字段） |
| avatar | String | 否 | 头像URL |
| avatarUrl | String | 否 | 头像URL（兼容字段） |

**说明**: user.id 和 wechatUserId 至少提供一个，如果都不提供则视为游客模式

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userName": "玩家1",
    "avatar": "https://example.com/avatar.jpg",
    "totalScore": 0,
    "isQuit": false
  }
}
```

### 2. 批量添加参与者

**接口**: `POST /api/v1/matches/{matchId}/participants/batch`

**描述**: 批量向对局中添加多个参与者

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**请求体**:
```json
[
  {
    "user": {
      "id": 1
    },
    "userName": "玩家1",
    "avatar": "https://example.com/avatar1.jpg"
  },
  {
    "wechatUserId": "wx_user_123456",
    "userName": "玩家2",
    "avatar": "https://example.com/avatar2.jpg"
  }
]
```

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userName": "玩家1",
      "totalScore": 0
    },
    {
      "id": 2,
      "userName": "玩家2",
      "totalScore": 0
    }
  ]
}
```

### 3. 获取参与者列表

**接口**: `GET /api/v1/matches/{matchId}/participants`

**描述**: 获取指定对局的所有参与者列表

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userName": "玩家1",
      "avatar": "https://example.com/avatar.jpg",
      "totalScore": 150,
      "isQuit": false
    }
  ]
}
```

### 4. 更新参与者信息

**接口**: `PUT /api/v1/matches/participants/{participantId}`

**描述**: 更新参与者信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| participantId | Long | 是 | 参与者ID |

**请求体**:
```json
{
  "userName": "新昵称",
  "avatar": "https://example.com/new-avatar.jpg"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userName": "新昵称",
    "avatar": "https://example.com/new-avatar.jpg"
  }
}
```

### 5. 批量更新参与者

**接口**: `PUT /api/v1/matches/participants/batch`

**描述**: 批量更新多个参与者信息

**请求体**:
```json
[
  {
    "id": 1,
    "userName": "新昵称1"
  },
  {
    "id": 2,
    "userName": "新昵称2"
  }
]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userName": "新昵称1"
    },
    {
      "id": 2,
      "userName": "新昵称2"
    }
  ]
}
```

### 6. 退出对局

**接口**: `PUT /api/v1/matches/participants/{participantId}/quit`

**描述**: 参与者退出对局

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| participantId | Long | 是 | 参与者ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "isQuit": true,
    "quitTime": 1765016628196
  }
}
```

### 7. 重新启用参与者

**接口**: `PUT /api/v1/matches/participants/{participantId}/reactivate`

**描述**: 重新启用已退出的参与者

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| participantId | Long | 是 | 参与者ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "isQuit": false,
    "quitTime": null
  }
}
```

### 8. 重新加入对局

**接口**: `PUT /api/v1/matches/participants/{participantId}/rejoin`

**描述**: 参与者重新加入对局（与reactivate功能相同）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| participantId | Long | 是 | 参与者ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "isQuit": false
  }
}
```

### 9. 批量退出对局

**接口**: `PUT /api/v1/matches/participants/batch/quit`

**描述**: 批量退出对局

**请求体**:
```json
[1, 2, 3]
```

**说明**: 传入参与者ID列表

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "isQuit": true
    },
    {
      "id": 2,
      "isQuit": true
    }
  ]
}
```

### 10. 批量删除参与者

**接口**: `DELETE /api/v1/matches/participants/batch`

**描述**: 批量删除参与者

**请求体**:
```json
[1, 2, 3]
```

**说明**: 传入参与者ID列表。注意：如果参与者已有轮次得分记录，则不允许删除

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**业务限制响应** (200):
```json
{
  "code": 200,
  "message": "参与者已有轮次得分记录，不允许删除",
  "data": null
}
```

### 11. 获取参与者排名

**接口**: `GET /api/v1/matches/{matchId}/participants/ranking`

**描述**: 获取对局中参与者的排名列表（按总分降序）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "userName": "玩家1",
      "totalScore": 200
    },
    {
      "id": 2,
      "userName": "玩家2",
      "totalScore": 150
    }
  ]
}
```

### 12. 获取参与者总分

**接口**: `GET /api/v1/matches/participants/{participantId}/total-score`

**描述**: 获取参与者的累计总分

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| participantId | Long | 是 | 参与者ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 150
}
```

### 13. 获取参与者最终得分

**接口**: `GET /api/v1/matches/participants/{participantId}/final-score`

**描述**: 获取参与者的最终得分（考虑结算倍率）

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| participantId | Long | 是 | 参与者ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 225.0
}
```

### 14. 获取参与者轮次记录

**接口**: `GET /api/v1/matches/participants/{participantId}/rounds`

**描述**: 获取参与者的所有轮次得分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| participantId | Long | 是 | 参与者ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "roundNumber": 1,
      "score": 50,
      "cumulativeScore": 50,
      "roundTime": 1765016628196
    }
  ]
}
```

---

## 轮次得分接口

### 1. 记录轮次得分

**接口**: `POST /api/v1/matches/{matchId}/rounds/{roundNumber}`

**描述**: 记录指定轮次的得分

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |
| roundNumber | Integer | 是 | 轮次号 |

**请求体**:
```json
[
  {
    "participantId": 1,
    "score": 50,
    "cumulativeScore": 50,
    "roundTime": 1765016628196
  },
  {
    "participantId": 2,
    "score": -30,
    "cumulativeScore": -30,
    "roundTime": 1765016628196
  }
]
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| participantId | Long | 是 | 参与者ID |
| score | Integer | 是 | 本轮得分 |
| cumulativeScore | Integer | 是 | 累计得分 |
| roundTime | Long | 否 | 轮次时间戳（毫秒），不提供则使用当前时间 |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "roundNumber": 1,
      "score": 50,
      "cumulativeScore": 50,
      "roundTime": 1765016628196
    },
    {
      "id": 2,
      "roundNumber": 1,
      "score": -30,
      "cumulativeScore": -30,
      "roundTime": 1765016628196
    }
  ]
}
```

### 2. 获取对局所有轮次记录

**接口**: `GET /api/v1/matches/{matchId}/rounds`

**描述**: 获取对局的所有轮次得分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "roundNumber": 1,
      "participantId": 1,
      "score": 50,
      "cumulativeScore": 50
    },
    {
      "id": 2,
      "roundNumber": 2,
      "participantId": 1,
      "score": 30,
      "cumulativeScore": 80
    }
  ]
}
```

### 3. 获取当前轮次号

**接口**: `GET /api/v1/matches/{matchId}/rounds/current-number`

**描述**: 获取对局的当前轮次号

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 3
}
```

### 4. 获取指定轮次详情

**接口**: `GET /api/v1/matches/{matchId}/rounds/{roundNumber}`

**描述**: 获取指定轮次的所有参与者得分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |
| roundNumber | Integer | 是 | 轮次号 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "roundNumber": 1,
      "participantId": 1,
      "score": 50,
      "cumulativeScore": 50
    },
    {
      "id": 2,
      "roundNumber": 1,
      "participantId": 2,
      "score": -30,
      "cumulativeScore": -30
    }
  ]
}
```

### 5. 批量创建轮次得分

**接口**: `POST /api/v1/matches/rounds/batch`

**描述**: 批量创建轮次得分记录

**请求体**:
```json
[
  {
    "matchId": 1,
    "participantId": 1,
    "roundNumber": 1,
    "score": 50,
    "cumulativeScore": 50,
    "roundTime": 1765016628196
  },
  {
    "matchId": 1,
    "participantId": 2,
    "roundNumber": 1,
    "score": -30,
    "cumulativeScore": -30,
    "roundTime": 1765016628196
  }
]
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |
| participantId | Long | 是 | 参与者ID |
| roundNumber | Integer | 是 | 轮次号 |
| score | Integer | 是 | 本轮得分 |
| cumulativeScore | Integer | 是 | 累计得分 |
| roundTime | Long | 否 | 轮次时间戳（毫秒） |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "roundNumber": 1,
      "score": 50,
      "cumulativeScore": 50
    }
  ]
}
```

### 6. 批量更新轮次得分

**接口**: `PUT /api/v1/matches/rounds/batch`

**描述**: 批量更新轮次得分记录

**请求体**:
```json
[
  {
    "id": 1,
    "score": 60,
    "cumulativeScore": 60
  },
  {
    "id": 2,
    "score": -40,
    "cumulativeScore": -40
  }
]
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 轮次得分记录ID |
| score | Integer | 否 | 本轮得分 |
| cumulativeScore | Integer | 否 | 累计得分 |

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "score": 60,
      "cumulativeScore": 60
    }
  ]
}
```

### 7. 批量获取轮次得分

**接口**: `GET /api/v1/matches/rounds/batch`

**描述**: 根据ID列表批量获取轮次得分记录

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| scoreIds | List<Long> | 是 | 轮次得分ID列表 |

**请求示例**:
```
GET /api/v1/matches/rounds/batch?scoreIds=1&scoreIds=2&scoreIds=3
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "roundNumber": 1,
      "score": 50,
      "cumulativeScore": 50
    },
    {
      "id": 2,
      "roundNumber": 1,
      "score": -30,
      "cumulativeScore": -30
    }
  ]
}
```

### 8. 批量删除轮次得分

**接口**: `DELETE /api/v1/matches/rounds/batch`

**描述**: 批量删除轮次得分记录

**请求体**:
```json
[1, 2, 3]
```

**说明**: 传入轮次得分ID列表

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 对局结算接口

### 1. 执行对局结算（收盘）

**接口**: `POST /api/v1/matches/{matchId}/settle`

**描述**: 执行对局结算（收盘），可设置房间名称和结算倍率

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**请求体**（可选）:
```json
{
  "roomName": "默认棋牌室",
  "multiplier": 1.5
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomName | String | 否 | 房间名称 |
| multiplier | Double | 否 | 结算倍率 |

**说明**: 
- 如果请求体为空，则执行简单收盘（仅设置状态为已完成）
- 如果请求体包含roomName或multiplier，则执行带详情的收盘

**成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "status": 1,
    "endTime": 1765016628196,
    "settlementMultiplier": 1.5
  }
}
```

### 2. 获取对局结果

**接口**: `GET /api/v1/matches/{matchId}/result`

**描述**: 获取对局结算结果，包含获胜者、最高分、最低分等信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "winnerId": 1,
    "highestScore": 200,
    "lowestScore": -100,
    "totalDuration": 3600000
  }
}
```

### 3. 检查对局是否已完成

**接口**: `GET /api/v1/matches/{matchId}/is-completed`

**描述**: 检查对局是否已完成结算

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

## 统计查询接口

### 1. 获取用户月度统计

**接口**: `POST /api/v1/statistics/users/{wechatUserId}/monthly`

**描述**: 获取指定微信用户的月度统计数据

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| wechatUserId | String | 是 | 微信用户ID（openid） |

**请求体**:
```json
{
  "year": 2024,
  "month": 12
}
```

**参数说明**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| year | Integer | 是 | 年份 |
| month | Integer | 否 | 月份，不提供则返回全年统计 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "year": 2024,
    "month": 12,
    "totalMatches": 10,
    "completedMatches": 8,
    "totalScore": 500,
    "averageScore": 62.5
  }
}
```

---

## 计分记录接口

### 1. 创建计分记录

**接口**: `POST /api/score-records`

**描述**: 创建新的计分记录

**请求体**:
```json
{
  "matchId": 1,
  "userId": 1,
  "roomId": 1,
  "score": 150,
  "status": "completed"
}
```

**成功响应** (201):
```json
{
  "matchId": 1,
  "userId": 1,
  "roomId": 1,
  "score": 150,
  "status": "completed"
}
```

### 2. 获取所有计分记录

**接口**: `GET /api/score-records`

**描述**: 获取所有计分记录列表

**响应示例**:
```json
[
  {
    "matchId": 1,
    "userId": 1,
    "score": 150,
    "status": "completed"
  }
]
```

### 3. 根据对局ID获取计分记录

**接口**: `GET /api/score-records/{matchId}`

**描述**: 根据对局ID获取计分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应示例**:
```json
{
  "matchId": 1,
  "userId": 1,
  "score": 150,
  "status": "completed"
}
```

### 4. 根据用户ID获取计分记录

**接口**: `GET /api/score-records/user/{userId}`

**描述**: 根据用户ID获取该用户的所有计分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
[
  {
    "matchId": 1,
    "userId": 1,
    "score": 150,
    "status": "completed"
  }
]
```

### 5. 获取用户最近的计分记录

**接口**: `GET /api/score-records/user/{userId}/recent`

**描述**: 获取用户最近的计分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |

**响应示例**:
```json
[
  {
    "matchId": 1,
    "userId": 1,
    "score": 150,
    "status": "completed"
  }
]
```

### 6. 根据房间ID获取计分记录

**接口**: `GET /api/score-records/room/{roomId}`

**描述**: 根据房间ID获取该房间的所有计分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roomId | Long | 是 | 房间ID |

**响应示例**:
```json
[
  {
    "matchId": 1,
    "roomId": 1,
    "score": 150,
    "status": "completed"
  }
]
```

### 7. 根据状态获取计分记录

**接口**: `GET /api/score-records/status/{status}`

**描述**: 根据状态获取计分记录列表

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | String | 是 | 状态（如：completed, ongoing） |

**响应示例**:
```json
[
  {
    "matchId": 1,
    "status": "completed",
    "score": 150
  }
]
```

### 8. 根据用户ID和状态获取计分记录

**接口**: `GET /api/score-records/user/{userId}/status/{status}`

**描述**: 根据用户ID和状态获取计分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 用户ID |
| status | String | 是 | 状态 |

**响应示例**:
```json
[
  {
    "matchId": 1,
    "userId": 1,
    "status": "completed",
    "score": 150
  }
]
```

### 9. 更新计分记录

**接口**: `PUT /api/score-records/{matchId}`

**描述**: 更新指定对局的计分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**请求体**:
```json
{
  "score": 200,
  "status": "completed"
}
```

**响应示例**:
```json
{
  "matchId": 1,
  "score": 200,
  "status": "completed"
}
```

### 10. 删除计分记录

**接口**: `DELETE /api/score-records/{matchId}`

**描述**: 删除指定对局的计分记录

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| matchId | Long | 是 | 对局ID |

**响应**: 204 No Content

---

## 附录

### 数据模型说明

#### Match（对局）
- `matchId`: 对局ID（主键）
- `roomName`: 房间名称
- `startTime`: 开始时间戳（毫秒）
- `endTime`: 结束时间戳（毫秒）
- `status`: 状态（0:进行中，1:已完成）
- `totalRounds`: 总轮数
- `settlementMultiplier`: 结算倍率

#### MatchParticipant（参与者）
- `id`: 参与者ID（主键）
- `matchId`: 对局ID
- `userId`: 用户ID（系统用户，可选）
- `wechatUserId`: 微信用户ID（可选）
- `userName`: 用户名称/昵称
- `avatar`: 头像URL
- `totalScore`: 累计总分
- `isQuit`: 是否已退出
- `quitTime`: 退出时间戳

#### RoundScore（轮次得分）
- `id`: 轮次得分ID（主键）
- `matchId`: 对局ID
- `participantId`: 参与者ID
- `roundNumber`: 轮次号
- `score`: 本轮得分
- `cumulativeScore`: 累计得分
- `roundTime`: 轮次时间戳

### 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如手机号已存在） |
| 500 | 服务器内部错误 |

### 注意事项

1. **时间戳格式**: 所有时间字段使用Unix时间戳（毫秒级）
2. **状态值**: 对局状态使用整数：0=进行中，1=已完成
3. **批量操作**: 批量接口支持一次处理多个记录，提高效率
4. **参与者模式**: 支持系统用户、微信用户和游客三种模式
5. **轮次得分**: 每次记录轮次得分时，需要同时更新参与者的累计总分
6. **对局结算**: 结算后对局状态变为已完成，不能再添加轮次得分

---

**文档版本**: v1.0  
**最后更新**: 2024-12-06

