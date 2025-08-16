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

package com.github.mem4j.configs;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * Configuration class for Mem4j system
 */

@Validated
@ConfigurationProperties(prefix = MemoryConfig.CONFIG_PREFIX)
public class MemoryConfig {

	public static final String CONFIG_PREFIX = "github.mem4j";

	@NotNull
	private VectorStoreConfig vectorStore;

	@NotNull
	private LLMConfig llm;

	@NotNull
	private EmbeddingConfig embeddings;

	private GraphConfig graph;

	@JsonProperty("memory-types")
	private Map<String, String> memoryTypes;

	@JsonProperty("max-memories")
	private Integer maxMemories = 1000;

	@JsonProperty("embedding-dimension")
	private Integer embeddingDimension = 1536;

	@JsonProperty("similarity-threshold")
	private Double similarityThreshold = 0.7;

	public static class VectorStoreConfig {

		@NotBlank
		private String type;

		// @NotBlank
		private String url;

		@NotBlank
		private String collection;

		private Map<String, Object> options;

		// Getters and Setters
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getCollection() {
			return collection;
		}

		public void setCollection(String collection) {
			this.collection = collection;
		}

		public Map<String, Object> getOptions() {
			return options;
		}

		public void setOptions(Map<String, Object> options) {
			this.options = options;
		}

	}

	public static class LLMConfig {

		@NotBlank
		private String type;

		@NotBlank
		private String apiKey;

		@NotBlank
		private String model;

		private Map<String, Object> options;

		// Getters and Setters
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public Map<String, Object> getOptions() {
			return options;
		}

		public void setOptions(Map<String, Object> options) {
			this.options = options;
		}

	}

	public static class EmbeddingConfig {

		@NotBlank
		private String type;

		@NotBlank
		private String model;

		private Map<String, Object> options;

		// Getters and Setters
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public Map<String, Object> getOptions() {
			return options;
		}

		public void setOptions(Map<String, Object> options) {
			this.options = options;
		}

	}

	public static class GraphConfig {

		private String type;

		private String uri;

		private String username;

		private String password;

		private Map<String, Object> options;

		// Getters and Setters
		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public Map<String, Object> getOptions() {
			return options;
		}

		public void setOptions(Map<String, Object> options) {
			this.options = options;
		}

	}

	// Getters and Setters
	public VectorStoreConfig getVectorStore() {
		return vectorStore;
	}

	public void setVectorStore(VectorStoreConfig vectorStore) {
		this.vectorStore = vectorStore;
	}

	public LLMConfig getLlm() {
		return llm;
	}

	public void setLlm(LLMConfig llm) {
		this.llm = llm;
	}

	public EmbeddingConfig getEmbeddings() {
		return embeddings;
	}

	public void setEmbeddings(EmbeddingConfig embeddings) {
		this.embeddings = embeddings;
	}

	public GraphConfig getGraph() {
		return graph;
	}

	public void setGraph(GraphConfig graph) {
		this.graph = graph;
	}

	public Map<String, String> getMemoryTypes() {
		return memoryTypes;
	}

	public void setMemoryTypes(Map<String, String> memoryTypes) {
		this.memoryTypes = memoryTypes;
	}

	public Integer getMaxMemories() {
		return maxMemories;
	}

	public void setMaxMemories(Integer maxMemories) {
		this.maxMemories = maxMemories;
	}

	public Integer getEmbeddingDimension() {
		return embeddingDimension;
	}

	public void setEmbeddingDimension(Integer embeddingDimension) {
		this.embeddingDimension = embeddingDimension;
	}

	public Double getSimilarityThreshold() {
		return similarityThreshold;
	}

	public void setSimilarityThreshold(Double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
	}

}
