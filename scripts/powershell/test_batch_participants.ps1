# 测试批量插入参与者接口

Write-Host "=== 麻将计分系统 - 批量参与者接口测试 ===" -ForegroundColor Green

# 1. 创建测试对局
Write-Host "`n1. 创建测试对局..." -ForegroundColor Yellow
$matchBody = @{
    roomName = "测试棋牌室"
    totalRounds = 4
} | ConvertTo-Json

try {
    $matchResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches" -Method POST -Body $matchBody -ContentType "application/json"
    $matchId = $matchResponse.data.matchId
    Write-Host "✓ 对局创建成功，ID: $matchId" -ForegroundColor Green
    Write-Host "对局信息: $($matchResponse.data | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "✗ 对局创建失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. 测试批量添加参与者
Write-Host "`n2. 测试批量添加参与者..." -ForegroundColor Yellow
$participantsBody = @(
    @{
        user = @{ id = 101 }
        userName = "张三"
    },
    @{
        user = @{ id = 102 }
        userName = "李四"
    },
    @{
        user = $null
        userName = "游客小王"
    },
    @{
        user = @{ id = 103 }
        userName = "王五"
    }
) | ConvertTo-Json -Depth 3

try {
    $participantsResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants/batch" -Method POST -Body $participantsBody -ContentType "application/json"
    Write-Host "✓ 批量添加参与者成功" -ForegroundColor Green
    Write-Host "参与者信息: $($participantsResponse.data | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "✗ 批量添加参与者失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "错误详情: $($_.Exception.Response.StatusCode)"
}

# 3. 验证参与者列表
Write-Host "`n3. 验证参与者列表..." -ForegroundColor Yellow
try {
    $participantsList = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants" -Method GET
    Write-Host "✓ 获取参与者列表成功" -ForegroundColor Green
    Write-Host "参与者数量: $($participantsList.data.Count)"
    Write-Host "参与者列表: $($participantsList.data | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "✗ 获取参与者列表失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. 测试错误场景 - 空参与者列表
Write-Host "`n4. 测试错误场景 - 空参与者列表..." -ForegroundColor Yellow
try {
    $emptyBody = "[]"
    $errorResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants/batch" -Method POST -Body $emptyBody -ContentType "application/json"
    Write-Host "✗ 应该返回错误，但成功了" -ForegroundColor Red
} catch {
    Write-Host "✓ 正确返回错误: $($_.Exception.Message)" -ForegroundColor Green
}

# 5. 测试错误场景 - 不存在的对局
Write-Host "`n5. 测试错误场景 - 不存在的对局..." -ForegroundColor Yellow
try {
    $errorResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/99999/participants/batch" -Method POST -Body $participantsBody -ContentType "application/json"
    Write-Host "✗ 应该返回错误，但成功了" -ForegroundColor Red
} catch {
    Write-Host "✓ 正确返回错误: $($_.Exception.Message)" -ForegroundColor Green
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
