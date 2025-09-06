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

package io.github.mem4j.llms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mem4j.config.MemoryConfigurable;
import io.github.mem4j.memory.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Anthropic implementation of LLMService using direct HTTP calls
 *
 * This service provides text generation functionality using Anthropic's Claude API.
 * Supports various Claude models including Claude-3 Sonnet, Haiku, and Opus.
 */
@Service
public class AnthropicLLMService implements LLMService {

	private static final Logger logger = LoggerFactory.getLogger(AnthropicLLMService.class);

	// Anthropic API configuration
	private static final String ANTHROPIC_API_URL = "https://api.anthropic.com/v1/messages";

	private static final String ANTHROPIC_VERSION = "2023-06-01";

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;

	private final String apiKey;

	private final String model;

	private final Integer maxTokens;

	private final Double temperature;

	/**
	 * Constructor for AnthropicLLMService
	 * @param config Memory configuration containing LLM settings
	 */
	public AnthropicLLMService(MemoryConfigurable config) {
		this.restTemplate = new RestTemplate();
		this.objectMapper = new ObjectMapper();
		this.apiKey = config.getLlm().getApiKey();
		this.model = config.getLlm().getModel() != null ? config.getLlm().getModel() : "claude-3-sonnet-20240229";
		this.maxTokens = config.getLlm().getOptions() != null && config.getLlm().getOptions().getMaxTokens() != null
				? config.getLlm().getOptions().getMaxTokens() : 1000;
		this.temperature = config.getLlm().getOptions() != null && config.getLlm().getOptions().getTemperature() != null
				? config.getLlm().getOptions().getTemperature() : 0.7;

		logger.info("Initialized AnthropicLLMService with model: {}", this.model);
	}

	@Override
	public String generate(String prompt) {
		try {
			List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", prompt));

			return generateFromMessages(messages);
		}
		catch (Exception e) {
			logger.error("Error generating response from Anthropic", e);
			throw new RuntimeException("Failed to generate response", e);
		}
	}

	@Override
	public String generate(List<Message> messages) {
		try {
			List<Map<String, String>> apiMessages = messages.stream()
				.map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
				.collect(Collectors.toList());

			return generateFromMessages(apiMessages);
		}
		catch (Exception e) {
			logger.error("Error generating response from messages", e);
			throw new RuntimeException("Failed to generate response", e);
		}
	}

	@Override
	public String generate(String systemPrompt, String userMessage) {
		try {
			// Anthropic API handles system prompts differently - we pass it as a separate
			// system parameter
			List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", userMessage));

			return generateFromMessages(messages, systemPrompt);
		}
		catch (Exception e) {
			logger.error("Error generating response with system prompt", e);
			throw new RuntimeException("Failed to generate response", e);
		}
	}

	@Override
	public String generateStructured(String prompt, String schema) {
		try {
			String structuredPrompt = String.format("""
					%s

					Please respond in the following JSON format:
					%s

					Ensure your response is valid JSON that follows the schema exactly.
					""", prompt, schema);

			List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", structuredPrompt));

			return generateFromMessages(messages);
		}
		catch (Exception e) {
			logger.error("Error generating structured response", e);
			throw new RuntimeException("Failed to generate structured response", e);
		}
	}

	@Override
	public boolean isAvailable() {
		try {
			// Simple test to check if service is available
			generate("Hello");
			return true;
		}
		catch (Exception e) {
			logger.warn("Anthropic service is not available", e);
			return false;
		}
	}

	/**
	 * Generate response from formatted messages
	 */
	private String generateFromMessages(List<Map<String, String>> messages) throws Exception {
		return generateFromMessages(messages, null);
	}

	/**
	 * Generate response from formatted messages with optional system prompt
	 */
	private String generateFromMessages(List<Map<String, String>> messages, String systemPrompt) throws Exception {
		// Build request body according to Anthropic Messages API format
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", model);
		requestBody.put("max_tokens", maxTokens);
		requestBody.put("temperature", temperature);
		requestBody.put("messages", messages);

		// Add system prompt if provided
		if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
			requestBody.put("system", systemPrompt);
		}

		HttpHeaders headers = createHeaders();
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(ANTHROPIC_API_URL, request, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException("Anthropic API returned error: " + response.getBody());
		}

		JsonNode responseJson = objectMapper.readTree(response.getBody());
		JsonNode content = responseJson.path("content");

		if (content.isArray() && !content.isEmpty()) {
			JsonNode firstContent = content.get(0);
			return firstContent.path("text").asText();
		}

		throw new RuntimeException("No response generated from Anthropic");
	}

	/**
	 * Create HTTP headers for Anthropic API requests
	 */
	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("x-api-key", apiKey);
		headers.set("anthropic-version", ANTHROPIC_VERSION);
		return headers;
	}

	/**
	 * Get the model being used
	 */
	public String getModel() {
		return model;
	}

	/**
	 * Get the max tokens setting
	 */
	public Integer getMaxTokens() {
		return maxTokens;
	}

	/**
	 * Get the temperature setting
	 */
	public Double getTemperature() {
		return temperature;
	}

}
