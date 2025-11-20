-- 为 match_participants 表添加头像字段
-- 执行时间: 2024年12月

-- 添加头像字段
ALTER TABLE match_participants 
ADD COLUMN avatar VARCHAR(500);

-- 添加字段注释
COMMENT ON COLUMN match_participants.avatar IS '参与者头像URL';

-- 验证字段添加成功
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns 
WHERE table_name = 'match_participants' 
AND column_name = 'avatar';
