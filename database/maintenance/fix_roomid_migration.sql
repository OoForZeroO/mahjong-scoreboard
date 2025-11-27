-- 修复现有对局数据中roomId为null的问题
-- 此脚本需要在使用新的代码之前执行

-- 1. 首先确保rooms表中有默认房间
INSERT INTO rooms (name, create_time, update_time) 
SELECT '默认房间', extract(epoch from now())::bigint, extract(epoch from now())::bigint
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE name = '默认房间');

-- 2. 获取默认房间的ID
-- 注意：这个查询结果需要手动记录，用于后续更新

-- 3. 为所有roomId为null的对局设置默认房间ID
-- 注意：需要将下面的1替换为实际的默认房间ID
UPDATE matches 
SET room_id = (SELECT id FROM rooms WHERE name = '默认房间' LIMIT 1)
WHERE room_id IS NULL;

-- 4. 验证修复结果
SELECT 
    COUNT(*) as total_matches,
    COUNT(room_id) as matches_with_room_id,
    COUNT(*) - COUNT(room_id) as matches_without_room_id
FROM matches;

-- 5. 显示修复后的对局信息
SELECT match_id, room_id, room_name, status 
FROM matches 
ORDER BY match_id 
LIMIT 10;
