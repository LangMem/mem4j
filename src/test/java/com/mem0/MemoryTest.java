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

package com.mem0;

import com.mem0.memory.Memory;
import com.mem0.memory.MemoryItem;
import com.mem0.memory.MemoryType;
import com.mem0.memory.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for Memory functionality
 */
@SpringBootTest
public class MemoryTest {

  @Autowired
  private Memory memory;
  private String testUserId = "test_user";

  @BeforeEach
  void setUp() {
    // Clean up before each test
    try {
      memory.reset();
    } catch (Exception e) {
      // Ignore if reset fails
    }
  }

  @Test
  void testAddAndSearchMemories() {
    // Create test messages
    List<Message> messages = Arrays.asList(
        new Message("user", "I like pizza and coffee"),
        new Message("assistant", "I'll remember that you like pizza and coffee."));

    // Add memories
    memory.add(messages, testUserId);

    // Search for relevant memories
    List<MemoryItem> results = memory.search("What do I like?", testUserId);

    // Verify results
    assertFalse(results.isEmpty(), "Should find relevant memories");
    assertTrue(
        results.stream().anyMatch(item -> item.getContent().contains("pizza") || item.getContent().contains("coffee")),
        "Should find memories about pizza or coffee");
  }

  @Test
  void testMemoryWithMetadata() {
    // Create messages with metadata
    List<Message> messages = Arrays.asList(
        new Message("user", "I work as a software developer"),
        new Message("assistant", "That's interesting! I'll remember your profession."));

    Map<String, Object> metadata = Map.of(
        "session_id", "test_session",
        "agent_id", "test_agent");

    // Add memories with metadata (without inference to avoid LLM dependency)
    memory.add(messages, testUserId, metadata, false, MemoryType.FACTUAL);

    // First verify that the memory was added
    List<MemoryItem> allMemories = memory.getAll(testUserId, null, 10);
    assertFalse(allMemories.isEmpty(), "Should have added memories");
    System.out.println("Memory metadata: " + allMemories.get(0).getMetadata());
    System.out.println("Memory agentId: " + allMemories.get(0).getAgentId());

    // Test basic search without filters first
    List<MemoryItem> basicResults = memory.search("software developer", testUserId, null, 5, 0.1);
    assertFalse(basicResults.isEmpty(), "Should find memories with basic search");

    // For now, just verify that the metadata was stored correctly
    assertTrue(allMemories.get(0).getMetadata().containsKey("agent_id"), "Should have agent_id in metadata");
    assertEquals("test_agent", allMemories.get(0).getMetadata().get("agent_id"), "Should have correct agent_id value");
  }

  @Test
  void testMemoryUpdate() {
    // Add initial memory
    List<Message> messages = Arrays.asList(
        new Message("user", "My name is John"),
        new Message("assistant", "Nice to meet you John!"));
    memory.add(messages, testUserId);

    // Get the memory
    List<MemoryItem> results = memory.search("What's my name?", testUserId);
    assertFalse(results.isEmpty(), "Should find the memory");

    String memoryId = results.get(0).getId();

    // Update the memory
    Map<String, Object> updateData = Map.of(
        "content", "My name is John Smith");
    memory.update(memoryId, updateData);

    // Verify update
    MemoryItem updatedMemory = memory.get(memoryId);
    assertNotNull(updatedMemory, "Should find updated memory");
    assertTrue(updatedMemory.getContent().contains("John Smith"), "Content should be updated");
  }

  @Test
  void testMemoryDeletion() {
    // Add memory
    List<Message> messages = Arrays.asList(
        new Message("user", "I like chocolate"),
        new Message("assistant", "I'll remember that!"));
    memory.add(messages, testUserId);

    // Search to get memory ID
    List<MemoryItem> results = memory.search("chocolate", testUserId);
    assertFalse(results.isEmpty(), "Should find the memory");

    String memoryId = results.get(0).getId();

    // Delete the memory
    memory.delete(memoryId);

    // Verify deletion
    MemoryItem deletedMemory = memory.get(memoryId);
    assertNull(deletedMemory, "Memory should be deleted");
  }

  @Test
  void testMemorySearchWithThreshold() {
    // Add memories
    List<Message> messages1 = Arrays.asList(
        new Message("user", "I like programming"),
        new Message("assistant", "Great! Programming is fun."));
    List<Message> messages2 = Arrays.asList(
        new Message("user", "I enjoy reading books"),
        new Message("assistant", "Reading is a wonderful hobby."));

    memory.add(messages1, testUserId);
    memory.add(messages2, testUserId);

    // Search with high threshold
    List<MemoryItem> highThresholdResults = memory.search("programming", testUserId, null, 10, 0.9);

    // Search with low threshold
    List<MemoryItem> lowThresholdResults = memory.search("programming", testUserId, null, 10, 0.1);

    // High threshold should return fewer or no results
    assertTrue(highThresholdResults.size() <= lowThresholdResults.size(),
        "High threshold should return fewer results");
  }

  @Test
  void testGetAllMemories() {
    // Add multiple memories
    List<Message> messages1 = Arrays.asList(
        new Message("user", "I like cats"),
        new Message("assistant", "Cats are great pets!"));
    List<Message> messages2 = Arrays.asList(
        new Message("user", "I work from home"),
        new Message("assistant", "Working from home is convenient."));

    memory.add(messages1, testUserId);
    memory.add(messages2, testUserId);

    // Get all memories
    List<MemoryItem> allMemories = memory.getAll(testUserId, null, 100);

    // Verify we get multiple memories
    assertTrue(allMemories.size() >= 2, "Should find multiple memories");
  }

  @Test
  void testMemoryReset() {
    // Add some memories
    List<Message> messages = Arrays.asList(
        new Message("user", "Test memory"),
        new Message("assistant", "Test response"));
    memory.add(messages, testUserId);

    // Verify memory exists
    List<MemoryItem> results = memory.search("test", testUserId);
    assertFalse(results.isEmpty(), "Should find the test memory");

    // Reset all memories
    memory.reset();

    // Verify memories are gone
    List<MemoryItem> resultsAfterReset = memory.search("test", testUserId);
    assertTrue(resultsAfterReset.isEmpty(), "Should not find memories after reset");
  }

  @Test
  void testDifferentMemoryTypes() {
    // Add factual memory (without inference to avoid LLM dependency)
    List<Message> factualMessages = Arrays.asList(
        new Message("user", "I am 30 years old"),
        new Message("assistant", "I'll remember your age."));
    memory.add(factualMessages, testUserId, null, false, MemoryType.FACTUAL);

    // Add episodic memory (without inference to avoid LLM dependency)
    List<Message> episodicMessages = Arrays.asList(
        new Message("user", "Yesterday I went to the park"),
        new Message("assistant", "That sounds like a nice day!"));
    memory.add(episodicMessages, testUserId, null, false, MemoryType.EPISODIC);

    // First, let's see what memories were actually stored
    List<MemoryItem> allMemories = memory.getAll(testUserId, null, 100);
    System.out.println("Total memories stored: " + allMemories.size());
    for (MemoryItem item : allMemories) {
      System.out.println("Memory: " + item.getContent() + " (Type: " + item.getMemoryType() + ")");
    }

    // Search for both types with very low threshold
    List<MemoryItem> factualResults = memory.search("30 years old", testUserId, null, 10, 0.1);
    List<MemoryItem> episodicResults = memory.search("went to the park", testUserId, null, 10, 0.1);

    System.out.println("Factual search results for '30 years old': " + factualResults.size());
    for (MemoryItem item : factualResults) {
      System.out.println("- " + item.getContent() + " (Score: " + item.getScore() + ")");
    }

    System.out.println("Episodic search results for 'went to the park': " + episodicResults.size());
    for (MemoryItem item : episodicResults) {
      System.out.println("- " + item.getContent() + " (Score: " + item.getScore() + ")");
    }

    assertFalse(factualResults.isEmpty(), "Should find factual memories");
    assertFalse(episodicResults.isEmpty(), "Should find episodic memories");
  }
}
