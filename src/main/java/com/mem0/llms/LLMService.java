package com.mem0.llms;

import com.mem0.memory.Message;

import java.util.List;

/**
 * Interface for LLM operations
 */
public interface LLMService {

  /**
   * Generate text response from a prompt
   */
  String generate(String prompt);

  /**
   * Generate response from a list of messages
   */
  String generate(List<Message> messages);

  /**
   * Generate response with system prompt and user message
   */
  String generate(String systemPrompt, String userMessage);

  /**
   * Generate structured response (JSON)
   */
  String generateStructured(String prompt, String schema);

  /**
   * Check if the service is available
   */
  boolean isAvailable();
}