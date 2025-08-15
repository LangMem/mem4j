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

package com.mem4j.vectorstores;

import com.mem4j.memory.MemoryItem;

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
