-- 测试数据库连接和表结构
-- 生产库：mahjong_scoreboard_system；测试库：mahjong_scoreboard_system_test

-- 1. 检查当前数据库
SELECT current_database();

-- 2. 检查match_results表是否存在
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'match_results'
) as table_exists;

-- 3. 列出所有表
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- 4. 检查match_results表的所有字段
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'public' 
AND table_name = 'match_results' 
ORDER BY ordinal_position;

-- 5. 检查total_duration字段是否存在
SELECT 
    column_name,
    data_type
FROM information_schema.columns 
WHERE table_schema = 'public' 
AND table_name = 'match_results' 
AND column_name = 'total_duration';

-- 6. 如果表存在，显示表结构
\c mahjong_scoreboard_system
\d match_results;
