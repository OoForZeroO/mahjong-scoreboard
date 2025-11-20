-- 修复match_results表缺少total_duration字段的问题
-- 此脚本用于解决"字段 mr1_0.total_duration 不存在"的错误

-- 1. 检查当前match_results表的结构
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'match_results' 
ORDER BY ordinal_position;

-- 2. 添加total_duration字段（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'match_results' AND column_name = 'total_duration'
    ) THEN
        ALTER TABLE match_results ADD COLUMN total_duration BIGINT;
        RAISE NOTICE 'Added total_duration column to match_results table';
    ELSE
        RAISE NOTICE 'total_duration column already exists in match_results table';
    END IF;
END $$;

-- 3. 为现有记录设置默认值
UPDATE match_results SET total_duration = 0 WHERE total_duration IS NULL;

-- 4. 验证修复结果
SELECT 
    'match_results' as table_name,
    COUNT(*) as total_records,
    COUNT(total_duration) as records_with_total_duration
FROM match_results;

-- 5. 显示更新后的表结构
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'match_results' 
ORDER BY ordinal_position;
