# 微信 Access Token 接口文档

## 接口概述

获取微信小程序的 access_token，用于调用微信 API。

## 接口信息

- **URL**: `/api/v1/wechat/access-token`
- **方法**: `GET`
- **认证**: 无需认证
- **Content-Type**: `application/json`

## 请求示例

### cURL

```bash
curl -X GET https://test.yaohufox.com/api/v1/wechat/access-token
```

### JavaScript (fetch)

```javascript
fetch('https://test.yaohufox.com/api/v1/wechat/access-token', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => {
  console.log('Access Token:', data.data.access_token);
  console.log('Expires In:', data.data.expires_in);
})
.catch(error => {
  console.error('Error:', error);
});
```

### 微信小程序 (wx.request)

```javascript
wx.request({
  url: 'https://test.yaohufox.com/api/v1/wechat/access-token',
  method: 'GET',
  success: function(res) {
    if (res.statusCode === 200 && res.data.code === 200) {
      console.log('Access Token:', res.data.data.access_token);
      console.log('Expires In:', res.data.data.expires_in);
    } else {
      console.error('获取失败:', res.data.message);
    }
  },
  fail: function(err) {
    console.error('请求失败:', err);
  }
});
```

## 响应格式

### 成功响应

**HTTP 状态码**: `200 OK`

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "access_token": "ACCESS_TOKEN",
    "expires_in": 7200
  }
}
```

**响应字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 响应状态码，200 表示成功 |
| message | String | 响应消息，成功时为 "success" |
| data | Object | 响应数据 |
| data.access_token | String | 微信 access_token，用于调用微信 API |
| data.expires_in | Integer | access_token 的有效期（秒），通常为 7200 秒（2小时） |

### 错误响应

**HTTP 状态码**: `500 Internal Server Error`

```json
{
  "code": 500,
  "message": "获取access_token失败: 错误信息",
  "data": null
}
```

**常见错误**:

| 错误信息 | 说明 | 解决方案 |
|---------|------|---------|
| 微信小程序配置未设置，请联系管理员 | appId 或 appSecret 未配置 | 检查服务器配置文件中的 `wechat.appId` 和 `wechat.appSecret` |
| 获取access_token失败: 响应为空 | 微信 API 返回空响应 | 检查网络连接和微信 API 状态 |
| 获取access_token失败: errcode | 微信 API 返回错误 | 检查 appId 和 appSecret 是否正确 |

## 特性说明

1. **缓存机制**: 接口会自动缓存 access_token，避免频繁请求微信 API
2. **自动刷新**: 当 access_token 即将过期时（提前 5 分钟），会自动刷新
3. **有效期**: access_token 的有效期通常为 7200 秒（2小时）

## 注意事项

1. **频率限制**: 微信 API 对 access_token 的获取有频率限制，建议使用缓存机制
2. **配置要求**: 需要在服务器配置文件中设置 `wechat.appId` 和 `wechat.appSecret`
3. **域名白名单**: 如果在小程序中调用，需要在微信公众平台配置域名白名单

## 配置说明

在 `application.properties` 中配置：

```properties
# 微信小程序配置
wechat.appId=your_app_id
wechat.appSecret=your_app_secret
```

## 故障排查

### 1. 接口返回 500 错误

**可能原因**:
- 微信小程序配置未设置
- appId 或 appSecret 错误
- 网络连接问题

**解决方法**:
1. 检查服务器日志，查看具体错误信息
2. 确认 `application.properties` 中的配置是否正确
3. 检查服务器网络连接是否正常

### 2. 小程序调用失败

**可能原因**:
- 域名未添加到小程序白名单
- DNS 解析问题
- SSL 证书问题

**解决方法**:
1. 在微信公众平台配置域名白名单：`https://test.yaohufox.com`
2. 检查 DNS 解析是否正常
3. 确认 SSL 证书是否有效

### 3. access_token 过期

**说明**: access_token 的有效期为 7200 秒（2小时），过期后需要重新获取。

**解决方法**: 接口会自动处理 token 刷新，无需手动处理。

## 相关接口

- 微信小程序二维码生成接口（使用 access_token）

## 更新日志

- 2026-01-16: 初始版本，实现获取 access_token 接口
