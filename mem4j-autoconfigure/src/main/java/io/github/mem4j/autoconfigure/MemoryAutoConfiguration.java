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

package io.github.mem4j.autoconfigure;

import io.github.mem4j.embeddings.DashScopeEmbeddingService;
import io.github.mem4j.embeddings.EmbeddingService;
import io.github.mem4j.embeddings.OpenAIEmbeddingService;
import io.github.mem4j.llms.DashScopeLLMService;
import io.github.mem4j.llms.LLMService;
import io.github.mem4j.llms.OpenAILLMService;
import io.github.mem4j.memory.Memory;
import io.github.mem4j.vectorstores.InMemoryVectorStoreService;
import io.github.mem4j.vectorstores.MilvusVectorStoreService;
import io.github.mem4j.vectorstores.QdrantVectorStoreService;
import io.github.mem4j.vectorstores.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Lang Memory system
 * <p>
 * This configuration will automatically set up Memory beans when the library is included
 * in a Spring Boot application. It provides:
 * <ul>
 * <li>Memory core bean with all required dependencies</li>
 * <li>Default InMemoryVectorStore when no other vector store is configured</li>
 * <li>Conditional VectorStore beans based on configuration type</li>
 * </ul>
 * <p>
 * LLM and Embedding services are automatically selected based on configuration.
 */

@AutoConfiguration
@ConditionalOnClass(Memory.class)
@EnableConfigurationProperties(MemoryConfig.class)
public class MemoryAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MemoryAutoConfiguration.class);

    /**
     * Creates the main Memory bean with all required dependencies. This bean will be
     * created only if no other Memory bean exists.
     *
     * @param memoryConfig       the memory configuration properties
     * @param vectorStoreService the vector store service (auto-selected based on config)
     * @param llmService         the LLM service (auto-selected based on config)
     * @param embeddingService   the embedding service (auto-selected based on config)
     * @return configured Memory instance
     */
    @Bean
    @ConditionalOnMissingBean
    public Memory memory(MemoryConfig memoryConfig, VectorStoreService vectorStoreService, LLMService llmService, EmbeddingService embeddingService) {
        logger.info("Creating Memory bean with vector store type: {}", memoryConfig.getVectorStore().getType());
        return new Memory(memoryConfig, vectorStoreService, llmService, embeddingService);
    }

    /**
     * Creates an InMemoryVectorStore service when no other VectorStore is configured or
     * when explicitly configured to use 'inmemory' type.
     *
     * @return InMemoryVectorStoreService instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "mem4j.vector-store", name = "type", havingValue = "inmemory", matchIfMissing = true)
    public VectorStoreService inMemoryVectorStoreService() {
        logger.info("Creating InMemoryVectorStoreService");
        return new InMemoryVectorStoreService();
    }

    /**
     * Creates a Qdrant VectorStore service when configured to use 'qdrant' type. This
     * bean will only be created if the Qdrant client classes are available on the
     * classpath.
     *
     * @param memoryConfig the memory configuration properties
     * @return QdrantVectorStoreService instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "mem4j.vector-store", name = "type", havingValue = "qdrant")
    @ConditionalOnClass(name = "io.qdrant.client.QdrantClient")
    public VectorStoreService qdrantVectorStoreService(MemoryConfig memoryConfig) {
        logger.info("Creating QdrantVectorStoreService");
        return new QdrantVectorStoreService(memoryConfig);
    }

    /**
     * Creates a Milvus VectorStore service when configured to use 'milvus' type. This
     * bean will only be created if the Milvus client classes are available on the
     * classpath.
     *
     * @param memoryConfig the memory configuration properties
     * @return MilvusVectorStoreService instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "mem4j.vector-store", name = "type", havingValue = "milvus", matchIfMissing = true)
    @ConditionalOnClass(name = "io.milvus.client.MilvusClient")
    public VectorStoreService milvusVectorStoreService(MemoryConfig memoryConfig) {
        logger.info("Creating MilvusVectorStoreService");
        return new MilvusVectorStoreService(memoryConfig);
    }

    /**
     * Creates the appropriate LLM service based on configuration. Supports OpenAI and
     * DashScope implementations.
     *
     * @param memoryConfig the memory configuration properties
     * @return LLMService instance based on configuration
     */
    @Bean
    @ConditionalOnMissingBean
    public LLMService llmService(MemoryConfig memoryConfig) {
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

    /**
     * Creates the appropriate Embedding service based on configuration. Supports OpenAI
     * and DashScope implementations.
     *
     * @param memoryConfig the memory configuration properties
     * @return EmbeddingService instance based on configuration
     */
    @Bean
    @ConditionalOnMissingBean
    public EmbeddingService embeddingService(MemoryConfig memoryConfig) {
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
