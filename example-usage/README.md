# Mem4j Usage Example

这是一个展示如何使用 Mem4j 的示例项目。

## 🚀 快速开始

### 1. 安装依赖

首先确保主项目已经安装到本地 Maven 仓库：

```bash
cd ..
mvn clean install -DskipTests
cd example-usage
```

### 2. 配置 API Key

设置 DashScope API Key 环境变量（推荐）：

```bash
export DASHSCOPE_API_KEY="your-dashscope-api-key"
```

或者编辑 `src/main/resources/application.yml` 直接修改配置：

```yaml
langmem:
  mem4j:
    llm:
      api-key: "your-actual-api-key-here"
    embeddings:
      type: dashscope
```

### 3. 运行示例

#### 方式一：使用便捷脚本（推荐）

```bash
./start-example.sh
```

#### 方式二：使用 Maven 命令

```bash
mvn spring-boot:run
```

### 4. 测试 API

#### 方式一：使用自动化测试脚本（推荐）

```bash
# 在另一个终端窗口运行
./test-api.sh
```

#### 方式二：手动测试 API

示例应用会在 `http://localhost:9090` 启动，提供以下 API：

#### 发送消息

```bash
curl -X POST "http://localhost:9090/api/chat/send" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user1",
    "message": "我是张三，我喜欢吃披萨"
  }'
```

#### 获取用户记忆

```bash
curl "http://localhost:9090/api/chat/memories/user1"
```

#### 清空用户记忆

```bash
curl -X DELETE "http://localhost:9090/api/chat/memories/user1"
```

## 📋 配置说明

此示例使用了以下配置：

- **向量存储**: 内存模式 (`inmemory`)，适合演示和测试
- **LLM**: DashScope (`qwen-turbo`)，需要 API Key
- **嵌入**: DashScope (`text-embedding-v1`)，使用相同的 API Key

### 🔑 API Key 配置

示例默认使用 DashScope 服务，您需要：

1. 访问 [DashScope 控制台](https://dashscope.console.aliyun.com/) 获取 API Key
2. 设置环境变量 `DASHSCOPE_API_KEY`
3. 或者直接在配置文件中修改 `api-key` 值

### 🚀 替代配置

如果您想使用 OpenAI 服务，可以修改配置：

```yaml
langmem:
  mem4j:
    llm:
      type: openai
      api-key: ${OPENAI_API_KEY}
      model: gpt-3.5-turbo
    embeddings:
      type: openai
      model: text-embedding-ada-002
```

在生产环境中，建议：

1. 使用真实的向量数据库 (如 Qdrant)
2. 根据需求选择合适的 LLM 服务
3. 配置合适的相似度阈值和记忆数量限制

## 🔧 项目文件

- `src/main/resources/application.yml` - 应用配置文件
- `src/main/java/com/example/ChatController.java` - REST API 控制器
- `start-example.sh` - 便捷启动脚本
- `test-api.sh` - API 自动化测试脚本

## 📝 核心代码

### Memory 服务注入

```java
@Autowired
private Memory memory;
```

### 添加记忆

```java
import com.langmem.mem4j.memory.Message;
import java.util.Arrays;
import java.util.List;

List<Message> messages = Arrays.asList(
    new Message("user", "我是张三，我喜欢吃披萨"),
    new Message("assistant", "很高兴认识你张三！我会记住你喜欢吃披萨。")
);

memory.add(messages, "user1");
```

### 搜索记忆

```java
import com.langmem.mem4j.memory.MemoryItem;
import java.util.List;

List<MemoryItem> memories = memory.search("张三喜欢什么？", "user1");
```

### 获取所有记忆

```java
import com.langmem.mem4j.memory.MemoryItem;
import java.util.List;

List<MemoryItem> allMemories = memory.getAll("user1", null, 50);
```
