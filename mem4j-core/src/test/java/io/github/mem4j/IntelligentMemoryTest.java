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

package io.github.mem4j;

import io.github.mem4j.config.MemoryConfigurable;
import io.github.mem4j.embeddings.EmbeddingService;
import io.github.mem4j.llms.LLMService;
import io.github.mem4j.memory.Memory;
import io.github.mem4j.memory.MemoryItem;
import io.github.mem4j.memory.MemoryType;
import io.github.mem4j.memory.Message;
import io.github.mem4j.vectorstores.VectorStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for intelligent memory management features This tests the new
 * INSERT/UPDATE/DELETE decision-making logic
 */
@ExtendWith(MockitoExtension.class)
public class IntelligentMemoryTest {

	@Mock
	private MemoryConfigurable memoryConfig;

	@Mock
	private VectorStoreService vectorStoreService;

	@Mock
	private LLMService llmService;

	@Mock
	private EmbeddingService embeddingService;

	private Memory memory;

	private final String testUserId = "test_user";

	@BeforeEach
	void setUp() {
		// Configure mock behavior
		lenient().when(memoryConfig.getSimilarityThreshold()).thenReturn(0.7);
		lenient().when(embeddingService.embed(anyString())).thenReturn(createMockEmbedding());
		lenient().when(embeddingService.getDimension()).thenReturn(1536);
		lenient().when(embeddingService.isAvailable()).thenReturn(true);
		lenient().when(llmService.isAvailable()).thenReturn(true);

		// Create Memory instance with mocked dependencies
		memory = new Memory(memoryConfig, vectorStoreService, llmService, embeddingService);
	}

	@Test
	void testIntelligentAdd_InsertNewMemory_WhenNoSimilarExists() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "I love hiking in the mountains"),
				new Message("assistant", "That's great!"));

		when(llmService.generate(anyString())).thenReturn("- User loves hiking in the mountains");
		when(vectorStoreService.search(any(Double[].class), any(), anyInt(), anyDouble()))
			.thenReturn(Collections.emptyList());

		// Act
		memory.add(messages, testUserId, null, true, MemoryType.FACTUAL);

		// Assert
		verify(vectorStoreService, times(1)).add(any(MemoryItem.class));
		verify(vectorStoreService, never()).update(any(MemoryItem.class));
		verify(vectorStoreService, never()).delete(anyString());
	}

	@Test
	void testIntelligentAdd_SkipDuplicate_WhenHighSimilarity() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "I like pizza"),
				new Message("assistant", "Got it!"));

		when(llmService.generate(anyString())).thenReturn("- User likes pizza");

		// Create a very similar existing memory (score > 0.95)
		MemoryItem existingMemory = createMockMemoryItem("existing-1", "User likes pizza");
		existingMemory.setScore(0.96);
		when(vectorStoreService.search(any(Double[].class), any(), anyInt(), anyDouble()))
			.thenReturn(Collections.singletonList(existingMemory));

		// Act
		memory.add(messages, testUserId, null, true, MemoryType.FACTUAL);

		// Assert - should skip (not insert, update, or delete)
		verify(vectorStoreService, never()).add(any(MemoryItem.class));
		verify(vectorStoreService, never()).update(any(MemoryItem.class));
		verify(vectorStoreService, never()).delete(anyString());
	}

	@Test
	void testIntelligentAdd_UpdateMemory_WhenLLMDecides() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "I now prefer tea over coffee"),
				new Message("assistant", "I'll remember that!"));

		when(llmService.generate(contains("Extract key memories"))).thenReturn("- User prefers tea over coffee");

		// Create existing memory with similarity 0.9 (should trigger LLM decision)
		MemoryItem existingMemory = createMockMemoryItem("existing-1", "User likes coffee");
		existingMemory.setScore(0.9);
		when(vectorStoreService.search(any(Double[].class), any(), anyInt(), anyDouble()))
			.thenReturn(Collections.singletonList(existingMemory));

		// LLM decides to update with merged content
		when(llmService.generate(contains("memory management system")))
			.thenReturn("UPDATE: User prefers tea over coffee (changed from coffee preference)");

		// Act
		memory.add(messages, testUserId, null, true, MemoryType.FACTUAL);

		// Assert
		ArgumentCaptor<MemoryItem> captor = ArgumentCaptor.forClass(MemoryItem.class);
		verify(vectorStoreService, times(1)).update(captor.capture());
		verify(vectorStoreService, never()).add(any(MemoryItem.class));
		verify(vectorStoreService, never()).delete(anyString());

		MemoryItem updatedItem = captor.getValue();
		assertTrue(updatedItem.getContent().contains("prefers tea"));
	}

	@Test
	void testIntelligentAdd_DeleteMemory_WhenLLMDecides() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "Actually, I'm not a developer anymore"),
				new Message("assistant", "Thanks for letting me know!"));

		when(llmService.generate(contains("Extract key memories"))).thenReturn("- User is no longer a developer");

		// Create existing memory with similarity 0.9
		MemoryItem existingMemory = createMockMemoryItem("existing-1", "User is a software developer");
		existingMemory.setScore(0.9);
		when(vectorStoreService.search(any(Double[].class), any(), anyInt(), anyDouble()))
			.thenReturn(Collections.singletonList(existingMemory));

		// LLM decides to delete old memory
		when(llmService.generate(contains("memory management system"))).thenReturn("DELETE");

		// Act
		memory.add(messages, testUserId, null, true, MemoryType.FACTUAL);

		// Assert
		verify(vectorStoreService, times(1)).delete("existing-1");
		verify(vectorStoreService, never()).add(any(MemoryItem.class));
		verify(vectorStoreService, never()).update(any(MemoryItem.class));
	}

	@Test
	void testIntelligentAdd_InsertSeparate_WhenModerateSimilarity() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "I also enjoy swimming"),
				new Message("assistant", "Great!"));

		when(llmService.generate(anyString())).thenReturn("- User enjoys swimming");

		// Create existing memory with moderate similarity (0.75)
		MemoryItem existingMemory = createMockMemoryItem("existing-1", "User loves hiking");
		existingMemory.setScore(0.75);
		when(vectorStoreService.search(any(Double[].class), any(), anyInt(), anyDouble()))
			.thenReturn(Collections.singletonList(existingMemory));

		// Act
		memory.add(messages, testUserId, null, true, MemoryType.FACTUAL);

		// Assert - should insert as separate memory
		verify(vectorStoreService, times(1)).add(any(MemoryItem.class));
		verify(vectorStoreService, never()).update(any(MemoryItem.class));
		verify(vectorStoreService, never()).delete(anyString());
	}

	@Test
	void testIntelligentAdd_MultipleMemories_DifferentActions() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "I like pizza and I moved to New York"),
				new Message("assistant", "Interesting updates!"));

		// Extract two memories
		when(llmService.generate(contains("Extract key memories")))
			.thenReturn("- User likes pizza\n- User moved to New York");

		// First memory - very similar existing (score > 0.95, should skip)
		MemoryItem existingPizza = createMockMemoryItem("existing-1", "User likes pizza");
		existingPizza.setScore(0.96);

		// Second memory - moderately similar existing (score 0.85-0.95, should consult
		// LLM)
		MemoryItem existingLocation = createMockMemoryItem("existing-2", "User lives in Boston");
		existingLocation.setScore(0.9);

		// Mock search to return different results for different embeddings
		when(vectorStoreService.search(any(Double[].class), any(), anyInt(), anyDouble()))
			.thenReturn(Collections.singletonList(existingPizza))
			.thenReturn(Collections.singletonList(existingLocation));

		// LLM decides to update the location
		when(llmService.generate(contains("memory management system")))
			.thenReturn("UPDATE: User moved to New York (previously in Boston)");

		// Act
		memory.add(messages, testUserId, null, true, MemoryType.FACTUAL);

		// Assert
		verify(vectorStoreService, times(1)).update(any(MemoryItem.class)); // Location
																			// updated
		verify(vectorStoreService, never()).add(any(MemoryItem.class)); // Pizza skipped
		verify(vectorStoreService, never()).delete(anyString()); // Nothing deleted
	}

	@Test
	void testIntelligentAdd_LLMError_FallsBackToInsert() {
		// Arrange
		List<Message> messages = Arrays.asList(new Message("user", "I like reading books"),
				new Message("assistant", "Nice!"));

		when(llmService.generate(contains("Extract key memories"))).thenReturn("- User likes reading books");

		MemoryItem existingMemory = createMockMemoryItem("existing-1", "User enjoys reading");
		existingMemory.setScore(0.9);
		when(vectorStoreService.search(any(Double[].class), any(), anyInt(), anyDouble()))
			.thenReturn(Collections.singletonList(existingMemory));

		// LLM throws error when making decision
		when(llmService.generate(contains("memory management system")))
			.thenThrow(new RuntimeException("LLM API error"));

		// Act
		memory.add(messages, testUserId, null, true, MemoryType.FACTUAL);

		// Assert - should fall back to INSERT
		verify(vectorStoreService, times(1)).add(any(MemoryItem.class));
		verify(vectorStoreService, never()).update(any(MemoryItem.class));
		verify(vectorStoreService, never()).delete(anyString());
	}

	// Helper methods
	private Double[] createMockEmbedding() {
		Double[] embedding = new Double[1536];
		for (int i = 0; i < embedding.length; i++) {
			embedding[i] = Math.random();
		}
		return embedding;
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
