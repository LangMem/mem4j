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

package com.mem0.llms;

import com.mem0.configs.MemoryConfig;
import com.mem0.memory.Message;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OpenAI implementation of LLMService
 */
@Service
public class OpenAILLMService implements LLMService {

  private static final Logger logger = LoggerFactory.getLogger(OpenAILLMService.class);

  private final OpenAiService openAiService;
  private final String model;

  public OpenAILLMService(MemoryConfig config) {
    this.model = config.getLlm().getModel();
    this.openAiService = new OpenAiService(
        config.getLlm().getApiKey(),
        Duration.ofSeconds(60));
  }

  @Override
  public String generate(String prompt) {
    try {
      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .model(model)
          .messages(List.of(new ChatMessage("user", prompt)))
          .maxTokens(1000)
          .temperature(0.7)
          .build();

      var response = openAiService.createChatCompletion(request);
      return response.getChoices().get(0).getMessage().getContent();
    } catch (Exception e) {
      logger.error("Error generating response from OpenAI", e);
      throw new RuntimeException("Failed to generate response", e);
    }
  }

  @Override
  public String generate(List<Message> messages) {
    try {
      List<ChatMessage> chatMessages = messages.stream()
          .map(msg -> new ChatMessage(msg.getRole(), msg.getContent()))
          .collect(Collectors.toList());

      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .model(model)
          .messages(chatMessages)
          .maxTokens(1000)
          .temperature(0.7)
          .build();

      var response = openAiService.createChatCompletion(request);
      return response.getChoices().get(0).getMessage().getContent();
    } catch (Exception e) {
      logger.error("Error generating response from messages", e);
      throw new RuntimeException("Failed to generate response", e);
    }
  }

  @Override
  public String generate(String systemPrompt, String userMessage) {
    try {
      List<ChatMessage> messages = List.of(
          new ChatMessage("system", systemPrompt),
          new ChatMessage("user", userMessage));

      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .model(model)
          .messages(messages)
          .maxTokens(1000)
          .temperature(0.7)
          .build();

      var response = openAiService.createChatCompletion(request);
      return response.getChoices().get(0).getMessage().getContent();
    } catch (Exception e) {
      logger.error("Error generating response with system prompt", e);
      throw new RuntimeException("Failed to generate response", e);
    }
  }

  @Override
  public String generateStructured(String prompt, String schema) {
    try {
      String structuredPrompt = String.format("""
          %s

          Please respond in the following JSON format:
          %s
          """, prompt, schema);

      ChatCompletionRequest request = ChatCompletionRequest.builder()
          .model(model)
          .messages(List.of(new ChatMessage("user", structuredPrompt)))
          .maxTokens(1000)
          .temperature(0.1) // Lower temperature for structured output
          .build();

      var response = openAiService.createChatCompletion(request);
      return response.getChoices().get(0).getMessage().getContent();
    } catch (Exception e) {
      logger.error("Error generating structured response", e);
      throw new RuntimeException("Failed to generate structured response", e);
    }
  }

  @Override
  public boolean isAvailable() {
    try {
      // Simple test to check if service is available
      generate("Hello");
      return true;
    } catch (Exception e) {
      logger.warn("OpenAI service is not available", e);
      return false;
    }
  }
}
