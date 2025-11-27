-- 为match_results表添加total_scores字段
-- 用于存储参与者得分信息的JSON数据

-- 1. 添加total_scores字段
ALTER TABLE match_results 
ADD COLUMN IF NOT EXISTS total_scores TEXT;

-- 2. 添加注释
COMMENT ON COLUMN match_results.total_scores IS '参与者得分信息JSON数据';

-- 3. 验证字段是否添加成功
SELECT column_name, data_type, is_nullable
FROM information_schema.columns 
WHERE table_name = 'match_results' 
AND column_name = 'total_scores';

-- 4. 查看完整的表结构
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'match_results'
ORDER BY ordinal_position;
