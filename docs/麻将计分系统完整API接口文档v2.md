# 麻将计分系统完整API接口文档 v2.0

## 文档版本信息
- **版本**: v2.3
- **最后更新**: 2024年12月
- **作者**: 麻将计分系统开发团队
- **说明**: 本文档涵盖了麻将计分系统的所有API接口，包含修复后的批量接口和新增的结束对局详细版接口

## 目录
1. [概述](#概述)
2. [基础信息](#基础信息)
3. [测试接口](#测试接口)
4. [用户管理接口](#用户管理接口)
5. [微信用户管理接口](#微信用户管理接口)
6. [棋牌室管理接口](#棋牌室管理接口)
7. [对局管理接口](#对局管理接口)
8. [参与者管理接口](#参与者管理接口)
9. [轮次计分接口](#轮次计分接口)
10. [对局结算接口](#对局结算接口)
11. [计分记录接口](#计分记录接口)
12. [统计查询接口](#统计查询接口)
13. [附录](#附录)

---

## 1. 概述

麻将计分系统是一个完整的麻将对局管理和计分系统，支持对局创建、参与者管理、多轮次计分、对局结算等功能。系统提供了完整的RESTful API接口，支持前端页面和微信小程序调用。

### 1.1 系统特性
- ✅ 完整的对局管理功能
- ✅ 多轮次计分支持
- ✅ 对局结算功能
- ✅ 用户和参与者管理
- ✅ 微信小程序用户支持
- ✅ 游客模式支持（isVisitor字段）

---

## 2. 基础信息

### 2.1 API基础路径
```
http://localhost:8080/api
```

### 2.2 通用响应格式

#### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

#### 失败响应
```json
{
  "code": 错误码,
  "message": "错误信息",
  "data": null
}
```

### 2.3 HTTP状态码
- `200`: 请求成功
- `201`: 创建成功
- `400`: 请求参数错误
- `404`: 资源不存在
- `409`: 资源冲突
- `500`: 服务器内部错误

### 2.4 请求头
```
Content-Type: application/json
```

---

## 3. 测试接口

**基础路径**: `/api/test`

### 3.1 健康检查
- **接口**: `GET /api/test/hello`
- **描述**: 健康检查接口
- **响应**: `Hello World!`

### 3.2 测试创建用户
- **接口**: `POST /api/test/user`
- **描述**: 测试创建用户
- **请求体**:
```json
{
  "username": "测试用户",
  "phone": "13800138000",
  "password": "123456"
}
```

---

## 4. 用户管理接口

### 4.1 旧版接口
**基础路径**: `/api/users`

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/users` | 创建用户 |
| GET | `/api/users` | 获取所有用户 |
| GET | `/api/users/{id}` | 根据ID获取用户 |
| GET | `/api/users/phone/{phone}` | 根据手机号获取用户 |
| PUT | `/api/users/{id}` | 更新用户 |
| DELETE | `/api/users/{id}` | 删除用户 |
| GET | `/api/users/exists/phone/{phone}` | 检查手机号是否存在 |

### 4.2 新版接口（支持分页）
**基础路径**: `/api/v1/users`

#### 4.2.1 创建用户
- **接口**: `POST /api/v1/users`
- **请求体**:
```json
{
  "username": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "password": "123456"
}
```

#### 4.2.2 获取用户列表（支持分页）
- **接口**: `GET /api/v1/users?page=1&limit=10`
- **参数**: page（页码）, limit（每页数量）

---

## 5. 微信用户管理接口

**基础路径**: `/api/v1/wechat-users`

### 5.1 创建微信用户
- **接口**: `POST /api/v1/wechat-users`
- **请求体**:
```json
{
  "userId": "wx_user_123456",
  "nickname": "微信用户",
  "username": "wxuser",
  "avatar": "http://example.com/avatar.jpg",
  "isVisitor": false
}
```

### 5.2 获取所有微信用户
- **接口**: `GET /api/v1/wechat-users`

### 5.3 根据ID获取微信用户
- **接口**: `GET /api/v1/wechat-users/{id}`
- **特点**: 无数据时返回`data: null`（不是404错误）

### 5.4 根据微信唯一标识获取用户
- **接口**: `GET /api/v1/wechat-users/user-id/{userId}`
- **特点**: 无数据时返回`data: null`（不是404错误）

### 5.5 更新微信用户
- **接口**: `PUT /api/v1/wechat-users/{id}`
- **支持字段**: nickname, username, avatar, isVisitor

### 5.6 删除微信用户
- **接口**: `DELETE /api/v1/wechat-users/{id}`

---

## 6. 棋牌室管理接口

**基础路径**: `/api/rooms`

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/rooms` | 创建棋牌室 |
| GET | `/api/rooms` | 获取所有棋牌室 |
| GET | `/api/rooms/{id}` | 根据ID获取棋牌室 |
| PUT | `/api/rooms/{id}` | 更新棋牌室 |
| DELETE | `/api/rooms/{id}` | 删除棋牌室 |
| GET | `/api/rooms/exists/name/{name}` | 检查名称是否存在 |

---

## 7. 对局管理接口

**基础路径**: `/api/v1/matches`

### 7.1 创建对局
- **接口**: `POST /api/v1/matches`
- **请求体**:
```json
{
  "roomName": "快乐棋牌室",
  "startTime": 1716700800000,
  "totalRounds": 4
}
```
- **请求参数说明**:

### 7.2 获取对局详情 ⭐ 新功能
- **接口**: `GET /api/v1/matches/{matchId}/detail`
- **功能**: 获取对局的完整详情信息，包括轮次数据和参与者数据

### 7.3 根据状态查询对局 ⭐ 新功能
- **接口**: `GET /api/v1/matches/status/{status}`
- **功能**: 根据对局状态查询对局列表，返回对局基本信息和参与者摘要信息。当指定wechat_user_id时，只返回该用户参与的对局，但参与者数据包含该对局下所有参与者信息
- **请求参数**:
  - `status` (路径参数): 对局状态，必填（0:进行中, 1:已完成）
  - `wechat_user_id` (查询参数): 微信用户ID，可选，用于过滤特定用户参与的对局
- **请求示例**:
```bash
# 查询所有进行中的对局
GET /api/v1/matches/status/0

# 查询特定用户参与的对局
GET /api/v1/matches/status/0?wechat_user_id=wx_openid_zhangsan
```
- **成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "matchId": 1,
      "roomName": "快乐棋牌室",
      "startTime": 1716700800000,
      "endTime": null,
      "status": 0,
      "totalRounds": 4,
      "settlementMultiplier": null,
      "createTime": 1716700800000,
      "updateTime": 1716700800000,
      "participants": [
        {
          "participantId": 1,
          "nickName": "张三",
          "avatar": "https://example.com/avatar1.jpg",
          "totalScore": 15,
          "wechatUserId": "wx_openid_zhangsan",
          "isVisitor": false
        },
        {
          "participantId": 2,
          "nickName": "李四",
          "avatar": "https://example.com/avatar2.jpg",
          "totalScore": 3,
          "wechatUserId": "wx_openid_lisi",
          "isVisitor": false
        },
        {
          "participantId": 3,
          "nickName": "游客小王",
          "avatar": "https://example.com/guest-avatar.jpg",
          "totalScore": 12,
          "wechatUserId": "",
          "isVisitor": true
        }
      ]
    }
  ]
}
```
- **字段说明**:
  - `matchId`: 对局ID
  - `roomName`: 房间名称
  - `startTime`: 开始时间（时间戳）
  - `endTime`: 结束时间（时间戳，null表示未结束）
  - `status`: 对局状态（0:进行中, 1:已完成）
  - `totalRounds`: 总轮次
  - `settlementMultiplier`: 结算倍数
  - `createTime`: 创建时间（时间戳）
  - `updateTime`: 更新时间（时间戳）
  - `participants`: 参与者列表（包含该对局下所有参与者信息，不受wechat_user_id过滤影响）
    - `participantId`: 参与者ID
    - `nickName`: 参与者昵称
    - `avatar`: 参与者头像URL
    - `totalScore`: 总得分
    - `wechatUserId`: 微信用户ID
    - `isVisitor`: 是否为访客
- **错误响应** (404):
```json
{
  "code": 404,
  "message": "未找到指定状态的对局",
  "data": null
}
```
- **错误响应** (500):
```json
{
  "code": 500,
  "message": "查询对局失败: 数据库连接异常",
  "data": null
}
```
- **cURL测试示例**:
```bash
# 查询进行中的对局
curl -X GET "http://localhost:8080/api/v1/matches/status/0"

# 查询已完成的对局
curl -X GET "http://localhost:8080/api/v1/matches/status/1"

# 查询特定用户参与的对局
curl -X GET "http://localhost:8080/api/v1/matches/status/0?wechat_user_id=wx_openid_zhangsan"
```

### 7.4 获取对局详情 ⭐ 新功能
- **接口**: `GET /api/v1/matches/{matchId}/detail`
- **功能**: 获取对局的完整详情信息，包括轮次数据和参与者数据
- **请求参数**:
  - `matchId` (路径参数): 对局ID，必填
- **请求示例**:
```bash
GET /api/v1/matches/1/detail
```
- **成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomName": "快乐棋牌室",
    "totalRounds": 4,
    "currentRound": 2,
    "matchStatus": "进行中",
    "createTime": 1716700800000,
    "updateTime": 1716700800000,
    "rounds": [
      {
        "roundNumber": 1,
        "roundTime": 1716700800000,
        "scores": [
          {
            "participantId": 1,
            "participantName": "张三",
            "score": 10,
            "cumulativeScore": 10
          },
          {
            "participantId": 2,
            "participantName": "李四",
            "score": -5,
            "cumulativeScore": -5
          }
        ]
      },
      {
        "roundNumber": 2,
        "roundTime": 1716701400000,
        "scores": [
          {
            "participantId": 1,
            "participantName": "张三",
            "score": 5,
            "cumulativeScore": 15
          },
          {
            "participantId": 2,
            "participantName": "李四",
            "score": 8,
            "cumulativeScore": 3
          }
        ]
      }
    ],
    "participants": [
      {
        "participantId": 1,
        "nickname": "张三",
        "avatar": "https://example.com/avatar1.jpg",
        "totalScore": 15,
        "isVisitor": false,
        "isQuit": false,
        "userId": 101
      },
      {
        "participantId": 2,
        "nickname": "李四",
        "avatar": null,
        "totalScore": 3,
        "isVisitor": true,
        "isQuit": false,
        "userId": null
      }
    ]
  }
}
```
- **错误响应** (404):
  ```json
  {
  "code": 404,
  "message": "对局不存在，matchId: 999",
  "data": null
  }
  ```
- **错误响应** (500):
  ```json
{
  "code": 500,
  "message": "获取对局详情失败: 数据库连接异常",
  "data": null
}
```
- **响应字段说明**:
  - **对局基本信息**:
    - `matchId`: 对局ID
    - `roomName`: 房间名称
    - `totalRounds`: 总轮次
    - `currentRound`: 当前轮次
    - `matchStatus`: 对局状态（进行中/已结束/已结算）
    - `createTime`: 创建时间戳
    - `updateTime`: 更新时间戳
  - **轮次数据** (`rounds`):
    - `roundNumber`: 轮次号
    - `roundTime`: 轮次时间戳
    - `scores`: 该轮次所有参与者的得分列表
      - `participantId`: 参与者ID
      - `participantName`: 参与者姓名
      - `score`: 该轮次得分
      - `cumulativeScore`: 累计得分
  - **参与者数据** (`participants`):
    - `participantId`: 参与者ID
    - `nickname`: 昵称
    - `avatar`: 参与者头像URL（优先使用参与者自己的头像，如果没有则使用关联用户的头像，游客可能为null）
    - `totalScore`: 总得分
    - `isVisitor`: 是否游客
    - `isQuit`: 是否已退出对局
    - `userId`: 关联用户ID（游客为null）
- **特点**:
  - 一次调用获取对局完整信息，减少前端请求次数
  - 数据结构层次清晰，便于前端展示
  - 包含所有轮次数据和参与者信息
  - 支持游客和注册用户混合模式
- **cURL测试示例**:
```bash
# 获取对局详情
curl -X GET "http://localhost:8080/api/v1/matches/1/detail" \
  -H "Content-Type: application/json"

# 预期响应
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomName": "快乐棋牌室",
    "totalRounds": 4,
    "currentRound": 2,
    "matchStatus": "进行中",
    "createTime": 1716700800000,
    "updateTime": 1716700800000,
    "rounds": [...],
    "participants": [...]
  }
}
```

### 7.3 获取对局列表
- **接口**: `GET /api/v1/matches`

### 7.4 获取单个对局
- **接口**: `GET /api/v1/matches/{matchId}`

### 7.5 更新对局
- **接口**: `PUT /api/v1/matches/{matchId}`

### 7.6 结束对局
- **接口**: `PUT /api/v1/matches/{matchId}/end`

### 7.7 结束对局（详细版）⭐ 新功能
- **接口**: `PUT /api/v1/matches/{matchId}/end-details`
- **说明**: 支持传入收盘倍率和棋牌室信息，自动创建room数据并更新match_results表
- **请求体**:
```json
{
  "roomName": "新棋牌室",
  "multiplier": 2.5
}
```
- **请求字段说明**:
  - `roomName` (String, 可选): 棋牌室名称，如果为空则不处理room
  - `multiplier` (Double, 可选): 收盘倍率

- **响应示例**:
```json
{
  "success": true,
  "message": "对局结束成功",
  "data": {
    "matchId": 108,
    "roomName": "测试结束对局",
    "totalRounds": 4,
    "status": 1,
    "startTime": 1703123456789,
    "endTime": 1703123456789,
    "createTime": 1703123456789,
    "updateTime": 1703123456789
  }
}
```

- **功能说明**:
  - 当`roomName`不为空时，会查询rooms表
  - 如果room不存在，则自动创建新的room记录
  - 如果room已存在，则不做任何操作
  - 对局结束后会自动计算并保存到match_results表
  - 包含最高分、最低分、获胜者等信息

- **cURL 测试命令**:
```bash
curl -X PUT "http://localhost:8080/api/v1/matches/108/end-details" \
  -H "Content-Type: application/json" \
  -d '{
    "roomName": "新棋牌室",
    "multiplier": 2.5
  }'
```

### 7.8 删除对局
- **接口**: `DELETE /api/v1/matches/{matchId}`

---

## 8. 参与者管理接口

**基础路径**: `/api/v1/matches/{matchId}/participants`

### 8.1 添加参与者
- **接口**: `POST /api/v1/matches/{matchId}/participants`
- **请求体**:
```json
{
  "wechatUserId": "wx_openid_123456",
  "nickName": "玩家1",
  "avatarUrl": "https://example.com/avatar1.jpg"
}
```
- **参数说明**:
  - `wechatUserId`: 微信用户OpenID（可选，为空表示游客）
  - `nickName`: 参与者昵称（必填）
  - `avatarUrl`: 参与者头像URL（可选，最大长度500字符）
- **响应字段**:
  - `wechatUserId`: 微信用户OpenID
  - `userName`: 参与者昵称（内部存储字段）
  - `avatar`: 参与者头像URL（内部存储字段）

### 8.2 获取参与者列表
- **接口**: `GET /api/v1/matches/{matchId}/participants`

### 8.3 更新参与者
- **接口**: `PUT /api/v1/participants/{participantId}`
- **请求体**:
```json
{
  "userName": "新名字",
  "totalScore": 100
}
```
- **说明**: 可更新用户名称和总得分

### 8.4 参与者退出
- **接口**: `PUT /api/v1/participants/{participantId}/quit`
- **功能**: 标记参与者为退出状态
- **路径参数**:
  - `participantId`: 参与者ID（必填）
- **业务规则**: **不受任何限制，任何参与者都可以标记为退出状态**
- **说明**: 
  - 退出操作会更新参与者的 `isQuit` 字段为 `true`，并设置 `quitTime` 为当前时间戳
  - 不会删除参与者记录，只是标记为退出状态
  - 即使参与者有轮次计分记录，也可以标记为退出
- **成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "match": {
      "matchId": 1
    },
    "userName": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "totalScore": 150,
    "isQuit": true,
    "quitTime": 1725936000000,
    "createTime": 1725930000000,
    "updateTime": 1725936000000
  }
}
```
- **失败响应** (404 - 参与者不存在):
```json
{
  "code": 404,
  "message": "参与者不存在",
  "data": null
}
```

### 8.5 重新启用参与者
- **接口**: `PUT /api/v1/participants/{participantId}/reactivate`
- **功能**: 重新启用已退出的参与者
- **路径参数**:
  - `participantId`: 参与者ID（必填）
- **说明**: 
  - 将参与者的 `isQuit` 字段设置为 `false`，并清空 `quitTime` 字段
  - 只有已退出（isQuit=true）的参与者可以重新启用
  - 不受任何限制，即使参与者有轮次计分记录也可以重新启用
- **成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "match": {
      "matchId": 1
    },
    "userName": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "totalScore": 150,
    "isQuit": false,
    "quitTime": null,
    "createTime": 1725930000000,
    "updateTime": 1725937000000
  }
}
```
- **失败响应** (404 - 参与者不存在):
```json
{
  "code": 404,
  "message": "参与者不存在",
  "data": null
}
```

### 8.6 参与者重新加入对局
- **接口**: `PUT /api/v1/matches/participants/{participantId}/rejoin`
- **功能**: 重新加入对局，将已退出的参与者重新启用
- **路径参数**:
  - `participantId`: 参与者ID（必填）
- **说明**: 
  - 将参与者的 `isQuit` 字段设置为 `false`，并清空 `quitTime` 字段
  - 功能与 `reactivate` 接口相同，提供更明确的语义（重新加入对局）
  - 不受任何限制，即使参与者有轮次计分记录也可以重新加入
- **成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "match": {
      "matchId": 1
    },
    "userName": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "totalScore": 150,
    "isQuit": false,
    "quitTime": null,
    "createTime": 1725930000000,
    "updateTime": 1725937000000
  }
}
```
- **失败响应** (404 - 参与者不存在):
```json
{
  "code": 404,
  "message": "参与者不存在",
  "data": null
}
```

### 8.7 批量参与者操作

#### 8.7.1 批量添加参与者（支持轮次计分字段）
- **接口**: `POST /api/v1/matches/{matchId}/participants/batch`
- **功能**: 一次性为指定对局添加多个参与者，支持游客模式和注册用户模式
- **说明**: 支持传入轮次计分数据字段，但不会自动创建轮次计分记录
- **请求体（基础版本）**:
```json
[
  {
    "wechatUserId": "wx_openid_123456",
    "nickName": "玩家1",
    "avatarUrl": "https://example.com/avatar1.jpg"
  },
  {
    "nickName": "游客小王",
    "avatarUrl": "https://example.com/guest-avatar.jpg"
  },
  {
    "wechatUserId": "wx_openid_789012",
    "nickName": "玩家2",
    "avatarUrl": "https://example.com/avatar2.jpg"
  }
]
```
- **请求体（包含轮次计分数据）**:
```json
[
  {
    "wechatUserId": "wx_openid_123456",
    "nickName": "玩家1",
    "avatarUrl": "https://example.com/avatar1.jpg",
    "roundScore": {
      "roundNumber": 1,
      "score": 10,
      "roundTime": 1716700800000
    }
  },
  {
    "nickName": "游客小王",
    "avatarUrl": "https://example.com/guest-avatar.jpg",
    "roundScore": {
      "score": 15
    }
  }
]
```
- **响应**: 返回创建的参与者列表，包含完整的参与者信息和计算后的总分
- **特点**: 
  - 支持游客模式（user为null）
  - 自动验证用户名不为空
  - 事务性操作，要么全部成功，要么全部回滚
  - **头像支持**: 支持传入avatar字段，最大长度500字符
  - **轮次计分字段**: 支持传入roundScore字段，但不会自动创建轮次计分记录
  - **单轮次限制**: 每个参与者只能包含一个轮次计分数据，roundNumber默认为1
  - **手动创建**: 如需创建轮次计分记录，请单独调用轮次计分接口
  - **字段验证**: roundScore字段为可选，如果提供则必须包含score字段

#### 8.7.2 批量更新参与者
- **接口**: `PUT /api/v1/participants/batch`
- **功能**: 批量更新多个参与者的信息
- **请求体**:
```json
[
  {
    "id": 1,
    "userName": "新名字1",
    "totalScore": 100
  },
  {
    "id": 2,
    "userName": "新名字2",
    "totalScore": 200
  }
]
```
- **响应**: 返回更新的参与者列表
- **特点**: 只更新提供的字段，未提供的字段保持不变

#### 8.7.3 批量退出对局
- **接口**: `PUT /api/v1/participants/batch/quit`
- **功能**: 批量让多个参与者退出对局
- **请求体**:
```json
[1, 2, 3]
```
- **说明**: 传入参与者ID列表，批量删除参与者记录

#### 8.7.4 批量删除参与者
- **接口**: `DELETE /api/v1/matches/participants/batch`
- **功能**: 批量删除多个参与者
- **限制**: **如果参与者在轮次表中有计分记录，则不允许删除**
- **请求体**:
```json
[1, 2, 3]
```
- **成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```
- **失败响应** (有轮次记录):
```json
{
  "code": 400,
  "message": "玩家已有对战记录不可删除！",
  "data": null
}
```
- **说明**: 
  - 传入参与者ID列表，批量删除参与者记录
  - 删除前会检查该参与者在轮次表（round_scores）中是否有记录
  - 如果有轮次记录，则返回400错误，不允许删除，确保数据完整性
  - 使用事务，批量删除时如果任何一个参与者有记录，整个操作都会回滚

### 8.8 获取参与者排名
- **接口**: `GET /api/v1/matches/{matchId}/participants/ranking`

---

## 9. 轮次计分接口

**基础路径**: `/api/v1/matches/{matchId}/rounds`

### 9.1 记录轮次得分
- **接口**: `POST /api/v1/matches/{matchId}/rounds/{roundNumber}`
- **功能**: 为指定对局的指定轮次记录所有参与者的得分
- **重复检查**: 系统会检查该对局下是否已存在该轮次的记录，如果存在则创建失败
- **请求体**:
```json
[
  {
    "participantId": 1,
    "score": 50
  },
  {
    "participantId": 2,
    "score": -50
  }
]
```
- **重要**: 请求体中提供`participantId`即可，系统会自动查找并设置对应的`MatchParticipant`对象
- **错误响应**: 如果轮次已存在，返回400错误，错误信息为"轮次 X 已存在，无法重复创建"

### 9.2 获取对局所有轮次
- **接口**: `GET /api/v1/matches/{matchId}/rounds`

### 9.3 获取当前轮次编号
- **接口**: `GET /api/v1/matches/{matchId}/rounds/current-number`

### 9.4 获取指定轮次详情
- **接口**: `GET /api/v1/matches/{matchId}/rounds/{roundNumber}`

### 9.5 批量轮次得分操作

#### 9.5.1 批量创建轮次得分
- **接口**: `POST /api/v1/matches/rounds/batch`
- **功能**: 批量创建轮次得分记录，自动计算累计得分和更新参与者总分
- **重复检查**: 系统会检查每个对局的每个轮次是否已存在，如果存在则创建失败
- **请求体**:
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
  }
]
```
- **响应**: 返回创建的轮次得分列表，包含自动计算的累计得分
- **特点**:
  - 自动根据matchId和participantId查找对应对象
  - 自动计算累计得分
  - 自动更新参与者总分
  - 事务性操作，要么全部成功，要么全部回滚

#### 9.5.2 批量更新轮次得分
- **接口**: `PUT /api/v1/matches/rounds/batch`
- **功能**: 批量更新轮次得分记录，重新计算累计得分
- **请求体**:
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
- **响应**: 返回更新的轮次得分列表
- **特点**: 重新计算累计得分，更新参与者总分

#### 9.5.3 批量获取轮次得分
- **接口**: `GET /api/v1/matches/rounds/batch?scoreIds=1,2,3`
- **功能**: 根据ID列表批量查询轮次得分记录
- **参数**: `scoreIds` - 轮次得分ID列表（逗号分隔）
- **响应**: 返回轮次得分列表

#### 9.5.4 批量删除轮次得分
- **接口**: `DELETE /api/v1/matches/rounds/batch`
- **功能**: 批量删除轮次得分记录
- **请求体**:
```json
[1, 2, 3]
```
- **说明**: 传入轮次得分ID列表，批量删除记录

### 7.7 获取对局二维码

- **接口**: `GET /api/v1/matches/{matchId}/qrcode`
- **功能**: 生成对局二维码，用于分享对局信息
- **路径参数**:
  - `matchId`: 对局ID（必填）
- **说明**: 
  - 调用微信小程序API生成不限制数量的二维码
  - 二维码的`scene`参数为对局ID（纯数字）
  - 扫码后跳转到小程序首页，小程序前端需解析`scene`获取`matchId`并跳转到对局详情页
  - 需要在`application.properties`中配置微信小程序AppID和AppSecret
  - 二维码以Base64格式返回，可直接用于前端显示
  - **重要**：小程序前端需要在小程序首页的`onLoad`方法中获取`scene`参数：
    ```javascript
    onLoad(options) {
      const scene = decodeURIComponent(options.scene);
      if (scene) {
        // scene 即为 matchId，跳转到对局详情页
        wx.navigateTo({
          url: `/pages/gameDetail/gameDetail?matchId=${scene}`
        });
      }
    }
    ```
- **成功响应** (200):
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
- **失败响应** (404 - 对局不存在):
```json
{
  "code": 404,
  "message": "对局不存在",
  "data": null
}
```
- **失败响应** (500 - 生成失败):
```json
{
  "code": 500,
  "message": "生成二维码失败: xxx",
  "data": null
}
```
- **配置说明**:
  在`application.properties`中需要配置以下参数：
  ```
  # 微信小程序配置
  wechat.appId=your_app_id_here
  wechat.appSecret=your_app_secret_here
  
  # 微信小程序二维码配置
  wechat.qrcode.width=430
  ```
  - `wechat.appId`: 微信小程序AppID（必填）
  - `wechat.appSecret`: 微信小程序AppSecret（必填）
  - `wechat.qrcode.width`: 二维码宽度（可选，默认：430）

---

## 10. 对局结算接口（收盘接口）

### 10.1 执行对局结算（收盘）
- **接口**: `POST /api/v1/matches/{matchId}/settle`
- **功能**: 收盘对局，计算对局结果并保存到match_results表
- **路径参数**:
  - `matchId`: 对局ID（必填）
- **请求体**（可选）:
```json
{
  "multiplier": 2.0,
  "roomName": "快乐棋牌室"
}
```
- **请求参数说明**:
  - `multiplier` (Double, 可选): 收盘倍率，用于计算参与者最终得分
  - `roomName` (String, 可选): 房间名称
- **成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 141,
    "roomName": "快乐棋牌室",
    "status": 1,
    "totalRounds": 8,
    "settlementMultiplier": 2.0,
    "createTime": 1725930000000,
    "updateTime": 1725936000000
  }
}
```
- **说明**:
  - 收盘操作会将对局状态设置为已完成（status=1）
  - 自动计算对局结果并保存到match_results表
  - 如果提供了multiplier，会应用到参与者最终得分
  - 计算total_duration（从创建时间到当前时间）
  - 生成total_scores JSON数据（包含所有参与者的得分信息）

### 10.2 获取对局结果
- **接口**: `GET /api/v1/matches/{matchId}/result`
- **功能**: 获取对局结果数据（从match_results表查询）
- **说明**: 返回对局结果表的所有数据，包括参与者得分信息

### 10.3 检查是否已收盘
- **接口**: `GET /api/v1/matches/{matchId}/is-completed`
- **功能**: 检查对局是否已收盘（status=1表示已完成）

---

## 11. 计分记录接口

**基础路径**: `/api/score-records`

| 方法 | 路径 | 功能 |
|------|------|------|
| POST | `/api/score-records` | 创建计分记录 |
| GET | `/api/score-records` | 获取所有计分记录 |
| GET | `/api/score-records/{matchId}` | 根据对局ID获取 |
| GET | `/api/score-records/user/{userId}` | 根据用户ID获取 |
| GET | `/api/score-records/user/{userId}/recent` | 获取最近的记录 |
| PUT | `/api/score-records/{matchId}` | 更新计分记录 |
| DELETE | `/api/score-records/{matchId}` | 删除计分记录 |

---

## 13. 附录

### 13.1 数据模型

#### WechatUser（微信用户）
- `id`: Long - 系统ID
- `userId`: String - 微信唯一标识
- `nickname`: String - 昵称
- `username`: String - 用户名
- `avatar`: String - 头像URL
- **`isVisitor`: Boolean - 是否游客（新增字段）**
- `createTime`: Long - 创建时间
- `updateTime`: Long - 更新时间

#### RoundScore（轮次得分）
- `id`: Long - 轮次得分ID
- `participant`: MatchParticipant - 参与者对象
- **`participantId`: Long - 参与者ID（临时字段，API使用）**
- `score`: Integer - 本轮得分
- `cumulativeScore`: Integer - 累计得分

### 13.2 关键更新说明

**v2.3 更新内容**:
1. ✅ 月度统计接口：新增`GET /api/v1/statistics/users/{wechatUserId}/monthly`接口，支持查询用户月度统计数据

**v2.2 更新内容**:
1. ✅ 参与者头像支持：添加`avatar`字段，支持参与者头像URL
2. ✅ 轮次重复检查：创建轮次得分时检查同对局下是否已存在该轮次，防止重复记录
3. ✅ 批量轮次创建优化：批量创建时也会进行轮次重复检查
4. ✅ 对局详情接口：新增`GET /api/v1/matches/{matchId}/detail`接口，返回完整对局信息

**v2.0 更新内容**:
1. ✅ 微信用户新增`isVisitor`字段，支持游客模式
2. ✅ 微信用户查询接口优化：无数据时返回`data: null`而非404错误
3. ✅ 轮次得分记录接口修复：支持通过`participantId`自动查找参与者
4. ✅ 完善用户更新功能：支持更新所有字段包括`isVisitor`

### 13.3 常见问题

**Q: 微信用户查询为什么返回null而不是404？**  
A: 为了简化前端处理，统一返回200状态码，前端只需判断`data`字段是否为null。

**Q: 记录轮次得分时如何提供参与者信息？**  
A: 在请求体中提供`participantId`字段即可，系统会自动查找对应的`MatchParticipant`对象。

**Q: 如何区分游客和注册用户？**  
A: 使用`isVisitor`字段，`true`表示游客，`false`表示注册用户。

**Q: 为什么创建轮次得分时会失败？**  
A: 系统会检查同对局下是否已存在该轮次的记录，如果存在则创建失败，防止重复记录。

**Q: 如何知道某个轮次是否已存在？**  
A: 可以通过`GET /api/v1/matches/{matchId}/rounds/{roundNumber}`接口查询，如果返回空列表则表示该轮次不存在。

---

## 12. 统计查询接口

### 12.1 获取用户月度统计数据

- **接口**: `POST /api/v1/statistics/users/{wechatUserId}/monthly`
- **功能**: 查询指定微信用户在指定月份或全年的统计数据，包括总得分、总倍率分、对局数、胜场数、负场数等
- **路径参数**:
  - `wechatUserId`: 微信用户ID（必填）
- **请求体**:
```json
{
  "year": 2024,
  "month": 12
}
```
  - `year`: 年份（必填，格式：YYYY，如 2024）
  - `month`: 月份（可选，格式：MM，如 12）
    - 如果传入 `month`，则查询指定月份的数据
    - 如果不传 `month` 或传入 `null`，则查询该年份全年的数据
- **说明**: 
  - 统计数据基于已完成的对局（status=1）
  - 时间范围基于对局的`end_time`字段判断
  - 总得分：所有对局中倍率前的得分总和
  - 总倍率分：所有对局中倍率后的得分总和
  - 胜场数：判断逻辑为该对局中自己的最终得分是否最高
  - 负场数：对局数 - 胜场数
  - 胜率：胜场数 / 总对局数
  - 胜场总分：所有胜场的倍率前得分总和
  - 胜场总倍率分：所有胜场的倍率后得分总和
  - 负场总分：所有负场的倍率前得分总和
  - 负场总倍率分：所有负场的倍率后得分总和
  - `yearMonth` 字段：
    - 查询月份时返回格式：`YYYY-MM`（如：`2024-12`）
    - 查询全年时返回格式：`YYYY`（如：`2024`）
  - `highestScorePlayer` 字段：该时间段内倍率分最高的玩家（已排除查询者自身）
  - `lowestScorePlayer` 字段：该时间段内倍率分最低的玩家（已排除查询者自身）
- **成功响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "wechatUserId": "wx_openid_123456",
    "nickname": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "yearMonth": "2024-12",
    "totalScore": 5000,
    "totalMultiplierScore": 12000.0,
    "totalMatches": 25,
    "winMatches": 12,
    "loseMatches": 13,
    "winRate": 0.48,
    "winTotalScore": 6000,
    "winTotalMultiplierScore": 15000.0,
    "loseTotalScore": -1000,
    "loseTotalMultiplierScore": -3000.0,
    "highestScorePlayer": {
      "wechatUserId": "wx_openid_789012",
      "nickname": "李四",
      "avatar": "https://example.com/lisi.jpg",
      "totalScore": 8000,
      "totalMultiplierScore": 20000.0
    },
    "lowestScorePlayer": {
      "wechatUserId": "wx_openid_345678",
      "nickname": "王五",
      "avatar": "https://example.com/wangwu.jpg",
      "totalScore": -3000,
      "totalMultiplierScore": -7500.0
    }
  }
}
```
- **数据为空时响应** (200):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "wechatUserId": "wx_openid_123456",
    "nickname": null,
    "avatar": null,
    "yearMonth": "2024-12",
    "totalScore": 0,
    "totalMultiplierScore": 0.0,
    "totalMatches": 0,
    "winMatches": 0,
    "loseMatches": 0,
    "winRate": 0.0,
    "winTotalScore": 0,
    "winTotalMultiplierScore": 0.0,
    "loseTotalScore": 0,
    "loseTotalMultiplierScore": 0.0,
    "highestScorePlayer": null,
    "lowestScorePlayer": null
  }
}
```
- **失败响应** (400 - 请求参数错误):
```json
{
  "code": 400,
  "message": "请求参数错误：year不能为空",
  "data": null
}
```
- **失败响应** (500 - 服务器错误):
```json
{
  "code": 500,
  "message": "获取月度统计失败: xxx",
  "data": null
}
```

### 12.2 使用示例

**示例1：查询2024年12月的统计数据**
```
POST /api/v1/statistics/users/wx_openid_123456/monthly
Content-Type: application/json

{
  "year": 2024,
  "month": 12
}
```

**示例2：查询2024年全年的统计数据**
```
POST /api/v1/statistics/users/wx_openid_123456/monthly
Content-Type: application/json

{
  "year": 2024
}
```

或

```
POST /api/v1/statistics/users/wx_openid_123456/monthly
Content-Type: application/json

{
  "year": 2024,
  "month": null
}
```

**示例3：查询2024年1月的统计数据**
```
POST /api/v1/statistics/users/wx_openid_123456/monthly
Content-Type: application/json

{
  "year": 2024,
  "month": 1
}
```

---

## 13. 附录
