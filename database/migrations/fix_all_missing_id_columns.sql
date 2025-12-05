-- =====================================================
-- 修复所有表缺少 ID 字段的问题
-- 根据实体类定义，为所有缺少主键 ID 的表添加或修复 ID 字段
-- 创建日期: 2025-12-05
-- =====================================================

-- 注意：此脚本会检查每个表，如果缺少主键 ID 字段，则添加该字段
-- 如果表已存在数据，添加主键时可能需要处理现有数据

-- =====================================================
-- 1. 修复 users 表
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'users' 
        AND column_name = 'id'
    ) THEN
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'users'
        ) THEN
            ALTER TABLE users ADD COLUMN id SERIAL PRIMARY KEY;
            RAISE NOTICE '已添加 id 列到 users 表';
        ELSE
            RAISE NOTICE 'users 表不存在，跳过';
        END IF;
    ELSE
        RAISE NOTICE 'users 表已包含 id 列，无需修复';
    END IF;
END $$;

-- =====================================================
-- 2. 修复 wechat_users 表
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'wechat_users' 
        AND column_name = 'id'
    ) THEN
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'wechat_users'
        ) THEN
            ALTER TABLE wechat_users ADD COLUMN id SERIAL PRIMARY KEY;
            RAISE NOTICE '已添加 id 列到 wechat_users 表';
        ELSE
            RAISE NOTICE 'wechat_users 表不存在，跳过';
        END IF;
    ELSE
        RAISE NOTICE 'wechat_users 表已包含 id 列，无需修复';
    END IF;
END $$;

-- =====================================================
-- 3. 修复 rooms 表
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'rooms' 
        AND column_name = 'id'
    ) THEN
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'rooms'
        ) THEN
            ALTER TABLE rooms ADD COLUMN id SERIAL PRIMARY KEY;
            RAISE NOTICE '已添加 id 列到 rooms 表';
        ELSE
            RAISE NOTICE 'rooms 表不存在，跳过';
        END IF;
    ELSE
        RAISE NOTICE 'rooms 表已包含 id 列，无需修复';
    END IF;
END $$;

-- =====================================================
-- 4. 修复 matches 表（注意：主键是 match_id，不是 id）
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'matches' 
        AND column_name = 'match_id'
    ) THEN
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'matches'
        ) THEN
            ALTER TABLE matches ADD COLUMN match_id SERIAL PRIMARY KEY;
            RAISE NOTICE '已添加 match_id 列到 matches 表';
        ELSE
            RAISE NOTICE 'matches 表不存在，跳过';
        END IF;
    ELSE
        RAISE NOTICE 'matches 表已包含 match_id 列，无需修复';
    END IF;
END $$;

-- =====================================================
-- 5. 修复 match_participants 表
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'match_participants' 
        AND column_name = 'id'
    ) THEN
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'match_participants'
        ) THEN
            ALTER TABLE match_participants ADD COLUMN id SERIAL PRIMARY KEY;
            RAISE NOTICE '已添加 id 列到 match_participants 表';
        ELSE
            RAISE NOTICE 'match_participants 表不存在，跳过';
        END IF;
    ELSE
        RAISE NOTICE 'match_participants 表已包含 id 列，无需修复';
    END IF;
END $$;

-- =====================================================
-- 6. 修复 round_scores 表
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'round_scores' 
        AND column_name = 'id'
    ) THEN
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'round_scores'
        ) THEN
            ALTER TABLE round_scores ADD COLUMN id SERIAL PRIMARY KEY;
            RAISE NOTICE '已添加 id 列到 round_scores 表';
        ELSE
            RAISE NOTICE 'round_scores 表不存在，跳过';
        END IF;
    ELSE
        RAISE NOTICE 'round_scores 表已包含 id 列，无需修复';
    END IF;
END $$;

-- =====================================================
-- 7. 修复 round_records 表
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'round_records' 
        AND column_name = 'id'
    ) THEN
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'round_records'
        ) THEN
            ALTER TABLE round_records ADD COLUMN id SERIAL PRIMARY KEY;
            RAISE NOTICE '已添加 id 列到 round_records 表';
        ELSE
            RAISE NOTICE 'round_records 表不存在，跳过';
        END IF;
    ELSE
        RAISE NOTICE 'round_records 表已包含 id 列，无需修复';
    END IF;
END $$;

-- =====================================================
-- 8. 修复 match_settlements 表（注意：主键是 settlement_id，不是 id）
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'match_settlements' 
        AND column_name = 'settlement_id'
    ) THEN
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'match_settlements'
        ) THEN
            ALTER TABLE match_settlements ADD COLUMN settlement_id SERIAL PRIMARY KEY;
            RAISE NOTICE '已添加 settlement_id 列到 match_settlements 表';
        ELSE
            RAISE NOTICE 'match_settlements 表不存在，跳过';
        END IF;
    ELSE
        RAISE NOTICE 'match_settlements 表已包含 settlement_id 列，无需修复';
    END IF;
END $$;

-- =====================================================
-- 9. 修复 score_records 表（注意：主键是 match_id，不是 id）
-- =====================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'score_records' 
        AND column_name = 'match_id'
    ) THEN
        IF EXISTS (
            SELECT 1 
            FROM information_schema.tables 
            WHERE table_name = 'score_records'
        ) THEN
            -- 检查是否已有其他主键
            IF NOT EXISTS (
                SELECT 1 
                FROM information_schema.table_constraints 
                WHERE table_name = 'score_records' 
                AND constraint_type = 'PRIMARY KEY'
            ) THEN
                ALTER TABLE score_records ADD COLUMN match_id SERIAL PRIMARY KEY;
                RAISE NOTICE '已添加 match_id 列到 score_records 表';
            ELSE
                RAISE NOTICE 'score_records 表已有主键，跳过添加 match_id';
            END IF;
        ELSE
            RAISE NOTICE 'score_records 表不存在，跳过';
        END IF;
    ELSE
        RAISE NOTICE 'score_records 表已包含 match_id 列，无需修复';
    END IF;
END $$;

-- =====================================================
-- 验证修复结果
-- =====================================================

-- 显示所有表的主键信息
SELECT 
    tc.table_name,
    kcu.column_name as primary_key_column,
    kcu.ordinal_position
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
WHERE tc.constraint_type = 'PRIMARY KEY'
    AND tc.table_schema = 'public'
    AND tc.table_name IN (
        'users', 'wechat_users', 'rooms', 'matches', 
        'match_participants', 'round_scores', 'round_records', 
        'match_settlements', 'score_records'
    )
ORDER BY tc.table_name, kcu.ordinal_position;

-- 显示所有表的列信息（仅主键列）
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
    AND table_name IN (
        'users', 'wechat_users', 'rooms', 'matches', 
        'match_participants', 'round_scores', 'round_records', 
        'match_settlements', 'score_records'
    )
    AND column_name IN ('id', 'match_id', 'settlement_id')
ORDER BY table_name, ordinal_position;

-- =====================================================
-- 修复完成提示
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE '所有表的 ID 字段修复完成！';
    RAISE NOTICE '请检查上方的验证结果，确认所有表都有正确的主键。';
    RAISE NOTICE '========================================';
END $$;

