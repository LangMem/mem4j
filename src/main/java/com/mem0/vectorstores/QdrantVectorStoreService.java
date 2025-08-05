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

//package com.mem0.vectorstores;
//
//import com.mem0.configs.MemoryConfig;
//import com.mem0.memory.MemoryItem;
//import io.qdrant.client.QdrantClient;
//import io.qdrant.client.QdrantGrpcClient;
//import io.qdrant.client.grpc.Collections;
//import io.qdrant.client.grpc.Points;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * Qdrant implementation of VectorStoreService
// */
//@Service
//public class QdrantVectorStoreService implements VectorStoreService {
//
//  private static final Logger logger = LoggerFactory.getLogger(QdrantVectorStoreService.class);
//
//  private final QdrantClient client;
//  private final String collectionName;
//  private final int vectorSize;
//
//  public QdrantVectorStoreService(MemoryConfig config) {
//    this.collectionName = config.getVectorStore().getCollection();
//    this.vectorSize = config.getEmbeddingDimension();
//
//    // Initialize Qdrant client
//    this.client = new QdrantClient(
//        QdrantGrpcClient.newBuilder(
//            config.getVectorStore().getUrl(),
//            false).build());
//
//    // Ensure collection exists
//    ensureCollectionExists();
//  }
//
//  @Override
//  public void add(MemoryItem item) {
//    try {
//      Points.PointStruct point = Points.PointStruct.newBuilder()
//          .setId(item.getId() != null ? item.getId() : UUID.randomUUID().toString())
//          .putAllPayload(buildPayload(item))
//          .addAllVectors(Collections.Vectors.newBuilder()
//              .addAllVector(Arrays.stream(item.getEmbedding())
//                  .boxed()
//                  .collect(Collectors.toList()))
//              .build())
//          .build();
//
//      client.upsert(collectionName, List.of(point), null, null, null);
//      logger.debug("Added memory item: {}", item.getId());
//    } catch (Exception e) {
//      logger.error("Error adding memory item", e);
//      throw new RuntimeException("Failed to add memory item", e);
//    }
//  }
//
//  @Override
//  public List<MemoryItem> search(double[] queryEmbedding, Map<String, Object> filters, int limit, double threshold) {
//    try {
//      // Build filter conditions
//      Collections.Filter filter = buildFilter(filters);
//
//      // Perform search
//      var response = client.search(
//          collectionName,
//          Arrays.stream(queryEmbedding).boxed().collect(Collectors.toList()),
//          Collections.Vectors.newBuilder().build(),
//          filter,
//          limit,
//          threshold,
//          null,
//          null,
//          null);
//
//      return response.getResultList().stream()
//          .map(this::convertToMemoryItem)
//          .collect(Collectors.toList());
//    } catch (Exception e) {
//      logger.error("Error searching memories", e);
//      throw new RuntimeException("Failed to search memories", e);
//    }
//  }
//
//  @Override
//  public List<MemoryItem> getAll(Map<String, Object> filters, int limit) {
//    try {
//      Collections.Filter filter = buildFilter(filters);
//
//      var response = client.scroll(
//          collectionName,
//          filter,
//          null,
//          limit,
//          null,
//          null,
//          null);
//
//      return response.getResultList().stream()
//          .map(this::convertToMemoryItem)
//          .collect(Collectors.toList());
//    } catch (Exception e) {
//      logger.error("Error getting all memories", e);
//      throw new RuntimeException("Failed to get memories", e);
//    }
//  }
//
//  @Override
//  public MemoryItem get(String memoryId) {
//    try {
//      var response = client.retrieve(
//          collectionName,
//          List.of(memoryId),
//          null,
//          null,
//          null);
//
//      if (!response.getResultList().isEmpty()) {
//        return convertToMemoryItem(response.getResultList().get(0));
//      }
//      return null;
//    } catch (Exception e) {
//      logger.error("Error getting memory: {}", memoryId, e);
//      throw new RuntimeException("Failed to get memory", e);
//    }
//  }
//
//  @Override
//  public void update(MemoryItem item) {
//    add(item); // Qdrant upsert handles updates
//  }
//
//  @Override
//  public void delete(String memoryId) {
//    try {
//      client.delete(collectionName, List.of(memoryId), null, null);
//      logger.debug("Deleted memory: {}", memoryId);
//    } catch (Exception e) {
//      logger.error("Error deleting memory: {}", memoryId, e);
//      throw new RuntimeException("Failed to delete memory", e);
//    }
//  }
//
//  @Override
//  public void deleteAll(Map<String, Object> filters) {
//    try {
//      Collections.Filter filter = buildFilter(filters);
//      client.delete(collectionName, null, filter, null);
//      logger.debug("Deleted memories with filters: {}", filters);
//    } catch (Exception e) {
//      logger.error("Error deleting memories with filters: {}", filters, e);
//      throw new RuntimeException("Failed to delete memories", e);
//    }
//  }
//
//  @Override
//  public void reset() {
//    try {
//      client.deleteCollection(collectionName);
//      ensureCollectionExists();
//      logger.info("Reset vector store collection");
//    } catch (Exception e) {
//      logger.error("Error resetting vector store", e);
//      throw new RuntimeException("Failed to reset vector store", e);
//    }
//  }
//
//  private void ensureCollectionExists() {
//    try {
//      var collections = client.listCollections();
//      boolean exists = collections.getCollectionsList().stream()
//          .anyMatch(info -> info.getName().equals(collectionName));
//
//      if (!exists) {
//        var vectorParams = Collections.VectorParams.newBuilder()
//            .setSize(vectorSize)
//            .setDistance(Collections.Distance.Cosine)
//            .build();
//
//        var createRequest = Collections.CreateCollection.newBuilder()
//            .setCollectionName(collectionName)
//            .setVectorsConfig(Collections.VectorsConfig.newBuilder()
//                .setParams(vectorParams)
//                .build())
//            .build();
//
//        client.createCollection(createRequest);
//        logger.info("Created collection: {}", collectionName);
//      }
//    } catch (Exception e) {
//      logger.error("Error ensuring collection exists", e);
//      throw new RuntimeException("Failed to create collection", e);
//    }
//  }
//
//  private Map<String, Collections.Value> buildPayload(MemoryItem item) {
//    Map<String, Collections.Value> payload = new HashMap<>();
//
//    if (item.getContent() != null) {
//      payload.put("content", Collections.Value.newBuilder().setStringValue(item.getContent()).build());
//    }
//    if (item.getMemoryType() != null) {
//      payload.put("memory_type", Collections.Value.newBuilder().setStringValue(item.getMemoryType()).build());
//    }
//    if (item.getUserId() != null) {
//      payload.put("user_id", Collections.Value.newBuilder().setStringValue(item.getUserId()).build());
//    }
//    if (item.getAgentId() != null) {
//      payload.put("agent_id", Collections.Value.newBuilder().setStringValue(item.getAgentId()).build());
//    }
//    if (item.getRunId() != null) {
//      payload.put("run_id", Collections.Value.newBuilder().setStringValue(item.getRunId()).build());
//    }
//    if (item.getActorId() != null) {
//      payload.put("actor_id", Collections.Value.newBuilder().setStringValue(item.getActorId()).build());
//    }
//    if (item.getCreatedAt() != null) {
//      payload.put("created_at",
//          Collections.Value.newBuilder().setIntegerValue(item.getCreatedAt().toEpochMilli()).build());
//    }
//    if (item.getUpdatedAt() != null) {
//      payload.put("updated_at",
//          Collections.Value.newBuilder().setIntegerValue(item.getUpdatedAt().toEpochMilli()).build());
//    }
//
//    return payload;
//  }
//
//  private Collections.Filter buildFilter(Map<String, Object> filters) {
//    if (filters == null || filters.isEmpty()) {
//      return Collections.Filter.newBuilder().build();
//    }
//
//    var conditions = filters.entrySet().stream()
//        .map(entry -> Collections.Condition.newBuilder()
//            .setField(Collections.FieldCondition.newBuilder()
//                .setKey(entry.getKey())
//                .setMatch(Collections.Match.newBuilder()
//                    .setKeyword(entry.getValue().toString())
//                    .build())
//                .build())
//            .build())
//        .collect(Collectors.toList());
//
//    return Collections.Filter.newBuilder()
//        .addAllMust(conditions)
//        .build();
//  }
//
//  private MemoryItem convertToMemoryItem(Points.Record record) {
//    MemoryItem item = new MemoryItem();
//    item.setId(record.getId());
//
//    // Convert payload back to MemoryItem fields
//    record.getPayloadMap().forEach((key, value) -> {
//      switch (key) {
//        case "content" -> item.setContent(value.getStringValue());
//        case "memory_type" -> item.setMemoryType(value.getStringValue());
//        case "user_id" -> item.setUserId(value.getStringValue());
//        case "agent_id" -> item.setAgentId(value.getStringValue());
//        case "run_id" -> item.setRunId(value.getStringValue());
//        case "actor_id" -> item.setActorId(value.getStringValue());
//        case "created_at" -> item.setCreatedAt(java.time.Instant.ofEpochMilli(value.getIntegerValue()));
//        case "updated_at" -> item.setUpdatedAt(java.time.Instant.ofEpochMilli(value.getIntegerValue()));
//      }
//    });
//
//    return item;
//  }
//}
