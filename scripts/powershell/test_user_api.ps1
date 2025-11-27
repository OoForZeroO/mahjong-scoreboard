# 测试修复后的用户API接口

Write-Host "Testing User API with complete response format..."

# 1. 创建测试用户
Write-Host "`n1. Creating test user..."
$userData = '{"username": "测试用户API", "phone": "13900139000", "email": "testapi@example.com", "password": "123456"}'
try {
    $createResult = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method POST -Body $userData -ContentType "application/json"
    Write-Host "✓ User created successfully:"
    Write-Host "  Code: $($createResult.code)"
    Write-Host "  Message: $($createResult.message)"
    Write-Host "  User ID: $($createResult.data.id)"
    Write-Host "  Username: $($createResult.data.username)"
    Write-Host "  Phone: $($createResult.data.phone)"
    Write-Host "  Email: $($createResult.data.email)"
    Write-Host "  Role: $($createResult.data.role)"
    Write-Host "  Status: $($createResult.data.status)"
    Write-Host "  Create Time: $($createResult.data.createTime)"
    $userId = $createResult.data.id
} catch {
    Write-Host "✗ Failed to create user: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody"
    }
    exit 1
}

# 2. 根据ID获取用户
Write-Host "`n2. Getting user by ID..."
try {
    $getResult = Invoke-RestMethod -Uri "http://localhost:8080/api/users/$userId" -Method GET
    Write-Host "✓ User retrieved successfully:"
    Write-Host "  Code: $($getResult.code)"
    Write-Host "  Message: $($getResult.message)"
    Write-Host "  User Data:"
    $getResult.data | Get-Member -MemberType NoteProperty | ForEach-Object {
        $value = $getResult.data.($_.Name)
        Write-Host "    $($_.Name): $value"
    }
} catch {
    Write-Host "✗ Failed to get user: $($_.Exception.Message)"
}

# 3. 根据手机号获取用户
Write-Host "`n3. Getting user by phone..."
try {
    $phoneResult = Invoke-RestMethod -Uri "http://localhost:8080/api/users/phone/13900139000" -Method GET
    Write-Host "✓ User retrieved by phone successfully:"
    Write-Host "  Code: $($phoneResult.code)"
    Write-Host "  Message: $($phoneResult.message)"
    Write-Host "  Username: $($phoneResult.data.username)"
    Write-Host "  Phone: $($phoneResult.data.phone)"
} catch {
    Write-Host "✗ Failed to get user by phone: $($_.Exception.Message)"
}

# 4. 获取所有用户列表
Write-Host "`n4. Getting all users..."
try {
    $allUsersResult = Invoke-RestMethod -Uri "http://localhost:8080/api/users" -Method GET
    Write-Host "✓ All users retrieved successfully:"
    Write-Host "  Code: $($allUsersResult.code)"
    Write-Host "  Message: $($allUsersResult.message)"
    Write-Host "  Total users: $($allUsersResult.data.Count)"
    $allUsersResult.data | ForEach-Object {
        Write-Host "    ID: $($_.id), Username: $($_.username), Phone: $($_.phone)"
    }
} catch {
    Write-Host "✗ Failed to get all users: $($_.Exception.Message)"
}

# 5. 更新用户信息
Write-Host "`n5. Updating user..."
$updateData = '{"username": "更新后的用户名", "email": "updated@example.com"}'
try {
    $updateResult = Invoke-RestMethod -Uri "http://localhost:8080/api/users/$userId" -Method PUT -Body $updateData -ContentType "application/json"
    Write-Host "✓ User updated successfully:"
    Write-Host "  Code: $($updateResult.code)"
    Write-Host "  Message: $($updateResult.message)"
    Write-Host "  Updated Username: $($updateResult.data.username)"
    Write-Host "  Updated Email: $($updateResult.data.email)"
} catch {
    Write-Host "✗ Failed to update user: $($_.Exception.Message)"
}

# 6. 检查手机号是否存在
Write-Host "`n6. Checking if phone exists..."
try {
    $existsResult = Invoke-RestMethod -Uri "http://localhost:8080/api/users/exists/phone/13900139000" -Method GET
    Write-Host "✓ Phone check completed:"
    Write-Host "  Code: $($existsResult.code)"
    Write-Host "  Message: $($existsResult.message)"
    Write-Host "  Phone exists: $($existsResult.data)"
} catch {
    Write-Host "✗ Failed to check phone: $($_.Exception.Message)"
}

Write-Host "`n=== User API Test completed ==="
