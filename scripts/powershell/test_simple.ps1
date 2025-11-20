# 简单测试批量插入参与者接口

Write-Host "Testing batch participants API..."

# 创建对局
$matchData = '{"roomName": "Test Room", "totalRounds": 4}'
try {
    $matchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches" -Method POST -Body $matchData -ContentType "application/json"
    $matchId = $matchResult.data.matchId
    Write-Host "Match created with ID: $matchId"
} catch {
    Write-Host "Failed to create match: $($_.Exception.Message)"
    exit 1
}

# 批量添加参与者
$participantsData = '[{"user": {"id": 101}, "userName": "Player1"}, {"user": null, "userName": "Guest"}, {"user": {"id": 102}, "userName": "Player2"}]'
try {
    $participantsResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants/batch" -Method POST -Body $participantsData -ContentType "application/json"
    Write-Host "Participants added successfully:"
    $participantsResult.data | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Failed to add participants: $($_.Exception.Message)"
}

# 获取参与者列表
try {
    $listResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants" -Method GET
    Write-Host "Participants list:"
    $listResult.data | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Failed to get participants list: $($_.Exception.Message)"
}
