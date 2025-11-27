-- 为wechat_users表添加is_visitor字段的SQL迁移脚本
-- 执行日期: 2024
-- 说明: 在wechat_users表中添加is_visitor字段，用于标识是否为游客用户

-- 检查并添加is_visitor字段
DO $$ 
BEGIN
    -- 检查字段是否已存在
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'wechat_users' 
        AND column_name = 'is_visitor'
    ) THEN
        -- 添加is_visitor字段
        ALTER TABLE wechat_users 
        ADD COLUMN is_visitor BOOLEAN NOT NULL DEFAULT FALSE;
        
        RAISE NOTICE '字段 is_visitor 已成功添加到 wechat_users 表';
    ELSE
        RAISE NOTICE '字段 is_visitor 已存在于 wechat_users 表中';
    END IF;
END $$;

-- 验证字段添加结果
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM information_schema.columns
WHERE table_name = 'wechat_users' 
AND column_name = 'is_visitor';
