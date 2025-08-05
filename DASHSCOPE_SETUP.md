# DashScope 集成设置

本文档说明如何在 Java Mem0 项目中使用阿里云 DashScope 服务。

## 概述

Java Mem0 现在支持使用阿里云 DashScope 作为 LLM 和 Embedding 服务提供商。DashScope 提供了强大的 AI 模型服务，包括通义千问系列模型。

## 配置步骤

### 1. 获取 DashScope API 密钥

1. 访问 [阿里云 DashScope 控制台](https://dashscope.console.aliyun.com/)
2. 注册并登录阿里云账号
3. 在控制台中创建 API 密钥
4. 复制 API 密钥

### 2. 环境变量设置

设置环境变量：

```bash
export DASHSCOPE_API_KEY="your-dashscope-api-key"
```

或者在 Windows 中：

```cmd
set DASHSCOPE_API_KEY=your-dashscope-api-key
```

### 3. 配置文件更新

在 `application.yml` 中配置 DashScope：

```yaml
mem0:
  llm:
    type: dashscope
    api-key: ${DASHSCOPE_API_KEY:your-dashscope-api-key}
    model: qwen-turbo # 或其他可用模型
    options:
      max-tokens: 1000
      temperature: 0.7

  embeddings:
    type: dashscope
    model: text-embedding-v1
    options:
      dimensions: 1536
```

## 支持的模型

### LLM 模型

- `qwen-turbo`: 通义千问 Turbo 版本
- `qwen-plus`: 通义千问 Plus 版本
- `qwen-max`: 通义千问 Max 版本
- `qwen-max-longcontext`: 通义千问 Max 长文本版本

### Embedding 模型

- `text-embedding-v1`: 文本嵌入模型

## 使用示例

### 基本使用

```java
import com.mem0.memory.Memory;
import com.mem0.memory.Message;
import java.util.Arrays;
import java.util.List;

// 初始化内存系统
Memory memory = new Memory(config, vectorStoreService, llmService, embeddingService);

// 添加记忆
List<Message> messages = Arrays.asList(
    new Message("user", "我喜欢吃披萨"),
    new Message("assistant", "好的，我会记住你喜欢吃披萨。")
);

memory.add(messages, "user123");

// 搜索记忆
List<MemoryItem> results = memory.search("我喜欢什么食物？", "user123");
```

### API 调用示例

```bash
# 添加记忆
curl -X POST http://localhost:8080/api/v1/memory/add \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {"role": "user", "content": "我的名字是张三"},
      {"role": "assistant", "content": "很高兴认识你，张三！"}
    ],
    "userId": "user123"
  }'

# 搜索记忆
curl -X GET "http://localhost:8080/api/v1/memory/search?query=我的名字是什么&userId=user123"
```

## 性能特点

- **低延迟**: DashScope 服务在亚太地区有良好的网络连接
- **高可用性**: 阿里云提供 99.9%的服务可用性保证
- **成本效益**: 相比国际服务商，DashScope 在亚太地区有更好的价格优势
- **中文优化**: 通义千问系列模型对中文有很好的理解和生成能力

## 故障排除

### 常见问题

1. **API 密钥错误**

   ```
   错误: Failed to generate response
   解决: 检查DASHSCOPE_API_KEY环境变量是否正确设置
   ```

2. **模型不存在**

   ```
   错误: Model not found
   解决: 确认使用的模型名称在DashScope中可用
   ```

3. **网络连接问题**
   ```
   错误: Connection timeout
   解决: 检查网络连接，确保可以访问dashscope.aliyuncs.com
   ```

### 调试模式

启用详细日志：

```yaml
logging:
  level:
    com.mem0: DEBUG
    org.springframework.web.client: DEBUG
```

## 与 OpenAI 对比

| 特性     | DashScope    | OpenAI       |
| -------- | ------------ | ------------ |
| 中文支持 | 优秀         | 良好         |
| 亚太延迟 | 低           | 较高         |
| 成本     | 较低         | 较高         |
| 模型选择 | 有限         | 丰富         |
| 文档质量 | 中文文档完善 | 英文文档完善 |

## 迁移指南

从 OpenAI 迁移到 DashScope：

1. 更新配置文件中的`type`字段
2. 设置 DashScope API 密钥
3. 选择对应的模型名称
4. 测试功能是否正常

## 更多资源

- [DashScope 官方文档](https://help.aliyun.com/zh/dashscope/)
- [通义千问模型介绍](https://help.aliyun.com/zh/dashscope/developer-reference/model-details)
- [Spring AI Alibaba 项目](https://github.com/alibaba/spring-ai-alibaba)
