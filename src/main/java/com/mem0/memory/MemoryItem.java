package com.mem0.memory;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a memory item stored in the system
 */
public class MemoryItem {

  private String id;

  private String content;

  @JsonProperty("memory_type")
  private String memoryType;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("agent_id")
  private String agentId;

  @JsonProperty("run_id")
  private String runId;

  @JsonProperty("actor_id")
  private String actorId;

  private Map<String, Object> metadata;

  private Double score;

  @JsonProperty("created_at")
  private Instant createdAt;

  @JsonProperty("updated_at")
  private Instant updatedAt;

  @JsonProperty("embedding")
  private double[] embedding;

  public MemoryItem() {
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  public MemoryItem(String content, String memoryType) {
    this.content = content;
    this.memoryType = memoryType;
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  // Getters and Setters
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getMemoryType() {
    return memoryType;
  }

  public void setMemoryType(String memoryType) {
    this.memoryType = memoryType;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getAgentId() {
    return agentId;
  }

  public void setAgentId(String agentId) {
    this.agentId = agentId;
  }

  public String getRunId() {
    return runId;
  }

  public void setRunId(String runId) {
    this.runId = runId;
  }

  public String getActorId() {
    return actorId;
  }

  public void setActorId(String actorId) {
    this.actorId = actorId;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, Object> metadata) {
    this.metadata = metadata;
  }

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public double[] getEmbedding() {
    return embedding;
  }

  public void setEmbedding(double[] embedding) {
    this.embedding = embedding;
  }

  @Override
  public String toString() {
    return "MemoryItem{" +
        "id='" + id + '\'' +
        ", content='" + content + '\'' +
        ", memoryType='" + memoryType + '\'' +
        ", userId='" + userId + '\'' +
        ", score=" + score +
        ", createdAt=" + createdAt +
        '}';
  }
}