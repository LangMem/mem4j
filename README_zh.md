# Mem4j

<p align="center">
  <img src="docs/images/mem4j-logo.png" width="200px" alt="Mem4j Logo">
</p>

<p align="center">
  <strong>🧠 个性化AI的记忆层</strong>
</p>

<p align="center">
  <strong>⚡ AI智能体的长期记忆 - Java实现</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/version-0.1.0-blue.svg" alt="Version">
  <img src="https://img.shields.io/badge/Java-17+-orange.svg" alt="Java Version">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2+-green.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/license-Apache%202.0-red.svg" alt="License">
</p>

## 简介

Mem4j 是一个为 AI 智能体和应用提供长期记忆能力的 Java 库。它提供智能记忆层功能，通过记住用户偏好、适应个人需求以及从对话历史中持续学习，实现个性化的 AI 交互。

> **注意**: Mem4j 是一个库，而不是独立的应用程序。要查看实际效果，请查看 [mem4j-example](mem4j-example/) 模块，它演示了如何在 Spring Boot 应用程序中集成和使用 Mem4j。

### 主要特性

- **多层级记忆**: 支持多种记忆类型（事实型、情景型、语义型、程序型、工作记忆）
- **开发者友好**: 直观的 API、Spring Boot 集成和全面的文档
- **向量存储支持**: 支持多种向量数据库集成（InMemory、Qdrant、Milvus）
- **LLM 集成**: 支持多种 LLM 提供商（DashScope、OpenAI）
- **异步支持**: 专为异步操作设计
- **中文语言支持**: 通过 DashScope 集成优化中文语言处理
- **模块化架构**: 清晰的关注点分离，支持可插拔组件

## 快速开始

> 🚀 **快速开始指南**: 查看 [QUICK_START.md](QUICK_START.md) 获取详细的项目设置和使用说明。

> 💡 **示例项目**: 查看 [mem4j-example](mem4j-example/) 获取完整的工作示例。

### 前置要求

- Java 17 或更高版本
- Maven 3.6 或更高版本

### 安装

在您的项目中添加 Mem4j Spring Boot starter：

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

**从源码构建:**

1. 克隆仓库：

```bash
git clone https://github.com/langMem/mem4j.git
cd mem4j
```

1. 构建并安装到本地仓库：

```bash
mvn clean install
```

### 运行示例

要查看 Mem4j 的实际效果，可以运行包含的示例应用程序：

```bash
# 导航到示例目录
cd mem4j-example

# 设置DashScope API密钥（演示可选）
export DASHSCOPE_API_KEY="your-api-key"

# 运行示例
mvn spring-boot:run
```

示例将在 `http://localhost:19090` 启动 Web 服务器，提供端点来测试记忆操作。

### 基本用法

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
        // 添加记忆
        List<Message> messages = Arrays.asList(
            new Message("user", "你好，我是小明，我喜欢披萨"),
            new Message("assistant", "很高兴认识你小明！我会记住你喜欢披萨。")
        );

        memory.add(messages, "xiaoming_user");

        // 搜索记忆
        List<MemoryItem> results = memory.search("小明喜欢什么？", "xiaoming_user");
    }
}
```

## 项目结构

Mem4j 是一个多模块的 Maven 项目：

- **mem4j-core**: 核心记忆管理功能
- **mem4j-autoconfigure**: Spring Boot 自动配置
- **mem4j-spring-boot-starter**: Spring Boot starter，便于集成
- **mem4j-example**: 演示用法的示例应用程序
- **mem4j-bom**: 依赖管理的物料清单

## 架构

### 核心组件

```text
mem4j-core/src/main/java/io/github/mem4j/
├── memory/           # 核心记忆管理
├── vectorstores/     # 向量数据库集成
├── llms/            # LLM提供商集成
├── embeddings/      # 嵌入模型集成
└── config/          # 配置管理
```

### 支持的集成

#### 向量存储

- ✅ **InMemory** - 内存存储实现
- ✅ **Qdrant** - 完整实现，支持搜索和持久化
- ✅ **Milvus** - 完整实现，支持向量搜索功能
- 📋 **Elasticsearch** - 计划实现
- 📋 **Weaviate** - 计划实现
- 📋 **Pinecone** - 计划实现
- 📋 **Chroma** - 计划实现

#### LLM 提供商

- ✅ **DashScope** - 完整实现，支持阿里云 DashScope API
- ✅ **OpenAI** - 完整实现，支持 OpenAI API
- 📋 **Anthropic** - 计划实现
- 📋 **Azure OpenAI** - 计划实现
- 📋 **AWS Bedrock** - 计划实现

#### 嵌入模型

- ✅ **DashScope 嵌入** - 完整实现，支持 text-embedding-v1 模型
- ✅ **OpenAI 嵌入** - 完整实现，支持各种嵌入模型
- 📋 **HuggingFace** - 计划实现
- 📋 **VertexAI** - 计划实现

## 配置

> 📖 **详细配置指南**: 查看 [CONFIGURATION.md](docs/CONFIGURATION.md) 了解完整的配置选项和最佳实践。

### 应用属性

```yaml
# 记忆配置
mem4j:
  vector-store:
    type: qdrant # 选项: inmemory, qdrant, milvus
    url: http://localhost:6333
    collection: memories
    options:
      similarity-threshold: 0.7

  llm:
    type: dashscope # 选项: openai, dashscope
    api-key: ${DASHSCOPE_API_KEY}
    model: qwen-turbo
    options:
      max-tokens: 1000
      temperature: 0.7

  embeddings:
    type: dashscope # 选项: openai, dashscope
    model: text-embedding-v1
    options:
      dimensions: 1536

  # 全局配置
  max-memories: 1000
  embedding-dimension: 1536
  similarity-threshold: 0.7
```

### 环境变量

```bash
export DASHSCOPE_API_KEY="your-dashscope-api-key"
export OPENAI_API_KEY="your-openai-api-key"
export QDRANT_URL="http://localhost:6333"
export MILVUS_URL="localhost:19530"
```

## API 参考

### 记忆操作

#### 添加记忆

```java
// 添加对话记忆（带推理）
memory.add(messages, userId);

// 添加带元数据和自定义记忆类型
memory.add(messages, userId, metadata, true, MemoryType.FACTUAL);

// 不带推理添加（更快，无LLM处理）
memory.add(messages, userId, metadata, false, MemoryType.FACTUAL);
```

#### 搜索记忆

```java
// 基本搜索
List<MemoryItem> results = memory.search(query, userId);

// 带过滤器和自定义参数的搜索
Map<String, Object> filters = Map.of("agent_id", "chatbot");
List<MemoryItem> results = memory.search(query, userId, filters, 10, 0.7);

// 获取用户的所有记忆
List<MemoryItem> allMemories = memory.getAll(userId, filters, 100);
```

#### 更新记忆

```java
// 更新现有记忆
memory.update(memoryId, updatedData);
```

#### 删除记忆

```java
// 删除特定记忆
memory.delete(memoryId);

// 删除用户的所有记忆
memory.deleteAll(userId);
```

> **注意**: 这是版本 0.1.0，目前正在积极开发中。API 在未来版本中可能会发生变化。

### 其他操作

```java
// 根据ID获取特定记忆
MemoryItem memory = memory.get(memoryId);

// 重置所有记忆（用于测试）
memory.reset();
```

> **注意**: 异步操作支持计划在未来版本中实现，当前版本使用同步 API。

## 示例

### 客户支持机器人

```java
@Service
public class CustomerSupportService {

    @Autowired
    private Memory memory;

    public String handleCustomerQuery(String query, String customerId) {
        // 搜索相关记忆
        List<MemoryItem> memories = memory.search(query, customerId);

        // 从记忆构建上下文
        String context = memories.stream()
            .map(MemoryItem::getContent)
            .collect(Collectors.joining("\n"));

        // 基于上下文生成响应
        return generateResponse(query, context);
    }

    private String generateResponse(String query, String context) {
        // 实现响应生成逻辑
        return "基于以下内容的响应: " + context;
    }
}
```

### 带记忆的 AI 助手

```java
@Component
public class AIAssistant {

    @Autowired
    private Memory memory;

    public String chat(String message, String userId) {
        // 获取相关记忆
        List<MemoryItem> memories = memory.search(message, userId);

        // 构建对话上下文
        String memoryContext = buildMemoryContext(memories);

        // 生成响应
        String response = generateResponse(message, memoryContext);

        // 存储对话
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
        return "基于上下文的AI响应: " + context;
    }
}
```

## 开发

### 从源码构建

```bash
# 克隆仓库
git clone https://github.com/langMem/mem4j.git
cd mem4j

# 构建项目
mvn clean install

# 运行测试
mvn test
```

### 运行测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=MemoryTest

# 运行测试并生成覆盖率报告
mvn clean test jacoco:report
```

### 运行示例应用程序

`mem4j-example` 模块提供了工作演示：

```bash
# 导航到示例目录
cd mem4j-example

# 设置环境变量（基本演示可选）
export DASHSCOPE_API_KEY="your-api-key"

# 运行示例应用程序
mvn spring-boot:run
```

示例将在 `http://localhost:19090` 启动，提供用于测试记忆操作的 REST 端点。

### 贡献

1. Fork 仓库
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 进行更改
4. 为新功能添加测试
5. 确保所有测试通过：`mvn test`
6. 提交拉取请求

### 开发注意事项

- **Docker 配置**: 项目包含用于开发和测试外部服务（Qdrant、Milvus 等）的 Docker 配置，但 Mem4j 本身是一个应该集成到应用程序中的库。
- **示例应用程序**: 使用 `mem4j-example` 模块作为集成模式的参考。
- **测试**: 单元测试使用内存实现来避免外部依赖。

## DashScope 集成

Mem4j 现在支持**DashScope**作为主要的 LLM 和嵌入提供商，为亚太地区提供出色的中文语言支持和优化性能。

### 快速设置

1. **获取 DashScope API 密钥**：

   - 访问 [DashScope 控制台](https://dashscope.console.aliyun.com/)
   - 创建 API 密钥

2. **配置环境**：

   ```bash
   export DASHSCOPE_API_KEY="your-dashscope-api-key"
   ```

3. **更新配置**：

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

### 支持的模型

- **LLM**: `qwen-turbo`, `qwen-plus`, `qwen-max`, `qwen-max-longcontext`
- **嵌入**: `text-embedding-v1`

### 优势

- **中文语言优化**: 出色的中文文本理解和生成
- **低延迟**: 为亚太地区优化，响应时间快
- **成本效益**: 企业使用的竞争性价格
- **高可用性**: 企业级服务，99.9%正常运行时间保证

详细设置说明，请参阅 [QUICK_START.md](QUICK_START.md)。

## 语言支持

- **中文**: [README_zh.md](README_zh.md) (本文件)
- **English**: [README.md](README.md)

## 许可证

Apache 2.0 - 详见 [LICENSE](LICENSE) 文件。

## 致谢

本项目受到原始 [Mem0 Python 实现](https://github.com/mem0ai/mem0) 的启发，并使用 [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba) 进行 DashScope 集成。
