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

package com.langmem.embeddings;

import com.langmem.configs.MemoryConfig;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.embedding.EmbeddingResult;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * OpenAI implementation of EmbeddingService
 */

@Service
public class OpenAIEmbeddingService implements EmbeddingService {

	private static final Logger logger = LoggerFactory.getLogger(OpenAIEmbeddingService.class);

	private final OpenAiService openAiService;

	private final String model;

	private final int dimension;

	public OpenAIEmbeddingService(MemoryConfig config) {

		this.model = config.getEmbeddings().getModel();
		this.dimension = config.getEmbeddingDimension();

		this.openAiService = new OpenAiService(
				// Use LLM API key for embeddings
				config.getLlm().getApiKey(), Duration.ofSeconds(60));
	}

	@Override
	public double[] embed(String text) {

		try {
			EmbeddingRequest request = EmbeddingRequest.builder().model(model).input(List.of(text)).build();

			EmbeddingResult result = openAiService.createEmbeddings(request);

			if (!result.getData().isEmpty()) {
				List<Double> embedding = result.getData().get(0).getEmbedding();
				return embedding.stream().mapToDouble(Double::doubleValue).toArray();
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
			EmbeddingRequest request = EmbeddingRequest.builder().model(model).input(Arrays.asList(texts)).build();

			EmbeddingResult result = openAiService.createEmbeddings(request);

			return result.getData()
				.stream()
				.map(data -> data.getEmbedding().stream().mapToDouble(Double::doubleValue).toArray())
				.toArray(double[][]::new);
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

			logger.warn("OpenAI embedding service is not available", e);
			return false;
		}
	}

}
