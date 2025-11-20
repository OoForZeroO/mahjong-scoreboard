# 优化删除参与者接口

## 优化内容

### 1. 添加轮次记录检查
删除参与者时，先检查该参与者在轮次表（round_scores）中是否有记录，如果有记录则不允许删除。

### 2. 移除测试代码
移除了之前的测试代码（participantId == 209L），使代码更加清晰。

### 3. 改进错误处理
- 更详细的错误消息，包含记录数量和参与者ID、对局ID
- 区分业务逻辑错误和系统错误
- 业务逻辑错误返回400状态码，系统错误返回500状态码

### 4. 添加详细日志
- 记录删除过程的每个步骤
- 记录参与者信息和对局ID
- 记录轮次记录检查结果
- 记录删除成功或失败的原因

## 修改内容

### MatchServiceImpl.deleteParticipants() 方法

#### 优化前
```java
// 检查是否有轮次计分记录
List<RoundScore> roundScores = rdao.findByParticipantOrderByRoundNumberAsc(participant);
if (!roundScores.isEmpty()) {
    throw new IllegalStateException("玩家已有分数产生不能删除，参与者ID: " + participantId + "，对局ID: " + participant.getMatch().getMatchId());
}
// 删除参与者
pdao.deleteById(participantId);
```

#### 优化后
```java
// 检查该参与者在轮次表中是否有记录
List<RoundScore> roundScores = rdao.findByParticipantOrderByRoundNumberAsc(participant);
logger.info("检查参与者轮次记录，参与者ID: {}，找到 {} 条轮次记录", participantId, roundScores.size());

if (!roundScores.isEmpty()) {
    String errorMsg = String.format("无法删除参与者：该参与者在轮次表中有 %d 条计分记录，不允许删除。参与者ID: %d，对局ID: %s", 
        roundScores.size(), participantId, matchId);
    logger.warn(errorMsg);
    throw new IllegalStateException(errorMsg);
}

// 验证通过，删除参与者
logger.info("验证通过，开始删除参与者，参与者ID: {}", participantId);
pdao.deleteById(participantId);
logger.info("成功删除参与者，参与者ID: {}", participantId);
```

### MatchController.deleteParticipants() 方法

#### 优化前
```java
catch (Exception e) {
    logger.error("批量删除参与者失败", e);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量删除参与者失败: " + e.getMessage());
}
```

#### 优化后
```java
catch (IllegalStateException e) {
    // 处理业务逻辑错误（如有轮次记录不允许删除）
    logger.warn("删除参与者失败，原因: {}", e.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
} catch (Exception e) {
    logger.error("批量删除参与者失败", e);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "批量删除参与者失败: " + e.getMessage());
}
```

## 接口说明

### DELETE /api/v1/matches/participants/batch

#### 请求
```json
[123, 456, 789]
```

#### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

#### 失败响应（有轮次记录）
```json
{
  "code": 400,
  "message": "无法删除参与者：该参与者在轮次表中有 5 条计分记录，不允许删除。参与者ID: 123，对局ID: 456",
  "data": null
}
```

#### 失败响应（系统错误）
```json
{
  "code": 500,
  "message": "批量删除参与者失败: 错误详情",
  "data": null
}
```

## 业务逻辑

### 删除流程

1. **验证输入**
   - 检查参与者ID列表是否为空

2. **遍历参与者**
   - 对每个参与者ID进行删除处理

3. **检查参与者是否存在**
   - 如果参与者不存在，跳过并记录警告

4. **检查轮次记录**
   - 查询该参与者在轮次表中的所有记录
   - 如果有记录，抛出异常，不允许删除

5. **执行删除**
   - 验证通过后，删除参与者记录

### 验证规则

- ✅ **允许删除**：参与者在轮次表中没有记录
- ❌ **不允许删除**：参与者在轮次表中有记录

## 日志输出示例

### 成功删除
```
INFO  开始批量删除参与者，数量: 2
INFO  处理删除参与者请求，参与者ID: 123
INFO  找到参与者，参与者ID: 123，对局ID: 456
INFO  检查参与者轮次记录，参与者ID: 123，找到 0 条轮次记录
INFO  验证通过，开始删除参与者，参与者ID: 123
INFO  成功删除参与者，参与者ID: 123
INFO  批量删除参与者完成，处理数量: 2
```

### 删除失败（有轮次记录）
```
INFO  开始批量删除参与者，数量: 1
INFO  处理删除参与者请求，参与者ID: 123
INFO  找到参与者，参与者ID: 123，对局ID: 456
INFO  检查参与者轮次记录，参与者ID: 123，找到 5 条轮次记录
WARN  无法删除参与者：该参与者在轮次表中有 5 条计分记录，不允许删除。参与者ID: 123，对局ID: 456
```

## 测试建议

1. **测试无轮次记录的参与者删除**
   - 创建一个参与者，不添加任何轮次记录
   - 尝试删除，应该成功

2. **测试有轮次记录的参与者删除**
   - 创建一个参与者，添加一些轮次记录
   - 尝试删除，应该失败并返回400错误

3. **测试不存在的参与者**
   - 尝试删除不存在的参与者ID
   - 应该跳过并记录警告，但不影响其他参与者的删除

4. **测试批量删除**
   - 批量删除多个参与者，部分有记录，部分无记录
   - 应该只删除无记录的参与者

## 注意事项

1. **事务性**：使用`@Transactional`确保操作的原子性
2. **错误处理**：区分业务逻辑错误和系统错误
3. **日志记录**：详细记录每个步骤，便于问题排查
4. **数据完整性**：确保不会删除有历史记录的参与者，保持数据完整性
