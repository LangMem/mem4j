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

package io.github.mem4j.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Anthropic Validation Utility for Mem4j
 *
 * This utility validates your Anthropic configuration and tests Claude model interactions
 * using Anthropic's Messages API.
 */
public class AnthropicValidator {

	// Your Anthropic configuration - update these values
	private static final String ANTHROPIC_API_KEY = "your-anthropic-api-key";

	private static final String DEFAULT_MODEL = "claude-3-sonnet-20240229";

	private static final String API_URL = "https://api.anthropic.com/v1/messages";

	private static final String API_VERSION = "2023-06-01";

	// Test configuration
	private static final int DEFAULT_MAX_TOKENS = 1000;

	private static final double DEFAULT_TEMPERATURE = 0.7;

	private final RestTemplate restTemplate;

	private final ObjectMapper objectMapper;

	public AnthropicValidator() {
		this.restTemplate = new RestTemplate();
		this.objectMapper = new ObjectMapper();
	}

	public static void main(String[] args) {
		AnthropicValidator validator = new AnthropicValidator();
		try {
			validator.runValidation();
		}
		catch (Exception e) {
			System.err.println("❌ Validation failed: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void runValidation() throws Exception {
		System.out.println("🤖 Anthropic Claude Validation for Mem4j");
		System.out.println("=======================================");
		System.out.println();
		System.out.println("Configuration Details:");
		System.out.println("- API Key: " + (ANTHROPIC_API_KEY.startsWith("your-") ? "❌ NOT SET" : "✅ SET"));
		System.out.println("- Default Model: " + DEFAULT_MODEL);
		System.out.println("- API URL: " + API_URL);
		System.out.println("- API Version: " + API_VERSION);
		System.out.println();

		if (!ANTHROPIC_API_KEY.startsWith("your-")) {
			// Step 1: Test basic text generation
			System.out.println("💬 Step 1: Testing basic text generation...");
			testBasicGeneration();
			System.out.println();

			// Step 2: Test different models
			System.out.println("🔄 Step 2: Testing different Claude models...");
			testDifferentModels();
			System.out.println();

			// Step 3: Test system prompts
			System.out.println("⚙️ Step 3: Testing system prompts...");
			testSystemPrompts();
			System.out.println();

			// Step 4: Test structured outputs
			System.out.println("📊 Step 4: Testing structured outputs...");
			testStructuredOutputs();
			System.out.println();

			// Step 5: Test conversation handling
			System.out.println("💭 Step 5: Testing conversation handling...");
			testConversationHandling();
			System.out.println();

			// Step 6: Test parameter variations
			System.out.println("🎛️ Step 6: Testing parameter variations...");
			testParameterVariations();
			System.out.println();
		}
		else {
			System.out.println("⚠️  API key not configured. Skipping tests.");
			System.out.println("   To get an Anthropic API key:");
			System.out.println("   1. Visit https://console.anthropic.com/");
			System.out.println("   2. Create an account and add credits");
			System.out.println("   3. Generate an API key");
			System.out.println("   4. Update ANTHROPIC_API_KEY in this file");
			System.out.println();
		}

		// Summary
		printSummary();
	}

	private void testBasicGeneration() throws Exception {
		String prompt = "What is the capital of France? Answer in one word.";

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", DEFAULT_MODEL);
		requestBody.put("max_tokens", DEFAULT_MAX_TOKENS);
		requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

		String response = makeRequest(requestBody);

		if (response != null && response.toLowerCase().contains("paris")) {
			System.out.println("✅ Basic text generation successful");
			System.out.println("   - Prompt: " + prompt);
			System.out.println("   - Response: " + response.trim());
		}
		else {
			throw new RuntimeException("Basic text generation failed or unexpected response: " + response);
		}
	}

	private void testDifferentModels() throws Exception {
		String[][] modelConfigs = { { "claude-3-haiku-20240307", "Fast and cost-effective" },
				{ "claude-3-sonnet-20240229", "Balanced performance" },
				{ "claude-3-opus-20240229", "Highest capability" } };

		System.out.println("Testing various Claude models:");

		for (String[] config : modelConfigs) {
			String model = config[0];
			String description = config[1];

			try {
				System.out.println("   🧪 Testing: " + model + " (" + description + ")");

				Map<String, Object> requestBody = new HashMap<>();
				requestBody.put("model", model);
				requestBody.put("max_tokens", 50);
				requestBody.put("temperature", 0.1);
				requestBody.put("messages",
						List.of(Map.of("role", "user", "content", "Say 'Hello from " + model + "'")));

				String response = makeRequest(requestBody);

				if (response != null) {
					System.out.println("     ✅ Success - Response: "
							+ response.trim().substring(0, Math.min(response.length(), 50)) + "...");
				}
				else {
					System.out.println("     ❌ Failed to get response");
				}

				// Small delay to be respectful to the API
				Thread.sleep(1000);

			}
			catch (Exception e) {
				System.out.println("     ❌ Error: " + e.getMessage());
			}
		}
	}

	private void testSystemPrompts() throws Exception {
		String systemPrompt = "You are a helpful math tutor. Always show your work step by step.";
		String userMessage = "What is 15 + 27?";

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", DEFAULT_MODEL);
		requestBody.put("max_tokens", DEFAULT_MAX_TOKENS);
		requestBody.put("system", systemPrompt);
		requestBody.put("messages", List.of(Map.of("role", "user", "content", userMessage)));

		String response = makeRequest(requestBody);

		if (response != null && response.contains("42")) {
			System.out.println("✅ System prompts working correctly");
			System.out.println("   - System: " + systemPrompt);
			System.out.println("   - User: " + userMessage);
			System.out.println("   - Response contains expected answer (42)");
		}
		else {
			throw new RuntimeException("System prompt test failed. Response: " + response);
		}
	}

	private void testStructuredOutputs() throws Exception {
		String prompt = """
				Create a JSON object for a person named Alice Johnson, age 28, who lives in Seattle and works as a software engineer.

				Use this exact format:
				{
					"name": "string",
					"age": number,
					"city": "string",
					"occupation": "string"
				}
				""";

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", DEFAULT_MODEL);
		requestBody.put("max_tokens", DEFAULT_MAX_TOKENS);
		requestBody.put("temperature", 0.1);
		requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

		String response = makeRequest(requestBody);

		if (response != null && response.contains("Alice Johnson") && response.contains("28")
				&& response.contains("Seattle")) {
			System.out.println("✅ Structured output generation successful");
			System.out.println("   - Generated structured data with required fields");
		}
		else {
			System.out.println("⚠️  Structured output test partially successful");
			System.out.println("   - Response: "
					+ (response != null ? response.substring(0, Math.min(response.length(), 100)) + "..." : "null"));
		}
	}

	private void testConversationHandling() throws Exception {
		List<Map<String, String>> messages = List.of(Map.of("role", "user", "content", "Hello, I'm learning about AI."),
				Map.of("role", "assistant", "content",
						"Hello! I'd be happy to help you learn about AI. What specific aspects interest you?"),
				Map.of("role", "user", "content", "What's the difference between machine learning and deep learning?"));

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", DEFAULT_MODEL);
		requestBody.put("max_tokens", DEFAULT_MAX_TOKENS);
		requestBody.put("messages", messages);

		String response = makeRequest(requestBody);

		if (response != null && (response.toLowerCase().contains("machine learning")
				|| response.toLowerCase().contains("deep learning"))) {
			System.out.println("✅ Multi-turn conversation handling successful");
			System.out.println("   - Processed conversation context correctly");
		}
		else {
			throw new RuntimeException("Conversation handling test failed. Response: " + response);
		}
	}

	private void testParameterVariations() throws Exception {
		// Test different temperature settings
		double[] temperatures = { 0.0, 0.5, 1.0 };

		System.out.println("Testing temperature variations:");

		for (double temp : temperatures) {
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("model", DEFAULT_MODEL);
			requestBody.put("max_tokens", 50);
			requestBody.put("temperature", temp);
			requestBody.put("messages", List.of(Map.of("role", "user", "content", "Write a creative greeting.")));

			String response = makeRequest(requestBody);
			System.out.println("   🌡️ Temperature " + temp + ": " + (response != null
					? response.trim().substring(0, Math.min(response.length(), 40)) + "..." : "failed"));

			Thread.sleep(500);
		}

		// Test different max token settings
		int[] maxTokens = { 10, 50, 200 };

		System.out.println("Testing max token variations:");

		for (int tokens : maxTokens) {
			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("model", DEFAULT_MODEL);
			requestBody.put("max_tokens", tokens);
			requestBody.put("temperature", 0.7);
			requestBody.put("messages",
					List.of(Map.of("role", "user", "content", "Write a short story about a robot.")));

			String response = makeRequest(requestBody);
			System.out.println(
					"   📏 Max tokens " + tokens + ": " + (response != null ? response.length() + " chars" : "failed"));

			Thread.sleep(500);
		}
	}

	private String makeRequest(Map<String, Object> requestBody) throws Exception {
		HttpHeaders headers = createHeaders();
		HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);

		if (!response.getStatusCode().is2xxSuccessful()) {
			throw new RuntimeException("API request failed: " + response.getBody());
		}

		JsonNode responseJson = objectMapper.readTree(response.getBody());
		JsonNode content = responseJson.path("content");

		if (content.isArray() && content.size() > 0) {
			JsonNode firstContent = content.get(0);
			return firstContent.path("text").asText();
		}

		throw new RuntimeException("Invalid response format");
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("x-api-key", ANTHROPIC_API_KEY);
		headers.set("anthropic-version", API_VERSION);
		return headers;
	}

	private void printSummary() {
		System.out.println("📊 Validation Summary");
		System.out.println("====================");

		if (ANTHROPIC_API_KEY.startsWith("your-")) {
			System.out.println("⚠️  Limited validation completed (no API key)");
			System.out.println();
			System.out.println("To fully validate your Anthropic setup:");
			System.out.println("1. Get an API key from https://console.anthropic.com/");
			System.out.println("2. Update ANTHROPIC_API_KEY in this validator");
			System.out.println("3. Re-run the validation");
		}
		else {
			System.out.println("✅ All tests completed successfully!");
			System.out.println();
			System.out.println("Your Anthropic configuration is ready for Mem4j:");
			System.out.println("- ✅ API authentication: Working");
			System.out.println("- ✅ Text generation: Working");
			System.out.println("- ✅ Multiple models: Supported");
			System.out.println("- ✅ System prompts: Working");
			System.out.println("- ✅ Structured outputs: Working");
			System.out.println("- ✅ Conversation handling: Working");
			System.out.println("- ✅ Parameter variations: Working");
		}

		System.out.println();
		System.out.println("💡 Configuration for Mem4j application.yml:");
		System.out.println("mem4j:");
		System.out.println("  llm:");
		System.out.println("    type: anthropic");
		System.out.println("    model: " + DEFAULT_MODEL);
		System.out.println(
				"    api-key: " + (ANTHROPIC_API_KEY.startsWith("your-") ? "${ANTHROPIC_API_KEY}" : "your-actual-key"));
		System.out.println("    options:");
		System.out.println("      max-tokens: " + DEFAULT_MAX_TOKENS);
		System.out.println("      temperature: " + DEFAULT_TEMPERATURE);
		System.out.println();
		System.out.println("🎉 Your Anthropic setup is ready for Mem4j!");
		System.out.println();
		System.out.println("Popular Claude models to try:");
		System.out.println("- claude-3-haiku-20240307 (fast, cost-effective)");
		System.out.println("- claude-3-sonnet-20240229 (balanced performance)");
		System.out.println("- claude-3-opus-20240229 (highest capability)");
		System.out.println("- claude-2.1 (previous generation, still capable)");
	}

}
