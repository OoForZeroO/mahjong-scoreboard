# 测试简单删除对局功能

Write-Host "Testing simple delete match functionality..."

# 1. 创建测试对局（不添加参与者）
Write-Host "`n1. Creating test match without participants..."
$matchData = '{"roomName": "Simple Delete Test", "totalRounds": 4}'
try {
    $matchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches" -Method POST -Body $matchData -ContentType "application/json"
    $matchId = $matchResult.data.matchId
    Write-Host "✓ Match created with ID: $matchId"
} catch {
    Write-Host "✗ Failed to create match: $($_.Exception.Message)"
    exit 1
}

# 2. 尝试删除对局
Write-Host "`n2. Attempting to delete match..."
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

# 3. 验证对局已删除
Write-Host "`n3. Verifying match is deleted..."
try {
    $matchCheck = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId" -Method GET
    Write-Host "✗ Match still exists (unexpected)"
} catch {
    Write-Host "✓ Match successfully deleted (404 expected)"
}

Write-Host "`n=== Test completed ==="
