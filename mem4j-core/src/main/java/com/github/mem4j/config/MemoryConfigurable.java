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

package com.github.mem4j.config;

/**
 * Interface for configurable Mem4j components
 */
public interface MemoryConfigurableurable {
    
    /**
     * Vector store configuration interface
     */
    interface VectorStore {
        String getType();
        String getUrl();
        String getCollection();
        VectorStoreOptions getOptions();
    }
    
    interface VectorStoreOptions {
        Double getSimilarityThreshold();
    }
    
    /**
     * LLM configuration interface
     */
    interface Llm {
        String getType();
        String getApiKey();
        String getModel();
        LlmOptions getOptions();
    }
    
    interface LlmOptions {
        Integer getMaxTokens();
        Double getTemperature();
    }
    
    /**
     * Embeddings configuration interface
     */
    interface Embeddings {
        String getType();
        String getModel();
        EmbeddingsOptions getOptions();
    }
    
    interface EmbeddingsOptions {
        Integer getDimensions();
    }
    
    // Root configuration methods
    VectorStore getVectorStore();
    Llm getLlm();
    Embeddings getEmbeddings();
    Integer getMaxMemories();
    Integer getEmbeddingDimension();
    Double getSimilarityThreshold();
}
