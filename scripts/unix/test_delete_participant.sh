#!/bin/bash

# 测试参与者删除接口
# 使用前请修改BASE_URL为您的服务器地址

BASE_URL="http://localhost:8080"

echo "========================================="
echo "测试参与者删除接口"
echo "========================================="
echo ""

# 测试1: 删除无记录的参与者（应该成功）
echo "测试1: 删除无记录的参与者（ID: 123）"
echo "----------------------------------------"
curl -X DELETE "${BASE_URL}/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[123]" \
  -w "\nHTTP状态码: %{http_code}\n" \
  -s
echo ""
echo ""

# 测试2: 删除有记录的参与者（应该失败）
echo "测试2: 删除有记录的参与者（ID: 456）"
echo "----------------------------------------"
curl -X DELETE "${BASE_URL}/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[456]" \
  -w "\nHTTP状态码: %{http_code}\n" \
  -s
echo ""
echo ""

# 测试3: 空列表测试（应该失败）
echo "测试3: 空列表测试"
echo "----------------------------------------"
curl -X DELETE "${BASE_URL}/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[]" \
  -w "\nHTTP状态码: %{http_code}\n" \
  -s
echo ""
echo ""

# 测试4: 批量删除测试
echo "测试4: 批量删除多个参与者"
echo "----------------------------------------"
curl -X DELETE "${BASE_URL}/api/v1/matches/participants/batch" \
  -H "Content-Type: application/json" \
  -d "[123, 456, 789]" \
  -w "\nHTTP状态码: %{http_code}\n" \
  -s
echo ""
echo ""

echo "========================================="
echo "测试完成"
echo "========================================="
