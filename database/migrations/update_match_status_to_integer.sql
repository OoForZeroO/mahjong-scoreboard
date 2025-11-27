-- 将对局状态字段从字符串改为整数
-- 执行时间: 2024年12月

-- 添加新的状态字段
ALTER TABLE matches 
ADD COLUMN status_new INTEGER DEFAULT 0;

-- 更新数据：将字符串状态转换为数字状态
UPDATE matches 
SET status_new = CASE 
    WHEN status = '进行中' THEN 0
    WHEN status = '已结束' OR status = '已完成' THEN 1
    ELSE 0
END;

-- 删除旧的状态字段
ALTER TABLE matches 
DROP COLUMN status;

-- 重命名新字段为status
ALTER TABLE matches 
RENAME COLUMN status_new TO status;

-- 设置非空约束
ALTER TABLE matches 
ALTER COLUMN status SET NOT NULL;

-- 添加字段注释
COMMENT ON COLUMN matches.status IS '对局状态: 0=进行中, 1=已完成';

-- 验证字段修改成功
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'matches' 
AND column_name = 'status';
