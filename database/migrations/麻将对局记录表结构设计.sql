-- 麻将对局记录数据库表结构设计

-- 1. 对局表 - 记录整个对局的基本信息和状态
CREATE TABLE IF NOT EXISTS matches (
    match_id SERIAL PRIMARY KEY,  -- 对局唯一标识
    room_id BIGINT NOT NULL,      -- 所属房间ID
    room_name VARCHAR(100) NOT NULL,  -- 房间名称
    start_time BIGINT NOT NULL,   -- 开始时间戳
    end_time BIGINT,             -- 结束时间戳（未结束时为null）
    status VARCHAR(20) NOT NULL DEFAULT '进行中',  -- 对局状态：进行中/已完成/已取消
    total_rounds INTEGER NOT NULL DEFAULT 0,  -- 总轮次数
    current_round INTEGER DEFAULT 0,  -- 当前轮次
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 2. 对局参与者表 - 记录对局与用户的多对多关系
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

-- 3. 轮次记录表 - 记录每一轮次每个参与者的得分情况
CREATE TABLE IF NOT EXISTS round_records (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,     -- 对局ID
    participant_id BIGINT NOT NULL,  -- 参与者ID
    round_number INTEGER NOT NULL,  -- 轮次号
    score_change INTEGER NOT NULL,  -- 本轮得分变化（正数表示赢，负数表示输）
    cumulative_score INTEGER NOT NULL,  -- 累计得分
    win_type VARCHAR(50),         -- 赢牌类型（如：自摸、放炮、抢杠胡等）
    loser_ids VARCHAR(255),       -- 输家ID列表（JSON格式，如[2,3]）
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (participant_id) REFERENCES match_participants(id),
    UNIQUE(match_id, participant_id, round_number)  -- 确保每个参与者每轮只有一条记录
);

-- 4. 对局结果汇总表（可选，用于快速查询统计数据）
CREATE TABLE IF NOT EXISTS match_results (
    match_id BIGINT PRIMARY KEY,
    winner_id BIGINT,            -- 最终胜利者ID（可能为null，如平局或未结束）
    highest_score INTEGER,       -- 最高得分
    lowest_score INTEGER,        -- 最低得分
    total_scores JSONB,          -- 所有参与者最终得分（JSON格式）
    completion_time BIGINT,      -- 完成时间
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (winner_id) REFERENCES match_participants(id)
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_matches_room_id ON matches(room_id);
CREATE INDEX IF NOT EXISTS idx_matches_status ON matches(status);
CREATE INDEX IF NOT EXISTS idx_matches_start_time ON matches(start_time);

CREATE INDEX IF NOT EXISTS idx_match_participants_match_id ON match_participants(match_id);
CREATE INDEX IF NOT EXISTS idx_match_participants_user_id ON match_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_match_participants_wechat_user_id ON match_participants(wechat_user_id);
CREATE INDEX IF NOT EXISTS idx_match_participants_is_quit ON match_participants(is_quit);

CREATE INDEX IF NOT EXISTS idx_round_records_match_id ON round_records(match_id);
CREATE INDEX IF NOT EXISTS idx_round_records_participant_id ON round_records(participant_id);
CREATE INDEX IF NOT EXISTS idx_round_records_match_round ON round_records(match_id, round_number);

-- 视图：用于查询特定对局的所有参与者当前状态
CREATE OR REPLACE VIEW v_match_participants_current AS
SELECT 
    mp.*,
    m.status as match_status,
    m.current_round,
    COUNT(rr.id) as round_count
FROM match_participants mp
JOIN matches m ON mp.match_id = m.match_id
LEFT JOIN round_records rr ON mp.id = rr.participant_id AND rr.match_id = m.match_id
GROUP BY mp.id, m.status, m.current_round;

-- 视图：用于查询特定对局的所有轮次记录
CREATE OR REPLACE VIEW v_match_rounds_summary AS
SELECT 
    m.match_id,
    r.round_number,
    COUNT(r.id) as participant_count,
    MAX(r.create_time) as round_end_time
FROM matches m
JOIN round_records r ON m.match_id = r.match_id
GROUP BY m.match_id, r.round_number
ORDER BY m.match_id, r.round_number;

-- 触发器函数：更新对局状态和总分
CREATE OR REPLACE FUNCTION update_match_status_and_scores()
RETURNS TRIGGER AS $$
BEGIN
    -- 更新参与者总分
    UPDATE match_participants 
    SET total_score = (SELECT COALESCE(SUM(score_change), 0) 
                      FROM round_records 
                      WHERE participant_id = NEW.participant_id),
        update_time = EXTRACT(EPOCH FROM NOW())::bigint
    WHERE id = NEW.participant_id;
    
    -- 更新对局当前轮次
    UPDATE matches
    SET current_round = NEW.round_number,
        update_time = EXTRACT(EPOCH FROM NOW())::bigint
    WHERE match_id = NEW.match_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器
CREATE TRIGGER trg_update_match_status
AFTER INSERT OR UPDATE ON round_records
FOR EACH ROW
EXECUTE FUNCTION update_match_status_and_scores();

-- 说明：
-- 1. 这种设计支持一个对局有多个参与者（通常是4人麻将）
-- 2. 可以同时支持系统注册用户和微信小程序用户
-- 3. 每个轮次记录每个参与者的得分变化
-- 4. 对局状态通过matches表的status字段控制
-- 5. 参与者退出状态通过match_participants表的is_quit字段控制
-- 6. 总分通过sum(score_change)计算或直接从total_score字段获取