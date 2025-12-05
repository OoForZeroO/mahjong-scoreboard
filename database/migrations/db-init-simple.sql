-- =====================================================
-- 麻将计分系统数据库初始化脚本（PostgreSQL版本）
-- 简化版本：不包含 COMMENT 语句，提高兼容性
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
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

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

-- 对局参与者表唯一约束（使用唯一索引实现 COALESCE 功能）
-- 确保一个对局中每个用户（user_id 或 wechat_user_id）只能有一条记录
CREATE UNIQUE INDEX IF NOT EXISTS idx_match_participants_unique_user 
ON match_participants(match_id, COALESCE(user_id::text, wechat_user_id));

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

