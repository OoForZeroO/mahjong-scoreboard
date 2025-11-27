-- 检查并修复数据库表结构
-- 此脚本用于检查和修复matches表的room_id字段问题

-- 1. 检查当前matches表的结构
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'matches' 
ORDER BY ordinal_position;

-- 2. 检查rooms表是否存在
SELECT EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_name = 'rooms'
) as rooms_table_exists;

-- 3. 如果rooms表不存在，创建它
CREATE TABLE IF NOT EXISTS rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    logo VARCHAR(500),
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

-- 4. 检查matches表是否有room_id字段
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'matches' AND column_name = 'room_id'
    ) THEN
        -- 添加room_id字段
        ALTER TABLE matches ADD COLUMN room_id BIGINT;
        RAISE NOTICE 'Added room_id column to matches table';
    ELSE
        RAISE NOTICE 'room_id column already exists in matches table';
    END IF;
END $$;

-- 5. 确保rooms表中有默认房间
INSERT INTO rooms (name, create_time, update_time) 
SELECT '默认房间', extract(epoch from now())::bigint, extract(epoch from now())::bigint
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE name = '默认房间');

-- 6. 为所有room_id为null的对局设置默认房间ID
UPDATE matches 
SET room_id = (SELECT id FROM rooms WHERE name = '默认房间' LIMIT 1)
WHERE room_id IS NULL;

-- 7. 现在可以安全地将room_id设置为NOT NULL（可选）
-- 注意：只有在确认所有记录都有room_id后才执行这一步
-- ALTER TABLE matches ALTER COLUMN room_id SET NOT NULL;

-- 8. 添加外键约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'matches' 
        AND constraint_name LIKE '%room_id%'
        AND constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE matches 
        ADD CONSTRAINT fk_matches_room_id 
        FOREIGN KEY (room_id) REFERENCES rooms(id);
        RAISE NOTICE 'Added foreign key constraint for room_id';
    ELSE
        RAISE NOTICE 'Foreign key constraint for room_id already exists';
    END IF;
END $$;

-- 9. 验证修复结果
SELECT 
    'matches' as table_name,
    COUNT(*) as total_records,
    COUNT(room_id) as records_with_room_id,
    COUNT(*) - COUNT(room_id) as records_without_room_id
FROM matches
UNION ALL
SELECT 
    'rooms' as table_name,
    COUNT(*) as total_records,
    COUNT(id) as records_with_room_id,
    0 as records_without_room_id
FROM rooms;

-- 10. 显示一些示例数据
SELECT 
    m.match_id, 
    m.room_id, 
    m.room_name, 
    r.name as actual_room_name,
    m.status 
FROM matches m
LEFT JOIN rooms r ON m.room_id = r.id
ORDER BY m.match_id 
LIMIT 10;
