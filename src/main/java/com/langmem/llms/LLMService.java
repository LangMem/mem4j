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

package com.langmem.llms;

import com.langmem.memory.Message;

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
