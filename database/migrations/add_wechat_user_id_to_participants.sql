-- 为 match_participants 表添加 wechat_user_id 字段
-- 执行时间: 2024年12月

-- 添加 wechat_user_id 字段
ALTER TABLE match_participants 
ADD COLUMN wechat_user_id VARCHAR(100);

-- 添加字段注释
COMMENT ON COLUMN match_participants.wechat_user_id IS '微信用户ID，支持游客模式';

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_match_participants_wechat_user_id ON match_participants(wechat_user_id);

-- 验证字段添加成功
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns 
WHERE table_name = 'match_participants' 
AND column_name = 'wechat_user_id';
