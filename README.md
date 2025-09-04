# Mem4j

<p align="center">
  <img src="docs/images/mem4j-logo.png" width="200px" alt="Mem4j Logo">
</p>

<p align="center">
  <strong>🧠 The Memory Layer for Personalized AI</strong>
</p>

<p align="center">
  <strong>⚡ Long-term memory for AI Agents - Java Implementation</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/version-0.1.0-blue.svg" alt="Version">
  <img src="https://img.shields.io/badge/Java-17+-orange.svg" alt="Java Version">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2+-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/license-Apache%202.0-red.svg" alt="License">
</p>

## Introduction

Mem4j is a Java library that provides long-term memory capabilities for AI agents and applications. It offers intelligent memory layer functionality that enables personalized AI interactions by remembering user preferences, adapting to individual needs, and continuously learning over time through conversation history.

> **Note**: Mem4j is a library, not a standalone application. To see it in action, check out the [mem4j-example](mem4j-example/) module which demonstrates how to integrate and use Mem4j in a Spring Boot application.

### Key Features

- **Multi-Level Memory**: Supports various memory types (Factual, Episodic, Semantic, Procedural, Working)
- **Developer-Friendly**: Intuitive API, Spring Boot integration, and comprehensive documentation
- **Vector Store Support**: Multiple vector database integrations (InMemory, Qdrant, Milvus)
- **LLM Integration**: Support for various LLM providers (DashScope, OpenAI)
- **Async Support**: Designed for asynchronous operations
- **Chinese Language Support**: Optimized for Chinese language processing with DashScope integration
- **Modular Architecture**: Clean separation of concerns with pluggable components

## Quick Start

> 🚀 **Quick Start Guide**: See [QUICK_START.md](QUICK_START.md) for detailed project setup and usage instructions.

> 💡 **Example Project**: Check out [mem4j-example](mem4j-example/) for a complete working example.

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

Add the Mem4j Spring Boot starter to your project:

**Maven:**

```xml
<dependency>
    <groupId>io.github.mem4j</groupId>
    <artifactId>mem4j-spring-boot-starter</artifactId>
    <version>0.1.0.RC1</version>
</dependency>
```

**Gradle:**

```gradle
implementation 'io.github.mem4j:mem4j-spring-boot-starter:0.1.0'
```

**Build from source:**

1. Clone the repository:

```bash
git clone https://github.com/langMem/mem4j.git
cd mem4j
```

1. Build and install to local repository:

```bash
mvn clean install
```

### Running the Example

To see Mem4j in action, you can run the included example application:

```bash
# Navigate to the example directory
cd mem4j-example

# Set your DashScope API key (optional for demo)
export DASHSCOPE_API_KEY="your-api-key"

# Run the example
mvn spring-boot:run
```

The example will start a web server on `http://localhost:19090` with endpoints to test memory operations.

### Basic Usage

```java
import io.github.mem4j.memory.Memory;
import io.github.mem4j.memory.MemoryItem;
import io.github.mem4j.memory.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyService {

    @Autowired
    private Memory memory;

    public void useMemory() {
        // Add memories
        List<Message> messages = Arrays.asList(
            new Message("user", "Hi, I'm John and I love pizza"),
            new Message("assistant", "Nice to meet you John! I'll remember that you love pizza.")
        );

        memory.add(messages, "john_user");

        // Search memories
        List<MemoryItem> results = memory.search("What does John like?", "john_user");
    }
}
```

## Project Structure

Mem4j is organized as a multi-module Maven project:

- **mem4j-core**: Core memory management functionality
- **mem4j-autoconfigure**: Spring Boot auto-configuration
- **mem4j-spring-boot-starter**: Spring Boot starter for easy integration
- **mem4j-example**: Example application demonstrating usage
- **mem4j-bom**: Bill of Materials for dependency management

## Architecture

### Core Components

```text
mem4j-core/src/main/java/io/github/mem4j/
├── memory/           # Core memory management
├── vectorstores/     # Vector database integrations
├── llms/            # LLM provider integrations
├── embeddings/      # Embedding model integrations
└── config/          # Configuration management
```

### Supported Integrations

#### Vector Stores

- ✅ **InMemory** - In-memory storage implementation
- ✅ **Qdrant** - Full implementation with search and persistence
- ✅ **Milvus** - Full implementation with vector search capabilities
- 📋 **Elasticsearch** - Planned implementation
- 📋 **Weaviate** - Planned implementation
- 📋 **Pinecone** - Planned implementation
- 📋 **Chroma** - Planned implementation

#### LLM Providers

- ✅ **DashScope** - Full implementation with Alibaba Cloud DashScope API
- ✅ **OpenAI** - Full implementation with OpenAI API
- ✅ **Anthropic** - Full implementation with Anthropic API
- 📋 **Azure OpenAI** - Planned implementation
- 📋 **AWS Bedrock** - Planned implementation

#### Embedding Models

- ✅ **DashScope Embeddings** - Full implementation with text-embedding-v1 model
- ✅ **OpenAI Embeddings** - Full implementation with various embedding models
- 📋 **HuggingFace** - Planned implementation
- 📋 **VertexAI** - Planned implementation

## Configuration

> 📖 **Detailed Configuration Guide**: See [CONFIGURATION.md](docs/CONFIGURATION.md) for complete configuration options and best practices.

### Application Properties

```yaml
# Memory Configuration
mem4j:
  vector-store:
    type: qdrant # Options: inmemory, qdrant, milvus
    url: http://localhost:6333
    collection: memories
    options:
      similarity-threshold: 0.7

  llm:
    type: dashscope # Options: openai, dashscope, anthropic
    api-key: ${DASHSCOPE_API_KEY}
    model: qwen-turbo
    options:
      max-tokens: 1000
      temperature: 0.7

  embeddings:
    type: dashscope # Options: openai, dashscope
    model: text-embedding-v1
    options:
      dimensions: 1536

  # Global configuration
  max-memories: 1000
  embedding-dimension: 1536
  similarity-threshold: 0.7
```

### Environment Variables

```bash
export DASHSCOPE_API_KEY="your-dashscope-api-key"
export OPENAI_API_KEY="your-openai-api-key"
export ANTHROPIC_API_KEY="your-anthropic-api-key"
export QDRANT_URL="http://localhost:6333"
export MILVUS_URL="localhost:19530"
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

> **Note**: This is version 0.1.0, currently in active development. APIs may change in future releases.

### Additional Operations

```java
// Get specific memory by ID
MemoryItem memory = memory.get(memoryId);

// Reset all memories (for testing)
memory.reset();
```

> **Note**: Async operation support is planned for future releases, current version uses synchronous API.

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
        // Implement response generation logic
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
        // Implement AI response generation logic
        return "AI response based on context: " + context;
    }
}
```

## Development

### Building from Source

```bash
# Clone repository
git clone https://github.com/langMem/mem4j.git
cd mem4j

# Build the project
mvn clean install

# Run tests
mvn test
```

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=MemoryTest

# Run tests with coverage report
mvn clean test jacoco:report
```

### Running the Example Application

The `mem4j-example` module provides a working demonstration:

```bash
# Navigate to example directory
cd mem4j-example

# Set environment variables (optional for basic demo)
export DASHSCOPE_API_KEY="your-api-key"

# Run the example application
mvn spring-boot:run
```

The example will start on `http://localhost:19090` with REST endpoints for testing memory operations.

### Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass: `mvn test`
6. Submit a pull request

### Development Notes

- **Docker Configuration**: The project includes Docker configurations for development and testing external services (Qdrant, Milvus, etc.), but Mem4j itself is a library that should be integrated into applications.
- **Example Application**: Use the `mem4j-example` module as a reference for integration patterns.
- **Testing**: Unit tests use in-memory implementations to avoid external dependencies.

## DashScope Integration

Mem4j now supports **DashScope** as a primary LLM and embedding provider, offering excellent Chinese language support and optimized performance for the Asia-Pacific region.

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
- **Low Latency**: Optimized for Asia-Pacific region with fast response times
- **Cost Effective**: Competitive pricing for enterprise usage
- **High Availability**: Enterprise-grade service with 99.9% uptime guarantee

For detailed setup instructions, see [QUICK_START.md](QUICK_START.md).

## Language Support

- **English**: [README.md](README.md) (This file)
- **中文**: [README_zh.md](README_zh.md)

## License

Apache 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This project is inspired by the original [Mem0 Python implementation](https://github.com/mem0ai/mem0) and built with [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba) for DashScope integration.
