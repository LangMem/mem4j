package com.mem0.configs;

import com.mem0.embeddings.DashScopeEmbeddingService;
import com.mem0.embeddings.EmbeddingService;
import com.mem0.embeddings.OpenAIEmbeddingService;
import com.mem0.llms.DashScopeLLMService;
import com.mem0.llms.LLMService;
import com.mem0.llms.OpenAILLMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Factory for creating LLM and Embedding services based on configuration
 */
@Configuration
public class ServiceFactory {

  private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);

  @Autowired
  private MemoryConfig memoryConfig;

  @Bean
  @Primary
  public LLMService llmService() {
    String llmType = memoryConfig.getLlm().getType();
    logger.info("Creating LLM service of type: {}", llmType);

    return switch (llmType.toLowerCase()) {
      case "dashscope" -> new DashScopeLLMService(memoryConfig);
      case "openai" -> new OpenAILLMService(memoryConfig);
      default -> {
        logger.warn("Unknown LLM type: {}, falling back to OpenAI", llmType);
        yield new OpenAILLMService(memoryConfig);
      }
    };
  }

  @Bean
  @Primary
  public EmbeddingService embeddingService() {
    String embeddingType = memoryConfig.getEmbeddings().getType();
    logger.info("Creating Embedding service of type: {}", embeddingType);

    return switch (embeddingType.toLowerCase()) {
      case "dashscope" -> new DashScopeEmbeddingService(memoryConfig);
      case "openai" -> new OpenAIEmbeddingService(memoryConfig);
      default -> {
        logger.warn("Unknown embedding type: {}, falling back to OpenAI", embeddingType);
        yield new OpenAIEmbeddingService(memoryConfig);
      }
    };
  }
}