-- 手动删除对局的SQL脚本
-- 使用对局ID 37 作为示例

-- 1. 查看对局信息
SELECT * FROM matches WHERE match_id = 37;

-- 2. 查看参与者信息
SELECT * FROM match_participants WHERE match_id = 37;

-- 3. 查看轮次得分信息
SELECT * FROM round_scores WHERE match_id = 37;

-- 4. 按正确顺序删除记录
-- 4.1 删除轮次得分记录
DELETE FROM round_scores WHERE match_id = 37;

-- 4.2 删除参与者记录
DELETE FROM match_participants WHERE match_id = 37;

-- 4.3 删除对局结果记录（如果存在）
DELETE FROM match_results WHERE match_id = 37;

-- 4.4 删除对局结算记录（如果存在）
DELETE FROM match_settlements WHERE match_id = 37;

-- 4.5 最后删除对局记录
DELETE FROM matches WHERE match_id = 37;

-- 5. 验证删除结果
SELECT 'After deletion:' as status;
SELECT COUNT(*) as match_count FROM matches WHERE match_id = 37;
SELECT COUNT(*) as participant_count FROM match_participants WHERE match_id = 37;
SELECT COUNT(*) as round_score_count FROM round_scores WHERE match_id = 37;
