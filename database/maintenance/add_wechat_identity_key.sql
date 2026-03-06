-- 为 wechat_users 表增加基于 WeChat 身份的统一唯一键 identity_key
-- 规则：
--   有 unionid: identity_key = 'unionid:' || unionid
--   否则:       identity_key = 'openid:' || app_id || ':' || openid

ALTER TABLE wechat_users
    ADD COLUMN IF NOT EXISTS app_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS openid VARCHAR(100),
    ADD COLUMN IF NOT EXISTS unionid VARCHAR(100),
    ADD COLUMN IF NOT EXISTS identity_key VARCHAR(200),
    ADD COLUMN IF NOT EXISTS last_login_at BIGINT;

-- 为 identity_key 建唯一索引，确保同一微信身份只对应一条记录
CREATE UNIQUE INDEX IF NOT EXISTS wechat_users_identity_key_uk
    ON wechat_users(identity_key);

