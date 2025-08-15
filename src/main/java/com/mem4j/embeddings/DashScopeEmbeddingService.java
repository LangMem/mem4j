/*
 * Copyright 2025-2026 the original author or authors.
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

package com.mem4j.embeddings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mem4j.configs.MemoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DashScope implementation of EmbeddingService using direct HTTP calls todo：直接替换为 Spring
 * AI Alibaba 的 DashScope Embedding Client？
 */

@Service
public class DashScopeEmbeddingService implements EmbeddingService {

	private static final Logger logger = LoggerFactory.getLogger(DashScopeEmbeddingService.class);

	private static final String DASHSCOPE_EMBEDDING_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/embeddings/text-embedding/text-embedding";

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;

	private final String apiKey;

	private final String model;

	private final int dimension;

	public DashScopeEmbeddingService(MemoryConfig config) {
		this.restTemplate = new RestTemplate();
		this.objectMapper = new ObjectMapper();
		this.apiKey = config.getLlm().getApiKey();
		this.model = config.getEmbeddings().getModel();
		this.dimension = config.getEmbeddingDimension();
	}

	@Override
	public double[] embed(String text) {
		try {
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("model", model);

			Map<String, Object> input = new HashMap<>();
			input.put("texts", List.of(text));
			requestBody.put("input", input);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + apiKey);

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(DASHSCOPE_EMBEDDING_API_URL, request,
					String.class);

			JsonNode responseJson = objectMapper.readTree(response.getBody());
			JsonNode embeddings = responseJson.path("output").path("embeddings");

			if (embeddings.isArray() && embeddings.size() > 0) {
				JsonNode firstEmbedding = embeddings.get(0);
				JsonNode embeddingArray = firstEmbedding.path("embedding");

				double[] result = new double[embeddingArray.size()];
				for (int i = 0; i < embeddingArray.size(); i++) {
					result[i] = embeddingArray.get(i).asDouble();
				}
				return result;
			}

			throw new RuntimeException("No embedding generated");
		}
		catch (Exception e) {
			logger.error("Error generating embedding", e);
			throw new RuntimeException("Failed to generate embedding", e);
		}
	}

	@Override
	public double[][] embed(String[] texts) {
		try {
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("model", model);

			Map<String, Object> input = new HashMap<>();
			input.put("texts", Arrays.asList(texts));
			requestBody.put("input", input);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + apiKey);

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
			ResponseEntity<String> response = restTemplate.postForEntity(DASHSCOPE_EMBEDDING_API_URL, request,
					String.class);

			JsonNode responseJson = objectMapper.readTree(response.getBody());
			JsonNode embeddings = responseJson.path("output").path("embeddings");

			double[][] result = new double[embeddings.size()][];
			for (int i = 0; i < embeddings.size(); i++) {
				JsonNode embeddingArray = embeddings.get(i).path("embedding");
				result[i] = new double[embeddingArray.size()];
				for (int j = 0; j < embeddingArray.size(); j++) {
					result[i][j] = embeddingArray.get(j).asDouble();
				}
			}

			return result;
		}
		catch (Exception e) {
			logger.error("Error generating embeddings", e);
			throw new RuntimeException("Failed to generate embeddings", e);
		}
	}

	@Override
	public int getDimension() {
		return dimension;
	}

	@Override
	public boolean isAvailable() {
		try {
			// Simple test to check if service is available
			embed("test");
			return true;
		}
		catch (Exception e) {
			logger.warn("DashScope embedding service is not available", e);
			return false;
		}
	}

}
