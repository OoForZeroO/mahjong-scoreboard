-- 修复外键约束，添加级联删除

-- 1. 删除现有的外键约束
ALTER TABLE match_participants DROP CONSTRAINT IF EXISTS match_participants_match_id_fkey;
ALTER TABLE round_scores DROP CONSTRAINT IF EXISTS round_scores_match_id_fkey;
ALTER TABLE round_scores DROP CONSTRAINT IF EXISTS round_scores_participant_id_fkey;

-- 2. 重新创建外键约束，支持级联删除
ALTER TABLE match_participants 
ADD CONSTRAINT match_participants_match_id_fkey 
FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE;

ALTER TABLE round_scores 
ADD CONSTRAINT round_scores_match_id_fkey 
FOREIGN KEY (match_id) REFERENCES matches(match_id) ON DELETE CASCADE;

ALTER TABLE round_scores 
ADD CONSTRAINT round_scores_participant_id_fkey 
FOREIGN KEY (participant_id) REFERENCES match_participants(id) ON DELETE CASCADE;

-- 3. 验证约束
SELECT 
    tc.table_name, 
    tc.constraint_name, 
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    rc.delete_rule
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
JOIN information_schema.referential_constraints AS rc
  ON tc.constraint_name = rc.constraint_name
  AND tc.table_schema = rc.constraint_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
  AND tc.table_name IN ('match_participants', 'round_scores')
ORDER BY tc.table_name, tc.constraint_name;
