# Mem4j 配置指南

## 📋 配置概览

Mem4j 使用 Spring Boot 配置系统，支持 YAML 和 Properties 格式。所有配置都在 `mem4j` 前缀下。

## 🔧 基本配置

### 最小配置

```yaml
langmem:
  mem4j:
    vector-store:
    type: inmemory
    collection: memories

    llm:
    type: dashscope
    api-key: ${DASHSCOPE_API_KEY}
    model: qwen-turbo

    embeddings:
    type: dashscope
    model: text-embedding-v1
```

### 完整配置示例

```yaml
langmem:
  mem4j:
    # 向量存储配置
    vector-store:
    type: qdrant # 可选: inmemory, qdrant
    url: http://localhost:6333 # Qdrant 服务地址
    collection: memories # 集合名称
    options:
      similarity-threshold: 0.7 # 相似度阈值
      batch-size: 100 # 批处理大小

  # LLM 配置
    llm:
    type: dashscope # 可选: openai, dashscope
    api-key: ${DASHSCOPE_API_KEY} # API 密钥
    model: qwen-turbo # 模型名称
    options:
      max-tokens: 1000 # 最大输出token数
      temperature: 0.7 # 温度参数
      timeout: 30 # 超时时间(秒)

  # 嵌入配置
    embeddings:
    type: dashscope # 可选: openai, dashscope
    model: text-embedding-v1 # 嵌入模型
    options:
      dimensions: 1536 # 向量维度
      batch-size: 10 # 批处理大小

  # 图数据库配置 (可选)
    graph:
    type: neo4j # 图数据库类型
    uri: bolt://localhost:7687 # 连接URI
    username: neo4j # 用户名
    password: password # 密码
    options:
      database: neo4j # 数据库名称
      max-connections: 10 # 最大连接数

  # 记忆类型定义
  memory-types:
    factual: "事实性记忆 - 存储具体的事实和信息"
    episodic: "情景记忆 - 存储事件和经历"
    semantic: "语义记忆 - 存储概念和关系"
    procedural: "程序记忆 - 存储操作步骤"
    working: "工作记忆 - 临时信息"

  # 全局设置
  max-memories: 1000 # 最大记忆数量
  embedding-dimension: 1536 # 嵌入向量维度
  similarity-threshold: 0.7 # 默认相似度阈值
```

## 🌟 支持的服务提供商

### 向量存储

#### 1. 内存存储 (inmemory)

```yaml
langmem:
  mem4j:
    vector-store:
    type: inmemory
    collection: memories
```

**适用场景**: 开发、测试、演示
**优点**: 零配置，启动快速
**缺点**: 数据不持久化

#### 2. Qdrant

```yaml
langmem:
  mem4j:
    vector-store:
    type: qdrant
    url: http://localhost:6333
    collection: memories
    options:
      api-key: your-qdrant-api-key # 可选
```

**适用场景**: 生产环境
**优点**: 高性能，支持分布式
**缺点**: 需要单独部署

### LLM 服务

#### 1. DashScope (通义千问)

```yaml
langmem:
  mem4j:
    llm:
    type: dashscope
    api-key: ${DASHSCOPE_API_KEY}
    model: qwen-turbo # 或 qwen-plus, qwen-max
    options:
      max-tokens: 1000
      temperature: 0.7
```

**支持模型**:

- `qwen-turbo`: 快速响应，成本较低
- `qwen-plus`: 平衡性能与成本
- `qwen-max`: 最高性能
- `qwen-max-longcontext`: 支持长文本

#### 2. OpenAI

```yaml
langmem:
  mem4j:
    llm:
    type: openai
    api-key: ${OPENAI_API_KEY}
    model: gpt-3.5-turbo # 或 gpt-4
    options:
      max-tokens: 1000
      temperature: 0.7
      organization: your-org-id # 可选
```

### 嵌入服务

#### 1. DashScope 嵌入

```yaml
langmem:
  mem4j:
    embeddings:
    type: dashscope
    model: text-embedding-v1
    options:
      dimensions: 1536
```

#### 2. OpenAI 嵌入

```yaml
langmem:
  mem4j:
    embeddings:
    type: openai
    model: text-embedding-ada-002
    options:
      dimensions: 1536
```

## 🔒 环境变量

为了安全起见，建议使用环境变量存储敏感信息：

```bash
# DashScope
export DASHSCOPE_API_KEY="your-dashscope-api-key"

# OpenAI
export OPENAI_API_KEY="your-openai-api-key"
export OPENAI_ORGANIZATION="your-organization-id"

# Qdrant
export QDRANT_URL="http://localhost:6333"
export QDRANT_API_KEY="your-qdrant-api-key"

# Neo4j
export NEO4J_URI="bolt://localhost:7687"
export NEO4J_USERNAME="neo4j"
export NEO4J_PASSWORD="your-password"
```

## 🏢 环境特定配置

### 开发环境 (application-dev.yml)

```yaml
langmem:
  mem4j:
    vector-store:
    type: inmemory
    llm:
    type: dashscope
    api-key: ${DASHSCOPE_API_KEY}
    model: qwen-turbo
    embeddings:
    type: dashscope
    model: text-embedding-v1

logging:
  level:
    com.langmem.mem4j: DEBUG
```

### 生产环境 (application-prod.yml)

```yaml
langmem:
  mem4j:
    vector-store:
    type: qdrant
    url: ${QDRANT_URL}
    collection: production-memories
    options:
      api-key: ${QDRANT_API_KEY}

    llm:
    type: dashscope
    api-key: ${DASHSCOPE_API_KEY}
    model: qwen-plus
    options:
      timeout: 30

    embeddings:
    type: dashscope
    model: text-embedding-v1

  max-memories: 10000
  similarity-threshold: 0.8

logging:
  level:
    com.langmem.mem4j: INFO
```

## 🐳 Docker 配置

使用 Docker 时的配置示例：

```yaml
# application-docker.yml
langmem:
  mem4j:
    vector-store:
    type: qdrant
    url: http://qdrant:6333
    collection: docker-memories

    llm:
    type: dashscope
    api-key: ${DASHSCOPE_API_KEY}
    model: qwen-turbo

    embeddings:
    type: dashscope
    model: text-embedding-v1

    graph:
    type: neo4j
    uri: bolt://neo4j:7687
    username: neo4j
    password: ${NEO4J_PASSWORD}
```

## ⚙️ 性能调优

### 向量存储优化

```yaml
langmem:
  mem4j:
    vector-store:
    options:
      batch-size: 100 # 调整批处理大小
      similarity-threshold: 0.8 # 提高阈值减少结果数量
      max-results: 50 # 限制最大结果数
```

### LLM 优化

```yaml
langmem:
  mem4j:
    llm:
    options:
      max-tokens: 500 # 减少输出长度
      temperature: 0.1 # 降低随机性
      timeout: 15 # 设置合理超时
```

### 嵌入优化

```yaml
langmem:
  mem4j:
    embeddings:
    options:
      batch-size: 20 # 批量处理嵌入
      cache-size: 1000 # 启用嵌入缓存
```

## 🔍 配置验证

启动时，Mem4j 会验证配置的有效性。如果配置有误，会在日志中看到详细的错误信息。

常见配置错误：

1. API Key 未设置或无效
2. 向量存储服务不可达
3. 模型名称错误
4. 端口冲突

## 📊 监控配置

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

langmem:
  mem4j:
  monitoring:
    enabled: true
    metrics:
      memory-operations: true
      vector-store-performance: true
      llm-usage: true
```

## 🛠️ 故障排除

### 常见问题

1. **向量存储连接失败**

   - 检查 URL 和端口
   - 验证网络连接
   - 确认服务是否运行

2. **LLM API 调用失败**

   - 验证 API Key
   - 检查网络连接
   - 确认配额和限制

3. **嵌入服务错误**
   - 检查模型名称
   - 验证输入文本长度
   - 确认服务可用性

### 调试配置

```yaml
logging:
  level:
    com.langmem.mem4j: DEBUG
    com.langmem.mem4j.configs: TRACE

langmem:
  mem4j:
  debug:
    enabled: true
    log-requests: true
    log-responses: true
```
