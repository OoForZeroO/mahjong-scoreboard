# 执行SQL脚本添加round_time字段

Write-Host "Adding round_time field to round_scores table..."

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$sqlFile = Join-Path $scriptRoot "..\..\database\migrations\add_round_time_field.sql"

# 尝试使用不同的方式连接数据库
# 方法1: 使用psql命令（如果可用）
try {
    $env:PGPASSWORD = "your_password"  # 替换为实际密码
    psql -h localhost -U postgres -d mahjong_score_system -f $sqlFile
    Write-Host "SQL script executed successfully using psql"
} catch {
    Write-Host "psql not available, trying alternative method..."
    
    # 方法2: 使用.NET连接
    try {
        Add-Type -AssemblyName System.Data
        $connectionString = "Host=localhost;Database=mahjong_score_system;Username=postgres;Password=your_password"  # 替换为实际密码
        
        $connection = New-Object System.Data.Odbc.OdbcConnection($connectionString)
        $connection.Open()
        
        $sql = @"
ALTER TABLE round_scores ADD COLUMN IF NOT EXISTS round_time BIGINT;
UPDATE round_scores SET round_time = create_time WHERE round_time IS NULL;
ALTER TABLE round_scores ALTER COLUMN round_time SET NOT NULL;
CREATE INDEX IF NOT EXISTS idx_round_scores_round_time ON round_scores(round_time);
"@
        
        $command = New-Object System.Data.Odbc.OdbcCommand($sql, $connection)
        $command.ExecuteNonQuery()
        
        $connection.Close()
        Write-Host "SQL script executed successfully using .NET"
    } catch {
        Write-Host "Failed to execute SQL script: $($_.Exception.Message)"
        Write-Host "Please manually execute the SQL script: add_round_time_field.sql"
    }
}
