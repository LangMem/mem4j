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

package com.langmem.mem4j.memory;

import com.langmem.mem4j.configs.MemoryConfig;
import com.langmem.mem4j.embeddings.EmbeddingService;
import com.langmem.mem4j.llms.LLMService;
import com.langmem.mem4j.vectorstores.VectorStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Core memory management class for Java Mem0
 */
@Service
public class Memory {

	private static final Logger logger = LoggerFactory.getLogger(Memory.class);

	private final MemoryConfig config;

	private final VectorStoreService vectorStoreService;

	private final LLMService llmService;

	private final EmbeddingService embeddingService;

	public Memory(MemoryConfig config, VectorStoreService vectorStoreService, LLMService llmService,
			EmbeddingService embeddingService) {

		this.config = config;
		this.vectorStoreService = vectorStoreService;
		this.llmService = llmService;
		this.embeddingService = embeddingService;
	}

	/**
	 * Add memories from a conversation
	 */
	public void add(List<Message> messages, String userId) {
		add(messages, userId, null, true, MemoryType.FACTUAL);
	}

	/**
	 * Add memories with custom parameters
	 */
	public void add(List<Message> messages, String userId, Map<String, Object> metadata, boolean infer,
			MemoryType memoryType) {

		try {

			// Extract memories from conversation using LLM
			List<String> extractedMemories = extractMemories(messages, infer);

			// Create memory items
			List<MemoryItem> memoryItems = extractedMemories.stream()
				.map(memory -> createMemoryItem(memory, userId, metadata, memoryType))
				.collect(Collectors.toList());

			// Generate embeddings and store
			for (MemoryItem item : memoryItems) {
                Double[] embedding = embeddingService.embed(item.getContent());
				item.setEmbedding(embedding);
				vectorStoreService.add(item);
			}

			logger.info("Added {} memories for user {}", memoryItems.size(), userId);
		}
		catch (Exception e) {
			logger.error("Error adding memories for user {}", userId, e);
			throw new RuntimeException("Failed to add memories", e);
		}

	}

	/**
	 * Search for relevant memories
	 */
	public List<MemoryItem> search(String query, String userId) {
		return search(query, userId, null, 10, null);
	}

	/**
	 * Search with custom parameters
	 */
	public List<MemoryItem> search(String query, String userId, Map<String, Object> filters, int limit,
			Double threshold) {

		try {
			// Generate embedding for query
            Double[] queryEmbedding = embeddingService.embed(query);

			// Build search filters
			Map<String, Object> searchFilters = buildSearchFilters(userId, filters);

			// Search vector store
			List<MemoryItem> results = vectorStoreService.search(queryEmbedding, searchFilters, limit,
					threshold != null ? threshold : config.getSimilarityThreshold());

			logger.info("Found {} memories for query: {}", results.size(), query);
			return results;
		}
		catch (Exception e) {

			logger.error("Error searching memories for query: {}", query, e);
			throw new RuntimeException("Failed to search memories", e);
		}

	}

	/**
	 * Get all memories for a user
	 */
	public List<MemoryItem> getAll(String userId, Map<String, Object> filters, int limit) {

		try {
			Map<String, Object> searchFilters = buildSearchFilters(userId, filters);
			return vectorStoreService.getAll(searchFilters, limit);
		}
		catch (Exception e) {
			logger.error("Error getting all memories for user {}", userId, e);
			throw new RuntimeException("Failed to get memories", e);
		}

	}

	/**
	 * Update a memory item
	 */
	public void update(String memoryId, Map<String, Object> data) {

		try {
			MemoryItem item = vectorStoreService.get(memoryId);
			if (item != null) {
				// Update fields
				if (data.containsKey("content")) {
					item.setContent((String) data.get("content"));
					// Re-embed if content changed
                    Double[] embedding = embeddingService.embed(item.getContent());
					item.setEmbedding(embedding);
				}
				if (data.containsKey("metadata")) {
					item.setMetadata((Map<String, Object>) data.get("metadata"));
				}
				item.setUpdatedAt(java.time.Instant.now());

				vectorStoreService.update(item);
				logger.info("Updated memory: {}", memoryId);
			}
		}
		catch (Exception e) {
			logger.error("Error updating memory: {}", memoryId, e);
			throw new RuntimeException("Failed to update memory", e);
		}

	}

	/**
	 * Delete a memory item
	 */
	public void delete(String memoryId) {

		try {
			vectorStoreService.delete(memoryId);
			logger.info("Deleted memory: {}", memoryId);
		}
		catch (Exception e) {
			logger.error("Error deleting memory: {}", memoryId, e);
			throw new RuntimeException("Failed to delete memory", e);
		}

	}

	/**
	 * Delete all memories for a user
	 */
	public void deleteAll(String userId) {

		try {
			Map<String, Object> filters = Map.of("user_id", userId);
			vectorStoreService.deleteAll(filters);
			logger.info("Deleted all memories for user: {}", userId);
		}
		catch (Exception e) {
			logger.error("Error deleting all memories for user {}", userId, e);
			throw new RuntimeException("Failed to delete memories", e);
		}

	}

	/**
	 * Extract memories from conversation using LLM
	 */
	private List<String> extractMemories(List<Message> messages, boolean infer) {

		if (!infer) {
			// Return raw conversation as single memory
			String conversation = messages.stream()
				.map(msg -> msg.getRole() + ": " + msg.getContent())
				.collect(Collectors.joining("\n"));
			return List.of(conversation);
		}

		// Use LLM to extract meaningful memories
		String conversation = messages.stream()
			.map(msg -> msg.getRole() + ": " + msg.getContent())
			.collect(Collectors.joining("\n"));

		String prompt = String.format("""
				Extract key memories from this conversation. Focus on:
				- Important facts about the user
				- User preferences and behaviors
				- Significant events or experiences
				- Useful information for future interactions

				Conversation:
				%s

				Return each memory as a separate line, starting with "- ".
				""", conversation);

		String response = llmService.generate(prompt);

		// Parse response into individual memories
		return Arrays.stream(response.split("\n"))
			.map(String::trim)
			.filter(line -> line.startsWith("- "))
			.map(line -> line.substring(2))
			.filter(line -> !line.isEmpty())
			.collect(Collectors.toList());
	}

	/**
	 * Create a memory item from content
	 */
	private MemoryItem createMemoryItem(String content, String userId, Map<String, Object> metadata,
			MemoryType memoryType) {

		MemoryItem item = new MemoryItem(content, memoryType.getValue());
		item.setUserId(userId);
		item.setMetadata(metadata);

		return item;
	}

	/**
	 * Build search filters
	 */
	private Map<String, Object> buildSearchFilters(String userId, Map<String, Object> additionalFilters) {

		Map<String, Object> filters = new HashMap<>();
		filters.put("user_id", userId);

		if (additionalFilters != null) {
			filters.putAll(additionalFilters);
		}

		return filters;
	}

	/**
	 * Get memory by ID
	 */
	public MemoryItem get(String memoryId) {

		try {
			return vectorStoreService.get(memoryId);
		}
		catch (Exception e) {
			logger.error("Error getting memory: {}", memoryId, e);
			throw new RuntimeException("Failed to get memory", e);
		}
	}

	/**
	 * Reset all memories (for testing)
	 */
	public void reset() {

		try {
			vectorStoreService.reset();
			logger.info("Reset all memories");
		}
		catch (Exception e) {
			logger.error("Error resetting memories", e);
			throw new RuntimeException("Failed to reset memories", e);
		}
	}

}
