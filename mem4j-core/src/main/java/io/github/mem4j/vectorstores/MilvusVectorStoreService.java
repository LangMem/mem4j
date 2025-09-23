/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the Lic		client.createCollection(createCollectionParam);
		logger.info("Milvus 集合已创建: {}", collectionName);
		
		// 加载集合到内存中
		ensureCollectionLoaded();se.
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

package io.github.mem4j.vectorstores;

import io.github.mem4j.config.MemoryConfigurable;
import io.github.mem4j.memory.MemoryItem;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Milvus implementation of VectorStoreService
 * <p>
 * This implementation provides vector storage capabilities using Milvus, supporting
 * operations like adding, searching, updating, and deleting memory items.
 */
@Service
@AllArgsConstructor
public class MilvusVectorStoreService implements VectorStoreService {

	private static final Logger logger = LoggerFactory.getLogger(MilvusVectorStoreService.class);

	private final MilvusClient client;

	private final String collectionName;

	private final int vectorSize;

	private final String url;

	// Default values for required fields
	private static final String DEFAULT_AGENT_ID = "default_agent";

	private static final String DEFAULT_USER_ID = "default_user";

	private static final String DEFAULT_MEMORY_TYPE = "factual";

	private static final String DEFAULT_CONTENT = "default_content";

	private static final String DEFAULT_RUN_ID = "default_run";

	private static final String DEFAULT_ACTOR_ID = "default_actor";

	/**
	 * Constructor for MilvusVectorStoreService
	 * @param config Memory configuration containing vector store settings
	 */
	public MilvusVectorStoreService(MemoryConfigurable config) {
		this.collectionName = config.getVectorStore().getCollection();
		this.vectorSize = config.getEmbeddingDimension();
		this.url = config.getVectorStore().getUrl() != null ? config.getVectorStore().getUrl()
				: "http://localhost:6333";
		logger.info("Initialized MilvusVectorStoreService with collection: {}, url: {}", collectionName, url);
		try {
			// 解析URL，支持grpc://和http://格式
			String host;
			int port;
			if (url.startsWith("grpc://")) {
				String[] parts = url.replace("grpc://", "").split(":");
				host = parts[0];
				port = Integer.parseInt(parts[1]);
			}
			else if (url.startsWith("http://")) {
				String[] parts = url.replace("http://", "").split(":");
				host = parts[0];
				port = Integer.parseInt(parts[1]);
			}
			else {
				// 默认格式 host:port
				String[] parts = url.split(":");
				host = parts[0];
				port = Integer.parseInt(parts[1]);
			}
			ConnectParam connectParam = ConnectParam.newBuilder().withHost(host).withPort(port).build();
			this.client = new MilvusServiceClient(connectParam);
			ensureCollectionExists();
			logger.info("Successfully connected to Milvus server at {}:{}", host, port);
		}
		catch (Exception e) {
			logger.error("Failed to initialize Milvus client", e);
			throw new RuntimeException("Failed to initialize Milvus client", e);
		}
	}

	private void ensureCollectionExists() {
		try {
			// 检查集合是否已存在
			HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
				.withCollectionName(collectionName)
				.build();
			R<Boolean> hasCollection = client.hasCollection(hasCollectionParam);
			if (hasCollection.getData() != null && hasCollection.getData()) {
				logger.info("Milvus 集合已存在: {}", collectionName);
				// 确保索引存在
				ensureIndexExists();
				// 确保集合已加载
				ensureCollectionLoaded();
				return;
			} // 构建字段定义
			List<FieldType> fieldsSchema = new ArrayList<>();
			// 主键字段
			fieldsSchema.add(FieldType.newBuilder()
				.withName("id")
				.withDataType(DataType.VarChar)
				.withMaxLength(64)
				.withPrimaryKey(true)
				.withAutoID(false)
				.build());
			// 向量字段
			fieldsSchema.add(FieldType.newBuilder()
				.withName("vector")
				.withDataType(DataType.FloatVector)
				.withDimension(vectorSize)
				.build());
			// 其他属性字段
			fieldsSchema.add(FieldType.newBuilder()
				.withName("content")
				.withDataType(DataType.VarChar)
				.withMaxLength(2048)
				.build());
			fieldsSchema.add(FieldType.newBuilder()
				.withName("memory_type")
				.withDataType(DataType.VarChar)
				.withMaxLength(64)
				.build());
			fieldsSchema.add(FieldType.newBuilder()
				.withName("user_id")
				.withDataType(DataType.VarChar)
				.withMaxLength(64)
				.build());
			fieldsSchema.add(FieldType.newBuilder()
				.withName("agent_id")
				.withDataType(DataType.VarChar)
				.withMaxLength(64)
				.build());
			fieldsSchema.add(
					FieldType.newBuilder().withName("run_id").withDataType(DataType.VarChar).withMaxLength(64).build());
			fieldsSchema.add(FieldType.newBuilder()
				.withName("actor_id")
				.withDataType(DataType.VarChar)
				.withMaxLength(64)
				.build());
			fieldsSchema.add(FieldType.newBuilder().withName("created_at").withDataType(DataType.Int64).build());
			fieldsSchema.add(FieldType.newBuilder().withName("updated_at").withDataType(DataType.Int64).build());

			// 创建集合参数
			CreateCollectionParam.Builder builder = CreateCollectionParam.newBuilder()
				.withCollectionName(collectionName)
				.withDescription("Mem4j memory collection")
				.withShardsNum(2);

			// 逐个添加字段类型
			for (FieldType fieldType : fieldsSchema) {
				builder.addFieldType(fieldType);
			}

			CreateCollectionParam createCollectionParam = builder.build();

			client.createCollection(createCollectionParam);
			logger.info("Milvus 集合已创建: {}", collectionName);

			// 创建向量字段的索引
			ensureIndexExists();

			// 加载集合到内存中
			ensureCollectionLoaded();
		}
		catch (Exception e) {
			logger.error("确保 Milvus 集合存在时出错", e);
			throw new RuntimeException("创建或检查 Milvus 集合失败", e);
		}
	}

	private void ensureIndexExists() {
		try {
			// 为向量字段创建索引
			CreateIndexParam indexParam = CreateIndexParam.newBuilder()
				.withCollectionName(collectionName)
				.withFieldName("vector")
				.withIndexType(IndexType.IVF_FLAT)
				.withMetricType(MetricType.COSINE)
				.withExtraParam("{\"nlist\":128}")
				.build();

			client.createIndex(indexParam);
			logger.info("Milvus 向量索引已创建: {}", collectionName);
		}
		catch (Exception e) {
			// 如果索引已存在，Milvus会抛出异常，但这是正常的
			logger.debug("索引可能已存在: {}", e.getMessage());
		}
	}

	private void ensureCollectionLoaded() {
		try {
			// 加载集合到内存中
			LoadCollectionParam loadParam = LoadCollectionParam.newBuilder().withCollectionName(collectionName).build();

			client.loadCollection(loadParam);

			logger.info("Milvus 集合已加载到内存: {}", collectionName);
		}
		catch (Exception e) {
			logger.error("确保 Milvus 集合加载时出错", e);
			throw new RuntimeException("加载 Milvus 集合失败", e);
		}
	}

	@Override
	public void add(MemoryItem item) {
		try {
			String pointId = item.getId() != null ? item.getId() : UUID.randomUUID().toString();
			item.setId(pointId);

			// Create vector from embedding
			List<Float> vectorList = Arrays.stream(item.getEmbedding())
				.map(Double::floatValue)
				.collect(Collectors.toList());

			// Build insert parameters
			List<InsertParam.Field> fields = new ArrayList<>();

			// Add ID field
			fields.add(new InsertParam.Field("id", Collections.singletonList(pointId)));

			// Add vector field
			fields.add(new InsertParam.Field("vector", Collections.singletonList(vectorList)));

			// Add payload fields
			// Add content field with default value if null
			String content = item.getContent() != null ? item.getContent() : DEFAULT_CONTENT;
			fields.add(new InsertParam.Field("content", Collections.singletonList(content)));
			// Add memory_type field with default value if null
			String memoryType = item.getMemoryType() != null ? item.getMemoryType() : DEFAULT_MEMORY_TYPE;
			fields.add(new InsertParam.Field("memory_type", Collections.singletonList(memoryType)));
			// Add user_id field with default value if null
			String userId = item.getUserId() != null ? item.getUserId() : DEFAULT_USER_ID;
			fields.add(new InsertParam.Field("user_id", Collections.singletonList(userId)));
			// Add agent_id field with default value if null
			String agentId = item.getAgentId() != null ? item.getAgentId() : DEFAULT_AGENT_ID;
			if (item.getAgentId() == null) {
				logger.debug("Using default agent_id: {} for memory item: {}", DEFAULT_AGENT_ID, pointId);
			}
			fields.add(new InsertParam.Field("agent_id", Collections.singletonList(agentId)));
			// Add run_id field with default value if null
			String runId = item.getRunId() != null ? item.getRunId() : DEFAULT_RUN_ID;
			if (item.getRunId() == null) {
				logger.debug("Using default run_id: {} for memory item: {}", DEFAULT_RUN_ID, pointId);
			}
			fields.add(new InsertParam.Field("run_id", Collections.singletonList(runId)));
			// Add actor_id field with default value if null
			String actorId = item.getActorId() != null ? item.getActorId() : DEFAULT_ACTOR_ID;
			if (item.getActorId() == null) {
				logger.debug("Using default actor_id: {} for memory item: {}", DEFAULT_ACTOR_ID, pointId);
			}
			fields.add(new InsertParam.Field("actor_id", Collections.singletonList(actorId)));
			// Add created_at field with current time if null
			long createdAt = item.getCreatedAt() != null ? item.getCreatedAt().toEpochMilli()
					: Instant.now().toEpochMilli();
			fields.add(new InsertParam.Field("created_at", Collections.singletonList(createdAt)));
			// Add updated_at field with current time if null
			long updatedAt = item.getUpdatedAt() != null ? item.getUpdatedAt().toEpochMilli()
					: Instant.now().toEpochMilli();
			fields.add(new InsertParam.Field("updated_at", Collections.singletonList(updatedAt)));

			InsertParam insertParam = InsertParam.newBuilder()
				.withCollectionName(collectionName)
				.withFields(fields)
				.build();

			client.insert(insertParam);
			logger.debug("Added memory item to Milvus: {}", pointId);
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
			// 确保集合已加载
			ensureCollectionLoaded();

			// 构建搜索表达式
			String searchExpr = buildSearchExpression(filters);

			// 转换查询向量为Float类型
			List<Float> queryVector = Arrays.stream(queryEmbedding).map(Double::floatValue).toList();

			// 构建搜索参数
			SearchParam searchParam = SearchParam.newBuilder()
				.withCollectionName(collectionName)
				.withMetricType(MetricType.COSINE)
				.withOutFields(Arrays.asList("id", "content", "memory_type", "user_id", "agent_id", "run_id",
						"actor_id", "created_at", "updated_at", "vector"))
				.withTopK(limit != null ? limit : 10)
				.withVectors(Collections.singletonList(queryVector))
				.withVectorFieldName("vector")
				.withExpr(searchExpr)
				.withParams("{\"nprobe\":10}")
				.build();

			// 执行搜索
			R<SearchResults> response = client.search(searchParam);
			if (response.getStatus() != R.Status.Success.getCode()) {
				throw new RuntimeException("Search failed: " + response.getMessage());
			}

			// 解析搜索结果 - 正确实现
			List<MemoryItem> results = new ArrayList<>();
			SearchResults searchResults = response.getData();

			if (searchResults != null && searchResults.getResults() != null) {
				// 获取搜索结果的字段数据
				List<FieldData> fieldsData = searchResults.getResults().getFieldsDataList();

				if (!fieldsData.isEmpty()) {
					// 创建字段映射
					Map<String, List<Object>> fieldMap = new HashMap<>();

					for (FieldData fieldData : fieldsData) {
						String fieldName = fieldData.getFieldName();
						List<Object> values = new ArrayList<>();

						if (fieldData.getType() == DataType.VarChar) {
							// 字符串字段
							List<String> stringData = fieldData.getScalars().getStringData().getDataList();
							values.addAll(stringData);
						}
						else if (fieldData.getType() == DataType.Int64) {
							// 长整型字段（时间戳）
							List<Long> longData = fieldData.getScalars().getLongData().getDataList();
							values.addAll(longData);
						}
						else if (fieldData.getType() == DataType.FloatVector) {
							// 向量字段
							List<Float> vectorData = fieldData.getVectors().getFloatVector().getDataList();
							values.addAll(vectorData);
						}

						fieldMap.put(fieldName, values);
					}

					// 获取结果数量
					int resultCount = fieldMap.get("id").size();

					// 构建MemoryItem对象并计算相似度
					for (int i = 0; i < resultCount; i++) {
						MemoryItem item = buildMemoryItemFromFieldMap(fieldMap, i, resultCount);

						// 计算相似度分数
						Double[] itemEmbedding = item.getEmbedding();
						if (itemEmbedding != null) {
							double similarity = cosineSimilarity(queryEmbedding, itemEmbedding);
							item.setScore(similarity);

							// 根据阈值过滤结果
							if (threshold == null || similarity >= threshold) {
								results.add(item);
							}
						}
					}

					// 按相似度分数降序排序
					results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
				}
			}

			logger.debug("Found {} similar memories", results.size());
			return results;
		}
		catch (Exception e) {
			logger.error("Error searching memories", e);
			throw new RuntimeException("Failed to search memories", e);
		}
	}

	@Override
	public List<MemoryItem> getAll(Map<String, Object> filters, Integer limit) {
		try {
			// 构建查询表达式
			String queryExpr = buildSearchExpression(filters);

			// 构建查询参数
			QueryParam queryParam = QueryParam.newBuilder()
				.withCollectionName(collectionName)
				.withExpr(queryExpr)
				.withOutFields(Arrays.asList("id", "content", "memory_type", "user_id", "agent_id", "run_id",
						"actor_id", "created_at", "updated_at"))
				.withLimit(limit != null ? Long.valueOf(limit) : 100L)
				.build();

			// 执行查询
			R<QueryResults> response = client.query(queryParam);
			if (response.getStatus() != R.Status.Success.getCode()) {
				throw new RuntimeException("Query failed: " + response.getMessage());
			}

			// 解析查询结果 - 正确实现
			List<MemoryItem> results = new ArrayList<>();
			QueryResults queryResults = response.getData();

			if (queryResults != null && queryResults.getFieldsDataCount() > 0) {
				// 直接获取字段数据列表
				List<FieldData> fieldsData = queryResults.getFieldsDataList();

				// 创建字段映射
				Map<String, List<Object>> fieldMap = new HashMap<>();

				for (FieldData fieldData : fieldsData) {
					String fieldName = fieldData.getFieldName();
					List<Object> values = new ArrayList<>();

					if (fieldData.getType() == DataType.VarChar) {
						// 字符串字段
						List<String> stringData = fieldData.getScalars().getStringData().getDataList();
						values.addAll(stringData);
					}
					else if (fieldData.getType() == DataType.Int64) {
						// 长整型字段（时间戳）
						List<Long> longData = fieldData.getScalars().getLongData().getDataList();
						values.addAll(longData);
					}

					fieldMap.put(fieldName, values);
				}

				// 获取结果数量
				int resultCount = fieldMap.get("id").size();

				// 构建MemoryItem对象
				for (int i = 0; i < resultCount; i++) {
					MemoryItem item = buildMemoryItemFromFieldMap(fieldMap, i, resultCount);
					results.add(item);
				}
			}

			logger.debug("Retrieved {} memories", results.size());
			return results;
		}
		catch (Exception e) {
			logger.error("Error getting all memories", e);
			throw new RuntimeException("Failed to get memories", e);
		}
	}

	@Override
	public MemoryItem get(String memoryId) {
		try {
			// 构建查询表达式，根据ID查询
			String queryExpr = "id == \"" + memoryId + "\"";

			// 构建查询参数
			QueryParam queryParam = QueryParam.newBuilder()
				.withCollectionName(collectionName)
				.withExpr(queryExpr)
				.withOutFields(Arrays.asList("id", "content", "memory_type", "user_id", "agent_id", "run_id",
						"actor_id", "created_at", "updated_at"))
				.withLimit(1L)
				.build();

			// 执行查询
			R<QueryResults> response = client.query(queryParam);
			if (response.getStatus() != R.Status.Success.getCode()) {
				throw new RuntimeException("Query failed: " + response.getMessage());
			}

			// 解析查询结果 - 正确实现
			QueryResults queryResults = response.getData();
			if (queryResults == null || queryResults.getFieldsDataCount() == 0) {
				logger.debug("Memory not found: {}", memoryId);
				return null;
			}

			// 获取字段数据列表
			List<FieldData> fieldsData = queryResults.getFieldsDataList();

			// 创建字段映射
			Map<String, List<Object>> fieldMap = new HashMap<>();

			for (FieldData fieldData : fieldsData) {
				String fieldName = fieldData.getFieldName();
				List<Object> values = new ArrayList<>();

				if (fieldData.getType() == DataType.VarChar) {
					// 字符串字段
					List<String> stringData = fieldData.getScalars().getStringData().getDataList();
					values.addAll(stringData);
				}
				else if (fieldData.getType() == DataType.Int64) {
					// 长整型字段（时间戳）
					List<Long> longData = fieldData.getScalars().getLongData().getDataList();
					values.addAll(longData);
				}

				fieldMap.put(fieldName, values);
			}

			// 检查是否有结果
			if (fieldMap.get("id") == null || fieldMap.get("id").isEmpty()) {
				logger.debug("Memory not found: {}", memoryId);
				return null;
			}

			// 构建MemoryItem对象
			MemoryItem item = buildMemoryItemFromFieldMap(fieldMap, 0, 1);

			logger.debug("Retrieved memory: {}", memoryId);
			return item;
		}
		catch (Exception e) {
			logger.error("Error getting memory: {}", memoryId, e);
			throw new RuntimeException("Failed to get memory", e);
		}
	}

	@Override
	public void update(MemoryItem item) {
		// 对于Milvus，更新操作等同于删除后重新插入
		try {
			// 先删除旧记录
			delete(item.getId());
			// 再插入新记录
			add(item);
			logger.debug("Updated memory item: {}", item.getId());
		}
		catch (Exception e) {
			logger.error("Error updating memory item: {}", item.getId(), e);
			throw new RuntimeException("Failed to update memory item", e);
		}
	}

	@Override
	public void delete(String memoryId) {
		try {
			// 构建删除表达式
			String deleteExpr = "id == \"" + memoryId + "\"";

			// 构建删除参数
			DeleteParam deleteParam = DeleteParam.newBuilder()
				.withCollectionName(collectionName)
				.withExpr(deleteExpr)
				.build();

			// 执行删除
			R<MutationResult> response = client.delete(deleteParam);
			if (response.getStatus() != R.Status.Success.getCode()) {
				throw new RuntimeException("Delete failed: " + response.getMessage());
			}

			logger.debug("Deleted memory: {}", memoryId);
		}
		catch (Exception e) {
			logger.error("Error deleting memory: {}", memoryId, e);
			throw new RuntimeException("Failed to delete memory", e);
		}
	}

	@Override
	public void deleteAll(Map<String, Object> filters) {
		try {
			// 构建删除表达式
			String deleteExpr = buildSearchExpression(filters);

			// 构建删除参数
			DeleteParam deleteParam = DeleteParam.newBuilder()
				.withCollectionName(collectionName)
				.withExpr(deleteExpr)
				.build();

			// 执行删除
			R<MutationResult> response = client.delete(deleteParam);
			if (response.getStatus() != R.Status.Success.getCode()) {
				throw new RuntimeException("Delete failed: " + response.getMessage());
			}

			logger.debug("Deleted memories with filters: {}", filters);
		}
		catch (Exception e) {
			logger.error("Error deleting memories with filters: {}", filters, e);
			throw new RuntimeException("Failed to delete memories", e);
		}
	}

	@Override
	public void reset() {
		try {
			// 删除整个集合
			client.dropCollection(DropCollectionParam.newBuilder().withCollectionName(collectionName).build());

			// 重新创建集合
			ensureCollectionExists();

			logger.info("Reset vector store collection: {}", collectionName);
		}
		catch (Exception e) {
			logger.error("Error resetting vector store", e);
			throw new RuntimeException("Failed to reset vector store", e);
		}
	}

	/**
	 * 构建搜索表达式
	 */
	private String buildSearchExpression(Map<String, Object> filters) {
		if (filters == null || filters.isEmpty()) {
			return "";
		}

		// 定义有效的集合字段
		Set<String> validFields = new HashSet<>(Arrays.asList("user_id", "agent_id", "run_id", "actor_id",
				"memory_type", "created_at", "updated_at", "content"));

		List<String> conditions = new ArrayList<>();
		for (Map.Entry<String, Object> filter : filters.entrySet()) {
			String key = filter.getKey();
			String value = filter.getValue().toString();

			// 只处理有效的集合字段，忽略其他参数如query、limit、threshold等
			if (!validFields.contains(key)) {
				logger.debug("Skipping invalid field in search expression: {}", key);
				continue;
			}

			// 根据字段类型构建不同的表达式
			switch (key) {
				case "user_id":
				case "agent_id":
				case "run_id":
				case "actor_id":
				case "memory_type":
					conditions.add(key + " == \"" + value + "\"");
					break;
				case "created_at":
				case "updated_at":
					// 时间字段可以支持范围查询，这里简化为精确匹配
					if (value.matches("\\d+")) {
						conditions.add(key + " == " + value);
					}
					break;
				case "content":
					// 内容字段使用模糊匹配
					conditions.add(key + " like \"%" + value + "%\"");
					break;
				default:
					// 其他有效字段使用模糊匹配
					conditions.add(key + " like \"%" + value + "%\"");
					break;
			}
		}

		return String.join(" && ", conditions);
	}

	/**
	 * 计算余弦相似度
	 * @param a 向量a
	 * @param b 向量b
	 * @return 余弦相似度值
	 */
	private Double cosineSimilarity(Double[] a, Double[] b) {
		logger.debug("计算余弦相似度 - 查询向量长度: {}, 存储向量长度: {}", a != null ? a.length : "null", b != null ? b.length : "null");

		if (a == null || b == null) {
			logger.warn("向量为null - 查询向量: {}, 存储向量: {}", a == null, b == null);
			return 0.0;
		}

		if (a.length != b.length) {
			logger.error("向量长度不匹配! 查询向量长度: {}, 存储向量长度: {}", a.length, b.length);
			logger.error("查询向量前5个值: {}", Arrays.toString(Arrays.copyOf(a, Math.min(5, a.length))));
			logger.error("存储向量前5个值: {}", Arrays.toString(Arrays.copyOf(b, Math.min(5, b.length))));
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
			logger.warn("向量范数为0 - normA: {}, normB: {}", normA, normB);
			return 0.0;
		}

		double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
		logger.debug("余弦相似度计算结果: {}", similarity);
		return similarity;
	}

	/**
	 * 从字段映射构建MemoryItem对象
	 * @param fieldMap 字段映射
	 * @param index 数据索引
	 * @param resultCount 结果总数（用于计算向量维度）
	 * @return MemoryItem对象
	 */
	private MemoryItem buildMemoryItemFromFieldMap(Map<String, List<Object>> fieldMap, int index, int resultCount) {
		MemoryItem item = new MemoryItem();

		// 设置ID
		String id = (String) fieldMap.get("id").get(index);
		item.setId(id);

		// 设置内容
		String content = (String) fieldMap.get("content").get(index);
		item.setContent(content);

		// 设置记忆类型
		String memoryType = (String) fieldMap.get("memory_type").get(index);
		item.setMemoryType(memoryType);

		// 设置用户ID
		String userId = (String) fieldMap.get("user_id").get(index);
		item.setUserId(userId);

		// 设置代理ID
		String agentId = (String) fieldMap.get("agent_id").get(index);
		item.setAgentId(agentId);

		// 设置运行ID
		String runId = (String) fieldMap.get("run_id").get(index);
		item.setRunId(runId);

		// 设置演员ID
		String actorId = (String) fieldMap.get("actor_id").get(index);
		item.setActorId(actorId);

		// 设置创建时间
		Long createdAt = (Long) fieldMap.get("created_at").get(index);
		item.setCreatedAt(Instant.ofEpochMilli(createdAt));

		// 设置更新时间
		Long updatedAt = (Long) fieldMap.get("updated_at").get(index);
		item.setUpdatedAt(Instant.ofEpochMilli(updatedAt));

		// 设置向量嵌入
		if (fieldMap.containsKey("vector")) {
			List<Object> vectorData = fieldMap.get("vector");
			if (vectorData != null && !vectorData.isEmpty()) {
				// 计算每个向量的维度
				int vectorDimension = vectorData.size() / resultCount;
				logger.debug("向量数据总数: {}, 结果数量: {}, 每个向量维度: {}", vectorData.size(), resultCount, vectorDimension);

				// 提取当前结果对应的向量数据
				int startIndex = index * vectorDimension;
				int endIndex = startIndex + vectorDimension;

				if (endIndex <= vectorData.size()) {
					List<Object> currentVectorData = vectorData.subList(startIndex, endIndex);
					Double[] embedding = currentVectorData.stream()
						.map(obj -> ((Float) obj).doubleValue())
						.toArray(Double[]::new);
					item.setEmbedding(embedding);
					logger.debug("提取向量成功 - index: {}, 向量长度: {}", index, embedding.length);
				}
				else {
					logger.warn("向量数据索引越界 - index: {}, startIndex: {}, endIndex: {}, vectorDataSize: {}", index,
							startIndex, endIndex, vectorData.size());
				}
			}
		}

		return item;
	}

}
