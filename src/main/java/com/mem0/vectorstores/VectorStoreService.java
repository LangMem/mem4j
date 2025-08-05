package com.mem0.vectorstores;

import com.mem0.memory.MemoryItem;

import java.util.List;
import java.util.Map;

/**
 * Interface for vector store operations
 */
public interface VectorStoreService {

  /**
   * Add a memory item to the vector store
   */
  void add(MemoryItem item);

  /**
   * Search for similar memories using vector similarity
   */
  List<MemoryItem> search(double[] queryEmbedding, Map<String, Object> filters, int limit, double threshold);

  /**
   * Get all memories matching filters
   */
  List<MemoryItem> getAll(Map<String, Object> filters, int limit);

  /**
   * Get a specific memory by ID
   */
  MemoryItem get(String memoryId);

  /**
   * Update an existing memory item
   */
  void update(MemoryItem item);

  /**
   * Delete a memory by ID
   */
  void delete(String memoryId);

  /**
   * Delete all memories matching filters
   */
  void deleteAll(Map<String, Object> filters);

  /**
   * Reset all memories (for testing)
   */
  void reset();
}