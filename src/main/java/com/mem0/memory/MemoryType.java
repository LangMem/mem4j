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