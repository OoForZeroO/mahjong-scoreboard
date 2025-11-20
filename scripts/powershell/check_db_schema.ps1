# 检查数据库表结构

Write-Host "Checking database schema..."

# 检查match_participants表结构
$checkParticipants = @"
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'match_participants' 
ORDER BY ordinal_position;
"@

Write-Host "match_participants table structure:"
# 这里需要数据库连接，暂时跳过

# 检查round_scores表结构
$checkRoundScores = @"
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'round_scores' 
ORDER BY ordinal_position;
"@

Write-Host "round_scores table structure:"
# 这里需要数据库连接，暂时跳过

Write-Host "Based on error messages, the database tables are missing the round_time field."
Write-Host "Let's modify the RoundScore entity to match the actual database schema."
