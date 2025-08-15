# Mem4j

<p align="center">
  <img src="docs/images/banner-sm.png" width="800px" alt="Mem4j - The Memory Layer for Personalized AI">
</p>

<p align="center">
  <strong>⚡ Long-term memory for AI Agents - Java Implementation</strong>
</p>

## Introduction

Mem4j is a Java implementation of the Mem0 memory system, providing intelligent memory layer capabilities for AI assistants and agents. It enables personalized AI interactions by remembering user preferences, adapting to individual needs, and continuously learning over time.

### Key Features

- **Multi-Level Memory**: Seamlessly retains User, Session, and Agent state with adaptive personalization
- **Developer-Friendly**: Intuitive API, Spring Boot integration, and comprehensive documentation
- **Vector Store Support**: Multiple vector database integrations (Qdrant, Elasticsearch, Weaviate, etc.)
- **LLM Integration**: Support for various LLM providers (**DashScope (通义千问)**, OpenAI, Anthropic, etc.)
- **Graph Database**: Neo4j integration for relationship management
- **Async Support**: Full asynchronous operation support
- **Chinese Language Support**: Optimized for Chinese language processing with DashScope integration

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

1. Clone the repository:

```bash
git clone https://github.com/Mem4j/mem4j.git
cd mem4j
```

2. Build the project:

```bash
mvn clean install
```

3. Run the application:

```bash
mvn spring-boot:run
```

### Basic Usage

```java
import com.mem4j.memory.Memory;
import com.mem4j.memory.MemoryItem;
import com.mem4j.memory.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    @Autowired
    private Memory memory;

    public void useMemory() {
        // Add memories
        List<Message> messages = Arrays.asList(
            new Message("user", "我是张三，我喜欢吃披萨"),
            new Message("assistant", "很高兴认识你张三！我会记住你喜欢吃披萨。")
        );

        memory.add(messages, "zhang_user");

        // Search memories
        List<MemoryItem> results = memory.search("张三喜欢什么？", "zhang_user");
    }
}
```

## Architecture

### Core Components

```
src/main/java/com/mem4j/
├── memory/           # Core memory management
├── vectorstores/     # Vector database integrations
├── llms/            # LLM provider integrations
├── embeddings/      # Embedding model integrations
├── controllers/     # REST API controllers
├── examples/        # Example implementations
└── configs/         # Configuration management
```

### Supported Integrations

#### Vector Stores

- ✅ **Qdrant** - 完整实现
- ✅ **InMemory** - 内存存储实现
- ⚠️ **Elasticsearch** - 依赖已添加，实现计划中
- ⚠️ **Weaviate** - 依赖已添加，实现计划中
- 📋 **Pinecone** - 计划通过 HTTP 客户端支持
- 📋 **Chroma** - 计划通过 HTTP 客户端支持

#### LLM Providers

- ✅ **DashScope** (通义千问系列) - 完整实现
- ✅ **OpenAI** - 完整实现
- ⚠️ **Anthropic** - 依赖已添加，实现计划中
- 📋 **Azure OpenAI** - 计划支持
- 📋 **AWS Bedrock** - 计划通过 HTTP 客户端支持

#### Embedding Models

- ✅ **DashScope Embeddings** - 完整实现
- ✅ **OpenAI Embeddings** - 完整实现
- 📋 **HuggingFace** - 计划通过 HTTP 客户端支持
- 📋 **VertexAI** - 计划通过 HTTP 客户端支持

## Configuration

### Application Properties

```yaml
# Memory Configuration
mem4j:
  vector-store:
    type: qdrant
    url: http://localhost:6333
    collection: memories

  llm:
    type: dashscope # 或 openai
    api-key: ${DASHSCOPE_API_KEY}
    model: qwen-turbo # DashScope模型

  embeddings:
    type: dashscope # 或 openai
    model: text-embedding-v1 # DashScope嵌入模型

  graph:
    type: neo4j
    uri: bolt://localhost:7687
    username: neo4j
    password: password
```

### Environment Variables

```bash
export DASHSCOPE_API_KEY="your-dashscope-api-key"
export OPENAI_API_KEY="your-openai-api-key"
export ANTHROPIC_API_KEY="your-anthropic-api-key"
export QDRANT_URL="http://localhost:6333"
export NEO4J_URI="bolt://localhost:7687"
export NEO4J_USERNAME="neo4j"
export NEO4J_PASSWORD="password"
```

## API Reference

### Memory Operations

#### Add Memories

```java
// Add conversation memories (with inference)
memory.add(messages, userId);

// Add with metadata and custom memory type
memory.add(messages, userId, metadata, true, MemoryType.FACTUAL);

// Add without inference (faster, no LLM processing)
memory.add(messages, userId, metadata, false, MemoryType.FACTUAL);
```

#### Search Memories

```java
// Basic search
List<MemoryItem> results = memory.search(query, userId);

// Search with filters and custom parameters
Map<String, Object> filters = Map.of("agent_id", "chatbot");
List<MemoryItem> results = memory.search(query, userId, filters, 10, 0.7);

// Get all memories for a user
List<MemoryItem> allMemories = memory.getAll(userId, filters, 100);
```

#### Update Memories

```java
// Update existing memory
memory.update(memoryId, updatedData);
```

#### Delete Memories

```java
// Delete specific memory
memory.delete(memoryId);

// Delete all user memories
memory.deleteAll(userId);
```

### Additional Operations

```java
// Get specific memory by ID
MemoryItem memory = memory.get(memoryId);

// Reset all memories (for testing)
memory.reset();
```

> **注意**: 异步操作支持正在开发中，当前版本使用同步 API。

## Examples

### Customer Support Bot

```java
@Service
public class CustomerSupportService {

    @Autowired
    private Memory memory;

    public String handleCustomerQuery(String query, String customerId) {
        // Search for relevant memories
        List<MemoryItem> memories = memory.search(query, customerId);

        // Build context from memories
        String context = memories.stream()
            .map(MemoryItem::getContent)
            .collect(Collectors.joining("\n"));

        // Generate response with context
        return generateResponse(query, context);
    }

    private String generateResponse(String query, String context) {
        // 实现响应生成逻辑
        return "Response based on: " + context;
    }
}
```

### AI Assistant with Memory

```java
@Component
public class AIAssistant {

    @Autowired
    private Memory memory;

    public String chat(String message, String userId) {
        // Get relevant memories
        List<MemoryItem> memories = memory.search(message, userId);

        // Build conversation context
        String memoryContext = buildMemoryContext(memories);

        // Generate response
        String response = generateResponse(message, memoryContext);

        // Store conversation
        List<Message> conversation = Arrays.asList(
            new Message("user", message),
            new Message("assistant", response)
        );
        memory.add(conversation, userId);

        return response;
    }

    private String buildMemoryContext(List<MemoryItem> memories) {
        return memories.stream()
            .map(MemoryItem::getContent)
            .collect(Collectors.joining("\n"));
    }

    private String generateResponse(String message, String context) {
        // 实现AI响应生成逻辑
        return "AI response based on context: " + context;
    }
}
```

## Development

### Building from Source

```bash
# Clone repository
git clone https://github.com/Mem4j/mem4j.git
cd mem4j

# Configure environment
export DASHSCOPE_API_KEY="your-dashscope-api-key"

# Build
mvn clean install

# Run tests
mvn test

# Run with Docker
docker-compose up -d
mvn spring-boot:run
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=MemoryTest

# Run with coverage
mvn jacoco:prepare-agent test jacoco:report
```

### Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## DashScope Integration

Mem4j now supports **DashScope** (通义千问) as a primary LLM and embedding provider, offering excellent Chinese language support and optimized performance for the Asia-Pacific region.

### Quick Setup

1. **Get DashScope API Key**:

   - Visit [DashScope Console](https://dashscope.console.aliyun.com/)
   - Create an API key

2. **Configure Environment**:

   ```bash
   export DASHSCOPE_API_KEY="your-dashscope-api-key"
   ```

3. **Update Configuration**:
   ```yaml
   mem4j:
     llm:
       type: dashscope
       api-key: ${DASHSCOPE_API_KEY}
       model: qwen-turbo
     embeddings:
       type: dashscope
       model: text-embedding-v1
   ```

### Supported Models

- **LLM**: `qwen-turbo`, `qwen-plus`, `qwen-max`, `qwen-max-longcontext`
- **Embeddings**: `text-embedding-v1`

### Benefits

- **Chinese Language Optimization**: Excellent Chinese text understanding and generation
- **Low Latency**: Optimized for Asia-Pacific region
- **Cost Effective**: Competitive pricing for Chinese market
- **High Availability**: 99.9% uptime guarantee

For detailed setup instructions, see [DASHSCOPE_SETUP.md](DASHSCOPE_SETUP.md).

## License

Apache 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This project is inspired by the original [Mem0 Python implementation](https://github.com/mem0ai/mem0) and [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba) for DashScope integration.
