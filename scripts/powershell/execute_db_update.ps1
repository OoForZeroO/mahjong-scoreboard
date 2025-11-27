# 执行数据库更新脚本

Write-Host "Executing database update to add round_time field..."

# 创建SQL命令
$sqlCommands = @"
-- 添加 round_time 字段到 round_scores 表
ALTER TABLE round_scores ADD COLUMN IF NOT EXISTS round_time BIGINT;
UPDATE round_scores SET round_time = create_time WHERE round_time IS NULL;
ALTER TABLE round_scores ALTER COLUMN round_time SET NOT NULL;
"@

Write-Host "SQL commands to execute:"
Write-Host $sqlCommands

Write-Host "`nPlease execute these SQL commands manually in your PostgreSQL database:"
Write-Host "1. Connect to your database"
Write-Host "2. Run the SQL commands above"
Write-Host "3. Restart the application"

# 尝试使用不同的方法执行SQL
try {
    # 方法1: 使用psql
    $env:PGPASSWORD = "postgres"  # 默认密码，请根据实际情况修改
    psql -h localhost -U postgres -d mahjong_score_system -c $sqlCommands
    Write-Host "Database updated successfully!"
} catch {
    Write-Host "Failed to execute via psql: $($_.Exception.Message)"
    Write-Host "Please execute the SQL commands manually."
}
