-- 麻将对局记录表结构设计（优化版）
-- 支持多轮计分、倍率计算和状态管理

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
    settlement_multiplier DECIMAL(10,2),  -- 收盘倍率
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 2. 对局参与者表 - 记录对局与用户的关系及基本信息
CREATE TABLE IF NOT EXISTS match_participants (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,     -- 对局ID
    user_id BIGINT,              -- 系统用户ID
    nickname VARCHAR(100) NOT NULL,  -- 用户昵称
    avatar VARCHAR(500),         -- 用户头像
    total_score INTEGER NOT NULL DEFAULT 0,  -- 总分（未乘倍率前）
    final_score DECIMAL(20,2),   -- 最终分数（乘以倍率后）
    is_quit BOOLEAN NOT NULL DEFAULT FALSE,  -- 是否已退出对局
    quit_time BIGINT,            -- 退出时间（未退出时为null）
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(match_id, COALESCE(user_id, nickname))  -- 确保一个对局中用户唯一
);

-- 3. 轮次记录表 - 记录每一轮的得分情况
CREATE TABLE IF NOT EXISTS round_scores (
    id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,     -- 对局ID
    participant_id BIGINT NOT NULL,  -- 参与者ID
    round_number INTEGER NOT NULL,  -- 轮次号
    score INTEGER NOT NULL,       -- 本轮得分
    cumulative_score INTEGER NOT NULL,  -- 累计得分（未乘倍率）
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (participant_id) REFERENCES match_participants(id),
    UNIQUE(match_id, participant_id, round_number)  -- 确保每个参与者每轮只有一条记录
);

-- 4. 对局结算表 - 记录对局结算信息
CREATE TABLE IF NOT EXISTS match_settlements (
    settlement_id SERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL,     -- 对局ID
    multiplier DECIMAL(10,2) NOT NULL,  -- 收盘倍率
    settlement_time BIGINT NOT NULL,  -- 结算时间
    notes TEXT,                  -- 结算备注
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    UNIQUE(match_id)  -- 确保每个对局只有一条结算记录
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_matches_room_id ON matches(room_id);
CREATE INDEX IF NOT EXISTS idx_matches_status ON matches(status);
CREATE INDEX IF NOT EXISTS idx_matches_start_time ON matches(start_time);

CREATE INDEX IF NOT EXISTS idx_match_participants_match_id ON match_participants(match_id);
CREATE INDEX IF NOT EXISTS idx_match_participants_user_id ON match_participants(user_id);
CREATE INDEX IF NOT EXISTS idx_match_participants_is_quit ON match_participants(is_quit);

CREATE INDEX IF NOT EXISTS idx_round_scores_match_id ON round_scores(match_id);
CREATE INDEX IF NOT EXISTS idx_round_scores_participant_id ON round_scores(participant_id);
CREATE INDEX IF NOT EXISTS idx_round_scores_round_number ON round_scores(round_number);
CREATE INDEX IF NOT EXISTS idx_round_scores_match_round ON round_scores(match_id, round_number);

CREATE INDEX IF NOT EXISTS idx_match_settlements_match_id ON match_settlements(match_id);

-- 创建触发器函数 - 更新对局的总轮次数
CREATE OR REPLACE FUNCTION update_match_total_rounds() RETURNS TRIGGER AS $$
BEGIN
    UPDATE matches
    SET total_rounds = GREATEST(COALESCE(total_rounds, 0), NEW.round_number),
        current_round = GREATEST(COALESCE(current_round, 0), NEW.round_number),
        update_time = EXTRACT(EPOCH FROM NOW())::bigint
    WHERE match_id = NEW.match_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器 - 在插入轮次记录时更新对局总轮次
CREATE TRIGGER trg_update_match_total_rounds
AFTER INSERT ON round_scores
FOR EACH ROW
EXECUTE FUNCTION update_match_total_rounds();

-- 创建触发器函数 - 计算参与者的总分
CREATE OR REPLACE FUNCTION update_participant_total_score() RETURNS TRIGGER AS $$
BEGIN
    -- 更新参与者的总分
    UPDATE match_participants
    SET total_score = (SELECT SUM(score) FROM round_scores WHERE participant_id = NEW.participant_id),
        update_time = EXTRACT(EPOCH FROM NOW())::bigint
    WHERE id = NEW.participant_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器 - 在插入或更新轮次记录时更新参与者总分
CREATE TRIGGER trg_update_participant_total_score
AFTER INSERT OR UPDATE ON round_scores
FOR EACH ROW
EXECUTE FUNCTION update_participant_total_score();

-- 创建触发器函数 - 收盘时更新最终分数
CREATE OR REPLACE FUNCTION update_final_scores() RETURNS TRIGGER AS $$
BEGIN
    -- 更新对局状态和倍率
    UPDATE matches
    SET status = '已完成',
        end_time = NEW.settlement_time,
        settlement_multiplier = NEW.multiplier,
        update_time = EXTRACT(EPOCH FROM NOW())::bigint
    WHERE match_id = NEW.match_id;
    
    -- 更新所有参与者的最终分数
    UPDATE match_participants
    SET final_score = total_score * NEW.multiplier,
        update_time = EXTRACT(EPOCH FROM NOW())::bigint
    WHERE match_id = NEW.match_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 创建触发器 - 在插入结算记录时更新最终分数
CREATE TRIGGER trg_update_final_scores
AFTER INSERT ON match_settlements
FOR EACH ROW
EXECUTE FUNCTION update_final_scores();

-- 创建视图 - 获取对局详情（包含参与者信息）
CREATE OR REPLACE VIEW v_match_details AS
SELECT 
    m.match_id,
    m.room_id,
    m.room_name,
    m.start_time,
    m.end_time,
    m.status,
    m.total_rounds,
    m.current_round,
    m.settlement_multiplier,
    COUNT(mp.id) as participant_count,
    SUM(mp.total_score) as total_match_score
FROM matches m
LEFT JOIN match_participants mp ON m.match_id = mp.match_id
GROUP BY m.match_id;

-- 创建视图 - 获取对局参与者详情
CREATE OR REPLACE VIEW v_match_participant_details AS
SELECT 
    mp.id as participant_record_id,
    m.match_id,
    m.room_name,
    m.status,
    m.settlement_multiplier,
    mp.user_id,
    COALESCE(u.username, mp.nickname) as participant_name,
    mp.avatar,
    mp.total_score,
    mp.final_score,
    mp.is_quit,
    mp.quit_time
FROM match_participants mp
JOIN matches m ON mp.match_id = m.match_id
LEFT JOIN users u ON mp.user_id = u.id;

-- 创建视图 - 获取轮次得分详情
CREATE OR REPLACE VIEW v_round_score_details AS
SELECT 
    rs.id,
    m.match_id,
    m.room_name,
    m.status,
    rs.round_number,
    mp.id as participant_record_id,
    COALESCE(u.username, mp.nickname) as participant_name,
    rs.score,
    rs.cumulative_score,
    rs.create_time
FROM round_scores rs
JOIN matches m ON rs.match_id = m.match_id
JOIN match_participants mp ON rs.participant_id = mp.id
LEFT JOIN users u ON mp.user_id = u.id
ORDER BY m.match_id, rs.round_number, rs.id;

-- 示例使用流程说明
-- 1. 创建对局
-- INSERT INTO matches (room_id, room_name, start_time, create_time, update_time) VALUES (...);

-- 2. 添加参与者
-- INSERT INTO match_participants (match_id, user_id, nickname, avatar, create_time, update_time) VALUES (...);

-- 3. 记录第一轮得分
-- INSERT INTO round_scores (match_id, participant_id, round_number, score, cumulative_score, create_time, update_time) VALUES (...);

-- 4. 记录第二轮得分（以此类推）

-- 5. 收盘并设置倍率
-- INSERT INTO match_settlements (match_id, multiplier, settlement_time, create_time, update_time) VALUES (...);

-- 查询示例
-- 1. 查询所有进行中的对局
-- SELECT * FROM v_match_details WHERE status = '进行中';

-- 2. 查询指定对局的参与者详情
-- SELECT * FROM v_match_participant_details WHERE match_id = ?;

-- 3. 查询指定对局的轮次得分详情
-- SELECT * FROM v_round_score_details WHERE match_id = ? ORDER BY round_number;

-- 4. 查询所有已完成并结算的对局
-- SELECT * FROM v_match_details WHERE status = '已完成' AND settlement_multiplier IS NOT NULL;