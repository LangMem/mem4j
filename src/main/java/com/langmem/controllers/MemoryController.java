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

package com.langmem.controllers;

import com.langmem.memory.Memory;
import com.langmem.memory.MemoryItem;
import com.langmem.memory.MemoryType;
import com.langmem.memory.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API controller for memory operations
 */
@RestController
@RequestMapping("/memory")
public class MemoryController {

  private static final Logger logger = LoggerFactory.getLogger(MemoryController.class);

  private final Memory memory;

  public MemoryController(Memory memory) {
    this.memory = memory;
  }

  /**
   * Add memories from conversation
   */
  @PostMapping("/add")
  public ResponseEntity<Map<String, Object>> addMemories(
      @RequestBody AddMemoryRequest request) {
    try {
      memory.add(request.getMessages(), request.getUserId(),
          request.getMetadata(), request.isInfer(),
          request.getMemoryType() != null ? MemoryType.fromString(request.getMemoryType()) : MemoryType.FACTUAL);

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "message", "Memories added successfully"));
    } catch (Exception e) {
      logger.error("Error adding memories", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }

  /**
   * Search for relevant memories
   */
  @GetMapping("/search")
  public ResponseEntity<Map<String, Object>> searchMemories(
      @RequestParam String query,
      @RequestParam String userId,
      @RequestParam(defaultValue = "10") int limit,
      @RequestParam(required = false) Double threshold,
      @RequestParam Map<String, Object> filters) {
    try {
      List<MemoryItem> results = memory.search(query, userId, filters, limit, threshold);

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "results", results,
          "count", results.size()));
    } catch (Exception e) {
      logger.error("Error searching memories", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }

  /**
   * Get all memories for a user
   */
  @GetMapping("/all")
  public ResponseEntity<Map<String, Object>> getAllMemories(
      @RequestParam String userId,
      @RequestParam(defaultValue = "100") int limit,
      @RequestParam Map<String, Object> filters) {
    try {
      List<MemoryItem> results = memory.getAll(userId, filters, limit);

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "results", results,
          "count", results.size()));
    } catch (Exception e) {
      logger.error("Error getting all memories", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }

  /**
   * Get a specific memory by ID
   */
  @GetMapping("/{memoryId}")
  public ResponseEntity<Map<String, Object>> getMemory(@PathVariable String memoryId) {
    try {
      MemoryItem item = memory.get(memoryId);

      if (item != null) {
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "memory", item));
      } else {
        return ResponseEntity.notFound().build();
      }
    } catch (Exception e) {
      logger.error("Error getting memory: {}", memoryId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }

  /**
   * Update a memory
   */
  @PutMapping("/{memoryId}")
  public ResponseEntity<Map<String, Object>> updateMemory(
      @PathVariable String memoryId,
      @RequestBody Map<String, Object> data) {
    try {
      memory.update(memoryId, data);

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "message", "Memory updated successfully"));
    } catch (Exception e) {
      logger.error("Error updating memory: {}", memoryId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }

  /**
   * Delete a memory
   */
  @DeleteMapping("/{memoryId}")
  public ResponseEntity<Map<String, Object>> deleteMemory(@PathVariable String memoryId) {
    try {
      memory.delete(memoryId);

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "message", "Memory deleted successfully"));
    } catch (Exception e) {
      logger.error("Error deleting memory: {}", memoryId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }

  /**
   * Delete all memories for a user
   */
  @DeleteMapping("/user/{userId}")
  public ResponseEntity<Map<String, Object>> deleteAllMemories(@PathVariable String userId) {
    try {
      memory.deleteAll(userId);

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "message", "All memories deleted successfully"));
    } catch (Exception e) {
      logger.error("Error deleting all memories for user: {}", userId, e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }

  /**
   * Reset all memories (for testing)
   */
  @PostMapping("/reset")
  public ResponseEntity<Map<String, Object>> resetMemories() {
    try {
      memory.reset();

      return ResponseEntity.ok(Map.of(
          "status", "success",
          "message", "All memories reset successfully"));
    } catch (Exception e) {
      logger.error("Error resetting memories", e);
      return ResponseEntity.badRequest().body(Map.of(
          "status", "error",
          "message", e.getMessage()));
    }
  }

  /**
   * Request class for adding memories
   */
  public static class AddMemoryRequest {
    private List<Message> messages;
    private String userId;
    private Map<String, Object> metadata;
    private boolean infer = true;
    private String memoryType;

    // Getters and Setters
    public List<Message> getMessages() {
      return messages;
    }

    public void setMessages(List<Message> messages) {
      this.messages = messages;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public Map<String, Object> getMetadata() {
      return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
      this.metadata = metadata;
    }

    public boolean isInfer() {
      return infer;
    }

    public void setInfer(boolean infer) {
      this.infer = infer;
    }

    public String getMemoryType() {
      return memoryType;
    }

    public void setMemoryType(String memoryType) {
      this.memoryType = memoryType;
    }
  }
}
