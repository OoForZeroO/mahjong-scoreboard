-- 简单的数据库更新脚本
-- 添加 round_time 字段到 round_scores 表

-- 添加字段
ALTER TABLE round_scores ADD COLUMN round_time BIGINT;

-- 设置默认值
UPDATE round_scores SET round_time = create_time;

-- 设置为非空
ALTER TABLE round_scores ALTER COLUMN round_time SET NOT NULL;
