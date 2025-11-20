-- 为 match_results 表添加 total_duration 字段

-- 1. 添加 total_duration 字段
ALTER TABLE match_results ADD COLUMN total_duration BIGINT;

-- 2. 为现有记录设置默认值（可选）
UPDATE match_results SET total_duration = 0 WHERE total_duration IS NULL;

-- 3. 验证字段添加成功
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'match_results' 
ORDER BY ordinal_position;
