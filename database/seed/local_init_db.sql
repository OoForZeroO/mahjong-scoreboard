-- 本地数据库初始化脚本

-- 创建参与者表
CREATE TABLE IF NOT EXISTS participants (
    id SERIAL PRIMARY KEY,
    match_id INTEGER NOT NULL,
    user_id INTEGER,
    nickname VARCHAR(50) NOT NULL,
    avatar VARCHAR(255),
    total_score INTEGER DEFAULT 0,
    is_quit BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建轮次分数表
CREATE TABLE IF NOT EXISTS round_scores (
    id SERIAL PRIMARY KEY,
    match_id INTEGER NOT NULL,
    round_number INTEGER NOT NULL,
    participant_id INTEGER NOT NULL,
    score INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(match_id, round_number, participant_id)
);

-- 创建对局表
CREATE TABLE IF NOT EXISTS matches (
    id SERIAL PRIMARY KEY,
    room_id INTEGER NOT NULL,
    room_name VARCHAR(100) NOT NULL,
    start_time BIGINT NOT NULL,
    end_time BIGINT,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    settlement_multiplier DECIMAL(5,2),
    settlement_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建结算记录表
CREATE TABLE IF NOT EXISTS settlements (
    id SERIAL PRIMARY KEY,
    match_id INTEGER NOT NULL UNIQUE,
    total_rounds INTEGER NOT NULL,
    multiplier DECIMAL(5,2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建结算详情表
CREATE TABLE IF NOT EXISTS settlement_details (
    id SERIAL PRIMARY KEY,
    settlement_id INTEGER NOT NULL,
    participant_id INTEGER NOT NULL,
    original_score INTEGER NOT NULL,
    final_score DECIMAL(10,2) NOT NULL,
    rank INTEGER
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_matches_id ON matches(id);
CREATE INDEX IF NOT EXISTS idx_participants_match_id ON participants(match_id);
CREATE INDEX IF NOT EXISTS idx_round_scores_match_round ON round_scores(match_id, round_number);
CREATE INDEX IF NOT EXISTS idx_settlements_match_id ON settlements(match_id);
CREATE INDEX IF NOT EXISTS idx_settlement_details_settlement_id ON settlement_details(settlement_id);

-- 添加外键约束
ALTER TABLE participants 
    ADD CONSTRAINT fk_participants_match 
    FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE;

ALTER TABLE round_scores 
    ADD CONSTRAINT fk_round_scores_match 
    FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE;

ALTER TABLE round_scores 
    ADD CONSTRAINT fk_round_scores_participant 
    FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE;

ALTER TABLE settlements 
    ADD CONSTRAINT fk_settlements_match 
    FOREIGN KEY (match_id) REFERENCES matches(id) ON DELETE CASCADE;

ALTER TABLE settlement_details 
    ADD CONSTRAINT fk_settlement_details_settlement 
    FOREIGN KEY (settlement_id) REFERENCES settlements(id) ON DELETE CASCADE;

ALTER TABLE settlement_details 
    ADD CONSTRAINT fk_settlement_details_participant 
    FOREIGN KEY (participant_id) REFERENCES participants(id) ON DELETE CASCADE;

-- 更新matches表的updated_at触发器
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_matches_updated_at 
    BEFORE UPDATE ON matches 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 插入测试数据（可选）
INSERT INTO matches (room_id, room_name, start_time) VALUES 
(1, '测试房间1', EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT);

INSERT INTO participants (match_id, nickname, avatar) VALUES 
(1, '测试玩家1', 'https://example.com/avatar1.jpg'),
(1, '测试玩家2', 'https://example.com/avatar2.jpg');
