-- 修复 match_participants 表的外键约束
-- 将 user_id 外键从 users 表改为 wechat_users 表

-- 1. 查看当前的外键约束
SELECT 
    tc.table_name, 
    tc.constraint_name, 
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
  AND tc.table_name = 'match_participants'
  AND kcu.column_name = 'user_id';

-- 2. 删除错误的外键约束
ALTER TABLE match_participants DROP CONSTRAINT IF EXISTS match_participants_user_id_fkey;

-- 3. 创建正确的外键约束，引用 wechat_users 表
ALTER TABLE match_participants 
ADD CONSTRAINT match_participants_user_id_fkey 
FOREIGN KEY (user_id) REFERENCES wechat_users(id) ON DELETE SET NULL;

-- 4. 验证新的外键约束
SELECT 
    tc.table_name, 
    tc.constraint_name, 
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
  AND tc.table_name = 'match_participants'
  AND kcu.column_name = 'user_id';

-- 5. 检查 wechat_users 表结构
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'wechat_users' 
ORDER BY ordinal_position;

-- 6. 检查 match_participants 表结构
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'match_participants' 
ORDER BY ordinal_position;
