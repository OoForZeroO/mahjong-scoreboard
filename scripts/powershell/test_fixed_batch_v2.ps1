# 修复后的批量接口测试 v2

Write-Host "Testing fixed batch interfaces v2..."

# 创建对局
$matchData = '{"roomName": "Fixed Test Room", "totalRounds": 4}'
try {
    $matchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches" -Method POST -Body $matchData -ContentType "application/json"
    $matchId = $matchResult.data.matchId
    Write-Host "Match created with ID: $matchId"
} catch {
    Write-Host "Failed to create match: $($_.Exception.Message)"
    exit 1
}

# 测试批量添加参与者 - 使用null用户ID（游客模式）
$participantsData = '[{"user": null, "userName": "游客张三"}, {"user": null, "userName": "游客李四"}]'
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

# 获取参与者列表以获取正确的ID
try {
    $participantsList = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants" -Method GET
    Write-Host "Participants list:"
    $participantsList.data | ConvertTo-Json -Depth 3
    
    # 使用第一个参与者的ID测试轮次得分
    if ($participantsList.data.Count -gt 0) {
        $participantId = $participantsList.data[0].id
        Write-Host "Using participant ID: $participantId"
        
        # 测试批量轮次得分
        $roundScoresData = "[{\"match\": {\"matchId\": $matchId}, \"participant\": {\"id\": $participantId}, \"roundNumber\": 1, \"score\": 100, \"roundTime\": 1716700800000}]"
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
    }
} catch {
    Write-Host "Failed to get participants list: $($_.Exception.Message)"
}
