-- 检查match_results表的实际结构
-- 用于诊断"字段 mr1_0.total_duration 不存在"的问题

-- 1. 检查表是否存在
SELECT 
    table_name,
    table_type
FROM information_schema.tables 
WHERE table_name = 'match_results';

-- 2. 检查表的所有字段
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default,
    ordinal_position
FROM information_schema.columns 
WHERE table_name = 'match_results' 
ORDER BY ordinal_position;

-- 3. 检查是否有大小写问题
SELECT 
    column_name,
    data_type
FROM information_schema.columns 
WHERE table_name ILIKE 'match_results' 
ORDER BY ordinal_position;

-- 4. 检查total_duration字段是否存在（不区分大小写）
SELECT 
    column_name,
    data_type
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name ILIKE '%total_duration%';

-- 5. 检查表的所有约束
SELECT 
    constraint_name,
    constraint_type,
    table_name
FROM information_schema.table_constraints 
WHERE table_name = 'match_results';

-- 6. 检查表的数据
SELECT COUNT(*) as record_count FROM match_results;

-- 7. 如果表存在，显示表结构（PostgreSQL特有）
\d match_results;
