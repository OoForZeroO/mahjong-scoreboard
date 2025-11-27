-- 简化版外键约束修复脚本

-- 1. 删除错误的外键约束
ALTER TABLE match_participants DROP CONSTRAINT IF EXISTS match_participants_user_id_fkey;

-- 2. 创建正确的外键约束，引用 wechat_users 表
ALTER TABLE match_participants 
ADD CONSTRAINT match_participants_user_id_fkey 
FOREIGN KEY (user_id) REFERENCES wechat_users(id) ON DELETE SET NULL;

-- 3. 验证修复结果
SELECT 
    'Foreign key constraint fixed successfully' as status,
    'user_id now references wechat_users(id)' as description;
