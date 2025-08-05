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

package com.mem0.examples;

import com.mem0.memory.Memory;
import com.mem0.memory.MemoryItem;
import com.mem0.memory.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Example chatbot that demonstrates memory usage
 */
@Component
public class ChatbotExample {

  private static final Logger logger = LoggerFactory.getLogger(ChatbotExample.class);

  private final Memory memory;
  private final Scanner scanner;

  public ChatbotExample(Memory memory) {
    this.memory = memory;
    this.scanner = new Scanner(System.in);
  }

  /**
   * Run the chatbot example
   */
  public void run() {
    System.out.println("=== Java Mem0 Chatbot Example ===");
    System.out.println("Type 'exit' to quit");
    System.out.println();

    String userId = "demo_user";

    // Add some initial memories
    addInitialMemories(userId);

    while (true) {
      System.out.print("You: ");
      String userInput = scanner.nextLine().trim();

      if ("exit".equalsIgnoreCase(userInput)) {
        System.out.println("Goodbye!");
        break;
      }

      if (userInput.isEmpty()) {
        continue;
      }

      // Generate response with memory context
      String response = generateResponse(userInput, userId);
      System.out.println("Bot: " + response);

      // Store the conversation
      List<Message> conversation = Arrays.asList(
          new Message("user", userInput),
          new Message("assistant", response));
      memory.add(conversation, userId);
    }
  }

  /**
   * Generate response using memory context
   */
  private String generateResponse(String userInput, String userId) {
    // Search for relevant memories
    List<MemoryItem> memories = memory.search(userInput, userId, null, 5, null);

    // Build context from memories
    StringBuilder context = new StringBuilder();
    if (!memories.isEmpty()) {
      context.append("Based on our previous conversations, I remember:\n");
      for (MemoryItem memory : memories) {
        context.append("- ").append(memory.getContent()).append("\n");
      }
      context.append("\n");
    }

    // Generate response using context
    String systemPrompt = "You are a helpful AI assistant. Use the memory context to provide personalized responses.";
    String fullPrompt = context.toString() + "User: " + userInput;

    // For demo purposes, generate a simple response
    return generateSimpleResponse(userInput, context.toString());
  }

  /**
   * Generate a simple response (in real app, this would use LLM)
   */
  private String generateSimpleResponse(String userInput, String context) {
    String lowerInput = userInput.toLowerCase();

    if (lowerInput.contains("hello") || lowerInput.contains("hi")) {
      return "Hello! Nice to see you again. " +
          (context.contains("remember") ? "I remember our previous conversations!" : "");
    }

    if (lowerInput.contains("name")) {
      return "I remember you! " +
          (context.contains("name") ? "Your name is mentioned in my memories." : "I don't have your name stored yet.");
    }

    if (lowerInput.contains("preference") || lowerInput.contains("like")) {
      return "Based on our conversations, I remember your preferences. " +
          (context.contains("preference") ? "I have some information about what you like!"
              : "I'm still learning about your preferences.");
    }

    if (lowerInput.contains("weather")) {
      return "I don't have real-time weather data, but I can remember if you've mentioned weather preferences before. "
          +
          (context.contains("weather") ? "I see you've talked about weather before!" : "");
    }

    return "I understand what you're saying. " +
        (context.contains("remember") ? "I'm using my memory to provide a personalized response."
            : "I'm learning from our conversation.");
  }

  /**
   * Add some initial memories for demonstration
   */
  private void addInitialMemories(String userId) {
    List<List<Message>> initialConversations = Arrays.asList(
        Arrays.asList(
            new Message("user", "Hi, I'm John"),
            new Message("assistant", "Nice to meet you John! I'll remember your name.")),
        Arrays.asList(
            new Message("user", "I like pizza and coffee"),
            new Message("assistant", "Great! I'll remember that you enjoy pizza and coffee.")),
        Arrays.asList(
            new Message("user", "I work as a software developer"),
            new Message("assistant", "That's interesting! I'll remember you're a software developer.")),
        Arrays.asList(
            new Message("user", "I prefer warm weather over cold weather"),
            new Message("assistant", "Noted! You prefer warm weather to cold weather.")));

    for (List<Message> conversation : initialConversations) {
      memory.add(conversation, userId);
    }

    logger.info("Added initial memories for user: {}", userId);
  }

  /**
   * Demonstrate memory search functionality
   */
  public void demonstrateMemorySearch(String userId) {
    System.out.println("\n=== Memory Search Demo ===");

    String[] testQueries = {
        "What's my name?",
        "What do I like?",
        "What's my job?",
        "What's my weather preference?"
    };

    for (String query : testQueries) {
      System.out.println("\nQuery: " + query);
      List<MemoryItem> results = memory.search(query, userId, null, 3, null);

      if (results.isEmpty()) {
        System.out.println("No relevant memories found.");
      } else {
        System.out.println("Found " + results.size() + " relevant memories:");
        for (MemoryItem memory : results) {
          System.out.println("- " + memory.getContent() + " (score: " + memory.getScore() + ")");
        }
      }
    }
  }
}
