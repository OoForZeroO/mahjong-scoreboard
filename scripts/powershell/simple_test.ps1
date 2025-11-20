# 简单测试批量接口

Write-Host "Testing batch interfaces..."

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

# 测试批量添加参与者
$participantsData = '[{"user": {"id": 101}, "userName": "Player1"}, {"user": null, "userName": "Guest"}]'
try {
    $participantsResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants/batch" -Method POST -Body $participantsData -ContentType "application/json"
    Write-Host "Participants added successfully:"
    $participantsResult.data | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Failed to add participants: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody"
    }
}

# 测试批量轮次得分
$roundScoresData = '[{"match": {"matchId": 1}, "participant": {"id": 1}, "roundNumber": 1, "score": 100}]'
try {
    $roundScoresResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/rounds/batch" -Method POST -Body $roundScoresData -ContentType "application/json"
    Write-Host "Round scores created successfully:"
    $roundScoresResult.data | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Failed to create round scores: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody"
    }
}
