# 测试参与者删除接口

## 接口信息

**URL**: `DELETE /api/v1/matches/participants/batch`  
**方法**: `DELETE`  
**Content-Type**: `application/json`

## 测试场景

### 场景1: 删除无轮次记录的参与者（应该成功）

#### 请求
```bash
curl -X DELETE "http://localhost:8080/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[123]"
```

#### 请求体
```json
[123]
```

#### 预期响应
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 测试步骤
1. 创建一个对局
2. 添加参与者（不添加任何轮次记录）
3. 调用删除接口
4. 验证返回200状态码
5. 验证参与者已被删除

---

### 场景2: 删除有轮次记录的参与者（应该失败）

#### 请求
```bash
curl -X DELETE "http://localhost:8080/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[456]"
```

#### 请求体
```json
[456]
```

#### 预期响应
```json
{
  "code": 400,
  "message": "玩家已有对战记录不可删除！",
  "data": null
}
```

#### 测试步骤
1. 创建一个对局
2. 添加参与者
3. 为该参与者添加轮次记录（至少一条）
4. 调用删除接口
5. 验证返回400状态码
6. 验证错误消息为"玩家已有对战记录不可删除！"
7. 验证参与者未被删除

---

### 场景3: 批量删除多个参与者（部分有记录，部分无记录）

#### 请求
```bash
curl -X DELETE "http://localhost:8080/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[123, 456, 789]"
```

#### 请求体
```json
[123, 456, 789]
```

假设：
- 参与者123：无轮次记录
- 参与者456：有轮次记录
- 参与者789：无轮次记录

#### 预期响应
```json
{
  "code": 400,
  "message": "玩家已有对战记录不可删除！",
  "data": null
}
```

#### 说明
由于使用`@Transactional`，如果任何一个参与者有轮次记录，整个操作都会回滚。

#### 测试步骤
1. 创建多个参与者
2. 为其中一个参与者添加轮次记录
3. 批量删除所有参与者
4. 验证返回400错误
5. 验证所有参与者都未被删除（事务回滚）

---

### 场景4: 删除不存在的参与者

#### 请求
```bash
curl -X DELETE "http://localhost:8080/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[999999]"
```

#### 请求体
```json
[999999]
```

#### 预期响应
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 说明
不存在的参与者会被跳过，不影响操作成功。

#### 测试步骤
1. 使用不存在的参与者ID
2. 调用删除接口
3. 验证返回200状态码（跳过不存在的参与者）

---

### 场景5: 空列表测试（应该失败）

#### 请求
```bash
curl -X DELETE "http://localhost:8080/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[]"
```

#### 请求体
```json
[]
```

#### 预期响应
```json
{
  "code": 400,
  "message": "参与者ID列表不能为空",
  "data": null
}
```

#### 测试步骤
1. 发送空列表
2. 验证返回400错误
3. 验证错误消息为"参与者ID列表不能为空"

---

## 使用Postman测试

### 1. 创建请求
- **Method**: DELETE
- **URL**: `http://localhost:8080/api/v1/matches/participants/batch`
- **Headers**: 
  - `Content-Type: application/json`

### 2. 设置请求体
在Body标签中选择"raw"和"JSON"，然后输入：
```json
[123]
```

### 3. 发送请求并查看响应

---

## 使用curl测试

### 测试成功删除
```bash
curl -X DELETE "http://localhost:8080/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[123]" \
  -v
```

### 测试失败删除（有记录）
```bash
curl -X DELETE "http://localhost:8080/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[456]" \
  -v
```

### 测试批量删除
```bash
curl -X DELETE "http://localhost:8080/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[123, 456, 789]" \
  -v
```

---

## 查看日志

删除操作会输出详细日志，包括：
- 开始批量删除参与者
- 处理每个参与者的详细信息
- 轮次记录检查结果
- 删除成功或失败的原因

### 成功删除的日志示例
```
INFO  开始批量删除参与者，数量: 1
INFO  处理删除参与者请求，参与者ID: 123
INFO  找到参与者，参与者ID: 123，对局ID: 456
INFO  检查参与者轮次记录，参与者ID: 123，找到 0 条轮次记录
INFO  验证通过，开始删除参与者，参与者ID: 123
INFO  成功删除参与者，参与者ID: 123
INFO  批量删除参与者完成，处理数量: 1
```

### 失败删除的日志示例
```
INFO  开始批量删除参与者，数量: 1
INFO  处理删除参与者请求，参与者ID: 456
INFO  找到参与者，参与者ID: 456，对局ID: 789
INFO  检查参与者轮次记录，参与者ID: 456，找到 5 条轮次记录
WARN  参与者 456 有 5 条轮次记录，不允许删除
WARN  删除参与者失败，原因: 玩家已有对战记录不可删除！
```

---

## 数据库验证

### 验证参与者是否被删除
```sql
SELECT * FROM match_participants WHERE id = 123;
```

### 验证轮次记录
```sql
SELECT * FROM round_scores WHERE participant_id = 123;
```

---

## 测试检查清单

- [ ] 测试删除无记录的参与者（应该成功）
- [ ] 测试删除有记录的参与者（应该失败，返回400）
- [ ] 测试批量删除（有记录应该回滚）
- [ ] 测试删除不存在的参与者（应该跳过）
- [ ] 测试空列表（应该返回400）
- [ ] 验证日志输出
- [ ] 验证数据库状态

---

## 注意事项

1. **事务性**：批量删除时，如果任何一个参与者有记录，整个操作都会回滚
2. **数据完整性**：有轮次记录的参与者不能删除，确保数据完整性
3. **错误信息**：统一返回"玩家已有对战记录不可删除！"
4. **日志记录**：所有操作都有详细的日志记录
