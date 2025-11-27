# 只测试游客模式的批量参与者接口

Write-Host "Testing guest-only batch participants..."

# 1. 创建测试对局
Write-Host "`n1. Creating test match..."
$matchData = '{"roomName": "Guest Only Test", "totalRounds": 4}'
try {
    $matchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches" -Method POST -Body $matchData -ContentType "application/json"
    $matchId = $matchResult.data.matchId
    Write-Host "✓ Match created with ID: $matchId"
} catch {
    Write-Host "✗ Failed to create match: $($_.Exception.Message)"
    exit 1
}

# 2. 测试游客模式批量添加参与者
Write-Host "`n2. Testing guest mode batch participants..."
$guestParticipantsData = '[{"user": null, "userName": "游客张三"}, {"user": null, "userName": "游客李四"}]'
try {
    $guestResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants/batch" -Method POST -Body $guestParticipantsData -ContentType "application/json"
    Write-Host "✓ Guest participants added successfully:"
    Write-Host "Count: $($guestResult.data.Count)"
    $guestResult.data | ForEach-Object {
        Write-Host "  - ID: $($_.id), Name: $($_.userName), User: $($_.user)"
    }
} catch {
    Write-Host "✗ Failed to add guest participants: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody"
    }
}

# 3. 获取参与者列表验证
Write-Host "`n3. Verifying participants..."
try {
    $allParticipants = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants" -Method GET
    Write-Host "✓ Retrieved $($allParticipants.data.Count) participants:"
    $allParticipants.data | ForEach-Object {
        Write-Host "  - ID: $($_.id), Name: $($_.userName), User: $($_.user)"
    }
} catch {
    Write-Host "✗ Failed to get participants: $($_.Exception.Message)"
}

Write-Host "`n=== Test completed ==="
