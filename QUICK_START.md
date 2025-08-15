# 🚀 Mem4j 快速开始

## 📦 安装库到本地仓库

首先，将 Mem4j 安装到你的本地 Maven 仓库：

```bash
# 克隆仓库
git clone https://github.com/Mem4j/mem4j.git
cd mem4j

# 安装到本地Maven仓库
mvn clean install -DskipTests
```

## 🆕 创建新项目

### 1. 创建 Maven 项目

```bash
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=my-mem4j-app \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

cd my-mem4j-app
```

### 2. 修改 pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-mem4j-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring.boot.version>3.2.0</spring.boot.version>
    </properties>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
            <version>${spring.boot.version}</version>
        </dependency>

        <!-- Mem4j -->
        <dependency>
            <groupId>com.langmem.mem4j</groupId>
            <artifactId>mem4j</artifactId>
            <version>0.1.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring.boot.version}</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3. 创建主应用类

创建 `src/main/java/com/example/Application.java`：

```java
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 4. 创建服务类

创建 `src/main/java/com/example/ChatService.java`：

```java
package com.example;

import com.langmem.mem4j.memory.Memory;
import com.langmem.mem4j.memory.MemoryItem;
import com.langmem.mem4j.memory.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private Memory memory;

    public String chat(String userMessage, String userId) {
        // 搜索相关记忆
        List<MemoryItem> memories = memory.search(userMessage, userId);

        // 生成响应
        String response = generateResponse(userMessage, memories);

        // 存储对话
        List<Message> conversation = Arrays.asList(
            new Message("user", userMessage),
            new Message("assistant", response)
        );
        memory.add(conversation, userId);

        return response;
    }

    private String generateResponse(String message, List<MemoryItem> memories) {
        if (memories.isEmpty()) {
            return "Hello! How can I help you today?";
        }
        return String.format("I remember our previous conversations (%d memories). About '%s'...",
                           memories.size(), message);
    }
}
```

### 5. 创建控制器

创建 `src/main/java/com/example/ChatController.java`：

```java
package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/chat")
    public String chat(@RequestParam String message, @RequestParam String userId) {
        return chatService.chat(message, userId);
    }
}
```

### 6. 配置文件

创建 `src/main/resources/application.yml`：

```yaml
server:
  port: 8080

langmem:
  mem4j:
    vector-store:
      type: inmemory
      collection: demo-memories
      options:
        similarity-threshold: 0.6

    llm:
      type: dashscope # 或使用 openai
      api-key: ${DASHSCOPE_API_KEY:your-dashscope-api-key}
      model: qwen-turbo
      options:
        max-tokens: 1000
        temperature: 0.7

    embeddings:
      type: dashscope # 或使用 openai
      model: text-embedding-v1
      options:
        dimensions: 1536

    # 全局配置
    max-memories: 1000
    similarity-threshold: 0.6
```

## 🏃 运行应用

```bash
# 编译并运行
mvn spring-boot:run
```

## 🧪 测试 API

```bash
# 发送第一条消息
curl -X POST "http://localhost:8080/chat" \
  -d "message=Hello, I'm John" \
  -d "userId=user1"

# 发送第二条消息
curl -X POST "http://localhost:8080/chat" \
  -d "message=What's my name?" \
  -d "userId=user1"
```

## 🔧 使用真实 LLM（可选）

如果你想使用真实的 LLM 服务，修改 `application.yml`：

```yaml
langmem:
  mem4j:
  vector-store:
    type: in-memory

  llm:
    type: dashscope # 或 openai
    api-key: ${DASHSCOPE_API_KEY} # 从环境变量读取
    model: qwen-turbo

  embeddings:
    type: dashscope # 或 openai
    model: text-embedding-v1
```

然后设置环境变量：

```bash
export DASHSCOPE_API_KEY="your-api-key"
mvn spring-boot:run
```

## 📚 下一步

1. 查看 [完整集成指南](INTEGRATION_GUIDE.md) 了解更多高级功能
2. 查看 [示例项目](example-usage/) 获取更多代码示例
3. 阅读 [API 文档](docs/API.md) 了解所有可用方法

---

🎉 恭喜！你已经成功集成了 Mem4j！
