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

import com.langmem.embeddings.EmbeddingService;
import com.langmem.llms.LLMService;
import com.langmem.memory.Memory;
import com.langmem.vectorstores.VectorStoreService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Mem0 Memory
 * 
 * This configuration will automatically set up Memory beans when the library is
 * included
 * in a Spring Boot application.
 */

@AutoConfiguration
@Import(ServiceFactory.class)
@ConditionalOnClass(Memory.class)
@EnableConfigurationProperties(MemoryConfig.class)
public class MemoryAutoConfiguration {

  /**
   * Creates a Memory bean if none exists
   */
  @Bean
  @ConditionalOnMissingBean
  public Memory memory(

      MemoryConfig memoryConfig,
      VectorStoreService vectorStoreService,
      LLMService llmService,
      EmbeddingService embeddingService) {

    return new Memory(memoryConfig, vectorStoreService, llmService, embeddingService);
  }

}
