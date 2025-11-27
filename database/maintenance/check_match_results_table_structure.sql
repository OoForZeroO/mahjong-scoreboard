-- 检查match_results表的结构
-- 确认字段名和数据类型

-- 1. 查看表结构
\d match_results;

-- 2. 查看所有字段
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'match_results'
ORDER BY ordinal_position;

-- 3. 检查是否有total_rounds字段
SELECT column_name, data_type
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name = 'total_rounds';

-- 4. 检查是否有total_scores字段
SELECT column_name, data_type
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name = 'total_scores';

-- 5. 查看表的所有字段名（不区分大小写）
SELECT column_name
FROM information_schema.columns 
WHERE table_name = 'match_results'
ORDER BY column_name;
