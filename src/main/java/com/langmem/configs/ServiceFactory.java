/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.langmem.configs;

import com.langmem.embeddings.DashScopeEmbeddingService;
import com.langmem.embeddings.EmbeddingService;
import com.langmem.embeddings.OpenAIEmbeddingService;
import com.langmem.llms.DashScopeLLMService;
import com.langmem.llms.LLMService;
import com.langmem.llms.OpenAILLMService;
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

  private final MemoryConfig memoryConfig;

  public ServiceFactory(MemoryConfig memoryConfig) {
      this.memoryConfig = memoryConfig;
  }

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
