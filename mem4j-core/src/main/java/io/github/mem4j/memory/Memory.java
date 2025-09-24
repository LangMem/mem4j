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

package io.github.mem4j.memory;

import io.github.mem4j.config.MemoryConfigurable;
import io.github.mem4j.embeddings.EmbeddingService;
import io.github.mem4j.llms.LLMService;
import io.github.mem4j.vectorstores.VectorStoreService;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Core memory management class for Java Mem4j */
@Service
public class Memory {

  private static final Logger logger = LoggerFactory.getLogger(Memory.class);

  private final MemoryConfigurable config;

  private final VectorStoreService vectorStoreService;

  private final LLMService llmService;

  private final EmbeddingService embeddingService;

  public Memory(
      MemoryConfigurable config,
      VectorStoreService vectorStoreService,
      LLMService llmService,
      EmbeddingService embeddingService) {

    this.config = config;
    this.vectorStoreService = vectorStoreService;
    this.llmService = llmService;
    this.embeddingService = embeddingService;
  }

  /** Add memories from a conversation */
  public void add(List<Message> messages, String userId) {
    add(messages, userId, null, true, MemoryType.FACTUAL);
  }

  /** Add memories with custom parameters */
  public void add(
      List<Message> messages,
      String userId,
      Map<String, Object> metadata,
      boolean infer,
      MemoryType memoryType) {

    try {

      // Extract memories from conversation using LLM
      List<String> extractedMemories = extractMemories(messages, infer);

      // Create memory items
      List<MemoryItem> memoryItems =
          extractedMemories.stream()
              .map(memory -> createMemoryItem(memory, userId, metadata, memoryType))
              .collect(Collectors.toList());

      // Filter out duplicate memories before adding
      List<MemoryItem> newMemories = new ArrayList<>();
      int duplicateCount = 0;

      for (MemoryItem item : memoryItems) {
        Double[] embedding = embeddingService.embed(item.getContent());
        item.setEmbedding(embedding);

        // Check for similar existing memories (high similarity threshold for
        // deduplication)
        List<MemoryItem> similarMemories =
            vectorStoreService.search(embedding, buildSearchFilters(userId, null), 5, 0.85);

        boolean isDuplicate = false;
        for (MemoryItem existing : similarMemories) {
          if (existing.getScore() > 0.85) {
            logger.debug(
                "Skipping duplicate memory: '{}' (similar to existing: '{}')",
                item.getContent(),
                existing.getContent());
            isDuplicate = true;
            duplicateCount++;
            break;
          }
        }

        if (!isDuplicate) {
          newMemories.add(item);
        }
      }

      // Store only new memories
      for (MemoryItem item : newMemories) {
        vectorStoreService.add(item);
      }

      logger.info(
          "Added {} new memories for user {} (skipped {} duplicates)",
          newMemories.size(),
          userId,
          duplicateCount);
    } catch (Exception e) {
      logger.error("Error adding memories for user {}", userId, e);
      throw new RuntimeException("Failed to add memories", e);
    }
  }

  /** Search for relevant memories */
  public List<MemoryItem> search(String query, String userId) {
    return search(query, userId, null, 10, null);
  }

  /** Search with custom parameters */
  public List<MemoryItem> search(
      String query, String userId, Map<String, Object> filters, int limit, Double threshold) {

    try {
      // Generate embedding for query
      Double[] queryEmbedding = embeddingService.embed(query);
      logger.debug(
          "Generated query embedding with {} dimensions for query: '{}'",
          queryEmbedding.length,
          query);

      // Build search filters
      Map<String, Object> searchFilters = buildSearchFilters(userId, filters);
      logger.debug("Search filters: {}", searchFilters);

      // Determine appropriate threshold based on query type
      Double actualThreshold = determineThreshold(query, threshold);
      logger.debug("Using similarity threshold: {} for query type", actualThreshold);

      // Search vector store
      List<MemoryItem> results =
          vectorStoreService.search(queryEmbedding, searchFilters, limit, actualThreshold);

      // If no results found and threshold > 0.3, try with lower threshold
      if (results.isEmpty() && actualThreshold > 0.3) {
        logger.debug("No results with threshold {}, retrying with 0.3", actualThreshold);
        results = vectorStoreService.search(queryEmbedding, searchFilters, limit, 0.3);
        actualThreshold = 0.3; // Update for logging
      }

      logger.info(
          "Found {} memories for query: '{}' with threshold: {}",
          results.size(),
          query,
          actualThreshold);

      // Filter results by semantic relevance
      List<MemoryItem> filteredResults = filterBySemanticRelevance(query, results);

      // Log found results for debugging
      if (!filteredResults.isEmpty()) {
        for (MemoryItem item : filteredResults) {
          logger.debug(
              "Found relevant memory: '{}' with score: {}", item.getContent(), item.getScore());
        }
      } else {
        logger.warn(
            "No relevant memories found for query: '{}' with user_id: '{}' and threshold: {}",
            query,
            userId,
            actualThreshold);
      }

      return filteredResults;
    } catch (Exception e) {

      logger.error("Error searching memories for query: {}", query, e);
      throw new RuntimeException("Failed to search memories", e);
    }
  }

  /** Get all memories for a user */
  public List<MemoryItem> getAll(String userId, Map<String, Object> filters, int limit) {

    try {
      Map<String, Object> searchFilters = buildSearchFilters(userId, filters);
      return vectorStoreService.getAll(searchFilters, limit);
    } catch (Exception e) {
      logger.error("Error getting all memories for user {}", userId, e);
      throw new RuntimeException("Failed to get memories", e);
    }
  }

  /** Update a memory item */
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
    } catch (Exception e) {
      logger.error("Error updating memory: {}", memoryId, e);
      throw new RuntimeException("Failed to update memory", e);
    }
  }

  /** Delete a memory item */
  public void delete(String memoryId) {

    try {
      vectorStoreService.delete(memoryId);
      logger.info("Deleted memory: {}", memoryId);
    } catch (Exception e) {
      logger.error("Error deleting memory: {}", memoryId, e);
      throw new RuntimeException("Failed to delete memory", e);
    }
  }

  /** Delete all memories for a user */
  public void deleteAll(String userId) {

    try {
      Map<String, Object> filters = Map.of("user_id", userId);
      vectorStoreService.deleteAll(filters);
      logger.info("Deleted all memories for user: {}", userId);
    } catch (Exception e) {
      logger.error("Error deleting all memories for user {}", userId, e);
      throw new RuntimeException("Failed to delete memories", e);
    }
  }

  /** Extract memories from conversation using LLM */
  private List<String> extractMemories(List<Message> messages, boolean infer) {

    if (!infer) {
      // Return raw conversation as single memory
      String conversation =
          messages.stream()
              .map(msg -> msg.getRole() + ": " + msg.getContent())
              .collect(Collectors.joining("\n"));
      return List.of(conversation);
    }

    // Use LLM to extract meaningful memories
    String conversation =
        messages.stream()
            .map(msg -> msg.getRole() + ": " + msg.getContent())
            .collect(Collectors.joining("\n"));

    String prompt =
        String.format(
            """
            Extract key memories from this conversation. Focus ONLY on:
            - Important facts about the user (name, age, profession, etc.)
            - User preferences and behaviors (likes, dislikes, habits)
            - Significant events or experiences mentioned by the user
            - Useful information for future personalized interactions

            DO NOT extract:
            - Generic system messages or responses
            - Conversation metadata or status messages
            - General greetings or pleasantries
            - Assistant responses unless they contain user-specific information

            Only extract memories that have real value for understanding the user.

            Conversation:
            %s

            Return each memory as a separate line, starting with "- ".
            If no valuable memories exist, return nothing.
            """,
            conversation);

    String response = llmService.generate(prompt);

    // Parse response into individual memories and filter out system-like content
    List<String> memories =
        Arrays.stream(response.split("\n"))
            .map(String::trim)
            .filter(line -> line.startsWith("- "))
            .map(this::cleanMemoryContent)
            .filter(line -> !line.isEmpty())
            .filter(this::isValidMemory)
            .collect(Collectors.toList());

    logger.debug("Extracted {} valid memories from conversation", memories.size());
    return memories;
  }

  /** Clean memory content by removing leading dashes and extra whitespace */
  private String cleanMemoryContent(String line) {
    // Remove the initial "- " prefix
    String content = line.substring(2);

    // Remove any additional leading dashes or whitespace
    content = content.replaceAll("^[-\\s]+", "");

    return content.trim();
  }

  /** Check if a memory content is valid and worth storing */
  private boolean isValidMemory(String content) {
    // Convert to lowercase for case-insensitive matching
    String lowerContent = content.toLowerCase().trim();

    // Filter out generic system messages
    String[] invalidPatterns = {
      "this is the user's first conversation",
      "this is the first conversation",
      "hello! this is my first time",
      "how can i help",
      "i'm here to help",
      "what can i do for you",
      "nice to meet you",
      "the user is asking",
      "the assistant responded",
      "conversation started",
      "session began",
      "first interaction"
    };

    for (String pattern : invalidPatterns) {
      if (lowerContent.contains(pattern)) {
        logger.debug("Filtering out invalid memory: '{}'", content);
        return false;
      }
    }

    // Must contain some meaningful content (at least 5 characters)
    if (content.length() < 5) {
      logger.debug("Filtering out too short memory: '{}'", content);
      return false;
    }

    // Should not be pure punctuation or numbers
    if (content.matches("[^a-zA-Z\\u4e00-\\u9fff]*")) {
      logger.debug("Filtering out non-text memory: '{}'", content);
      return false;
    }

    return true;
  }

  /** Create a memory item from content */
  private MemoryItem createMemoryItem(
      String content, String userId, Map<String, Object> metadata, MemoryType memoryType) {

    MemoryItem item = new MemoryItem(content, memoryType.getValue());
    item.setUserId(userId);
    item.setMetadata(metadata);

    return item;
  }

  /** Build search filters */
  private Map<String, Object> buildSearchFilters(
      String userId, Map<String, Object> additionalFilters) {

    Map<String, Object> filters = new HashMap<>();
    filters.put("user_id", userId);

    if (additionalFilters != null) {
      filters.putAll(additionalFilters);
    }

    return filters;
  }

  /** Get memory by ID */
  public MemoryItem get(String memoryId) {

    try {
      return vectorStoreService.get(memoryId);
    } catch (Exception e) {
      logger.error("Error getting memory: {}", memoryId, e);
      throw new RuntimeException("Failed to get memory", e);
    }
  }

  /** Reset all memories (for testing) */
  public void reset() {

    try {
      vectorStoreService.reset();
      logger.info("Reset all memories");
    } catch (Exception e) {
      logger.error("Error resetting memories", e);
      throw new RuntimeException("Failed to reset memories", e);
    }
  }

  /** Filter results by semantic relevance using keyword matching */
  private List<MemoryItem> filterBySemanticRelevance(String query, List<MemoryItem> results) {
    // If similarity threshold is already high (>= 0.4), trust the vector search
    // results
    // Only filter if we have many low-quality results
    if (results.isEmpty() || results.size() <= 3) {
      logger.debug(
          "Skipping semantic filtering - too few results or high threshold already applied");
      return results;
    }

    // Extract key topics from query
    String lowerQuery = query.toLowerCase();

    // For comprehensive queries (introduce, tell me about), return all results
    String[] comprehensiveQueries = {
      "介绍",
      "告诉我",
      "关于我",
      "我的信息",
      "我是谁",
      "说说我",
      "讲讲我",
      "describe me",
      "tell me about",
      "introduce me",
      "about me",
      "who am i",
      "my information"
    };

    for (String pattern : comprehensiveQueries) {
      if (lowerQuery.contains(pattern)) {
        logger.debug(
            "Comprehensive query detected, skipping semantic filtering to return all related"
                + " memories");
        return results;
      }
    }

    List<String> queryKeywords = extractKeywords(lowerQuery);

    // If no meaningful keywords extracted, return all results
    if (queryKeywords.isEmpty()) {
      logger.debug("No meaningful keywords extracted, returning all results");
      return results;
    }

    return results.stream()
        .filter(
            item -> {
              String lowerContent = item.getContent().toLowerCase();

              // Check if memory content contains relevant keywords
              boolean hasRelevantKeywords =
                  queryKeywords.stream().anyMatch(keyword -> lowerContent.contains(keyword));

              // Cross-language semantic matching for better multilingual support
              boolean hasCrossLanguageMatch = checkCrossLanguageRelevance(query, item.getContent());

              // Keep high similarity results regardless
              boolean isHighSimilarity = item.getScore() > 0.4;

              // More precise filtering - keep if semantic match found OR high similarity
              boolean isRelevant =
                  hasRelevantKeywords
                      || hasCrossLanguageMatch
                      || isHighSimilarity
                      || item.getScore() > 0.35;

              if (!isRelevant) {
                logger.debug(
                    "Filtered out irrelevant memory: '{}' (score: {}, keywords: {})",
                    item.getContent(),
                    item.getScore(),
                    queryKeywords);
              }

              return isRelevant;
            })
        .collect(java.util.stream.Collectors.toList());
  }

  /** Extract keywords from query for relevance checking */
  private List<String> extractKeywords(String query) {
    // Remove common words and extract meaningful keywords
    List<String> keywords = new ArrayList<>();

    // Split query into words and extract meaningful ones
    String[] words = query.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9\\s]", " ").split("\\s+");

    for (String word : words) {
      word = word.trim().toLowerCase();
      // Skip common words and short words
      if (word.length() >= 2 && !isCommonWord(word)) {
        keywords.add(word);
      }
    }

    logger.debug("Extracted keywords from query '{}': {}", query, keywords);
    return keywords;
  }

  /** Check if a word is a common word that should be ignored */
  private boolean isCommonWord(String word) {
    // Chinese and English common words
    String[] commonWords = {
      "我", "你", "他", "她", "它", "的", "是", "在", "有", "和", "与", "了", "吗", "呢", "吧", "啊", "什么", "怎么",
      "为什么", "哪里", "谁", "when", "where", "what", "how", "why", "who", "i", "you", "he", "she", "it",
      "is", "are", "was", "were", "am", "be", "been", "being", "a", "an", "the", "and", "or", "but",
      "if", "then", "that", "this", "these", "those"
    };

    for (String common : commonWords) {
      if (word.equals(common)) {
        return true;
      }
    }
    return false;
  }

  /** Determine appropriate similarity threshold based on query type */
  private Double determineThreshold(String query, Double customThreshold) {
    // If custom threshold is provided, use it
    if (customThreshold != null) {
      return customThreshold;
    }

    String lowerQuery = query.toLowerCase();

    // For comprehensive queries (like "introduce me", "tell me about myself"),
    // use a lower threshold to capture more related information
    String[] comprehensiveQueries = {
      "介绍",
      "告诉我",
      "关于我",
      "我的信息",
      "我是谁",
      "说说我",
      "讲讲我",
      "describe me",
      "tell me about",
      "introduce me",
      "about me",
      "who am i",
      "my information"
    };

    for (String pattern : comprehensiveQueries) {
      if (lowerQuery.contains(pattern)) {
        logger.debug("Detected comprehensive query, using lower threshold: 0.1");
        return 0.1; // Much lower threshold for comprehensive queries
      }
    }

    // For specific queries, use medium threshold to balance precision and recall
    String[] specificQueries = {
      "喜欢喝",
      "喜欢吃",
      "喜欢玩",
      "喜欢看",
      "爱好",
      "什么食物",
      "什么运动",
      "什么饮料",
      "like to",
      "love to",
      "enjoy",
      "favorite",
      "what food",
      "what sport",
      "what drink",
      "my favorite",
      "i like",
      "i love",
      "i enjoy"
    };

    for (String pattern : specificQueries) {
      if (lowerQuery.contains(pattern)) {
        logger.debug("Detected specific query, using medium-high threshold: 0.3");
        return 0.3; // Medium-high threshold for specific queries - prioritize
        // precision
      }
    }

    // Default to config threshold
    return config.getSimilarityThreshold();
  }

  /** Check cross-language semantic relevance */
  private boolean checkCrossLanguageRelevance(String query, String memoryContent) {
    String lowerQuery = query.toLowerCase();
    String lowerMemory = memoryContent.toLowerCase();

    // Define semantic mappings between Chinese and English for common concepts
    Map<String, String[]> semanticMappings = new HashMap<>();

    // Food-related mappings
    semanticMappings.put("food", new String[] {"食物", "吃", "喜欢吃", "爱吃"});
    semanticMappings.put("favorite", new String[] {"喜欢", "最爱", "偏爱", "钟爱"});
    semanticMappings.put("drink", new String[] {"喝", "饮料", "喜欢喝", "爱喝"});
    semanticMappings.put("sport", new String[] {"运动", "体育", "喜欢玩", "锻炼"});
    semanticMappings.put("hobby", new String[] {"爱好", "兴趣", "喜欢"});

    // Reverse mappings (Chinese to English)
    semanticMappings.put("食物", new String[] {"food", "eat", "favorite food"});
    semanticMappings.put("喜欢吃", new String[] {"like to eat", "love eating", "favorite food"});
    semanticMappings.put("喜欢喝", new String[] {"like to drink", "love drinking", "favorite drink"});
    semanticMappings.put("运动", new String[] {"sport", "exercise", "activity"});
    semanticMappings.put("爱好", new String[] {"hobby", "interest", "favorite"});

    // Check if query contains concepts that map to memory content
    // Use more precise matching - require both concept and mapping to be present
    for (Map.Entry<String, String[]> entry : semanticMappings.entrySet()) {
      String concept = entry.getKey();
      String[] mappings = entry.getValue();

      if (lowerQuery.contains(concept)) {
        for (String mapping : mappings) {
          if (lowerMemory.contains(mapping)) {
            // Additional check: ensure the match is contextually relevant
            if (isContextuallyRelevant(concept, mapping, lowerQuery, lowerMemory)) {
              logger.debug(
                  "Cross-language match found: query '{}' contains '{}', memory '{}' contains '{}'",
                  query,
                  concept,
                  memoryContent,
                  mapping);
              return true;
            }
          }
        }
      }
    }

    return false;
  }

  /** Check if the cross-language match is contextually relevant */
  private boolean isContextuallyRelevant(
      String concept, String mapping, String query, String memory) {
    // For food-related queries, ensure both query and memory are about food
    if ((concept.equals("food") || concept.equals("食物"))
        && (mapping.contains("吃") || mapping.contains("食物"))) {
      // Query should contain food-related terms and memory should contain
      // eating-related terms
      return (query.contains("food") || query.contains("favorite"))
          && (memory.contains("吃") || memory.contains("食物"));
    }

    // For drink-related queries
    if ((concept.equals("drink") || concept.equals("饮料"))
        && (mapping.contains("喝") || mapping.contains("饮料"))) {
      return (query.contains("drink") || query.contains("favorite"))
          && (memory.contains("喝") || memory.contains("饮料"));
    }

    // For sport-related queries
    if ((concept.equals("sport") || concept.equals("运动"))
        && (mapping.contains("运动") || mapping.contains("打"))) {
      return (query.contains("sport") || query.contains("exercise"))
          && (memory.contains("运动") || memory.contains("打") || memory.contains("球"));
    }

    // For general favorite queries, be more permissive but still contextual
    if (concept.equals("favorite") && memory.contains("喜欢")) {
      return true;
    }

    // Default: allow the match if it's a direct conceptual mapping
    return true;
  }
}
