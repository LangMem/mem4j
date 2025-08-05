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

package com.mem0.memory;

/**
 * Enumeration of memory types supported by the system
 */
public enum MemoryType {

  /**
   * Factual memory - stores facts and information
   */
  FACTUAL("factual"),

  /**
   * Episodic memory - stores events and experiences
   */
  EPISODIC("episodic"),

  /**
   * Semantic memory - stores concepts and relationships
   */
  SEMANTIC("semantic"),

  /**
   * Procedural memory - stores how-to information and procedures
   */
  PROCEDURAL("procedural"),

  /**
   * Working memory - temporary information for current task
   */
  WORKING("working");

  private final String value;

  MemoryType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static MemoryType fromString(String text) {
    for (MemoryType type : MemoryType.values()) {
      if (type.value.equalsIgnoreCase(text)) {
        return type;
      }
    }
    throw new IllegalArgumentException("No memory type with value " + text + " found");
  }

  @Override
  public String toString() {
    return value;
  }
}
