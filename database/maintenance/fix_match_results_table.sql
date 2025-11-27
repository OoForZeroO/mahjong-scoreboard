-- 修复 match_results 表，添加缺失的 total_duration 字段

-- 1. 添加 total_duration 字段
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS total_duration BIGINT;

-- 2. 为现有记录设置默认值
UPDATE match_results SET total_duration = 0 WHERE total_duration IS NULL;

-- 3. 验证表结构
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'match_results' 
ORDER BY ordinal_position;

-- 4. 显示修复结果
SELECT 'match_results table structure updated successfully' as status;
