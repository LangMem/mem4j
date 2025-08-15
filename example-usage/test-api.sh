#!/bin/bash

# Mem4j Example API Test Script

API_BASE="http://localhost:9090/api/chat"

echo "🧪 Mem4j Example API Test Script"
echo "================================"

# Check if server is running
echo "🔍 Checking if server is running..."
if ! curl -s "$API_BASE/memories/test-user" > /dev/null 2>&1; then
    echo "❌ Error: Server is not running on http://localhost:9090"
    echo "   Please start the server first:"
    echo "   ./start-example.sh"
    exit 1
fi

echo "✅ Server is running"
echo ""

# Test 1: Send first message
echo "📝 Test 1: Sending first message..."
response1=$(curl -s -X POST "$API_BASE/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "message": "Hi, I am John. I love playing basketball and I am from New York."
  }')

echo "Response: $response1"
echo ""

# Test 2: Send second message
echo "📝 Test 2: Sending second message..."
response2=$(curl -s -X POST "$API_BASE/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user", 
    "message": "What do you know about me?"
  }')

echo "Response: $response2"
echo ""

# Test 3: Get memories
echo "📋 Test 3: Getting user memories..."
memories=$(curl -s "$API_BASE/memories/test-user")
echo "Memories: $memories"
echo ""

# Test 4: Send message in Chinese
echo "📝 Test 4: Testing Chinese message..."
response3=$(curl -s -X POST "$API_BASE/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user-cn",
    "message": "你好，我是张三，我住在北京，喜欢吃火锅。"
  }')

echo "Response: $response3"
echo ""

# Test 5: Query in Chinese
echo "📝 Test 5: Querying in Chinese..."
response4=$(curl -s -X POST "$API_BASE/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user-cn",
    "message": "我喜欢什么？"
  }')

echo "Response: $response4"
echo ""

# Test 6: Get Chinese user memories
echo "📋 Test 6: Getting Chinese user memories..."
memories_cn=$(curl -s "$API_BASE/memories/test-user-cn")
echo "Memories: $memories_cn"
echo ""

# Cleanup option
echo "🧹 Clean up test data? (y/N)"
read -r cleanup
if [[ $cleanup =~ ^[Yy]$ ]]; then
    echo "Clearing test-user memories..."
    curl -s -X DELETE "$API_BASE/memories/test-user"
    echo "Clearing test-user-cn memories..."
    curl -s -X DELETE "$API_BASE/memories/test-user-cn"
    echo "✅ Cleanup completed"
fi

echo ""
echo "✅ API tests completed!"
echo "   You can now test the API manually with:"
echo "   curl -X POST '$API_BASE/send' -H 'Content-Type: application/json' -d '{\"userId\": \"your-user\", \"message\": \"your message\"}'"
