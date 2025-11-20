-- 删除旧表（如果存在）
DROP TABLE IF EXISTS score_records CASCADE;
DROP TABLE IF EXISTS rooms CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 创建用户表
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'user',
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    avatar VARCHAR(500),
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

-- 创建棋牌室表
CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    logo VARCHAR(500),
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL
);

-- 创建计分记录表
CREATE TABLE score_records (
    match_id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    rounds INTEGER NOT NULL,
    score INTEGER NOT NULL,
    total_score INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_status VARCHAR(100),
    room_name VARCHAR(100) NOT NULL,
    room_id BIGINT NOT NULL,
    create_time BIGINT NOT NULL,
    update_time BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_score_records_user_id ON score_records(user_id);
CREATE INDEX IF NOT EXISTS idx_score_records_room_id ON score_records(room_id);

-- 插入测试数据
INSERT INTO users (username, phone, email, password, role, status, avatar, create_time, update_time)
VALUES ('测试用户', '13800138000', 'test@example.com', '123456', 'user', 'active', 'test.jpg', extract(epoch from now())::bigint, extract(epoch from now())::bigint);

-- 验证表创建
SELECT 'Users table created' AS status FROM users LIMIT 1;