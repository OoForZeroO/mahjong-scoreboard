-- 修复数据库表结构以匹配实体类

-- 1. 修复 match_participants 表
-- 检查并添加 user_name 字段（如果不存在）
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'match_participants' AND column_name = 'user_name') THEN
        ALTER TABLE match_participants ADD COLUMN user_name VARCHAR(100);
    END IF;
END $$;

-- 如果存在 nickname 字段，将其数据迁移到 user_name 字段
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'match_participants' AND column_name = 'nickname') THEN
        UPDATE match_participants SET user_name = nickname WHERE user_name IS NULL;
        ALTER TABLE match_participants DROP COLUMN nickname;
    END IF;
END $$;

-- 确保 user_name 字段不为空
ALTER TABLE match_participants ALTER COLUMN user_name SET NOT NULL;

-- 2. 修复 round_scores 表
-- 检查并添加 round_time 字段（如果不存在）
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'round_scores' AND column_name = 'round_time') THEN
        ALTER TABLE round_scores ADD COLUMN round_time BIGINT NOT NULL DEFAULT 0;
    END IF;
END $$;

-- 3. 确保外键关系正确
-- 检查并修复 match_participants 表的外键
DO $$ 
BEGIN
    -- 删除可能存在的错误外键
    IF EXISTS (SELECT 1 FROM information_schema.table_constraints 
               WHERE table_name = 'match_participants' AND constraint_name LIKE '%user_id%') THEN
        ALTER TABLE match_participants DROP CONSTRAINT IF EXISTS match_participants_user_id_fkey;
    END IF;
    
    -- 添加正确的外键
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'match_participants' AND constraint_name = 'match_participants_user_id_fkey') THEN
        ALTER TABLE match_participants 
        ADD CONSTRAINT match_participants_user_id_fkey 
        FOREIGN KEY (user_id) REFERENCES wechat_users(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 4. 确保索引正确
CREATE INDEX IF NOT EXISTS idx_match_participants_user_name ON match_participants(user_name);
CREATE INDEX IF NOT EXISTS idx_round_scores_round_time ON round_scores(round_time);

-- 5. 更新现有数据
-- 为现有的 round_scores 记录设置 round_time
UPDATE round_scores SET round_time = create_time WHERE round_time = 0 OR round_time IS NULL;

-- 6. 验证表结构
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns 
WHERE table_name IN ('match_participants', 'round_scores')
ORDER BY table_name, ordinal_position;
