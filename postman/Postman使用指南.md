# Postman 接口测试使用指南

## 一、环境准备

### 1. 安装 Postman

- **下载地址**: https://www.postman.com/downloads/
- **支持平台**: Windows、macOS、Linux
- **推荐版本**: 最新版本

### 2. 导入环境配置

1. 打开 Postman
2. 点击右上角的 **"Environments"** 或 **"环境"** 按钮
3. 点击 **"Import"** 或 **"导入"** 按钮
4. 选择以下文件进行导入：
   - `生产环境.postman_environment.json`
   - `测试环境.postman_environment.json`

### 3. 导入接口集合

1. 点击左侧的 **"Collections"** 或 **"集合"** 标签
2. 点击 **"Import"** 或 **"导入"** 按钮
3. 选择 `麻将计分系统API接口.postman_collection.json` 文件
4. 导入完成后，左侧会显示接口集合

### 4. 切换环境

1. 点击右上角的环境选择下拉框
2. 选择 **"生产环境"** 或 **"测试环境"**
3. 所有接口会自动使用对应环境的 `base_url`

---

## 二、环境变量说明

### 生产环境变量

| 变量名 | 值 | 说明 |
|--------|-----|------|
| base_url | https://yaohufox.com | 生产环境基础URL |
| api_version | v1 | API版本 |
| test_match_id | (空) | 测试用的对局ID（执行创建对局后自动填充） |
| test_participant_id | (空) | 测试用的参与者ID |
| test_user_id | (空) | 测试用的用户ID |
| test_wechat_user_id | (空) | 测试用的微信用户ID |

### 测试环境变量

| 变量名 | 值 | 说明 |
|--------|-----|------|
| base_url | http://localhost:8082 | 测试环境基础URL |
| api_version | v1 | API版本 |
| test_match_id | (空) | 测试用的对局ID |
| test_participant_id | (空) | 测试用的参与者ID |
| test_user_id | (空) | 测试用的用户ID |
| test_wechat_user_id | (空) | 测试用的微信用户ID |

---

## 三、接口测试流程

### 1. 测试接口（健康检查）

**目的**: 验证服务是否正常运行

1. 选择环境（生产环境或测试环境）
2. 打开集合：`麻将计分系统API接口` → `测试接口` → `健康检查`
3. 点击 **"Send"** 按钮
4. 查看响应，应该返回 `Hello World!`

### 2. 创建用户

**目的**: 创建测试用户

1. 打开：`用户管理` → `创建用户`
2. 修改请求体中的用户信息：
   ```json
   {
     "username": "测试用户",
     "phone": "13800138000",
     "password": "123456"
   }
   ```
3. 点击 **"Send"**
4. 查看响应，记录返回的 `id` 值
5. 手动更新环境变量 `test_user_id` 为返回的用户ID

### 3. 创建对局

**目的**: 创建测试对局

1. 打开：`对局管理` → `创建对局`
2. 修改请求体（可选）：
   ```json
   {
     "roomName": "测试房间",
     "startTime": 1765016628196
   }
   ```
3. 点击 **"Send"**
4. 查看响应，记录返回的 `matchId`
5. 手动更新环境变量 `test_match_id` 为返回的对局ID

### 4. 添加参与者

**目的**: 向对局中添加参与者

1. 打开：`参与者管理` → `添加参与者`
2. 注意：URL中的 `{{matchId}}` 会自动替换为环境变量 `test_match_id`
3. 修改请求体：
   ```json
   {
     "userName": "玩家1",
     "avatar": "https://example.com/avatar.jpg"
   }
   ```
4. 点击 **"Send"**
5. 记录返回的参与者ID，更新环境变量 `test_participant_id`

### 5. 记录轮次得分

**目的**: 记录一轮得分

1. 打开：`轮次得分` → `记录轮次得分`
2. URL中的 `{{matchId}}` 和 `roundNumber` 会自动替换
3. 修改请求体：
   ```json
   [
     {
       "participantId": 1,
       "score": 50,
       "cumulativeScore": 50
     },
     {
       "participantId": 2,
       "score": -30,
       "cumulativeScore": -30
     }
   ]
   ```
4. 点击 **"Send"**

### 6. 结束对局

**目的**: 结束对局并结算

1. 打开：`对局结算` → `执行对局结算`
2. URL中的 `{{matchId}}` 会自动替换
3. 可选：添加请求体设置倍率：
   ```json
   {
     "multiplier": 1.5,
     "roomName": "测试房间"
   }
   ```
4. 点击 **"Send"**

---

## 四、常用操作技巧

### 1. 使用环境变量

在请求URL或请求体中使用 `{{变量名}}` 来引用环境变量：

- URL示例: `{{base_url}}/api/{{api_version}}/matches/{{matchId}}`
- 请求体示例: `{"matchId": {{test_match_id}}}`

### 2. 自动保存响应值到环境变量

在 **Tests** 标签页中添加脚本，自动保存响应值：

```javascript
// 保存对局ID到环境变量
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.data && jsonData.data.matchId) {
        pm.environment.set("test_match_id", jsonData.data.matchId);
        console.log("已保存对局ID: " + jsonData.data.matchId);
    }
}
```

### 3. 批量测试

1. 选择集合或文件夹
2. 点击 **"Run"** 按钮
3. 选择要运行的接口
4. 点击 **"Run 麻将计分系统API接口"**
5. 查看测试结果

### 4. 导出测试报告

1. 运行集合后
2. 点击 **"Export Results"** 导出测试结果
3. 可以导出为 JSON 或 HTML 格式

### 5. 设置请求前脚本（Pre-request Script）

在接口的 **Pre-request Script** 标签页中添加脚本，自动设置变量：

```javascript
// 自动生成时间戳
pm.environment.set("current_timestamp", Date.now());
```

### 6. 设置测试脚本（Tests）

在接口的 **Tests** 标签页中添加验证脚本：

```javascript
// 验证响应状态码
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

// 验证响应结构
pm.test("Response has correct structure", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('code');
    pm.expect(jsonData).to.have.property('message');
    pm.expect(jsonData).to.have.property('data');
});
```

---

## 五、测试场景示例

### 场景1：完整对局流程测试

1. **创建对局** → 记录 `matchId`
2. **添加参与者1** → 记录 `participantId1`
3. **添加参与者2** → 记录 `participantId2`
4. **记录第1轮得分** → 为两个参与者记录得分
5. **记录第2轮得分** → 继续记录得分
6. **获取对局详情** → 验证数据
7. **结束对局** → 执行结算
8. **获取对局结果** → 查看结算结果

### 场景2：用户管理流程测试

1. **创建用户** → 记录 `userId`
2. **根据手机号查询用户** → 验证创建成功
3. **检查手机号是否存在** → 应该返回 `true`
4. **更新用户信息** → 修改用户信息
5. **获取用户列表** → 验证更新成功

### 场景3：微信用户流程测试

1. **创建微信用户** → 记录 `wechatUserId`
2. **根据微信用户ID查询** → 验证创建成功
3. **创建对局** → 使用微信用户参与
4. **添加参与者** → 使用 `wechatUserId`
5. **记录得分** → 正常流程

---

## 六、常见问题

### 1. 环境变量不生效

**问题**: URL中的 `{{变量名}}` 没有被替换

**解决**:
- 确认已选择正确的环境（右上角环境选择器）
- 确认变量名拼写正确
- 刷新请求或重新发送

### 2. 跨域问题（CORS）

**问题**: 浏览器中测试时出现跨域错误

**解决**:
- 使用 Postman 桌面版（不受CORS限制）
- 或确保服务端已配置 CORS 允许跨域

### 3. 请求超时

**问题**: 请求长时间无响应

**解决**:
- 检查服务是否正常运行
- 检查网络连接
- 检查防火墙设置
- 增加请求超时时间（Settings → General → Request timeout）

### 4. 响应格式错误

**问题**: 响应不是预期的JSON格式

**解决**:
- 检查请求头 `Content-Type: application/json`
- 检查请求体格式是否正确
- 查看服务端日志

### 5. 变量值未自动更新

**问题**: 创建资源后，变量值没有自动更新

**解决**:
- 手动更新环境变量
- 或在 Tests 脚本中添加自动保存逻辑（见第四部分第2点）

---

## 七、最佳实践

1. **先测试健康检查接口** - 确保服务正常运行
2. **按顺序测试** - 先创建资源，再查询和更新
3. **使用环境变量** - 避免硬编码，方便切换环境
4. **添加测试脚本** - 自动验证响应，提高测试效率
5. **保存常用请求** - 创建文件夹组织接口
6. **定期导出集合** - 备份接口配置
7. **使用描述** - 为每个接口添加说明
8. **分组管理** - 按功能模块组织接口

---

## 八、快速开始

### 第一次使用

1. ✅ 导入环境配置文件（生产环境和测试环境）
2. ✅ 导入接口集合文件
3. ✅ 选择测试环境
4. ✅ 运行"健康检查"接口，确认服务正常
5. ✅ 开始测试其他接口

### 日常测试

1. 选择环境（生产/测试）
2. 打开要测试的接口
3. 修改请求参数（如需要）
4. 点击 Send
5. 查看响应结果

---

**提示**: 建议先在测试环境进行完整测试，确认无误后再在生产环境测试。

