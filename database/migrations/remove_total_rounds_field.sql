-- 从match_results表中移除total_rounds字段
-- 因为结果表不需要存储总轮次信息

-- 1. 检查字段是否存在
SELECT column_name, data_type
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name = 'total_rounds';

-- 2. 如果字段存在，则删除它
ALTER TABLE match_results DROP COLUMN IF EXISTS total_rounds;

-- 3. 验证字段已删除
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'match_results'
ORDER BY ordinal_position;

-- 4. 确认total_scores字段存在
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name = 'total_scores';

-- 5. 显示最终的表结构
SELECT 'match_results表结构已更新，移除了total_rounds字段' AS status;
