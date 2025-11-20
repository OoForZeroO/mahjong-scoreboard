# 测试批量轮次得分接口

Write-Host "Testing batch round scores interface..."

# 使用之前创建的参与者ID
$participantId = 21
$matchId = 27

# 测试批量轮次得分
$roundScoresData = @"
[
  {
    "match": {
      "matchId": $matchId
    },
    "participant": {
      "id": $participantId
    },
    "roundNumber": 1,
    "score": 100,
    "roundTime": 1716700800000
  },
  {
    "match": {
      "matchId": $matchId
    },
    "participant": {
      "id": 22
    },
    "roundNumber": 1,
    "score": -50,
    "roundTime": 1716700800000
  }
]
"@

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

# 测试获取轮次得分
try {
    $roundsResult = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/matches/$matchId/rounds" -Method GET
    Write-Host "Round scores list:"
    $roundsResult.data | ConvertTo-Json -Depth 3
} catch {
    Write-Host "Failed to get round scores: $($_.Exception.Message)"
}
