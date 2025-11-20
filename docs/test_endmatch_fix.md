# 测试收盘接口修复

## 问题描述
之前的错误：`null identifier (com.mahjong.model.MatchResult)`

## 修复内容

### 1. MatchResult实体类修复
- ✅ 添加了独立的`resultId`主键字段
- ✅ 使用`@GeneratedValue(strategy = GenerationType.IDENTITY)`
- ✅ 将`matchId`改为普通字段，添加唯一约束

### 2. MatchResultRepository修复
- ✅ 添加了`findByMatchId`方法
- ✅ 添加了`existsByMatchId`方法
- ✅ 添加了`deleteByMatchId`方法

### 3. endMatch方法修复
- ✅ 在保存前检查是否已存在MatchResult
- ✅ 如果存在则更新，否则创建新的
- ✅ 添加了详细的日志记录

## 测试步骤

### 1. 执行数据库修复
```bash
psql -U your_username -d your_database -f fix_matchresult_table.sql
```

### 2. 重启应用
重启Spring Boot应用以加载新的代码。

### 3. 测试收盘接口

#### 测试场景1：简单结束对局
```bash
curl -X PUT "http://localhost:8080/api/v1/matches/{matchId}/end" \
  -H "Content-Type: application/json"
```

#### 测试场景2：带参数结束对局
```bash
curl -X PUT "http://localhost:8080/api/v1/matches/{matchId}/end" \
  -H "Content-Type: application/json" \
  -d '{
    "roomName": "测试房间",
    "multiplier": 1.5
  }'
```

### 4. 检查日志
应该能看到以下日志信息：
- `Creating new match result for match: {matchId}` 或 `Updating existing match result for match: {matchId}`
- 没有`null identifier`错误

### 5. 验证数据库
检查match_results表：
```sql
SELECT result_id, match_id, winner_id, highest_score, lowest_score 
FROM match_results 
ORDER BY result_id DESC 
LIMIT 5;
```

## 预期结果

1. **不再出现null identifier错误**
2. **MatchResult记录正确保存**
3. **对局状态正确更新为"已完成"**
4. **对局结果统计正确计算**

## 如果仍然失败

如果仍然出现错误，请检查：

1. **数据库表结构**：
   ```sql
   \d match_results
   ```

2. **应用日志**：查看详细的错误信息和MatchResult创建日志

3. **代码是否生效**：确认应用已重启并加载了新的代码

4. **数据库连接**：确认数据库连接正常

## 回滚方案

如果修复后出现问题，可以回滚到之前的版本：

1. 恢复MatchResult实体类到之前的状态
2. 恢复endMatch方法到之前的状态
3. 重启应用

## 注意事项

- 确保在测试前备份数据库
- 建议先在测试环境中验证修复效果
- 如果有多人使用系统，建议在维护时间窗口进行修复
