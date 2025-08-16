# Mem4j API Documentation

## Overview

Mem4j provides a REST API for managing AI memory operations. The API is built on Spring Boot and provides endpoints for adding, searching, updating, and deleting memories.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Currently, the API does not require authentication. In production, you should implement proper authentication and authorization.

## Endpoints

### 1. Add Memories

**POST** `/memory/add`

Add memories from a conversation.

**Request Body:**

```json
{
  "messages": [
    {
      "role": "user",
      "content": "I like pizza and coffee"
    },
    {
      "role": "assistant",
      "content": "I'll remember that you like pizza and coffee."
    }
  ],
  "userId": "user123",
  "metadata": {
    "session_id": "session456",
    "agent_id": "agent789"
  },
  "infer": true,
  "memoryType": "factual"
}
```

**Response:**

```json
{
  "status": "success",
  "message": "Memories added successfully"
}
```

### 2. Search Memories

**GET** `/memory/search`

Search for relevant memories based on a query.

**Parameters:**

- `query` (required): Search query
- `userId` (required): User ID
- `limit` (optional): Maximum number of results (default: 10)
- `threshold` (optional): Similarity threshold (default: 0.7)
- `filters` (optional): Additional filters

**Example:**

```
GET /memory/search?query=What do I like?&userId=user123&limit=5
```

**Response:**

```json
{
  "status": "success",
  "results": [
    {
      "id": "memory123",
      "content": "User likes pizza and coffee",
      "memoryType": "factual",
      "userId": "user123",
      "score": 0.85,
      "createdAt": "2024-01-01T12:00:00Z",
      "updatedAt": "2024-01-01T12:00:00Z"
    }
  ],
  "count": 1
}
```

### 3. Get All Memories

**GET** `/memory/all`

Get all memories for a user.

**Parameters:**

- `userId` (required): User ID
- `limit` (optional): Maximum number of results (default: 100)
- `filters` (optional): Additional filters

**Example:**

```
GET /memory/all?userId=user123&limit=50
```

### 4. Get Memory by ID

**GET** `/memory/{memoryId}`

Get a specific memory by its ID.

**Example:**

```
GET /memory/memory123
```

**Response:**

```json
{
  "status": "success",
  "memory": {
    "id": "memory123",
    "content": "User likes pizza and coffee",
    "memoryType": "factual",
    "userId": "user123",
    "createdAt": "2024-01-01T12:00:00Z",
    "updatedAt": "2024-01-01T12:00:00Z"
  }
}
```

### 5. Update Memory

**PUT** `/memory/{memoryId}`

Update a specific memory.

**Request Body:**

```json
{
  "content": "Updated memory content",
  "metadata": {
    "updated": true
  }
}
```

**Response:**

```json
{
  "status": "success",
  "message": "Memory updated successfully"
}
```

### 6. Delete Memory

**DELETE** `/memory/{memoryId}`

Delete a specific memory.

**Response:**

```json
{
  "status": "success",
  "message": "Memory deleted successfully"
}
```

### 7. Delete All User Memories

**DELETE** `/memory/user/{userId}`

Delete all memories for a specific user.

**Response:**

```json
{
  "status": "success",
  "message": "All memories deleted successfully"
}
```

### 8. Reset All Memories

**POST** `/memory/reset`

Reset all memories (for testing purposes).

**Response:**

```json
{
  "status": "success",
  "message": "All memories reset successfully"
}
```

## Data Models

### Message

```json
{
  "role": "user|assistant|system",
  "content": "Message content",
  "name": "Optional name",
  "functionCall": {
    "name": "Function name",
    "arguments": "Function arguments"
  },
  "toolCalls": [
    {
      "id": "tool123",
      "type": "function",
      "function": {
        "name": "Function name",
        "arguments": "Function arguments"
      }
    }
  ],
  "metadata": {
    "key": "value"
  }
}
```

### MemoryItem

```json
{
  "id": "memory123",
  "content": "Memory content",
  "memoryType": "factual|episodic|semantic|procedural|working",
  "userId": "user123",
  "agentId": "agent456",
  "runId": "run789",
  "actorId": "actor123",
  "metadata": {
    "key": "value"
  },
  "score": 0.85,
  "createdAt": "2024-01-01T12:00:00Z",
  "updatedAt": "2024-01-01T12:00:00Z",
  "embedding": [0.1, 0.2, 0.3, ...]
}
```

## Error Responses

All endpoints return error responses in the following format:

```json
{
  "status": "error",
  "message": "Error description"
}
```

Common HTTP status codes:

- `200`: Success
- `400`: Bad Request
- `404`: Not Found
- `500`: Internal Server Error

## Examples

### cURL Examples

**Add memories:**

```bash
curl -X POST http://localhost:8080/api/v1/memory/add \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"role": "user", "content": "I like pizza"},
      {"role": "assistant", "content": "I'll remember that!"}
    ],
    "userId": "user123"
  }'
```

**Search memories:**

```bash
curl "http://localhost:8080/api/v1/memory/search?query=pizza&userId=user123&limit=5"
```

**Delete memory:**

```bash
curl -X DELETE http://localhost:8080/api/v1/memory/memory123
```

### JavaScript Examples

**Add memories:**

```javascript
const response = await fetch("http://localhost:8080/api/v1/memory/add", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    messages: [
      { role: "user", content: "I like pizza" },
      { role: "assistant", content: "I'll remember that!" },
    ],
    userId: "user123",
  }),
});

const result = await response.json();
console.log(result);
```

**Search memories:**

```javascript
const response = await fetch(
  "http://localhost:8080/api/v1/memory/search?query=pizza&userId=user123"
);
const result = await response.json();
console.log(result.results);
```

## Rate Limiting

Currently, the API does not implement rate limiting. In production, you should implement appropriate rate limiting to prevent abuse.

## Monitoring

The application exposes health check endpoints:

- `GET /actuator/health`: Application health
- `GET /actuator/info`: Application information
- `GET /actuator/metrics`: Application metrics

## Configuration

The API behavior can be configured through application properties:

```yaml
github:
  mem4j:
    vector-store:
      type: inmemory
      similarity-threshold: 0.7
    llm:
      type: openai
      model: gpt-4o-mini
  embeddings:
    type: openai
    model: text-embedding-3-small
```
