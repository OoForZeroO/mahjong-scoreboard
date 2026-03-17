-- 为 users 表增加/校验后台登录所需字段。
-- 生产库如果已按最新结构创建，此脚本会通过 IF NOT EXISTS 保持幂等。

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS phone    VARCHAR(20),
    ADD COLUMN IF NOT EXISTS avatar   VARCHAR(500),
    ADD COLUMN IF NOT EXISTS create_time BIGINT,
    ADD COLUMN IF NOT EXISTS update_time BIGINT;

-- 为手机号增加唯一约束（若尚未存在）
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_users_phone'
    ) THEN
        ALTER TABLE users
            ADD CONSTRAINT uq_users_phone UNIQUE (phone);
    END IF;
END$$;

-- 补齐时间戳
UPDATE users
SET create_time = COALESCE(create_time, EXTRACT(EPOCH FROM NOW())::BIGINT * 1000),
    update_time = COALESCE(update_time, EXTRACT(EPOCH FROM NOW())::BIGINT * 1000);

