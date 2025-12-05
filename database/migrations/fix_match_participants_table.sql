-- 修复 match_participants 表结构
-- 如果表缺少 id 列，则添加该列

-- 检查并添加 id 列（如果不存在）
DO $$
BEGIN
    -- 检查 id 列是否存在
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'match_participants' 
        AND column_name = 'id'
    ) THEN
        -- 如果表存在但没有 id 列，添加 id 列
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'match_participants'
        ) THEN
            -- 添加 id 列作为主键
            ALTER TABLE match_participants 
            ADD COLUMN id SERIAL PRIMARY KEY;
            
            RAISE NOTICE '已添加 id 列到 match_participants 表';
        ELSE
            -- 如果表不存在，创建完整的表
            CREATE TABLE match_participants (
                id SERIAL PRIMARY KEY,
                match_id BIGINT NOT NULL,
                user_id BIGINT,
                wechat_user_id VARCHAR(100),
                nickname VARCHAR(100) NOT NULL,
                avatar VARCHAR(500),
                total_score INTEGER NOT NULL DEFAULT 0,
                is_quit BOOLEAN NOT NULL DEFAULT FALSE,
                quit_time BIGINT,
                create_time BIGINT NOT NULL,
                update_time BIGINT NOT NULL,
                FOREIGN KEY (match_id) REFERENCES matches(match_id),
                FOREIGN KEY (user_id) REFERENCES users(id),
                UNIQUE(match_id, COALESCE(user_id, wechat_user_id))
            );
            
            RAISE NOTICE '已创建 match_participants 表';
        END IF;
    ELSE
        RAISE NOTICE 'match_participants 表已包含 id 列，无需修复';
    END IF;
END $$;

-- 验证表结构
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'match_participants'
ORDER BY ordinal_position;

