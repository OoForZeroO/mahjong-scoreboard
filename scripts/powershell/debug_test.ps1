# 调试测试脚本

Write-Host "=== 调试测试 ===" -ForegroundColor Green

# 1. 测试健康检查
Write-Host "`n1. 测试健康检查..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/test/hello" -Method GET
    Write-Host "✓ 健康检查成功: $health" -ForegroundColor Green
} catch {
    Write-Host "✗ 健康检查失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 2. 创建对局
Write-Host "`n2. 创建对局..." -ForegroundColor Yellow
$matchData = '{"roomName": "测试房间", "totalRounds": 4}'
try {
    $matchResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches" -Method POST -Body $matchData -ContentType "application/json"
    Write-Host "✓ 对局创建成功" -ForegroundColor Green
    Write-Host "响应: $($matchResult | ConvertTo-Json -Depth 3)"
    $matchId = $matchResult.data.matchId
    Write-Host "对局ID: $matchId" -ForegroundColor Cyan
} catch {
    Write-Host "✗ 对局创建失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应体: $responseBody" -ForegroundColor Red
    }
    exit 1
}

# 3. 测试批量添加参与者
Write-Host "`n3. 测试批量添加参与者..." -ForegroundColor Yellow
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
    Write-Host "响应: $($participantsResult | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "✗ 批量添加参与者失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应体: $responseBody" -ForegroundColor Red
    }
}

# 4. 获取参与者列表
Write-Host "`n4. 获取参与者列表..." -ForegroundColor Yellow
try {
    $listResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/participants" -Method GET
    Write-Host "✓ 获取参与者列表成功" -ForegroundColor Green
    Write-Host "参与者数量: $($listResult.data.Count)"
    Write-Host "响应: $($listResult | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "✗ 获取参与者列表失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应体: $responseBody" -ForegroundColor Red
    }
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
