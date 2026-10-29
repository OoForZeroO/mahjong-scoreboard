-- 用户隐藏对局：只从该用户的历史列表中去掉，不删除对局、分数和统计
CREATE TABLE IF NOT EXISTS user_hidden_matches (
    id BIGSERIAL PRIMARY KEY,
    user_key VARCHAR(100) NOT NULL,
    match_id BIGINT NOT NULL,
    create_time BIGINT,
    CONSTRAINT uq_user_hidden_matches_user_match UNIQUE (user_key, match_id)
);

COMMENT ON TABLE user_hidden_matches IS '用户自己隐藏的对局，不影响其他参与者的记录和计分';
COMMENT ON COLUMN user_hidden_matches.user_key IS '与历史列表接口的 wechat_user_id 相同';

CREATE INDEX IF NOT EXISTS idx_user_hidden_matches_user_key
ON user_hidden_matches (user_key);
