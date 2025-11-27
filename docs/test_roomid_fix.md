# 测试roomId修复的步骤

## 1. 执行数据库修复脚本

首先执行数据库检查和修复脚本：

```bash
psql -U your_username -d your_database -f check_and_fix_database.sql
```

## 2. 重启应用

重启Spring Boot应用以加载新的代码。

## 3. 测试结束对局接口

### 测试场景1：正常结束对局
```bash
curl -X PUT "http://localhost:8080/api/v1/matches/{matchId}/end" \
  -H "Content-Type: application/json"
```

### 测试场景2：带房间名称结束对局
```bash
curl -X PUT "http://localhost:8080/api/v1/matches/{matchId}/end" \
  -H "Content-Type: application/json" \
  -d '{
    "roomName": "测试房间",
    "multiplier": 1.5
  }'
```

## 4. 检查日志

查看应用日志，应该能看到以下信息：
- `ensureRoomIdIsSet called for match: X, current roomId: null, roomName: Y`
- `Match X has null roomId, attempting to fix...`
- `Set roomId for existing room: Y with id: Z` 或 `Created new room: Y with id: Z`
- `ensureRoomIdIsSet completed for match: X, final roomId: Z`

## 5. 验证数据库

检查数据库中的对局记录：
```sql
SELECT match_id, room_id, room_name, status 
FROM matches 
ORDER BY match_id DESC 
LIMIT 5;
```

## 6. 如果仍然失败

如果仍然出现roomId为null的错误，请检查：

1. **数据库表结构**：
   ```sql
   \d matches
   ```

2. **rooms表是否存在**：
   ```sql
   SELECT * FROM rooms;
   ```

3. **应用日志**：查看详细的错误信息和ensureRoomIdIsSet方法的执行日志

4. **代码是否生效**：确认应用已重启并加载了新的代码

## 7. 手动修复现有数据

如果有很多现有数据需要修复，可以执行：

```sql
-- 为所有room_id为null的对局设置默认房间
UPDATE matches 
SET room_id = (SELECT id FROM rooms WHERE name = '默认房间' LIMIT 1)
WHERE room_id IS NULL;
```
