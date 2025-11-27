-- 修复match_results表结构，使用match_id作为主键
-- 此脚本用于解决MatchResult实体类的主键ID为null的问题

-- 1. 检查当前match_results表的结构
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'match_results' 
ORDER BY ordinal_position;

-- 2. 如果表不存在，创建它
CREATE TABLE IF NOT EXISTS match_results (
    match_id BIGINT PRIMARY KEY,
    winner_id BIGINT,
    highest_score INTEGER,
    lowest_score INTEGER,
    total_duration BIGINT,
    completion_time BIGINT,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

-- 3. 如果表存在但结构不正确，修复它
DO $$
BEGIN
    -- 如果存在result_id字段，删除它
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'match_results' AND column_name = 'result_id'
    ) THEN
        -- 先删除主键约束
        ALTER TABLE match_results DROP CONSTRAINT IF EXISTS match_results_pkey;
        -- 删除result_id字段
        ALTER TABLE match_results DROP COLUMN IF EXISTS result_id;
        RAISE NOTICE 'Removed result_id column from match_results table';
    END IF;
    
    -- 确保match_id字段存在且为NOT NULL
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'match_results' AND column_name = 'match_id'
    ) THEN
        ALTER TABLE match_results ADD COLUMN match_id BIGINT NOT NULL;
        RAISE NOTICE 'Added match_id column to match_results table';
    ELSE
        ALTER TABLE match_results ALTER COLUMN match_id SET NOT NULL;
        RAISE NOTICE 'Updated match_id column to NOT NULL';
    END IF;
    
    -- 设置match_id为主键
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'match_results' 
        AND constraint_type = 'PRIMARY KEY'
    ) THEN
        ALTER TABLE match_results ADD PRIMARY KEY (match_id);
        RAISE NOTICE 'Set match_id as primary key';
    ELSE
        RAISE NOTICE 'Primary key already exists';
    END IF;
END $$;

-- 4. 添加外键约束（如果不存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'match_results' 
        AND constraint_name LIKE '%match_id%'
        AND constraint_type = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE match_results 
        ADD CONSTRAINT fk_match_results_match_id 
        FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE;
        RAISE NOTICE 'Added foreign key constraint for match_id';
    ELSE
        RAISE NOTICE 'Foreign key constraint for match_id already exists';
    END IF;
END $$;

-- 5. 添加其他字段（如果不存在）
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS winner_id BIGINT;
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS highest_score INTEGER;
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS lowest_score INTEGER;
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS total_duration BIGINT;
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS completion_time BIGINT;
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS create_time BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::bigint;
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS update_time BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW())::bigint;

-- 6. 添加索引
CREATE INDEX IF NOT EXISTS idx_match_results_winner_id ON match_results(winner_id);

-- 7. 验证修复结果
SELECT 
    'match_results' as table_name,
    COUNT(*) as total_records,
    COUNT(match_id) as records_with_match_id
FROM match_results;

-- 10. 显示表结构
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'match_results' 
ORDER BY ordinal_position;
