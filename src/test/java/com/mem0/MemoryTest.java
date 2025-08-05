package com.mem0;

import com.mem0.memory.Memory;
import com.mem0.memory.MemoryItem;
import com.mem0.memory.MemoryType;
import com.mem0.memory.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for Memory functionality
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
public class MemoryTest {

  @Autowired
  private Memory memory;
  private String testUserId = "test_user";

  @BeforeEach
  void setUp() {
    // Memory will be injected by Spring
    // For testing, we'll use the in-memory implementation
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

    // Add memories with metadata
    memory.add(messages, testUserId, metadata, true, MemoryType.FACTUAL);

    // Search with filters
    Map<String, Object> filters = Map.of("agent_id", "test_agent");
    List<MemoryItem> results = memory.search("What's my job?", testUserId, filters, 5, null);

    // Verify results
    assertFalse(results.isEmpty(), "Should find memories with matching agent_id");
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
    // Add factual memory
    List<Message> factualMessages = Arrays.asList(
        new Message("user", "I am 30 years old"),
        new Message("assistant", "I'll remember your age."));
    memory.add(factualMessages, testUserId, null, true, MemoryType.FACTUAL);

    // Add episodic memory
    List<Message> episodicMessages = Arrays.asList(
        new Message("user", "Yesterday I went to the park"),
        new Message("assistant", "That sounds like a nice day!"));
    memory.add(episodicMessages, testUserId, null, true, MemoryType.EPISODIC);

    // Search for both types
    List<MemoryItem> factualResults = memory.search("age", testUserId);
    List<MemoryItem> episodicResults = memory.search("park", testUserId);

    assertFalse(factualResults.isEmpty(), "Should find factual memories");
    assertFalse(episodicResults.isEmpty(), "Should find episodic memories");
  }
}