-- 诊断和修复外键约束问题

-- 1. 检查当前的外键约束状态
SELECT 
    'Current foreign key constraints:' as info;

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
  AND tc.table_name = 'match_participants';

-- 2. 检查 wechat_users 表是否存在且有数据
SELECT 
    'WeChat users table status:' as info;

SELECT COUNT(*) as wechat_users_count FROM wechat_users;

-- 3. 检查 match_participants 表的当前数据
SELECT 
    'Current match_participants data:' as info;

SELECT 
    id, 
    match_id, 
    user_id, 
    user_name,
    CASE 
        WHEN user_id IS NULL THEN 'Guest'
        ELSE 'Registered User'
    END as user_type
FROM match_participants 
ORDER BY id DESC 
LIMIT 10;

-- 4. 强制删除所有相关的外键约束
ALTER TABLE match_participants DROP CONSTRAINT IF EXISTS match_participants_user_id_fkey;
ALTER TABLE match_participants DROP CONSTRAINT IF EXISTS match_participants_match_id_fkey;

-- 5. 重新创建正确的外键约束
-- 5.1 创建 match_id 外键约束
ALTER TABLE match_participants 
ADD CONSTRAINT match_participants_match_id_fkey 
FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE;

-- 5.2 创建 user_id 外键约束（引用 wechat_users）
ALTER TABLE match_participants 
ADD CONSTRAINT match_participants_user_id_fkey 
FOREIGN KEY (user_id) REFERENCES wechat_users(id) ON DELETE SET NULL;

-- 6. 验证修复结果
SELECT 
    'After fix - foreign key constraints:' as info;

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
  AND tc.table_name = 'match_participants';

-- 7. 测试插入游客数据
SELECT 
    'Testing guest data insertion:' as info;

-- 插入测试数据（游客模式）
INSERT INTO match_participants (match_id, user_id, user_name, total_score, create_time, update_time)
VALUES (1, NULL, '测试游客', 0, EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000)
ON CONFLICT DO NOTHING;

-- 8. 检查插入结果
SELECT 
    'Insertion test result:' as info;

SELECT 
    id, 
    match_id, 
    user_id, 
    user_name,
    CASE 
        WHEN user_id IS NULL THEN 'Guest - SUCCESS'
        ELSE 'Registered User'
    END as test_result
FROM match_participants 
WHERE user_name = '测试游客';

-- 9. 清理测试数据
DELETE FROM match_participants WHERE user_name = '测试游客';
