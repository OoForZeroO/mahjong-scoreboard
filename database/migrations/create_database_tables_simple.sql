-- 创建用户表
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

-- 创建微信用户表
CREATE TABLE IF NOT EXISTS wechat_users (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    nickname VARCHAR(100) NOT NULL,
    username VARCHAR(100),
    avatar VARCHAR(500),
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

-- 创建棋牌室表
CREATE TABLE IF NOT EXISTS rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    logo VARCHAR(500),
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

-- 创建对局表
CREATE TABLE IF NOT EXISTS matches (
    match_id SERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    room_name VARCHAR(100) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT '进行中',
    total_rounds INTEGER NOT NULL DEFAULT 0,
    current_round INTEGER DEFAULT 0,
    settlement_multiplier DOUBLE PRECISION,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 创建对局参与者表
CREATE TABLE IF NOT EXISTS match_participants (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    user_id BIGINT,
    wechat_user_id VARCHAR(100),
    nickname VARCHAR(100) NOT NULL,
    avatar VARCHAR(500),
    total_score INTEGER NOT NULL DEFAULT 0,
    final_score DOUBLE PRECISION,
    is_quit BOOLEAN NOT NULL DEFAULT FALSE,
    quit_time BIGINT,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 创建轮次得分表
CREATE TABLE IF NOT EXISTS round_scores (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    participant_id BIGINT NOT NULL,
    round_number INTEGER NOT NULL,
    score INTEGER NOT NULL,
    cumulative_score INTEGER NOT NULL,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (participant_id) REFERENCES match_participants(id)
);

-- 创建对局结果表
CREATE TABLE IF NOT EXISTS match_results (
    match_id BIGINT PRIMARY KEY,
    winner_id BIGINT,
    highest_score INTEGER,
    lowest_score INTEGER,
    total_scores TEXT,
    completion_time BIGINT,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (winner_id) REFERENCES match_participants(id)
);

-- 创建对局结算表
CREATE TABLE IF NOT EXISTS match_settlements (
    settlement_id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,
    multiplier DOUBLE PRECISION NOT NULL,
    settlement_time BIGINT NOT NULL,
    notes TEXT,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    CONSTRAINT unique_match_id UNIQUE (match_id)
);

-- 创建计分记录表
CREATE TABLE IF NOT EXISTS score_records (
    match_id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rounds INTEGER NOT NULL,
    score INTEGER NOT NULL,
    total_score INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_status VARCHAR(100),
    room_name VARCHAR(100) NOT NULL,
    room_id BIGINT NOT NULL,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 插入示例数据
INSERT INTO rooms (name, create_time, update_time) 
VALUES ('默认棋牌室', extract(epoch from now()) * 1000, extract(epoch from now()) * 1000) 
ON CONFLICT (name) DO NOTHING;

-- 创建索引
CREATE INDEX idx_matches_room_id ON matches(room_id);
CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_match_participants_match_id ON match_participants(match_id);
CREATE INDEX idx_match_participants_user_id ON match_participants(user_id);
CREATE INDEX idx_round_scores_match_id ON round_scores(match_id);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_wechat_users_user_id ON wechat_users(user_id);
CREATE INDEX idx_score_records_user_id ON score_records(user_id);
CREATE INDEX idx_score_records_room_id ON score_records(room_id);
