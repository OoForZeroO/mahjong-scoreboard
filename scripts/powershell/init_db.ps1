# 数据库初始化PowerShell脚本
Write-Host "开始初始化PostgreSQL数据库..."

# 设置参数
$DB_NAME = "mahjong_db"
$DB_USER = "postgres"
$DB_PASSWORD = "cch815566"

# 设置密码环境变量
$env:PGPASSWORD = $DB_PASSWORD

# 检查psql是否可用
if (-not (Get-Command "psql" -ErrorAction SilentlyContinue)) {
    Write-Host "错误：未找到psql命令，请确保PostgreSQL客户端已安装并添加到PATH"
    exit 1
}

# 创建数据库
try {
    Write-Host "正在创建数据库 $DB_NAME..."
    psql -U $DB_USER -c "CREATE DATABASE $DB_NAME;"
} 
catch {
    Write-Host "数据库可能已存在，继续下一步..."
}

# 直接在PowerShell中执行SQL语句
Write-Host "正在创建表结构..."

# 删除旧表（如果存在）
psql -U $DB_USER -d $DB_NAME -c "DROP TABLE IF EXISTS score_records CASCADE;"
psql -U $DB_USER -d $DB_NAME -c "DROP TABLE IF EXISTS rooms CASCADE;"
psql -U $DB_USER -d $DB_NAME -c "DROP TABLE IF EXISTS users CASCADE;"

# 创建用户表
psql -U $DB_USER -d $DB_NAME -c "CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, username VARCHAR(100) NOT NULL, phone VARCHAR(20) NOT NULL UNIQUE, email VARCHAR(255), password VARCHAR(255) NOT NULL, role VARCHAR(50) NOT NULL DEFAULT 'user', status VARCHAR(20) NOT NULL DEFAULT 'active', avatar VARCHAR(500), create_time BIGINT NOT NULL, update_time BIGINT NOT NULL);"

# 创建棋牌室表
psql -U $DB_USER -d $DB_NAME -c "CREATE TABLE IF NOT EXISTS rooms (id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL UNIQUE, logo VARCHAR(500), create_time BIGINT NOT NULL, update_time BIGINT NOT NULL);"

# 创建计分记录表
psql -U $DB_USER -d $DB_NAME -c "CREATE TABLE IF NOT EXISTS score_records (match_id SERIAL PRIMARY KEY, user_id BIGINT NOT NULL, rounds INTEGER NOT NULL, score INTEGER NOT NULL, total_score INTEGER NOT NULL, status VARCHAR(20) NOT NULL, user_status VARCHAR(100), room_name VARCHAR(100) NOT NULL, room_id BIGINT NOT NULL, create_time BIGINT NOT NULL, update_time BIGINT NOT NULL, FOREIGN KEY (user_id) REFERENCES users(id), FOREIGN KEY (room_id) REFERENCES rooms(id));"

# 创建索引
psql -U $DB_USER -d $DB_NAME -c "CREATE INDEX IF NOT EXISTS idx_score_records_user_id ON score_records(user_id);"
psql -U $DB_USER -d $DB_NAME -c "CREATE INDEX IF NOT EXISTS idx_score_records_room_id ON score_records(room_id);"

# 插入测试数据
psql -U $DB_USER -d $DB_NAME -c "INSERT INTO users (username, phone, email, password, role, status, avatar, create_time, update_time) VALUES ('测试用户', '13800138000', 'test@example.com', '123456', 'user', 'active', 'test.jpg', extract(epoch from now())::bigint, extract(epoch from now())::bigint) ON CONFLICT (phone) DO NOTHING;"

Write-Host "数据库初始化完成！请验证表是否成功创建。"

# 清除密码环境变量
Remove-Item Env:\PGPASSWORD