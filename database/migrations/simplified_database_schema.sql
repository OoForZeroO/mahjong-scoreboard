-- 简化的麻将计分系统数据库表结构
-- 移除了棋牌室管理相关的复杂逻辑

-- 1. 对局表 - 简化为只记录对局基本信息
CREATE TABLE IF NOT EXISTS matches (
    match_id SERIAL PRIMARY KEY,  -- 对局唯一标识
    room_name VARCHAR(100),       -- 房间名称（仅作为显示字段，不关联房间表）
    start_time BIGINT NOT NULL,   -- 开始时间戳
    end_time BIGINT,             -- 结束时间戳（未结束时为null）
    status INTEGER NOT NULL DEFAULT 0,  -- 对局状态：0:进行中, 1:已完成
    total_rounds INTEGER NOT NULL DEFAULT 0,  -- 总轮次数
    settlement_multiplier DECIMAL(10,2),  -- 收盘倍率
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

-- 2. 对局参与者表 - 记录对局与用户的关系及基本信息
CREATE TABLE IF NOT EXISTS match_participants (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,     -- 对局ID
    user_id BIGINT,              -- 系统用户ID
    wechat_user_id VARCHAR(100),  -- 微信用户ID（支持游客模式）
    nickname VARCHAR(100) NOT NULL,  -- 用户昵称
    avatar VARCHAR(500),         -- 用户头像
    total_score INTEGER NOT NULL DEFAULT 0,  -- 总分
    is_quit BOOLEAN NOT NULL DEFAULT FALSE,  -- 是否已退出对局
    quit_time BIGINT,            -- 退出时间（未退出时为null）
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(match_id, COALESCE(user_id, wechat_user_id))  -- 确保每个用户在一个对局中只能有一条记录
);

-- 3. 轮次得分表 - 记录每一轮的得分情况
CREATE TABLE IF NOT EXISTS round_scores (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,     -- 对局ID
    participant_id BIGINT NOT NULL,  -- 参与者ID
    round_number INTEGER NOT NULL,   -- 轮次号
    score INTEGER NOT NULL,          -- 本轮得分
    cumulative_score INTEGER NOT NULL,  -- 累计得分
    round_time BIGINT,               -- 轮次时间戳
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (participant_id) REFERENCES match_participants(id),
    UNIQUE(match_id, participant_id, round_number)  -- 确保每个参与者在每轮只有一条记录
);

-- 4. 对局结果表 - 记录对局结束时的统计信息
CREATE TABLE IF NOT EXISTS match_results (
    match_id BIGINT PRIMARY KEY,  -- 对局ID（主键）
    winner_id BIGINT,             -- 获胜者ID
    highest_score INTEGER,        -- 最高分
    lowest_score INTEGER,         -- 最低分
    total_duration BIGINT,        -- 对局总时长（毫秒）
    total_scores TEXT,            -- 参与者得分信息JSON数据
    completion_time BIGINT,       -- 完成时间
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    FOREIGN KEY (winner_id) REFERENCES match_participants(id)
);

-- 5. 对局结算表 - 记录收盘时的结算信息
CREATE TABLE IF NOT EXISTS match_settlements (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,     -- 对局ID
    multiplier DECIMAL(10,2) NOT NULL,  -- 结算倍率
    settlement_time BIGINT NOT NULL,    -- 结算时间
    notes TEXT,                   -- 备注
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_matches_status ON matches(status);
CREATE INDEX IF NOT EXISTS idx_matches_room_name ON matches(room_name);
CREATE INDEX IF NOT EXISTS idx_match_participants_match_id ON match_participants(match_id);
CREATE INDEX IF NOT EXISTS idx_match_participants_user_id ON match_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_round_scores_match_id ON round_scores(match_id);
CREATE INDEX IF NOT EXISTS idx_round_scores_participant_id ON round_scores(participant_id);
-- match_id已经是主键，不需要额外索引
CREATE INDEX IF NOT EXISTS idx_match_settlements_match_id ON match_settlements(match_id);

-- 插入测试数据
INSERT INTO matches (room_name, start_time, status, total_rounds, create_time, update_time)
VALUES ('测试房间', extract(epoch from now())::bigint, 0, 0, extract(epoch from now())::bigint, extract(epoch from now())::bigint);

-- 验证表创建
SELECT 'Database schema simplified successfully' AS status;
