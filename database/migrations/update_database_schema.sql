-- 数据库表结构更新SQL
-- 根据麻将对局数据库表结构设计文档更新

-- 1. 更新matches表
-- 移除room_id字段（如果不支持ALTER TABLE DROP COLUMN，需要先备份数据再重建表）
-- ALTER TABLE matches DROP COLUMN IF EXISTS room_id;
ALTER TABLE matches ALTER COLUMN room_name DROP NOT NULL;
ALTER TABLE matches DROP COLUMN IF EXISTS current_round;

-- 2. 更新match_participants表
-- 重命名并更新user_id字段
ALTER TABLE match_participants DROP CONSTRAINT IF EXISTS match_participants_user_id_fkey;
ALTER TABLE match_participants DROP CONSTRAINT IF EXISTS match_participants_match_id_fkey;

-- 添加新字段
ALTER TABLE match_participants RENAME COLUMN id TO participant_id;
ALTER TABLE match_participants ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE match_participants ADD COLUMN IF NOT EXISTS user_name VARCHAR(100) NOT NULL;

-- 迁移数据（将nickname数据迁移到user_name）
UPDATE match_participants SET user_name = nickname WHERE user_name IS NULL;

-- 删除旧字段
ALTER TABLE match_participants DROP COLUMN IF EXISTS nickname;
ALTER TABLE match_participants DROP COLUMN IF EXISTS avatar;
ALTER TABLE match_participants DROP COLUMN IF EXISTS wechat_user_id;
ALTER TABLE match_participants DROP COLUMN IF EXISTS final_score;
ALTER TABLE match_participants DROP COLUMN IF EXISTS is_quit;
ALTER TABLE match_participants DROP COLUMN IF EXISTS quit_time;

-- 添加外键约束
ALTER TABLE match_participants 
    ADD CONSTRAINT fk_match_participants_match_id 
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE;

ALTER TABLE match_participants 
    ADD CONSTRAINT fk_match_participants_user_id 
    FOREIGN KEY (user_id) REFERENCES wechat_users(id) ON DELETE SET NULL;

-- 3. 更新round_scores表
ALTER TABLE round_scores RENAME COLUMN id TO score_id;
ALTER TABLE round_scores ADD COLUMN IF NOT EXISTS round_time BIGINT NOT NULL DEFAULT EXTRACT(EPOCH FROM NOW()) * 1000;

-- 迁移数据（将create_time赋值给round_time）
UPDATE round_scores SET round_time = create_time WHERE round_time IS NULL;

-- 4. 更新match_results表
-- 添加result_id主键
ALTER TABLE match_results DROP CONSTRAINT IF EXISTS match_results_pkey;
ALTER TABLE match_results DROP CONSTRAINT IF EXISTS match_results_match_id_fkey;
ALTER TABLE match_results DROP CONSTRAINT IF EXISTS match_results_winner_id_fkey;

-- 添加result_id字段
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS result_id BIGSERIAL PRIMARY KEY;

-- 更新match_id字段
ALTER TABLE match_results ALTER COLUMN match_id SET NOT NULL;
ALTER TABLE match_results ADD CONSTRAINT uk_match_results_match_id UNIQUE (match_id);

-- 添加total_duration字段
ALTER TABLE match_results ADD COLUMN IF NOT EXISTS total_duration BIGINT;

-- 添加外键约束
ALTER TABLE match_results 
    ADD CONSTRAINT fk_match_results_match_id 
    FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE;

ALTER TABLE match_results 
    ADD CONSTRAINT fk_match_results_winner_id 
    FOREIGN KEY (winner_id) REFERENCES match_participants(participant_id) ON DELETE SET NULL;

-- 5. 添加索引
CREATE INDEX IF NOT EXISTS idx_matches_status ON matches(status);
CREATE INDEX IF NOT EXISTS idx_matches_start_time ON matches(start_time);
CREATE INDEX IF NOT EXISTS idx_matches_create_time ON matches(create_time);

CREATE INDEX IF NOT EXISTS idx_match_participants_match_id ON match_participants(match_id);
CREATE INDEX IF NOT EXISTS idx_match_participants_user_id ON match_participants(user_id);

CREATE INDEX IF NOT EXISTS idx_round_scores_match_id ON round_scores(match_id);
CREATE INDEX IF NOT EXISTS idx_round_scores_participant_id ON round_scores(participant_id);
CREATE INDEX IF NOT EXISTS idx_round_scores_round_number ON round_scores(round_number);

CREATE INDEX IF NOT EXISTS idx_match_results_winner_id ON match_results(winner_id);

-- 完成
SELECT '数据库表结构更新完成' as status;
