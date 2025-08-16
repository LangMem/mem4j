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
 * Configuration properties for Mem4j components
 */
public class MemoryConfig {

	public static class VectorStoreConfig {
		private String type;
		private String url;
		private String collection;
		private VectorStoreOptions options;

		// Getters and setters
		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		public String getUrl() { return url; }
		public void setUrl(String url) { this.url = url; }
		public String getCollection() { return collection; }
		public void setCollection(String collection) { this.collection = collection; }
		public VectorStoreOptions getOptions() { return options; }
		public void setOptions(VectorStoreOptions options) { this.options = options; }
	}

	public static class VectorStoreOptions {
		private Double similarityThreshold;

		public Double getSimilarityThreshold() { return similarityThreshold; }
		public void setSimilarityThreshold(Double similarityThreshold) { this.similarityThreshold = similarityThreshold; }
	}

	public static class LlmConfig {
		private String type;
		private String apiKey;
		private String model;
		private LlmOptions options;

		// Getters and setters
		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		public String getApiKey() { return apiKey; }
		public void setApiKey(String apiKey) { this.apiKey = apiKey; }
		public String getModel() { return model; }
		public void setModel(String model) { this.model = model; }
		public LlmOptions getOptions() { return options; }
		public void setOptions(LlmOptions options) { this.options = options; }
	}

	public static class LlmOptions {
		private Integer maxTokens;
		private Double temperature;

		public Integer getMaxTokens() { return maxTokens; }
		public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }
		public Double getTemperature() { return temperature; }
		public void setTemperature(Double temperature) { this.temperature = temperature; }
	}

	public static class EmbeddingsConfig {
		private String type;
		private String model;
		private EmbeddingsOptions options;

		// Getters and setters
		public String getType() { return type; }
		public void setType(String type) { this.type = type; }
		public String getModel() { return model; }
		public void setModel(String model) { this.model = model; }
		public EmbeddingsOptions getOptions() { return options; }
		public void setOptions(EmbeddingsOptions options) { this.options = options; }
	}

	public static class EmbeddingsOptions {
		private Integer dimensions;

		public Integer getDimensions() { return dimensions; }
		public void setDimensions(Integer dimensions) { this.dimensions = dimensions; }
	}

	// Root configuration properties
	private VectorStoreConfig vectorStore;
	private LlmConfig llm;
	private EmbeddingsConfig embeddings;
	private Integer maxMemories;
	private Integer embeddingDimension;
	private Double similarityThreshold;

	// Getters and setters for root properties
	public VectorStoreConfig getVectorStore() { return vectorStore; }
	public void setVectorStore(VectorStoreConfig vectorStore) { this.vectorStore = vectorStore; }
	
	public LlmConfig getLlm() { return llm; }
	public void setLlm(LlmConfig llm) { this.llm = llm; }
	
	public EmbeddingsConfig getEmbeddings() { return embeddings; }
	public void setEmbeddings(EmbeddingsConfig embeddings) { this.embeddings = embeddings; }
	
	public Integer getMaxMemories() { return maxMemories; }
	public void setMaxMemories(Integer maxMemories) { this.maxMemories = maxMemories; }
	
	public Integer getEmbeddingDimension() { return embeddingDimension; }
	public void setEmbeddingDimension(Integer embeddingDimension) { this.embeddingDimension = embeddingDimension; }
	
	public Double getSimilarityThreshold() { return similarityThreshold; }
	public void setSimilarityThreshold(Double similarityThreshold) { this.similarityThreshold = similarityThreshold; }
}
