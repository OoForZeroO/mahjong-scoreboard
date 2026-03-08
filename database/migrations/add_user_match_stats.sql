-- 用户对局结果汇总表：按「用户 + 对局」存一条结果，便于按用户+时间范围做统计
-- 与 match_results 同时在对局结束时写入，统计查询只读本表，无需解析 JSON

-- 1. 建表（依赖 wechat_users.id；若你库中用户主表不同，请改 FK）
CREATE TABLE IF NOT EXISTS user_match_stats (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    match_id BIGINT NOT NULL,
    participant_id BIGINT,
    total_score INTEGER NOT NULL,
    final_score INTEGER NOT NULL,
    settlement_multiplier DOUBLE PRECISION NOT NULL DEFAULT 1,
    is_winner BOOLEAN NOT NULL DEFAULT FALSE,
    match_end_time BIGINT NOT NULL,
    create_time BIGINT NOT NULL,
    CONSTRAINT uq_user_match_stats_user_match UNIQUE (user_id, match_id),
    CONSTRAINT fk_user_match_stats_user FOREIGN KEY (user_id) REFERENCES wechat_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_match_stats_match FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_match_stats_participant FOREIGN KEY (participant_id) REFERENCES match_participants(id) ON DELETE SET NULL
);

COMMENT ON TABLE user_match_stats IS '用户对局结果汇总：每局每用户一条，用于按用户+时间范围统计';
COMMENT ON COLUMN user_match_stats.user_id IS 'wechat_users.id，与接口 wechatUserId 一致';
COMMENT ON COLUMN user_match_stats.match_end_time IS '对局结束时间戳，用于时间范围查询';

CREATE INDEX IF NOT EXISTS idx_user_match_stats_user_end_time
ON user_match_stats (user_id, match_end_time);

CREATE INDEX IF NOT EXISTS idx_user_match_stats_match_id
ON user_match_stats (match_id);

-- 2. 可选：月度预聚合表（「本月数据」可查单行）
CREATE TABLE IF NOT EXISTS user_monthly_stats (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    year_month INTEGER NOT NULL,
    total_matches INTEGER NOT NULL DEFAULT 0,
    win_matches INTEGER NOT NULL DEFAULT 0,
    lose_matches INTEGER NOT NULL DEFAULT 0,
    total_score INTEGER NOT NULL DEFAULT 0,
    total_multiplier_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    win_total_score INTEGER NOT NULL DEFAULT 0,
    win_total_multiplier_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    lose_total_score INTEGER NOT NULL DEFAULT 0,
    lose_total_multiplier_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    CONSTRAINT uq_user_monthly_stats_user_ym UNIQUE (user_id, year_month),
    CONSTRAINT fk_user_monthly_stats_user FOREIGN KEY (user_id) REFERENCES wechat_users(id) ON DELETE CASCADE
);

COMMENT ON TABLE user_monthly_stats IS '用户月度统计预聚合：按 user_id + year_month 更新，便于本月/本年快速查询';
COMMENT ON COLUMN user_monthly_stats.year_month IS '年月，格式 YYYYMM，如 202503';

CREATE INDEX IF NOT EXISTS idx_user_monthly_stats_user_ym
ON user_monthly_stats (user_id, year_month);
