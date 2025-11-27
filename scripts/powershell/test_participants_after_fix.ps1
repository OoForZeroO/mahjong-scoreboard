# 修复外键约束后的批量参与者接口测试

Write-Host "Testing batch participants after foreign key fix..."

# 1. 创建测试对局
Write-Host "`n1. Creating test match..."
$matchData = '{"roomName": "Foreign Key Fix Test", "totalRounds": 4}'
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
    $guestResult.data | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed to add guest participants: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody"
    }
}

# 3. 测试注册用户模式（需要先创建微信用户）
Write-Host "`n3. Testing registered user mode..."
# 先创建一些测试微信用户
$wechatUserData = '{"userId": "test_user_001", "nickname": "测试用户1", "username": "testuser1"}'
try {
    $wechatUserResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/wechat-users" -Method POST -Body $wechatUserData -ContentType "application/json"
    $wechatUserId = $wechatUserResult.data.id
    Write-Host "✓ WeChat user created with ID: $wechatUserId"
    
    # 使用创建的微信用户ID添加参与者
    $registeredParticipantsData = "[{\"user\": {\"id\": $wechatUserId}, \"userName\": \"注册用户1\"}]"
    try {
        $registeredResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants/batch" -Method POST -Body $registeredParticipantsData -ContentType "application/json"
        Write-Host "✓ Registered user participants added successfully:"
        $registeredResult.data | ConvertTo-Json -Depth 3
    } catch {
        Write-Host "✗ Failed to add registered user participants: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $responseBody = $reader.ReadToEnd()
            Write-Host "Error response: $responseBody"
        }
    }
} catch {
    Write-Host "✗ Failed to create WeChat user: $($_.Exception.Message)"
}

# 4. 获取所有参与者列表
Write-Host "`n4. Getting all participants..."
try {
    $allParticipants = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants" -Method GET
    Write-Host "✓ All participants retrieved:"
    $allParticipants.data | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Failed to get participants: $($_.Exception.Message)"
}

Write-Host "`n=== Test completed ==="
