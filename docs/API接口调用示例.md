# API 接口调用示例

## 基础信息

- **生产环境**: `https://yaohufox.com` 或 `http://localhost:8081`
- **测试环境**: `https://test.yaohufox.com` 或 `http://localhost:8082`
- **API 基础路径**: `/api`

## 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 失败响应
```json
{
  "code": 错误码,
  "message": "错误信息",
  "data": null
}
```

---

## 1. 测试接口（最简单）

### 1.1 健康检查

**接口**: `GET /api/test/hello`

**描述**: 简单的健康检查接口，用于验证服务是否正常运行

**请求参数**: 无

**响应示例**:
```
Hello World!
```

**调用示例**:
```bash
# 生产环境
curl https://yaohufox.com/api/test/hello
curl http://localhost:8081/api/test/hello

# 测试环境
curl https://test.yaohufox.com/api/test/hello
curl http://localhost:8082/api/test/hello
```

---

## 2. 用户管理接口

### 2.1 获取所有用户列表

**接口**: `GET /api/users`

**描述**: 获取系统中所有用户列表

**请求参数**: 无

**请求头**: 无特殊要求

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
      "avatar": "https://example.com/avatar.jpg",
      "createTime": 1701234567890,
      "updateTime": 1701234567890
    },
    {
      "id": 2,
      "username": "李四",
      "phone": "13900139000",
      "email": "lisi@example.com",
      "role": "user",
      "status": "active",
      "avatar": null,
      "createTime": 1701234567891,
      "updateTime": 1701234567891
    }
  ]
}
```

**调用示例**:
```bash
curl https://yaohufox.com/api/users
curl http://localhost:8081/api/users
```

**响应字段说明**:
- `id`: 用户ID（Long）
- `username`: 用户名（String）
- `phone`: 手机号（String，唯一）
- `email`: 邮箱（String，可选）
- `role`: 角色（String，默认 "user"）
- `status`: 状态（String，默认 "active"）
- `avatar`: 头像URL（String，可选）
- `createTime`: 创建时间戳（Long，毫秒）
- `updateTime`: 更新时间戳（Long，毫秒）

---

### 2.2 根据ID获取用户

**接口**: `GET /api/users/{id}`

**描述**: 根据用户ID获取用户详细信息

**路径参数**:
- `id` (Long, 必填): 用户ID

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
    "status": "active",
    "avatar": "https://example.com/avatar.jpg",
    "createTime": 1701234567890,
    "updateTime": 1701234567890
  }
}
```

**调用示例**:
```bash
# 获取ID为1的用户
curl https://yaohufox.com/api/users/1
curl http://localhost:8081/api/users/1
```

**错误响应**（用户不存在）:
```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
```

---

### 2.3 根据手机号获取用户

**接口**: `GET /api/users/phone/{phone}`

**描述**: 根据手机号获取用户信息

**路径参数**:
- `phone` (String, 必填): 手机号

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
    "status": "active",
    "avatar": "https://example.com/avatar.jpg",
    "createTime": 1701234567890,
    "updateTime": 1701234567890
  }
}
```

**调用示例**:
```bash
curl https://yaohufox.com/api/users/phone/13800138000
curl http://localhost:8081/api/users/phone/13800138000
```

---

### 2.4 检查手机号是否存在

**接口**: `GET /api/users/exists/phone/{phone}`

**描述**: 检查指定手机号是否已被注册

**路径参数**:
- `phone` (String, 必填): 手机号

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**调用示例**:
```bash
curl https://yaohufox.com/api/users/exists/phone/13800138000
curl http://localhost:8081/api/users/exists/phone/13800138000
```

**响应说明**:
- `data: true`: 手机号已存在
- `data: false`: 手机号不存在

---

## 3. 房间管理接口

### 3.1 获取所有房间列表

**接口**: `GET /api/rooms`

**描述**: 获取系统中所有棋牌室列表

**请求参数**: 无

**响应示例**:
```json
[
  {
    "id": 1,
    "name": "快乐棋牌室",
    "logo": "https://example.com/logo1.jpg",
    "createTime": 1701234567890,
    "updateTime": 1701234567890
  },
  {
    "id": 2,
    "name": "休闲棋牌室",
    "logo": null,
    "createTime": 1701234567891,
    "updateTime": 1701234567891
  }
]
```

**调用示例**:
```bash
curl https://yaohufox.com/api/rooms
curl http://localhost:8081/api/rooms
```

**响应字段说明**:
- `id`: 房间ID（Long）
- `name`: 房间名称（String）
- `logo`: 房间Logo URL（String，可选）
- `createTime`: 创建时间戳（Long，毫秒）
- `updateTime`: 更新时间戳（Long，毫秒）

---

### 3.2 根据ID获取房间

**接口**: `GET /api/rooms/{id}`

**描述**: 根据房间ID获取房间详细信息

**路径参数**:
- `id` (Long, 必填): 房间ID

**响应示例**:
```json
{
  "id": 1,
  "name": "快乐棋牌室",
  "logo": "https://example.com/logo1.jpg",
  "createTime": 1701234567890,
  "updateTime": 1701234567890
}
```

**调用示例**:
```bash
curl https://yaohufox.com/api/rooms/1
curl http://localhost:8081/api/rooms/1
```

**错误响应**（房间不存在）:
```
HTTP 404 Not Found
```

---

### 3.3 检查房间名称是否存在

**接口**: `GET /api/rooms/exists/name/{name}`

**描述**: 检查指定房间名称是否已存在

**路径参数**:
- `name` (String, 必填): 房间名称

**响应示例**:
```json
true
```

**调用示例**:
```bash
curl https://yaohufox.com/api/rooms/exists/name/快乐棋牌室
curl http://localhost:8081/api/rooms/exists/name/快乐棋牌室
```

**响应说明**:
- `true`: 房间名称已存在
- `false`: 房间名称不存在

---

## 4. 对局管理接口

### 4.1 获取所有对局列表

**接口**: `GET /api/v1/matches`

**描述**: 获取系统中所有对局列表

**请求参数**: 无

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "matchId": 1,
      "roomName": "快乐棋牌室",
      "startTime": 1701234567890,
      "endTime": null,
      "status": 0,
      "totalRounds": 4,
      "settlementMultiplier": null,
      "createTime": 1701234567890,
      "updateTime": 1701234567890
    }
  ]
}
```

**调用示例**:
```bash
curl https://yaohufox.com/api/v1/matches
curl http://localhost:8081/api/v1/matches
```

**响应字段说明**:
- `matchId`: 对局ID（Long）
- `roomName`: 房间名称（String）
- `startTime`: 开始时间戳（Long，毫秒）
- `endTime`: 结束时间戳（Long，毫秒，未结束时为null）
- `status`: 对局状态（Integer，0=进行中，1=已完成）
- `totalRounds`: 总轮次数（Integer）
- `settlementMultiplier`: 结算倍率（Double，可选）
- `createTime`: 创建时间戳（Long，毫秒）
- `updateTime`: 更新时间戳（Long，毫秒）

---

### 4.2 根据ID获取对局

**接口**: `GET /api/v1/matches/{matchId}`

**描述**: 根据对局ID获取对局详细信息

**路径参数**:
- `matchId` (Long, 必填): 对局ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "matchId": 1,
    "roomName": "快乐棋牌室",
    "startTime": 1701234567890,
    "endTime": null,
    "status": 0,
    "totalRounds": 4,
    "settlementMultiplier": null,
    "createTime": 1701234567890,
    "updateTime": 1701234567890
  }
}
```

**调用示例**:
```bash
curl https://yaohufox.com/api/v1/matches/1
curl http://localhost:8081/api/v1/matches/1
```

---

## 5. Spring Boot Actuator 健康检查

### 5.1 应用健康状态

**接口**: `GET /actuator/health`

**描述**: Spring Boot Actuator 提供的健康检查接口

**请求参数**: 无

**响应示例**:
```json
{
  "status": "UP"
}
```

**调用示例**:
```bash
curl https://yaohufox.com/actuator/health
curl http://localhost:8081/actuator/health
```

**响应说明**:
- `status: "UP"`: 应用正常运行
- `status: "DOWN"`: 应用异常

---

## 使用 curl 的完整示例

### 示例 1: 测试服务是否启动
```bash
# 最简单的方式
curl http://localhost:8081/api/test/hello

# 预期输出: Hello World!
```

### 示例 2: 获取用户列表（格式化输出）
```bash
# 使用 jq 格式化 JSON（如果已安装）
curl -s http://localhost:8081/api/users | jq .

# 或使用 python 格式化
curl -s http://localhost:8081/api/users | python3 -m json.tool
```

### 示例 3: 获取特定用户
```bash
# 获取ID为1的用户
curl -s http://localhost:8081/api/users/1 | python3 -m json.tool

# 根据手机号获取用户
curl -s http://localhost:8081/api/users/phone/13800138000 | python3 -m json.tool
```

### 示例 4: 检查手机号是否存在
```bash
curl -s http://localhost:8081/api/users/exists/phone/13800138000
# 输出: {"code":200,"message":"success","data":true}
```

### 示例 5: 获取房间列表
```bash
curl -s http://localhost:8081/api/rooms | python3 -m json.tool
```

### 示例 6: 获取对局列表
```bash
curl -s http://localhost:8081/api/v1/matches | python3 -m json.tool
```

---

## 使用 Postman 或浏览器测试

### 浏览器直接访问

1. **测试接口**:
   ```
   http://localhost:8081/api/test/hello
   ```

2. **获取用户列表**:
   ```
   http://localhost:8081/api/users
   ```

3. **获取房间列表**:
   ```
   http://localhost:8081/api/rooms
   ```

### Postman 配置

1. **Method**: GET
2. **URL**: `http://localhost:8081/api/users`
3. **Headers**: 无需特殊配置
4. **Body**: 无需配置（GET 请求）

---

## 错误处理

### 常见错误码

- `200`: 请求成功
- `404`: 资源不存在
- `500`: 服务器内部错误

### 错误响应示例

```json
{
  "code": 404,
  "message": "用户不存在",
  "data": null
}
```

---

## 推荐测试顺序

1. **首先测试服务是否启动**:
   ```bash
   curl http://localhost:8081/api/test/hello
   ```

2. **然后测试健康检查**:
   ```bash
   curl http://localhost:8081/actuator/health
   ```

3. **测试获取用户列表**:
   ```bash
   curl http://localhost:8081/api/users
   ```

4. **测试其他业务接口**:
   ```bash
   curl http://localhost:8081/api/rooms
   curl http://localhost:8081/api/v1/matches
   ```

---

## 总结

最简单的 GET 接口调用：

```bash
# 1. 测试接口（最简单）
curl http://localhost:8081/api/test/hello

# 2. 获取用户列表（有实际功能）
curl http://localhost:8081/api/users

# 3. 获取房间列表（有实际功能）
curl http://localhost:8081/api/rooms
```

这些接口都不需要认证，可以直接调用测试。

