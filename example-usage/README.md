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

### 2. 运行示例

```bash
mvn spring-boot:run
```

### 3. 测试 API

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
- **LLM**: Mock 模式，不需要真实的 API Key
- **嵌入**: Mock 模式，使用随机向量进行演示

在生产环境中，您应该：

1. 使用真实的向量数据库 (如 Qdrant)
2. 配置真实的 LLM 服务 (如 DashScope 或 OpenAI)
3. 使用真实的嵌入服务

## 🔧 配置文件

查看 `src/main/resources/application.yml` 了解完整配置。

## 📝 核心代码

### Memory 服务注入

```java
@Autowired
private Memory memory;
```

### 添加记忆

```java
List<Message> messages = Arrays.asList(
    new Message("user", "我是张三，我喜欢吃披萨"),
    new Message("assistant", "很高兴认识你张三！我会记住你喜欢吃披萨。")
);

memory.add(messages, "user1");
```

### 搜索记忆

```java
List<MemoryItem> memories = memory.search("张三喜欢什么？", "user1");
```

### 获取所有记忆

```java
List<MemoryItem> allMemories = memory.getAll("user1", null, 50);
```
