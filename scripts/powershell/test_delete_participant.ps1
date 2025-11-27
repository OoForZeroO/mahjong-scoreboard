# 测试参与者删除接口
# 使用前请修改 $baseUrl 为您的服务器地址

$baseUrl = "http://localhost:8080"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "测试参与者删除接口" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# 测试1: 删除无记录的参与者（应该成功）
Write-Host "测试1: 删除无记录的参与者（ID: 123）" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$body1 = "[123]" | ConvertTo-Json
try {
    $response1 = Invoke-RestMethod -Uri "$baseUrl/api/v1/matches/participants/batch" `
        -Method DELETE `
        -ContentType "application/json" `
        -Body $body1 `
        -ErrorAction Stop
    
    Write-Host "✓ 请求成功" -ForegroundColor Green
    Write-Host "响应: $($response1 | ConvertTo-Json)" -ForegroundColor White
} catch {
    Write-Host "✗ 请求失败" -ForegroundColor Red
    Write-Host "错误: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 测试2: 删除有记录的参与者（应该失败）
Write-Host "测试2: 删除有记录的参与者（ID: 456）" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$body2 = "[456]" | ConvertTo-Json
try {
    $response2 = Invoke-RestMethod -Uri "$baseUrl/api/v1/matches/participants/batch" `
        -Method DELETE `
        -ContentType "application/json" `
        -Body $body2 `
        -ErrorAction Stop
    
    Write-Host "✓ 请求成功" -ForegroundColor Green
    Write-Host "响应: $($response2 | ConvertTo-Json)" -ForegroundColor White
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "✗ 请求失败（预期行为）" -ForegroundColor Yellow
    Write-Host "状态码: $statusCode" -ForegroundColor Yellow
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应: $responseBody" -ForegroundColor White
    }
}
Write-Host ""

# 测试3: 空列表测试（应该失败）
Write-Host "测试3: 空列表测试" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$body3 = "[]" | ConvertTo-Json
try {
    $response3 = Invoke-RestMethod -Uri "$baseUrl/api/v1/matches/participants/batch" `
        -Method DELETE `
        -ContentType "application/json" `
        -Body $body3 `
        -ErrorAction Stop
    
    Write-Host "✓ 请求成功" -ForegroundColor Green
    Write-Host "响应: $($response3 | ConvertTo-Json)" -ForegroundColor White
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "✗ 请求失败（预期行为）" -ForegroundColor Yellow
    Write-Host "状态码: $statusCode" -ForegroundColor Yellow
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应: $responseBody" -ForegroundColor White
    }
}
Write-Host ""

# 测试4: 批量删除测试
Write-Host "测试4: 批量删除多个参与者" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Gray

$body4 = "[123, 456, 789]" | ConvertTo-Json
try {
    $response4 = Invoke-RestMethod -Uri "$baseUrl/api/v1/matches/participants/batch" `
        -Method DELETE `
        -ContentType "application/json" `
        -Body $body4 `
        -ErrorAction Stop
    
    Write-Host "✓ 请求成功" -ForegroundColor Green
    Write-Host "响应: $($response4 | ConvertTo-Json)" -ForegroundColor White
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "✗ 请求失败（如果有参与者有记录，这是预期行为）" -ForegroundColor Yellow
    Write-Host "状态码: $statusCode" -ForegroundColor Yellow
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "响应: $responseBody" -ForegroundColor White
    }
}
Write-Host ""

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "测试完成" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
