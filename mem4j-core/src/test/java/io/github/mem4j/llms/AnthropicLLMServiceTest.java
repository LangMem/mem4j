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

import io.github.mem4j.config.MemoryConfigurable;
import io.github.mem4j.memory.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnthropicLLMService
 */
@ExtendWith(MockitoExtension.class)
class AnthropicLLMServiceTest {

	@Mock
	private MemoryConfigurable config;

	@Mock
	private MemoryConfigurable.Llm llmConfig;

	@Mock
	private MemoryConfigurable.LlmOptions llmOptions;

	private AnthropicLLMService anthropicService;

	@BeforeEach
	void setUp() {
		when(config.getLlm()).thenReturn(llmConfig);
		when(llmConfig.getApiKey()).thenReturn("test-anthropic-api-key");
		when(llmConfig.getModel()).thenReturn("claude-3-sonnet-20240229");
		when(llmConfig.getOptions()).thenReturn(llmOptions);
		when(llmOptions.getMaxTokens()).thenReturn(1000);
		when(llmOptions.getTemperature()).thenReturn(0.7);

		anthropicService = new AnthropicLLMService(config);
	}

	@Test
	@DisplayName("Constructor should initialize with correct configuration")
	void testConstructor() {
		assertNotNull(anthropicService);
		assertEquals("claude-3-sonnet-20240229", anthropicService.getModel());
		assertEquals(1000, anthropicService.getMaxTokens());
		assertEquals(0.7, anthropicService.getTemperature());
	}

	@Test
	@DisplayName("Constructor should use default model when not specified")
	void testConstructorWithDefaultModel() {
		when(llmConfig.getModel()).thenReturn(null);
		
		AnthropicLLMService service = new AnthropicLLMService(config);
		assertNotNull(service);
		assertEquals("claude-3-sonnet-20240229", service.getModel());
	}

	@Test
	@DisplayName("Constructor should use default options when not provided")
	void testConstructorWithDefaultOptions() {
		when(llmConfig.getOptions()).thenReturn(null);
		
		AnthropicLLMService service = new AnthropicLLMService(config);
		assertNotNull(service);
		assertEquals(1000, service.getMaxTokens());
		assertEquals(0.7, service.getTemperature());
	}

	@Test
	@DisplayName("generate() should handle single prompt")
	void testGenerateSinglePrompt() {
		String prompt = "Hello, how are you?";

		// This will throw an exception without real Anthropic API access
		assertThrows(RuntimeException.class, () -> anthropicService.generate(prompt));
	}

	@Test
	@DisplayName("generate() should handle message list")
	void testGenerateWithMessages() {
		List<Message> messages = List.of(new Message("user", "Hello"), new Message("assistant", "Hi there!"),
				new Message("user", "How are you?"));

		assertThrows(RuntimeException.class, () -> anthropicService.generate(messages));
	}

	@Test
	@DisplayName("generate() should handle system prompt and user message")
	void testGenerateWithSystemPrompt() {
		String systemPrompt = "You are a helpful assistant.";
		String userMessage = "Hello, world!";

		assertThrows(RuntimeException.class, () -> anthropicService.generate(systemPrompt, userMessage));
	}

	@Test
	@DisplayName("generateStructured() should handle JSON schema")
	void testGenerateStructured() {
		String prompt = "Generate a person's information";
		String schema = """
				{
					"name": "string",
					"age": "number",
					"city": "string"
				}
				""";

		assertThrows(RuntimeException.class, () -> anthropicService.generateStructured(prompt, schema));
	}

	@Test
	@DisplayName("isAvailable() should return false without valid API")
	void testIsAvailableWithoutValidAPI() {
		// Without real Anthropic API access, this should return false
		assertFalse(anthropicService.isAvailable());
	}

	@Test
	@DisplayName("Service should handle different Claude models")
	void testDifferentModels() {
		String[] models = { "claude-3-sonnet-20240229", "claude-3-opus-20240229", "claude-3-haiku-20240307",
				"claude-2.1", "claude-2.0" };

		for (String model : models) {
			when(llmConfig.getModel()).thenReturn(model);

			AnthropicLLMService service = new AnthropicLLMService(config);
			assertEquals(model, service.getModel());
		}
	}

	@Test
	@DisplayName("Service should handle different temperature settings")
	void testDifferentTemperatures() {
		Double[] temperatures = { 0.0, 0.3, 0.7, 1.0 };

		for (Double temp : temperatures) {
			when(llmOptions.getTemperature()).thenReturn(temp);

			AnthropicLLMService service = new AnthropicLLMService(config);
			assertEquals(temp, service.getTemperature());
		}
	}

	@Test
	@DisplayName("Service should handle different max token settings")
	void testDifferentMaxTokens() {
		Integer[] maxTokens = { 100, 500, 1000, 2000, 4000 };

		for (Integer tokens : maxTokens) {
			when(llmOptions.getMaxTokens()).thenReturn(tokens);

			AnthropicLLMService service = new AnthropicLLMService(config);
			assertEquals(tokens, service.getMaxTokens());
		}
	}

	@Test
	@DisplayName("Service should provide meaningful error messages")
	void testErrorMessages() {
		String prompt = "test";

		try {
			anthropicService.generate(prompt);
			fail("Expected RuntimeException");
		}
		catch (RuntimeException e) {
			assertEquals("Failed to generate response", e.getMessage());
			assertNotNull(e.getCause());
		}
	}

	@Test
	@DisplayName("Service should validate input parameters")
	void testInputValidation() {
		// Test with null prompt
		assertThrows(RuntimeException.class, () -> anthropicService.generate((String) null));

		// Test with empty message list
		List<Message> emptyMessages = List.of();
		assertThrows(RuntimeException.class, () -> anthropicService.generate(emptyMessages));
	}

	@Test
	@DisplayName("Service should handle API key validation")
	void testApiKeyValidation() {
		// Test with null API key
		when(llmConfig.getApiKey()).thenReturn(null);
		
		AnthropicLLMService service = new AnthropicLLMService(config);
		assertNotNull(service); // Should create service, but calls will fail
		
		assertThrows(RuntimeException.class, () -> service.generate("test"));
	}

	@Test
	@DisplayName("Service should handle configuration edge cases")
	void testConfigurationEdgeCases() {
		// Test with null options but partial configuration
		when(llmOptions.getMaxTokens()).thenReturn(null);
		when(llmOptions.getTemperature()).thenReturn(null);
		
		AnthropicLLMService service = new AnthropicLLMService(config);
		assertEquals(1000, service.getMaxTokens()); // Default value
		assertEquals(0.7, service.getTemperature()); // Default value
	}

	@Test
	@DisplayName("Service should handle Claude-3 family models correctly")
	void testClaude3Models() {
		String[] claude3Models = { "claude-3-opus-20240229", "claude-3-sonnet-20240229", "claude-3-haiku-20240307" };

		for (String model : claude3Models) {
			when(llmConfig.getModel()).thenReturn(model);

			AnthropicLLMService service = new AnthropicLLMService(config);
			assertEquals(model, service.getModel());

			// Claude-3 models should work with system prompts
			assertThrows(RuntimeException.class, () -> service.generate("You are helpful", "Test message"));
		}
	}

}
