-- 检查match_results表的字段结构
-- 确认total_scores和total_rounds字段是否存在

-- 1. 查看表结构
\d match_results;

-- 2. 查看所有字段
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'match_results'
ORDER BY ordinal_position;

-- 3. 检查total_scores字段
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name = 'total_scores';

-- 4. 检查total_rounds字段
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name = 'total_rounds';

-- 5. 查看表的所有字段名（不区分大小写）
SELECT column_name
FROM information_schema.columns 
WHERE table_name = 'match_results'
ORDER BY column_name;

-- 6. 检查是否有大小写问题
SELECT column_name
FROM information_schema.columns 
WHERE table_name = 'match_results'
AND (column_name ILIKE '%total%' OR column_name ILIKE '%scores%' OR column_name ILIKE '%rounds%');
