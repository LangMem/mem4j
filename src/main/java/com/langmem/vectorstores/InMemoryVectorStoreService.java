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

package com.langmem.vectorstores;

import com.langmem.configs.MemoryConfig;
import com.langmem.memory.MemoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of VectorStoreService for testing and development
 */
@Service
public class InMemoryVectorStoreService implements VectorStoreService {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryVectorStoreService.class);

  private final Map<String, MemoryItem> memoryStore = new ConcurrentHashMap<>();
  private final Map<String, double[]> embeddings = new ConcurrentHashMap<>();

  @Override
  public void add(MemoryItem item) {
    try {
      String id = item.getId() != null ? item.getId() : UUID.randomUUID().toString();
      item.setId(id);
      memoryStore.put(id, item);
      if (item.getEmbedding() != null) {
        embeddings.put(id, item.getEmbedding());
      }
      logger.debug("Added memory item: {}", id);
    } catch (Exception e) {
      logger.error("Error adding memory item", e);
      throw new RuntimeException("Failed to add memory item", e);
    }
  }

  @Override
  public List<MemoryItem> search(double[] queryEmbedding, Map<String, Object> filters, int limit, double threshold) {
    try {
      return memoryStore.values().stream()
          .filter(item -> matchesFilters(item, filters))
          .map(item -> {
            MemoryItem result = new MemoryItem(item.getContent(), item.getMemoryType());
            result.setId(item.getId());
            result.setUserId(item.getUserId());
            result.setAgentId(item.getAgentId());
            result.setRunId(item.getRunId());
            result.setActorId(item.getActorId());
            result.setMetadata(item.getMetadata());
            result.setCreatedAt(item.getCreatedAt());
            result.setUpdatedAt(item.getUpdatedAt());

            // Calculate similarity score
            double[] itemEmbedding = embeddings.get(item.getId());
            if (itemEmbedding != null) {
              double similarity = cosineSimilarity(queryEmbedding, itemEmbedding);
              result.setScore(similarity);
              return similarity >= threshold ? result : null;
            }
            return null;
          })
          .filter(Objects::nonNull)
          .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
          .limit(limit)
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error searching memories", e);
      throw new RuntimeException("Failed to search memories", e);
    }
  }

  @Override
  public List<MemoryItem> getAll(Map<String, Object> filters, int limit) {
    try {
      return memoryStore.values().stream()
          .filter(item -> matchesFilters(item, filters))
          .limit(limit)
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("Error getting all memories", e);
      throw new RuntimeException("Failed to get memories", e);
    }
  }

  @Override
  public MemoryItem get(String memoryId) {
    try {
      return memoryStore.get(memoryId);
    } catch (Exception e) {
      logger.error("Error getting memory: {}", memoryId, e);
      throw new RuntimeException("Failed to get memory", e);
    }
  }

  @Override
  public void update(MemoryItem item) {
    try {
      if (item.getId() != null && memoryStore.containsKey(item.getId())) {
        memoryStore.put(item.getId(), item);
        if (item.getEmbedding() != null) {
          embeddings.put(item.getId(), item.getEmbedding());
        }
        logger.debug("Updated memory item: {}", item.getId());
      } else {
        add(item);
      }
    } catch (Exception e) {
      logger.error("Error updating memory item", e);
      throw new RuntimeException("Failed to update memory item", e);
    }
  }

  @Override
  public void delete(String memoryId) {
    try {
      memoryStore.remove(memoryId);
      embeddings.remove(memoryId);
      logger.debug("Deleted memory: {}", memoryId);
    } catch (Exception e) {
      logger.error("Error deleting memory: {}", memoryId, e);
      throw new RuntimeException("Failed to delete memory", e);
    }
  }

  @Override
  public void deleteAll(Map<String, Object> filters) {
    try {
      List<String> toDelete = memoryStore.values().stream()
          .filter(item -> matchesFilters(item, filters))
          .map(MemoryItem::getId)
          .collect(Collectors.toList());

      toDelete.forEach(id -> {
        memoryStore.remove(id);
        embeddings.remove(id);
      });

      logger.debug("Deleted {} memories with filters: {}", toDelete.size(), filters);
    } catch (Exception e) {
      logger.error("Error deleting memories with filters: {}", filters, e);
      throw new RuntimeException("Failed to delete memories", e);
    }
  }

  @Override
  public void reset() {
    try {
      memoryStore.clear();
      embeddings.clear();
      logger.info("Reset in-memory vector store");
    } catch (Exception e) {
      logger.error("Error resetting vector store", e);
      throw new RuntimeException("Failed to reset vector store", e);
    }
  }

  private boolean matchesFilters(MemoryItem item, Map<String, Object> filters) {
    if (filters == null || filters.isEmpty()) {
      return true;
    }

    return filters.entrySet().stream().allMatch(entry -> {
      String key = entry.getKey();
      Object value = entry.getValue();

      return switch (key) {
        case "user_id" -> Objects.equals(item.getUserId(), value);
        case "agent_id" -> Objects.equals(item.getAgentId(), value);
        case "run_id" -> Objects.equals(item.getRunId(), value);
        case "actor_id" -> Objects.equals(item.getActorId(), value);
        case "memory_type" -> Objects.equals(item.getMemoryType(), value);
        default -> true;
      };
    });
  }

  private double cosineSimilarity(double[] a, double[] b) {
    if (a.length != b.length) {
      return 0.0;
    }

    double dotProduct = 0.0;
    double normA = 0.0;
    double normB = 0.0;

    for (int i = 0; i < a.length; i++) {
      dotProduct += a[i] * b[i];
      normA += a[i] * a[i];
      normB += b[i] * b[i];
    }

    if (normA == 0.0 || normB == 0.0) {
      return 0.0;
    }

    return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
  }
}
