# 微信小程序用户管理API接口文档

## 概述
本文档详细描述了麻将计分系统的微信小程序用户管理API接口，专为微信小程序端调用设计，实现微信用户的注册、查询、更新和删除等功能。

## 基础URL
- 基础URL: `http://localhost:8080/api/v1/wechat-users`

## 一、用户创建接口

### 1. 创建微信用户
- **接口**: `POST /api/v1/wechat-users`
- **功能**: 创建新的微信用户，用于微信小程序用户注册或首次登录
- **请求体**:
  ```json
  {
    "userId": "wx_user_123456",
    "nickname": "微信用户",
    "username": "wxuser",
    "avatar": "http://example.com/avatar.jpg"
  }
  ```
- **参数说明**:
  - `userId`: 微信用户唯一标识（必填）
  - `nickname`: 用户昵称（必填）
  - `username`: 用户名称（选填）
  - `avatar`: 用户头像URL（选填）
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
  ```json
  {
    "code": 409,
    "message": "用户ID已存在",
    "data": null
  }
  ```

## 二、用户查询接口

### 2. 获取所有微信用户
- **接口**: `GET /api/v1/wechat-users`
- **功能**: 获取所有微信用户列表
- **请求参数**: 无
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
      },
      {
        "id": 2,
        "userId": "wx_user_789012",
        "nickname": "另一用户",
        "username": "another",
        "avatar": null
      }
    ]
  }
  ```

### 3. 根据系统ID获取微信用户
- **接口**: `GET /api/v1/wechat-users/{id}`
- **功能**: 根据系统自增ID获取微信用户信息
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
  - 失败: `404 Not Found` (用户不存在)
  ```json
  {
    "code": 404,
    "message": "用户不存在",
    "data": null
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
  - 失败: `404 Not Found` (用户不存在)
  ```json
  {
    "code": 404,
    "message": "用户不存在",
    "data": null
  }
  ```

## 三、用户更新接口

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
  - 失败: `404 Not Found` (用户不存在)
  ```json
  {
    "code": 404,
    "message": "用户不存在",
    "data": null
  }
  ```

## 四、用户删除接口

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
  - 失败: `404 Not Found` (用户不存在)
  ```json
  {
    "code": 404,
    "message": "用户不存在",
    "data": null
  }
  ```

## 五、微信小程序调用示例

### 1. 创建或更新用户
```javascript
// 小程序登录并获取用户信息
wx.login({
  success: (res) => {
    if (res.code) {
      // 获取用户信息
      wx.getUserProfile({
        desc: '用于完善用户资料',
        success: (userProfile) => {
          const userInfo = userProfile.userInfo;
          
          // 检查用户是否已存在
          wx.request({
            url: `http://localhost:8080/api/v1/wechat-users/user-id/${userInfo.openId || 'wx_' + Date.now()}`,
            method: 'GET',
            success: (checkRes) => {
              if (checkRes.statusCode === 404) {
                // 用户不存在，创建新用户
                createWechatUser({
                  userId: userInfo.openId || 'wx_' + Date.now(),
                  nickname: userInfo.nickName,
                  avatar: userInfo.avatarUrl
                });
              } else if (checkRes.statusCode === 200) {
                // 用户存在，可能需要更新信息
                console.log('用户已存在', checkRes.data.data);
              }
            },
            fail: () => {
              // 检查失败，尝试创建用户
              createWechatUser({
                userId: userInfo.openId || 'wx_' + Date.now(),
                nickname: userInfo.nickName,
                avatar: userInfo.avatarUrl
              });
            }
          });
        }
      });
    }
  }
});

// 创建微信用户函数
function createWechatUser(userData) {
  wx.request({
    url: 'http://localhost:8080/api/v1/wechat-users',
    method: 'POST',
    data: userData,
    header: {
      'Content-Type': 'application/json'
    },
    success(res) {
      if (res.data.code === 200) {
        console.log('用户创建成功', res.data.data);
        // 保存用户信息到本地存储
        wx.setStorageSync('userInfo', res.data.data);
      } else if (res.data.code === 409) {
        console.log('用户已存在，尝试获取用户信息');
        // 尝试获取已存在的用户信息
        wx.request({
          url: `http://localhost:8080/api/v1/wechat-users/user-id/${userData.userId}`,
          method: 'GET',
          success(getRes) {
            if (getRes.data.code === 200) {
              wx.setStorageSync('userInfo', getRes.data.data);
            }
          }
        });
      } else {
        wx.showToast({
          title: res.data.message || '创建失败',
          icon: 'none'
        });
      }
    },
    fail(err) {
      console.error('请求失败', err);
      wx.showToast({
        title: '网络错误，请重试',
        icon: 'none'
      });
    }
  });
}
```

### 2. 获取用户信息
```javascript
// 获取用户信息函数
function getUserInfo(userId) {
  wx.request({
    url: `http://localhost:8080/api/v1/wechat-users/user-id/${userId}`,
    method: 'GET',
    success(res) {
      if (res.data.code === 200) {
        console.log('获取用户信息成功', res.data.data);
        // 更新本地存储的用户信息
        wx.setStorageSync('userInfo', res.data.data);
        return res.data.data;
      } else {
        wx.showToast({
          title: '获取用户信息失败',
          icon: 'none'
        });
        return null;
      }
    },
    fail(err) {
      console.error('获取用户信息失败', err);
      return null;
    }
  });
}
```

### 3. 更新用户信息
```javascript
// 更新用户信息函数
function updateUserInfo(userId, userData) {
  const localUser = wx.getStorageSync('userInfo');
  if (!localUser || !localUser.id) {
    wx.showToast({
      title: '用户未登录',
      icon: 'none'
    });
    return;
  }
  
  wx.request({
    url: `http://localhost:8080/api/v1/wechat-users/${localUser.id}`,
    method: 'PUT',
    data: userData,
    header: {
      'Content-Type': 'application/json'
    },
    success(res) {
      if (res.data.code === 200) {
        console.log('用户信息更新成功', res.data.data);
        // 更新本地存储
        wx.setStorageSync('userInfo', res.data.data);
        wx.showToast({
          title: '更新成功',
          icon: 'success'
        });
      } else {
        wx.showToast({
          title: res.data.message || '更新失败',
          icon: 'none'
        });
      }
    },
    fail(err) {
      console.error('更新失败', err);
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      });
    }
  });
}
```

## 六、数据模型说明

### 微信用户表 (wechat_users) 结构
字段说明：
- **id**: 自增主键 (Long)
- **userId**: 微信用户唯一标识（必填，唯一）
- **nickname**: 用户昵称（必填）
- **username**: 用户名称（选填）
- **avatar**: 用户头像URL（选填）
- **createTime**: 创建时间（自动生成）
- **updateTime**: 更新时间（自动生成）

## 七、错误码说明

| 错误码 | 描述 | 可能原因 |
|-------|------|----------|
| 200 | 操作成功 | 接口调用成功 |
| 400 | 请求参数错误 | 参数格式不正确或缺失必填参数 |
| 404 | 资源不存在 | 请求的用户ID不存在 |
| 409 | 资源冲突 | 微信用户ID已存在 |
| 500 | 服务器内部错误 | 服务器处理异常 |

## 八、安全注意事项

1. **环境配置**
   - 生产环境请使用HTTPS协议
   - 建议配置IP白名单或域名白名单，仅允许微信小程序域名访问

2. **请求验证**
   - 建议添加适当的接口签名验证机制
   - 考虑添加请求频率限制，防止恶意请求

3. **数据安全**
   - 对用户输入进行严格校验，防止XSS攻击
   - 敏感操作建议添加日志记录

4. **微信特有安全**
   - 建议使用微信登录code换取openid的方式验证用户身份
   - 不要在前端存储敏感用户信息

## 九、部署建议

1. **本地开发环境**
   - 使用微信开发者工具的"不校验合法域名"选项进行开发测试
   - 确保本地服务可以被微信开发者工具访问

2. **测试/生产环境**
   - 确保域名已在微信公众平台配置为合法域名
   - 配置正确的SSL证书（HTTPS）
   - 建议使用Nginx等代理服务器优化访问