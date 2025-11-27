# 用户管理API接口文档

## 概述
本文档详细描述了麻将计分系统的用户管理相关API接口，包括普通用户管理和微信小程序用户管理。

## 一、普通用户管理接口

### 基础URL
- 基础URL: `http://localhost:8080/api/v1/users`

### 1. 创建用户
- **接口**: `POST /api/v1/users`
- **功能**: 创建新用户，支持前端页面和微信小程序调用
- **请求体**:
  ```json
  {
    "username": "张三",
    "phone": "13800138000",
    "password": "123456",
    "email": "zhangsan@example.com"
  }
  ```
- **响应**:
  - 成功: `201 Created`
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
  - 失败: `409 Conflict` (手机号已存在)

### 2. 获取用户列表（支持分页）
- **接口**: `GET /api/v1/users?page=1&limit=10`
- **功能**: 获取用户列表，支持分页
- **请求参数**:
  - `page`: 页码，默认为1
  - `limit`: 每页数量，默认为10
- **响应**:
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

### 3. 根据手机号查询用户
- **接口**: `GET /api/v1/users/phone/{phone}`
- **功能**: 根据手机号查询用户信息
- **路径参数**:
  - `phone`: 用户手机号
- **响应**:
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
  - 失败: `404 Not Found` (用户不存在)

## 二、微信小程序用户管理接口

### 基础URL
- 基础URL: `http://localhost:8080/api/v1/wechat-users`

### 1. 创建微信用户
- **接口**: `POST /api/v1/wechat-users`
- **功能**: 创建新的微信用户，用于微信小程序用户注册
- **请求体**:
  ```json
  {
    "userId": "wx_user_123456",
    "nickname": "微信用户",
    "username": "wxuser",
    "avatar": "http://example.com/avatar.jpg"
  }
  ```
- **响应**:
  - 成功: `201 Created`
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
  - 失败: `409 Conflict` (微信用户ID已存在)

### 2. 获取所有微信用户
- **接口**: `GET /api/v1/wechat-users`
- **功能**: 获取所有微信用户列表
- **响应**:
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
- **接口**: `GET /api/v1/wechat-users/{id}`
- **功能**: 根据系统ID获取微信用户信息
- **路径参数**:
  - `id`: 系统用户ID
- **响应**:
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
- **接口**: `GET /api/v1/wechat-users/user-id/{userId}`
- **功能**: 根据微信用户唯一标识获取用户信息
- **路径参数**:
  - `userId`: 微信用户唯一标识
- **响应**:
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

### 5. 更新微信用户信息
- **接口**: `PUT /api/v1/wechat-users/{id}`
- **功能**: 更新微信用户信息
- **路径参数**:
  - `id`: 系统用户ID
- **请求体**:
  ```json
  {
    "userId": "wx_user_123456",
    "nickname": "更新后的昵称",
    "username": "updateduser",
    "avatar": "http://example.com/new_avatar.jpg"
  }
  ```
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": 1,
      "userId": "wx_user_123456",
      "nickname": "更新后的昵称",
      "username": "updateduser",
      "avatar": "http://example.com/new_avatar.jpg"
    }
  }
  ```

### 6. 删除微信用户
- **接口**: `DELETE /api/v1/wechat-users/{id}`
- **功能**: 删除指定ID的微信用户
- **路径参数**:
  - `id`: 系统用户ID
- **响应**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": null
  }
  ```

## 三、数据模型说明

### 1. 普通用户表 (users)
字段说明：
- id: 自增主键
- username: 用户名
- phone: 手机号（唯一）
- email: 邮箱
- password: 密码
- role: 角色
- status: 状态
- avatar: 头像
- createTime: 创建时间
- updateTime: 更新时间

### 2. 微信用户表 (wechat_users)
字段说明：
- id: 自增主键
- userId: 微信用户唯一标识（唯一）
- nickname: 用户昵称
- username: 用户名称
- avatar: 用户头像
- createTime: 创建时间
- updateTime: 更新时间

## 5. 前端调用示例

### 5.1 JavaScript (Fetch API)

#### 创建用户示例
```javascript
const createUser = async (userData) => {
  try {
    const response = await fetch('http://localhost:8080/api/v1/users', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    });
    const result = await response.json();
    
    if (result.code === 200) {
      console.log('用户创建成功:', result.data);
      return result.data;
    } else {
      console.error('创建失败:', result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('请求异常:', error);
    throw error;
  }
};

// 调用示例
createUser({
  username: '测试用户',
  phone: '13800138000',
  avatar: 'https://example.com/avatar.jpg'
});
```

### 5.2 微信小程序调用

```javascript
// 创建用户
wx.request({
  url: 'http://localhost:8080/api/v1/users',
  method: 'POST',
  data: {
    username: '微信用户',
    phone: '13800138001',
    avatar: 'https://wx.qlogo.cn/mmopen/xxxxx/0'
  },
  header: {
    'Content-Type': 'application/json'
  },
  success(res) {
    if (res.data.code === 200) {
      console.log('用户创建成功', res.data.data);
      // 处理成功逻辑
    } else {
      wx.showToast({
        title: res.data.message,
        icon: 'none'
      });
    }
  },
  fail(err) {
    console.error('请求失败', err);
    wx.showToast({
      title: '网络错误',
      icon: 'none'
    });
  }
});

// 检查手机号是否已注册
wx.request({
  url: 'http://localhost:8080/api/v1/users/exists/phone/13800138000',
  method: 'GET',
  success(res) {
    if (res.data.data.exists) {
      console.log('手机号已注册');
    } else {
      console.log('手机号可注册');
    }
  }
});
```

## 6. 错误码说明

| 错误码 | 描述 |
|-------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如手机号已存在） |
| 500 | 服务器内部错误 |

## 7. 安全注意事项

1. 生产环境建议使用HTTPS协议
2. 建议添加适当的权限验证机制
3. 对用户输入进行严格校验，防止注入攻击
4. 敏感操作建议添加日志记录