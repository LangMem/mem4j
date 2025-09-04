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
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AnthropicLLMService
 *
 * These tests require a real Anthropic API key and will make actual API calls. Set the
 * ANTHROPIC_API_KEY environment variable to run these tests.
 */
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class AnthropicLLMServiceIntegrationTest {

	private AnthropicLLMService anthropicService;

	private String apiKey;

	@BeforeEach
	void setUp() {
		apiKey = System.getenv("ANTHROPIC_API_KEY");
		assertNotNull(apiKey, "ANTHROPIC_API_KEY environment variable must be set for integration tests");

		// Create a test configuration
		MemoryConfigurable config = createTestConfig(apiKey);
		anthropicService = new AnthropicLLMService(config);
	}

	@Test
	@DisplayName("Should generate response from simple prompt")
	void testGenerateFromPrompt() {
		String prompt = "What is the capital of France? Answer in one word.";

		String response = anthropicService.generate(prompt);

		assertNotNull(response);
		assertFalse(response.trim().isEmpty());
		assertTrue(response.toLowerCase().contains("paris"));
	}

	@Test
	@DisplayName("Should generate response from message list")
	void testGenerateFromMessages() {
		List<Message> messages = List.of(new Message("user", "Hello, I'm testing the API"),
				new Message("assistant", "Hello! I'm Claude, how can I help you today?"),
				new Message("user", "What's 2 + 2?"));

		String response = anthropicService.generate(messages);

		assertNotNull(response);
		assertFalse(response.trim().isEmpty());
		assertTrue(response.contains("4"));
	}

	@Test
	@DisplayName("Should generate response with system prompt")
	void testGenerateWithSystemPrompt() {
		String systemPrompt = "You are a helpful math tutor. Always explain your answers clearly.";
		String userMessage = "What is 15 + 27?";

		String response = anthropicService.generate(systemPrompt, userMessage);

		assertNotNull(response);
		assertFalse(response.trim().isEmpty());
		assertTrue(response.contains("42"));
	}

	@Test
	@DisplayName("Should generate structured JSON response")
	void testGenerateStructured() {
		String prompt = "Create a person profile for John Smith, age 30, from New York.";
		String schema = """
				{
					"name": "string",
					"age": "number",
					"city": "string",
					"occupation": "string"
				}
				""";

		String response = anthropicService.generateStructured(prompt, schema);

		assertNotNull(response);
		assertFalse(response.trim().isEmpty());
		assertTrue(response.contains("John Smith"));
		assertTrue(response.contains("30"));
		assertTrue(response.contains("New York"));
	}

	@Test
	@DisplayName("Should handle different Claude models")
	void testDifferentModels() {
		// Test with Claude-3 Haiku (faster, cheaper model)
		String haikuModel = "claude-3-haiku-20240307";
		MemoryConfigurable haikuConfig = createTestConfigWithModel(apiKey, haikuModel);
		AnthropicLLMService haikuService = new AnthropicLLMService(haikuConfig);

		String response = haikuService.generate("Say hello in one word.");
		assertNotNull(response);
		assertFalse(response.trim().isEmpty());
	}

	@Test
	@DisplayName("Should be available with valid API key")
	void testServiceAvailability() {
		assertTrue(anthropicService.isAvailable());
	}

	@Test
	@DisplayName("Should handle temperature variations")
	void testTemperatureVariations() {
		// Test with low temperature (more deterministic)
		MemoryConfigurable lowTempConfig = createTestConfigWithTemperature(apiKey, 0.1);
		AnthropicLLMService lowTempService = new AnthropicLLMService(lowTempConfig);

		String response1 = lowTempService.generate("Count from 1 to 3.");
		String response2 = lowTempService.generate("Count from 1 to 3.");

		// Responses should be similar with low temperature
		assertNotNull(response1);
		assertNotNull(response2);
		// Note: We can't guarantee exact matching due to model variations
	}

	@Test
	@DisplayName("Should handle max token limits")
	void testMaxTokenLimits() {
		// Test with very low max tokens
		MemoryConfigurable lowTokenConfig = createTestConfigWithMaxTokens(apiKey, 10);
		AnthropicLLMService lowTokenService = new AnthropicLLMService(lowTokenConfig);

		String response = lowTokenService.generate("Write a long essay about artificial intelligence.");

		assertNotNull(response);
		// Response should be truncated due to low token limit
		assertTrue(response.length() < 200); // Approximate check
	}

	@Test
	@DisplayName("Should handle multilingual content")
	void testMultilingualContent() {
		String prompt = "Translate 'Hello, how are you?' to Spanish.";

		String response = anthropicService.generate(prompt);

		assertNotNull(response);
		assertTrue(response.toLowerCase().contains("hola"));
	}

	@Test
	@DisplayName("Should handle code generation requests")
	void testCodeGeneration() {
		String prompt = "Write a simple Python function that adds two numbers.";

		String response = anthropicService.generate(prompt);

		assertNotNull(response);
		assertTrue(response.contains("def"));
		assertTrue(response.contains("return"));
	}

	// Helper methods to create test configurations

	private MemoryConfigurable createTestConfig(String apiKey) {
		return createTestConfigWithModel(apiKey, "claude-3-sonnet-20240229");
	}

	private MemoryConfigurable createTestConfigWithModel(String apiKey, String model) {
		return new MemoryConfigurable() {
			@Override
			public VectorStore getVectorStore() {
				return null;
			}

			@Override
			public Llm getLlm() {
				return new Llm() {
					@Override
					public String getType() {
						return "anthropic";
					}

					@Override
					public String getApiKey() {
						return apiKey;
					}

					@Override
					public String getModel() {
						return model;
					}

					@Override
					public LlmOptions getOptions() {
						return new LlmOptions() {
							@Override
							public Integer getMaxTokens() {
								return 1000;
							}

							@Override
							public Double getTemperature() {
								return 0.7;
							}
						};
					}
				};
			}

			@Override
			public Embeddings getEmbeddings() {
				return null;
			}

			@Override
			public Integer getMaxMemories() {
				return 1000;
			}

			@Override
			public Integer getEmbeddingDimension() {
				return 1536;
			}

			@Override
			public Double getSimilarityThreshold() {
				return 0.7;
			}
		};
	}

	private MemoryConfigurable createTestConfigWithTemperature(String apiKey, Double temperature) {
		return new MemoryConfigurable() {
			@Override
			public VectorStore getVectorStore() {
				return null;
			}

			@Override
			public Llm getLlm() {
				return new Llm() {
					@Override
					public String getType() {
						return "anthropic";
					}

					@Override
					public String getApiKey() {
						return apiKey;
					}

					@Override
					public String getModel() {
						return "claude-3-sonnet-20240229";
					}

					@Override
					public LlmOptions getOptions() {
						return new LlmOptions() {
							@Override
							public Integer getMaxTokens() {
								return 1000;
							}

							@Override
							public Double getTemperature() {
								return temperature;
							}
						};
					}
				};
			}

			@Override
			public Embeddings getEmbeddings() {
				return null;
			}

			@Override
			public Integer getMaxMemories() {
				return 1000;
			}

			@Override
			public Integer getEmbeddingDimension() {
				return 1536;
			}

			@Override
			public Double getSimilarityThreshold() {
				return 0.7;
			}
		};
	}

	private MemoryConfigurable createTestConfigWithMaxTokens(String apiKey, Integer maxTokens) {
		return new MemoryConfigurable() {
			@Override
			public VectorStore getVectorStore() {
				return null;
			}

			@Override
			public Llm getLlm() {
				return new Llm() {
					@Override
					public String getType() {
						return "anthropic";
					}

					@Override
					public String getApiKey() {
						return apiKey;
					}

					@Override
					public String getModel() {
						return "claude-3-sonnet-20240229";
					}

					@Override
					public LlmOptions getOptions() {
						return new LlmOptions() {
							@Override
							public Integer getMaxTokens() {
								return maxTokens;
							}

							@Override
							public Double getTemperature() {
								return 0.7;
							}
						};
					}
				};
			}

			@Override
			public Embeddings getEmbeddings() {
				return null;
			}

			@Override
			public Integer getMaxMemories() {
				return 1000;
			}

			@Override
			public Integer getEmbeddingDimension() {
				return 1536;
			}

			@Override
			public Double getSimilarityThreshold() {
				return 0.7;
			}
		};
	}

}