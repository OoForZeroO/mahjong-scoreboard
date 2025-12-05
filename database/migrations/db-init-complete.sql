-- =====================================================
-- 麻将计分系统数据库初始化脚本（PostgreSQL版本）
-- 根据实体类自动生成，包含所有表的完整结构
-- 创建日期: 2025-12-05
-- =====================================================

-- 注意：执行此脚本前，请确保：
-- 1. 已创建数据库：mahjong_scoreboard_system（生产环境）或 mahjong_scoreboard_system_test（测试环境）
-- 2. 已创建数据库用户：yaohu
-- 3. 已授予用户相应的权限

-- =====================================================
-- 1. 用户表 (users)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'user',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    avatar VARCHAR(500),
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

COMMENT ON TABLE users IS '系统用户表';
COMMENT ON COLUMN users.id IS '用户ID，主键';
COMMENT ON COLUMN users.username IS '用户名';
COMMENT ON COLUMN users.phone IS '手机号，唯一';
COMMENT ON COLUMN users.email IS '邮箱';
COMMENT ON COLUMN users.password IS '密码（加密后）';
COMMENT ON COLUMN users.role IS '角色：user/admin';
COMMENT ON COLUMN users.status IS '状态：active/inactive';
COMMENT ON COLUMN users.avatar IS '头像URL';
COMMENT ON COLUMN users.create_time IS '创建时间戳（毫秒）';
COMMENT ON COLUMN users.update_time IS '更新时间戳（毫秒）';

-- =====================================================
-- 2. 微信用户表 (wechat_users)
-- =====================================================
CREATE TABLE IF NOT EXISTS wechat_users (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    nickname VARCHAR(100) NOT NULL,
    username VARCHAR(100),
    avatar VARCHAR(500),
    is_visitor BOOLEAN NOT NULL DEFAULT FALSE,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

COMMENT ON TABLE wechat_users IS '微信用户表';
COMMENT ON COLUMN wechat_users.id IS '微信用户ID，主键';
COMMENT ON COLUMN wechat_users.user_id IS '微信用户唯一标识（openid），唯一';
COMMENT ON COLUMN wechat_users.nickname IS '用户昵称';
COMMENT ON COLUMN wechat_users.username IS '用户名称';
COMMENT ON COLUMN wechat_users.avatar IS '用户头像URL';
COMMENT ON COLUMN wechat_users.is_visitor IS '是否游客';
COMMENT ON COLUMN wechat_users.create_time IS '创建时间戳（毫秒）';
COMMENT ON COLUMN wechat_users.update_time IS '更新时间戳（毫秒）';

-- =====================================================
-- 3. 房间表 (rooms)
-- =====================================================
CREATE TABLE IF NOT EXISTS rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    logo VARCHAR(500),
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

COMMENT ON TABLE rooms IS '房间表';
COMMENT ON COLUMN rooms.id IS '房间ID，主键';
COMMENT ON COLUMN rooms.name IS '房间名称';
COMMENT ON COLUMN rooms.logo IS '房间Logo URL';
COMMENT ON COLUMN rooms.create_time IS '创建时间戳（毫秒）';
COMMENT ON COLUMN rooms.update_time IS '更新时间戳（毫秒）';

-- =====================================================
-- 4. 对局表 (matches)
-- =====================================================
CREATE TABLE IF NOT EXISTS matches (
    match_id SERIAL PRIMARY KEY,
    room_name VARCHAR(100),
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    status INTEGER NOT NULL DEFAULT 0,
    total_rounds INTEGER NOT NULL DEFAULT 0,
    settlement_multiplier DOUBLE PRECISION,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

COMMENT ON TABLE matches IS '对局表';
COMMENT ON COLUMN matches.match_id IS '对局ID，主键';
COMMENT ON COLUMN matches.room_name IS '房间名称';
COMMENT ON COLUMN matches.start_time IS '开始时间戳（毫秒）';
COMMENT ON COLUMN matches.end_time IS '结束时间戳（毫秒），未结束时为NULL';
COMMENT ON COLUMN matches.status IS '对局状态：0=进行中, 1=已完成';
COMMENT ON COLUMN matches.total_rounds IS '总轮次数';
COMMENT ON COLUMN matches.settlement_multiplier IS '结算倍率';
COMMENT ON COLUMN matches.create_time IS '创建时间戳（毫秒）';
COMMENT ON COLUMN matches.update_time IS '更新时间戳（毫秒）';

-- =====================================================
-- 5. 对局参与者表 (match_participants)
-- =====================================================
CREATE TABLE IF NOT EXISTS match_participants (
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
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE(match_id, COALESCE(user_id, wechat_user_id))
);

COMMENT ON TABLE match_participants IS '对局参与者表';
COMMENT ON COLUMN match_participants.id IS '参与者ID，主键';
COMMENT ON COLUMN match_participants.match_id IS '对局ID，外键关联matches表';
COMMENT ON COLUMN match_participants.user_id IS '系统用户ID，外键关联users表，可为NULL（游客模式）';
COMMENT ON COLUMN match_participants.wechat_user_id IS '微信用户ID，支持游客模式';
COMMENT ON COLUMN match_participants.nickname IS '用户昵称';
COMMENT ON COLUMN match_participants.avatar IS '用户头像URL';
COMMENT ON COLUMN match_participants.total_score IS '总分（未乘倍率前）';
COMMENT ON COLUMN match_participants.is_quit IS '是否已退出对局';
COMMENT ON COLUMN match_participants.quit_time IS '退出时间戳（毫秒），未退出时为NULL';
COMMENT ON COLUMN match_participants.create_time IS '创建时间戳（毫秒）';
COMMENT ON COLUMN match_participants.update_time IS '更新时间戳（毫秒）';

-- =====================================================
-- 6. 轮次得分表 (round_scores)
-- =====================================================
CREATE TABLE IF NOT EXISTS round_scores (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    round_number INTEGER NOT NULL,
    round_time BIGINT NOT NULL,
    score INTEGER NOT NULL,
    cumulative_score INTEGER NOT NULL,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (participant_id) REFERENCES match_participants(id) ON DELETE CASCADE,
    UNIQUE(match_id, participant_id, round_number)
);

COMMENT ON TABLE round_scores IS '轮次得分表';
COMMENT ON COLUMN round_scores.id IS '轮次得分ID，主键';
COMMENT ON COLUMN round_scores.match_id IS '对局ID，外键关联matches表';
COMMENT ON COLUMN round_scores.participant_id IS '参与者ID，外键关联match_participants表';
COMMENT ON COLUMN round_scores.round_number IS '轮次号';
COMMENT ON COLUMN round_scores.round_time IS '轮次时间戳（毫秒）';
COMMENT ON COLUMN round_scores.score IS '本轮得分';
COMMENT ON COLUMN round_scores.cumulative_score IS '累计得分（未乘倍率）';
COMMENT ON COLUMN round_scores.create_time IS '创建时间戳（毫秒）';
COMMENT ON COLUMN round_scores.update_time IS '更新时间戳（毫秒）';

-- =====================================================
-- 7. 轮次记录表 (round_records)
-- =====================================================
CREATE TABLE IF NOT EXISTS round_records (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    round_number INTEGER NOT NULL,
    score_change INTEGER NOT NULL,
    cumulative_score INTEGER NOT NULL,
    win_type VARCHAR(50),
    loser_ids VARCHAR(255),
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (participant_id) REFERENCES match_participants(id) ON DELETE CASCADE
);

COMMENT ON TABLE round_records IS '轮次记录表';
COMMENT ON COLUMN round_records.id IS '轮次记录ID，主键';
COMMENT ON COLUMN round_records.match_id IS '对局ID，外键关联matches表';
COMMENT ON COLUMN round_records.participant_id IS '参与者ID，外键关联match_participants表';
COMMENT ON COLUMN round_records.round_number IS '轮次号';
COMMENT ON COLUMN round_records.score_change IS '得分变化';
COMMENT ON COLUMN round_records.cumulative_score IS '累计得分';
COMMENT ON COLUMN round_records.win_type IS '获胜类型';
COMMENT ON COLUMN round_records.loser_ids IS '失败者ID列表（逗号分隔）';
COMMENT ON COLUMN round_records.create_time IS '创建时间戳（毫秒）';
COMMENT ON COLUMN round_records.update_time IS '更新时间戳（毫秒）';

-- =====================================================
-- 8. 对局结算表 (match_settlements)
-- =====================================================
CREATE TABLE IF NOT EXISTS match_settlements (
    settlement_id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL UNIQUE,
    multiplier DOUBLE PRECISION NOT NULL,
    settlement_time BIGINT NOT NULL,
    notes TEXT,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE
);

COMMENT ON TABLE match_settlements IS '对局结算表';
COMMENT ON COLUMN match_settlements.settlement_id IS '结算ID，主键';
COMMENT ON COLUMN match_settlements.match_id IS '对局ID，外键关联matches表，唯一';
COMMENT ON COLUMN match_settlements.multiplier IS '结算倍率';
COMMENT ON COLUMN match_settlements.settlement_time IS '结算时间戳（毫秒）';
COMMENT ON COLUMN match_settlements.notes IS '结算备注';
COMMENT ON COLUMN match_settlements.create_time IS '创建时间戳（毫秒）';
COMMENT ON COLUMN match_settlements.update_time IS '更新时间戳（毫秒）';

-- =====================================================
-- 9. 得分记录表 (score_records)
-- =====================================================
CREATE TABLE IF NOT EXISTS score_records (
    match_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rounds INTEGER NOT NULL,
    score INTEGER NOT NULL,
    total_score INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_status VARCHAR(100),
    room_name VARCHAR(100) NOT NULL,
    room_id BIGINT NOT NULL,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

COMMENT ON TABLE score_records IS '得分记录表（历史表，可能已废弃）';
COMMENT ON COLUMN score_records.match_id IS '对局ID，主键';
COMMENT ON COLUMN score_records.user_id IS '用户ID';
COMMENT ON COLUMN score_records.rounds IS '对局圈数';
COMMENT ON COLUMN score_records.score IS '对局分数';
COMMENT ON COLUMN score_records.total_score IS '对局总分';
COMMENT ON COLUMN score_records.status IS '对局状态：进行中/已完成/已取消';
COMMENT ON COLUMN score_records.user_status IS '对局用户状态';
COMMENT ON COLUMN score_records.room_name IS '对局棋牌室名称';
COMMENT ON COLUMN score_records.room_id IS '棋牌室ID';
COMMENT ON COLUMN score_records.create_time IS '创建时间戳（毫秒）';
COMMENT ON COLUMN score_records.update_time IS '更新时间戳（毫秒）';

-- =====================================================
-- 创建索引以优化查询性能
-- =====================================================

-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- 微信用户表索引
CREATE INDEX IF NOT EXISTS idx_wechat_users_user_id ON wechat_users(user_id);
CREATE INDEX IF NOT EXISTS idx_wechat_users_is_visitor ON wechat_users(is_visitor);

-- 对局表索引
CREATE INDEX IF NOT EXISTS idx_matches_status ON matches(status);
CREATE INDEX IF NOT EXISTS idx_matches_start_time ON matches(start_time);
CREATE INDEX IF NOT EXISTS idx_matches_end_time ON matches(end_time);

-- 对局参与者表索引
CREATE INDEX IF NOT EXISTS idx_match_participants_match_id ON match_participants(match_id);
CREATE INDEX IF NOT EXISTS idx_match_participants_user_id ON match_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_match_participants_wechat_user_id ON match_participants(wechat_user_id);

-- 轮次得分表索引
CREATE INDEX IF NOT EXISTS idx_round_scores_match_id ON round_scores(match_id);
CREATE INDEX IF NOT EXISTS idx_round_scores_participant_id ON round_scores(participant_id);
CREATE INDEX IF NOT EXISTS idx_round_scores_round_number ON round_scores(round_number);

-- 轮次记录表索引
CREATE INDEX IF NOT EXISTS idx_round_records_match_id ON round_records(match_id);
CREATE INDEX IF NOT EXISTS idx_round_records_participant_id ON round_records(participant_id);
CREATE INDEX IF NOT EXISTS idx_round_records_round_number ON round_records(round_number);

-- 对局结算表索引
CREATE INDEX IF NOT EXISTS idx_match_settlements_match_id ON match_settlements(match_id);
CREATE INDEX IF NOT EXISTS idx_match_settlements_settlement_time ON match_settlements(settlement_time);

-- 得分记录表索引
CREATE INDEX IF NOT EXISTS idx_score_records_user_id ON score_records(user_id);
CREATE INDEX IF NOT EXISTS idx_score_records_room_id ON score_records(room_id);
CREATE INDEX IF NOT EXISTS idx_score_records_status ON score_records(status);

-- =====================================================
-- 验证表结构
-- =====================================================

-- 显示所有创建的表
SELECT 
    table_name,
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = t.table_name) as column_count
FROM information_schema.tables t
WHERE table_schema = 'public' 
    AND table_name IN (
        'users', 'wechat_users', 'rooms', 'matches', 
        'match_participants', 'round_scores', 'round_records', 
        'match_settlements', 'score_records'
    )
ORDER BY table_name;

-- 显示所有表的主键信息
SELECT 
    tc.table_name,
    kcu.column_name as primary_key_column
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.constraint_type = 'PRIMARY KEY'
    AND tc.table_schema = 'public'
    AND tc.table_name IN (
        'users', 'wechat_users', 'rooms', 'matches', 
        'match_participants', 'round_scores', 'round_records', 
        'match_settlements', 'score_records'
    )
ORDER BY tc.table_name;

-- =====================================================
-- 初始化完成提示
-- =====================================================
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE '数据库初始化完成！';
    RAISE NOTICE '已创建以下表：';
    RAISE NOTICE '  1. users - 系统用户表';
    RAISE NOTICE '  2. wechat_users - 微信用户表';
    RAISE NOTICE '  3. rooms - 房间表';
    RAISE NOTICE '  4. matches - 对局表';
    RAISE NOTICE '  5. match_participants - 对局参与者表';
    RAISE NOTICE '  6. round_scores - 轮次得分表';
    RAISE NOTICE '  7. round_records - 轮次记录表';
    RAISE NOTICE '  8. match_settlements - 对局结算表';
    RAISE NOTICE '  9. score_records - 得分记录表';
    RAISE NOTICE '========================================';
END $$;

