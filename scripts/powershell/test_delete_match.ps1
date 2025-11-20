# 测试删除对局功能

Write-Host "Testing delete match functionality..."

# 1. 创建测试对局
Write-Host "`n1. Creating test match..."
$matchData = '{"roomName": "Delete Test Room", "totalRounds": 4}'
try {
    $matchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches" -Method POST -Body $matchData -ContentType "application/json"
    $matchId = $matchResult.data.matchId
    Write-Host "✓ Match created with ID: $matchId"
} catch {
    Write-Host "✗ Failed to create match: $($_.Exception.Message)"
    exit 1
}

# 2. 添加参与者
Write-Host "`n2. Adding participants..."
$participantsData = '[{"user": null, "userName": "测试用户1"}, {"user": null, "userName": "测试用户2"}]'
try {
    $participantsResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants/batch" -Method POST -Body $participantsData -ContentType "application/json"
    Write-Host "✓ Participants added: $($participantsResult.data.Count)"
} catch {
    Write-Host "✗ Failed to add participants: $($_.Exception.Message)"
}

# 3. 验证参与者存在
Write-Host "`n3. Verifying participants exist..."
try {
    $participantsList = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants" -Method GET
    Write-Host "✓ Found $($participantsList.data.Count) participants"
} catch {
    Write-Host "✗ Failed to get participants: $($_.Exception.Message)"
}

# 4. 尝试删除对局
Write-Host "`n4. Attempting to delete match..."
try {
    $deleteResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId" -Method DELETE
    Write-Host "✓ Match deleted successfully"
    Write-Host "Response: $($deleteResult | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "✗ Failed to delete match: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody"
    }
}

# 5. 验证对局已删除
Write-Host "`n5. Verifying match is deleted..."
try {
    $matchCheck = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId" -Method GET
    Write-Host "✗ Match still exists (unexpected)"
} catch {
    Write-Host "✓ Match successfully deleted (404 expected)"
}

Write-Host "`n=== Test completed ==="
