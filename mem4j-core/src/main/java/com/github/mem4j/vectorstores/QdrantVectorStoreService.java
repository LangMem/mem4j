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

package com.github.mem4j.vectorstores;

import com.github.mem4j.config.MemoryConfigurableurable;
import com.github.mem4j.memory.MemoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Qdrant implementation of VectorStoreService
 * <p>
 * This implementation provides vector storage capabilities using Qdrant, supporting
 * operations like adding, searching, updating, and deleting memory items.
 *
 * Note: This is a simplified implementation that falls back to in-memory storage. For
 * full Qdrant integration, ensure the correct Qdrant client dependencies are available
 * and uncomment the Qdrant-specific code sections.
 */
@Service
public class QdrantVectorStoreService implements VectorStoreService {

	private static final Logger logger = LoggerFactory.getLogger(QdrantVectorStoreService.class);

	// TODO: Add Qdrant client when dependencies are properly configured
	// private final QdrantClient client;
	private final String collectionName;

	private final int vectorSize;

	private final String url;

	// In-memory storage for development/testing - replace with actual Qdrant client
	private final Map<String, MemoryItem> memoryStore = new HashMap<>();

	/**
	 * Constructor for QdrantVectorStoreService
	 * @param config Memory configuration containing vector store settings
	 */
	public QdrantVectorStoreService(MemoryConfigurable config) {
		this.collectionName = config.getVectorStore().getCollection();
		this.vectorSize = config.getEmbeddingDimension();
		this.url = config.getVectorStore().getUrl() != null ? config.getVectorStore().getUrl()
				: "http://localhost:6333";

		logger.info("Initialized QdrantVectorStoreService with collection: {}, url: {}", collectionName, url);
		logger.warn("Using in-memory storage fallback. For production, configure Qdrant client properly.");

		// TODO: Initialize actual Qdrant client
		/*
		 * try { this.client = new QdrantClient(QdrantGrpcClient.newBuilder(url,
		 * false).build()); ensureCollectionExists(); } catch (Exception e) {
		 * logger.error("Failed to initialize Qdrant client", e); throw new
		 * RuntimeException("Failed to initialize Qdrant client", e); }
		 */
	}

	@Override
	public void add(MemoryItem item) {
		try {
			String pointId = item.getId() != null ? item.getId() : UUID.randomUUID().toString();
			item.setId(pointId);

			// For now, use in-memory storage
			memoryStore.put(pointId, item);
			logger.debug("Added memory item: {}", pointId);

			// TODO: Replace with actual Qdrant implementation
			/*
			 * // Create vector from embedding List<Float> vectorList =
			 * Arrays.stream(item.getEmbedding()) .boxed() .map(Double::floatValue)
			 * .collect(Collectors.toList());
			 *
			 * // Build point with payload and vector Points.PointStruct point =
			 * Points.PointStruct.newBuilder()
			 * .setId(Points.PointId.newBuilder().setUuid(pointId).build())
			 * .putAllPayload(buildPayload(item)) .setVectors(Points.Vectors.newBuilder()
			 * .setVector(Points.Vector.newBuilder() .addAllData(vectorList) .build())
			 * .build()) .build();
			 *
			 * // Upsert the point Points.UpsertPoints request =
			 * Points.UpsertPoints.newBuilder() .setCollectionName(collectionName)
			 * .addPoints(point) .build();
			 *
			 * client.upsertAsync(request).get();
			 */
		}
		catch (Exception e) {
			logger.error("Error adding memory item", e);
			throw new RuntimeException("Failed to add memory item", e);
		}
	}

	@Override
	public List<MemoryItem> search(Double[] queryEmbedding, Map<String, Object> filters, Integer limit,
			Double threshold) {
		try {
			// Simple similarity search using in-memory storage
			List<MemoryItem> results = memoryStore.values()
				.stream()
				.filter(item -> matchesFilters(item, filters))
				.map(item -> {
					// Calculate simple cosine similarity
					Double similarity = calculateCosineSimilarity(queryEmbedding, item.getEmbedding());
					// Store similarity for sorting
					return new AbstractMap.SimpleEntry<>(similarity, item);
				})
				.filter(entry -> entry.getKey() >= threshold)
				.sorted((e1, e2) -> Double.compare(e2.getKey(), e1.getKey()))
				.limit(limit)
				.map(AbstractMap.SimpleEntry::getValue)
				.collect(Collectors.toList());

			logger.debug("Found {} similar memories", results.size());
			return results;

			// TODO: Replace with actual Qdrant implementation
			/*
			 * Collections.Filter filter = buildFilter(filters);
			 *
			 * List<Float> queryVector = Arrays.stream(queryEmbedding) .boxed()
			 * .map(Double::floatValue) .collect(Collectors.toList());
			 *
			 * Points.SearchPoints searchRequest = Points.SearchPoints.newBuilder()
			 * .setCollectionName(collectionName) .addAllVector(queryVector)
			 * .setLimit(limit) .setScoreThreshold((float) threshold) .setFilter(filter)
			 * .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).
			 * build ()) .build();
			 *
			 * List<Points.ScoredPoint> response =
			 * client.searchAsync(searchRequest).get(); return response.stream()
			 * .map(this::convertToMemoryItem) .collect(Collectors.toList());
			 */
		}
		catch (Exception e) {
			logger.error("Error searching memories", e);
			throw new RuntimeException("Failed to search memories", e);
		}
	}

	@Override
	public List<MemoryItem> getAll(Map<String, Object> filters, Integer limit) {
		try {
			List<MemoryItem> results = memoryStore.values()
				.stream()
				.filter(item -> matchesFilters(item, filters))
				.limit(limit)
				.collect(Collectors.toList());

			logger.debug("Retrieved {} memories", results.size());
			return results;

			// TODO: Replace with actual Qdrant implementation
			/*
			 * Collections.Filter filter = buildFilter(filters);
			 *
			 * Points.ScrollPoints scrollRequest = Points.ScrollPoints.newBuilder()
			 * .setCollectionName(collectionName) .setFilter(filter) .setLimit(limit)
			 * .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).
			 * build ()) .build();
			 *
			 * List<Points.RetrievedPoint> response =
			 * client.scrollAsync(scrollRequest).get(); return response.stream()
			 * .map(this::convertToMemoryItem) .collect(Collectors.toList());
			 */
		}
		catch (Exception e) {
			logger.error("Error getting all memories", e);
			throw new RuntimeException("Failed to get memories", e);
		}
	}

	@Override
	public MemoryItem get(String memoryId) {
		try {
			MemoryItem item = memoryStore.get(memoryId);
			logger.debug("Retrieved memory: {}", memoryId);
			return item;

			// TODO: Replace with actual Qdrant implementation
			/*
			 * Points.GetPoints getRequest = Points.GetPoints.newBuilder()
			 * .setCollectionName(collectionName)
			 * .addIds(Points.PointId.newBuilder().setUuid(memoryId).build())
			 * .setWithPayload(Points.WithPayloadSelector.newBuilder().setEnable(true).
			 * build ()) .build();
			 *
			 * List<Points.RetrievedPoint> response = client.getAsync(getRequest).get();
			 * if (!response.isEmpty()) { return convertToMemoryItem(response.get(0)); }
			 * return null;
			 */
		}
		catch (Exception e) {
			logger.error("Error getting memory: {}", memoryId, e);
			throw new RuntimeException("Failed to get memory", e);
		}
	}

	@Override
	public void update(MemoryItem item) {
		add(item); // Both in-memory and Qdrant upsert handle updates the same way
	}

	@Override
	public void delete(String memoryId) {
		try {
			memoryStore.remove(memoryId);
			logger.debug("Deleted memory: {}", memoryId);

			// TODO: Replace with actual Qdrant implementation
			/*
			 * Points.DeletePoints deleteRequest = Points.DeletePoints.newBuilder()
			 * .setCollectionName(collectionName)
			 * .setPoints(Points.PointsSelector.newBuilder()
			 * .setPoints(Points.PointsIdsList.newBuilder()
			 * .addIds(Points.PointId.newBuilder().setUuid(memoryId).build()) .build())
			 * .build()) .build();
			 *
			 * client.deleteAsync(deleteRequest).get();
			 */
		}
		catch (Exception e) {
			logger.error("Error deleting memory: {}", memoryId, e);
			throw new RuntimeException("Failed to delete memory", e);
		}
	}

	@Override
	public void deleteAll(Map<String, Object> filters) {
		try {
			List<String> toDelete = memoryStore.values()
				.stream()
				.filter(item -> matchesFilters(item, filters))
				.map(MemoryItem::getId)
				.collect(Collectors.toList());

			toDelete.forEach(memoryStore::remove);
			logger.debug("Deleted {} memories with filters: {}", toDelete.size(), filters);

			// TODO: Replace with actual Qdrant implementation
			/*
			 * Collections.Filter filter = buildFilter(filters);
			 *
			 * Points.DeletePoints deleteRequest = Points.DeletePoints.newBuilder()
			 * .setCollectionName(collectionName)
			 * .setPoints(Points.PointsSelector.newBuilder() .setFilter(filter) .build())
			 * .build();
			 *
			 * client.deleteAsync(deleteRequest).get();
			 */
		}
		catch (Exception e) {
			logger.error("Error deleting memories with filters: {}", filters, e);
			throw new RuntimeException("Failed to delete memories", e);
		}
	}

	@Override
	public void reset() {
		try {
			memoryStore.clear();
			logger.info("Reset vector store collection");

			// TODO: Replace with actual Qdrant implementation
			/*
			 * client.deleteCollectionAsync(collectionName).get();
			 * ensureCollectionExists();
			 */
		}
		catch (Exception e) {
			logger.error("Error resetting vector store", e);
			throw new RuntimeException("Failed to reset vector store", e);
		}
	}

	/**
	 * Calculate cosine similarity between two vectors
	 */
	private Double calculateCosineSimilarity(Double[] vec1, Double[] vec2) {
		if (vec1.length != vec2.length) {
			return 0.0;
		}

		double dotProduct = 0.0;
		double norm1 = 0.0;
		double norm2 = 0.0;

		for (int i = 0; i < vec1.length; i++) {
			dotProduct += vec1[i] * vec2[i];
			norm1 += vec1[i] * vec1[i];
			norm2 += vec2[i] * vec2[i];
		}

		return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
	}

	/**
	 * Check if a memory item matches the given filters
	 */
	private boolean matchesFilters(MemoryItem item, Map<String, Object> filters) {
		if (filters == null || filters.isEmpty()) {
			return true;
		}

		for (Map.Entry<String, Object> filter : filters.entrySet()) {
			String key = filter.getKey();
			String value = filter.getValue().toString();

			switch (key) {
				case "user_id" -> {
					if (!value.equals(item.getUserId()))
						return false;
				}
				case "agent_id" -> {
					if (!value.equals(item.getAgentId()))
						return false;
				}
				case "run_id" -> {
					if (!value.equals(item.getRunId()))
						return false;
				}
				case "actor_id" -> {
					if (!value.equals(item.getActorId()))
						return false;
				}
				case "memory_type" -> {
					if (!value.equals(item.getMemoryType()))
						return false;
				}
			}
		}

		return true;
	}

	/*
	 * TODO: Uncomment when Qdrant dependencies are properly configured
	 *
	 * private void ensureCollectionExists() { try { List<String> collections =
	 * client.listCollectionsAsync().get();
	 *
	 * boolean exists = collections.contains(collectionName);
	 *
	 * if (!exists) { Collections.VectorParams vectorParams =
	 * Collections.VectorParams.newBuilder() .setSize(vectorSize)
	 * .setDistance(Collections.Distance.Cosine) .build();
	 *
	 * Collections.CreateCollection createRequest =
	 * Collections.CreateCollection.newBuilder() .setCollectionName(collectionName)
	 * .setVectorsConfig(Collections.VectorsConfig.newBuilder() .setParams(vectorParams)
	 * .build()) .build();
	 *
	 * client.createCollectionAsync(createRequest).get();
	 * logger.info("Created collection: {}", collectionName); } } catch (Exception e) {
	 * logger.error("Error ensuring collection exists", e); throw new
	 * RuntimeException("Failed to create collection", e); } }
	 *
	 * private Map<String, Collections.Value> buildPayload(MemoryItem item) { Map<String,
	 * Collections.Value> payload = new HashMap<>();
	 *
	 * if (item.getContent() != null) { payload.put("content",
	 * Collections.Value.newBuilder().setStringValue(item.getContent()).build()); } if
	 * (item.getMemoryType() != null) { payload.put("memory_type",
	 * Collections.Value.newBuilder().setStringValue(item.getMemoryType()).build()); } if
	 * (item.getUserId() != null) { payload.put("user_id",
	 * Collections.Value.newBuilder().setStringValue(item.getUserId()).build()); } if
	 * (item.getAgentId() != null) { payload.put("agent_id",
	 * Collections.Value.newBuilder().setStringValue(item.getAgentId()).build()); } if
	 * (item.getRunId() != null) { payload.put("run_id",
	 * Collections.Value.newBuilder().setStringValue(item.getRunId()).build()); } if
	 * (item.getActorId() != null) { payload.put("actor_id",
	 * Collections.Value.newBuilder().setStringValue(item.getActorId()).build()); } if
	 * (item.getCreatedAt() != null) { payload.put("created_at",
	 * Collections.Value.newBuilder().setIntegerValue(item.getCreatedAt().
	 * toEpochMilli()).build()); } if (item.getUpdatedAt() != null) {
	 * payload.put("updated_at",
	 * Collections.Value.newBuilder().setIntegerValue(item.getUpdatedAt().
	 * toEpochMilli()).build()); }
	 *
	 * return payload; }
	 *
	 * private Collections.Filter buildFilter(Map<String, Object> filters) { if (filters
	 * == null || filters.isEmpty()) { return Collections.Filter.newBuilder().build(); }
	 *
	 * var conditions = filters.entrySet().stream() .map(entry ->
	 * Collections.Condition.newBuilder()
	 * .setField(Collections.FieldCondition.newBuilder() .setKey(entry.getKey())
	 * .setMatch(Collections.Match.newBuilder() .setKeyword(entry.getValue().toString())
	 * .build()) .build()) .build()) .collect(Collectors.toList());
	 *
	 * return Collections.Filter.newBuilder() .addAllMust(conditions) .build(); }
	 *
	 * private MemoryItem convertToMemoryItem(Points.RetrievedPoint record) { MemoryItem
	 * item = new MemoryItem();
	 *
	 * if (record.getId().hasUuid()) { item.setId(record.getId().getUuid()); } else if
	 * (record.getId().hasNum()) { item.setId(String.valueOf(record.getId().getNum())); }
	 *
	 * record.getPayloadMap().forEach((key, value) -> { switch (key) { case "content" ->
	 * item.setContent(value.getStringValue()); case "memory_type" ->
	 * item.setMemoryType(value.getStringValue()); case "user_id" ->
	 * item.setUserId(value.getStringValue()); case "agent_id" ->
	 * item.setAgentId(value.getStringValue()); case "run_id" ->
	 * item.setRunId(value.getStringValue()); case "actor_id" ->
	 * item.setActorId(value.getStringValue()); case "created_at" ->
	 * item.setCreatedAt(Instant.ofEpochMilli(value.getIntegerValue())); case "updated_at"
	 * -> item.setUpdatedAt(Instant.ofEpochMilli(value.getIntegerValue())); } });
	 *
	 * return item; }
	 *
	 * private MemoryItem convertToMemoryItem(Points.ScoredPoint scoredPoint) { MemoryItem
	 * item = new MemoryItem();
	 *
	 * if (scoredPoint.getId().hasUuid()) { item.setId(scoredPoint.getId().getUuid()); }
	 * else if (scoredPoint.getId().hasNum()) {
	 * item.setId(String.valueOf(scoredPoint.getId().getNum())); }
	 *
	 * scoredPoint.getPayloadMap().forEach((key, value) -> { switch (key) { case "content"
	 * -> item.setContent(value.getStringValue()); case "memory_type" ->
	 * item.setMemoryType(value.getStringValue()); case "user_id" ->
	 * item.setUserId(value.getStringValue()); case "agent_id" ->
	 * item.setAgentId(value.getStringValue()); case "run_id" ->
	 * item.setRunId(value.getStringValue()); case "actor_id" ->
	 * item.setActorId(value.getStringValue()); case "created_at" ->
	 * item.setCreatedAt(Instant.ofEpochMilli(value.getIntegerValue())); case "updated_at"
	 * -> item.setUpdatedAt(Instant.ofEpochMilli(value.getIntegerValue())); } });
	 *
	 * return item; }
	 */

}
