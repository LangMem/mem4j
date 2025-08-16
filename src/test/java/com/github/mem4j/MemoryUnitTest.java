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

package com.github.mem4j;

import com.github.mem4j.configs.MemoryConfig;
import com.github.mem4j.embeddings.EmbeddingService;
import com.github.mem4j.llms.LLMService;
import com.github.mem4j.memory.Memory;
import com.github.mem4j.memory.MemoryItem;
import com.github.mem4j.memory.MemoryType;
import com.github.mem4j.memory.Message;
import com.github.mem4j.vectorstores.VectorStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit test class for Memory functionality using Mockito This test doesn't require real
 * API keys or external services
 */
@ExtendWith(MockitoExtension.class)
public class MemoryUnitTest {

	@Mock
	private MemoryConfig memoryConfig;

	@Mock
	private VectorStoreService vectorStoreService;

	@Mock
	private LLMService llmService;

	@Mock
	private EmbeddingService embeddingService;

	private Memory memory;

	private String testUserId = "test_user";

	@BeforeEach
	void setUp() {
		// Configure mock behavior with lenient stubbing
		lenient().when(memoryConfig.getSimilarityThreshold()).thenReturn(0.7);
		lenient().when(embeddingService.embed(anyString())).thenReturn(createMockEmbedding());
		lenient().when(embeddingService.getDimension()).thenReturn(1536);
		lenient().when(embeddingService.isAvailable()).thenReturn(true);
		lenient().when(llmService.isAvailable()).thenReturn(true);

		// Create Memory instance with mocked dependencies
		memory = new Memory(memoryConfig, vectorStoreService, llmService, embeddingService);
	}

	@Test
	void testAddMemories() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "I like pizza"),
				new Message("assistant", "I'll remember that you like pizza."));

		// Mock LLM response for memory extraction
		when(llmService.generate(anyString())).thenReturn("- User likes pizza");

		// Act
		memory.add(messages, testUserId);

		// Assert
		verify(embeddingService, times(1)).embed(anyString());
		verify(vectorStoreService, times(1)).add(any(MemoryItem.class));
	}

	@Test
	void testAddMemoriesWithoutInference() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "I work as a developer"),
				new Message("assistant", "That's interesting!"));

		// Act
		memory.add(messages, testUserId, null, false, MemoryType.FACTUAL);

		// Assert
		verify(llmService, never()).generate(anyString()); // No LLM call when infer=false
		verify(embeddingService, times(1)).embed(anyString());
		verify(vectorStoreService, times(1)).add(any(MemoryItem.class));
	}

	@Test
	void testSearchMemories() {
		// Arrange
		String query = "What do I like?";
		List<MemoryItem> mockResults = createMockMemoryItems();

		when(vectorStoreService.search(any(Double[].class), any(), anyInt(), anyDouble())).thenReturn(mockResults);

		// Act
		List<MemoryItem> results = memory.search(query, testUserId);

		// Assert
		assertNotNull(results);
		assertFalse(results.isEmpty());
		assertEquals(2, results.size());
		verify(embeddingService, times(1)).embed(query);
		verify(vectorStoreService, times(1)).search(any(Double[].class), any(), anyInt(), anyDouble());
	}

	@Test
	void testSearchMemoriesWithFilters() {
		// Arrange
		String query = "programming";
		Map<String, Object> filters = Map.of("agent_id", "test_agent");
		List<MemoryItem> mockResults = createMockMemoryItems();

		when(vectorStoreService.search(any(Double[].class), any(), anyInt(), anyDouble())).thenReturn(mockResults);

		// Act
		List<MemoryItem> results = memory.search(query, testUserId, filters, 5, 0.8);

		// Assert
		assertNotNull(results);
		verify(embeddingService, times(1)).embed(query);
		verify(vectorStoreService, times(1)).search(any(Double[].class), any(), eq(5), eq(0.8));
	}

	@Test
	void testGetAllMemories() {
		// Arrange
		List<MemoryItem> mockResults = createMockMemoryItems();
		when(vectorStoreService.getAll(any(), anyInt())).thenReturn(mockResults);

		// Act
		List<MemoryItem> results = memory.getAll(testUserId, null, 100);

		// Assert
		assertNotNull(results);
		assertEquals(2, results.size());
		verify(vectorStoreService, times(1)).getAll(any(), eq(100));
	}

	@Test
	void testUpdateMemory() {
		// Arrange
		String memoryId = "test-memory-id";
		Map<String, Object> updateData = Map.of("content", "Updated content");
		MemoryItem existingMemory = createMockMemoryItem("test-memory-id", "Original content");

		when(vectorStoreService.get(memoryId)).thenReturn(existingMemory);

		// Act
		memory.update(memoryId, updateData);

		// Assert
		verify(vectorStoreService, times(1)).get(memoryId);
		verify(embeddingService, times(1)).embed("Updated content");
		verify(vectorStoreService, times(1)).update(any(MemoryItem.class));
	}

	@Test
	void testDeleteMemory() {
		// Arrange
		String memoryId = "test-memory-id";

		// Act
		memory.delete(memoryId);

		// Assert
		verify(vectorStoreService, times(1)).delete(memoryId);
	}

	@Test
	void testDeleteAllMemories() {
		// Arrange
		String userId = "test_user";

		// Act
		memory.deleteAll(userId);

		// Assert
		verify(vectorStoreService, times(1)).deleteAll(any());
	}

	@Test
	void testGetMemoryById() {
		// Arrange
		String memoryId = "test-memory-id";
		MemoryItem mockMemory = createMockMemoryItem(memoryId, "Test content");
		when(vectorStoreService.get(memoryId)).thenReturn(mockMemory);

		// Act
		MemoryItem result = memory.get(memoryId);

		// Assert
		assertNotNull(result);
		assertEquals(memoryId, result.getId());
		assertEquals("Test content", result.getContent());
		verify(vectorStoreService, times(1)).get(memoryId);
	}

	@Test
	void testResetMemories() {
		// Act
		memory.reset();

		// Assert
		verify(vectorStoreService, times(1)).reset();
	}

	@Test
	void testMemoryWithMetadata() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "I work as a software developer"),
				new Message("assistant", "That's interesting!"));
		Map<String, Object> metadata = Map.of("session_id", "test_session", "agent_id", "test_agent");

		// Act
		memory.add(messages, testUserId, metadata, false, MemoryType.FACTUAL);

		// Assert
		verify(vectorStoreService, times(1)).add(argThat(memoryItem -> {
			return memoryItem.getUserId().equals(testUserId)
					&& memoryItem.getMemoryType().equals(MemoryType.FACTUAL.getValue())
					&& memoryItem.getMetadata() != null
					&& memoryItem.getMetadata().get("agent_id").equals("test_agent");
		}));
	}

	@Test
	void testMemoryExtractionWithLLM() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "My name is John and I like chocolate"),
				new Message("assistant", "Nice to meet you John!"));

		when(llmService.generate(anyString())).thenReturn("- User's name is John\n- User likes chocolate");

		// Act
		memory.add(messages, testUserId, null, true, MemoryType.FACTUAL);

		// Assert
		verify(llmService, times(1)).generate(anyString());
		verify(embeddingService, times(2)).embed(anyString()); // Two memories extracted
		verify(vectorStoreService, times(2)).add(any(MemoryItem.class));
	}

	// Helper methods
	private Double[] createMockEmbedding() {
		Double[] embedding = new Double[1536];
		for (int i = 0; i < embedding.length; i++) {
			embedding[i] = Math.random();
		}
		return embedding;
	}

	private List<MemoryItem> createMockMemoryItems() {
		MemoryItem item1 = createMockMemoryItem("1", "I like pizza");
		MemoryItem item2 = createMockMemoryItem("2", "I work as a developer");
		return Arrays.asList(item1, item2);
	}

	private MemoryItem createMockMemoryItem(String id, String content) {
		MemoryItem item = new MemoryItem(content, MemoryType.FACTUAL.getValue());
		item.setId(id);
		item.setUserId(testUserId);
		item.setScore(0.9);
		item.setEmbedding(createMockEmbedding());
		return item;
	}

}
