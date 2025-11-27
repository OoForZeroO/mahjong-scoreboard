-- 添加 round_time 字段到 round_scores 表

-- 1. 添加 round_time 字段
ALTER TABLE round_scores ADD COLUMN IF NOT EXISTS round_time BIGINT;

-- 2. 为现有记录设置默认的 round_time 值（使用 create_time 的值）
UPDATE round_scores SET round_time = create_time WHERE round_time IS NULL;

-- 3. 设置 round_time 字段为 NOT NULL
ALTER TABLE round_scores ALTER COLUMN round_time SET NOT NULL;

-- 4. 添加索引以提升查询性能
CREATE INDEX IF NOT EXISTS idx_round_scores_round_time ON round_scores(round_time);

-- 5. 验证表结构
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'round_scores' 
ORDER BY ordinal_position;
