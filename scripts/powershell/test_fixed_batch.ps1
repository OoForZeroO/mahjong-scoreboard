# 修复后的批量接口测试

Write-Host "=== 修复后的批量接口测试 ===" -ForegroundColor Green

# 1. 创建测试对局
Write-Host "`n1. 创建测试对局..." -ForegroundColor Yellow
$matchData = '{"roomName": "修复测试房间", "totalRounds": 4}'
try {
    $matchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches" -Method POST -Body $matchData -ContentType "application/json"
    $matchId = $matchResult.data.matchId
    Write-Host "✓ 对局创建成功，ID: $matchId" -ForegroundColor Green
} catch {
    Write-Host "✗ 对局创建失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. 测试批量添加参与者
Write-Host "`n2. 测试批量添加参与者..." -ForegroundColor Yellow
$participantsData = @'
[
  {
    "user": {
      "id": 101
    },
    "userName": "张三"
  },
  {
    "user": null,
    "userName": "游客小王"
  },
  {
    "user": {
      "id": 102
    },
    "userName": "李四"
  }
]
'@

try {
    $participantsResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants/batch" -Method POST -Body $participantsData -ContentType "application/json"
    Write-Host "✓ 批量添加参与者成功" -ForegroundColor Green
    Write-Host "参与者数量: $($participantsResult.data.Count)"
    Write-Host "响应: $($participantsResult | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "✗ 批量添加参与者失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "错误响应: $responseBody" -ForegroundColor Red
    }
}

# 3. 测试批量轮次得分
Write-Host "`n3. 测试批量轮次得分..." -ForegroundColor Yellow
$roundScoresData = @'
[
  {
    "match": {
      "matchId": 1
    },
    "participant": {
      "id": 1
    },
    "roundNumber": 1,
    "score": 100,
    "roundTime": 1716700800000
  },
  {
    "match": {
      "matchId": 1
    },
    "participant": {
      "id": 2
    },
    "roundNumber": 1,
    "score": -50,
    "roundTime": 1716700800000
  }
]
'@

try {
    $roundScoresResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/rounds/batch" -Method POST -Body $roundScoresData -ContentType "application/json"
    Write-Host "✓ 批量创建轮次得分成功" -ForegroundColor Green
    Write-Host "轮次得分数量: $($roundScoresResult.data.Count)"
    Write-Host "响应: $($roundScoresResult | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "✗ 批量创建轮次得分失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "错误响应: $responseBody" -ForegroundColor Red
    }
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
