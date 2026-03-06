-- 清空 wechat_users 表
-- 说明：match_participants.user_id 外键为 ON DELETE SET NULL，执行后对局参与者的 user_id 会被置为 NULL，wechat_user_id 列不受影响

DELETE FROM wechat_users;
