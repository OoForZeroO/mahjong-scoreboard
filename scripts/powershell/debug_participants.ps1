# 调试批量参与者接口

Write-Host "Debugging batch participants interface..."

# 创建对局
$matchData = '{"roomName": "Debug Test Room", "totalRounds": 4}'
try {
    $matchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches" -Method POST -Body $matchData -ContentType "application/json"
    $matchId = $matchResult.data.matchId
    Write-Host "Match created with ID: $matchId"
} catch {
    Write-Host "Failed to create match: $($_.Exception.Message)"
    exit 1
}

# 测试单个参与者添加
Write-Host "`nTesting single participant addition..."
$singleParticipant = '{"user": null, "userName": "测试用户"}'
try {
    $singleResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants" -Method POST -Body $singleParticipant -ContentType "application/json"
    Write-Host "Single participant added successfully:"
    $singleResult.data | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Failed to add single participant: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody"
    }
}

# 测试批量参与者添加
Write-Host "`nTesting batch participants addition..."
$participantsData = '[{"user": null, "userName": "游客张三"}, {"user": null, "userName": "游客李四"}]'
try {
    $participantsResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants/batch" -Method POST -Body $participantsData -ContentType "application/json"
    Write-Host "Batch participants added successfully:"
    $participantsResult.data | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Failed to add batch participants: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody"
    }
}
