package com.mem0.embeddings;

/**
 * Interface for embedding operations
 */
public interface EmbeddingService {

  /**
   * Generate embedding for a text
   */
  double[] embed(String text);

  /**
   * Generate embeddings for multiple texts
   */
  double[][] embed(String[] texts);

  /**
   * Get the dimension of embeddings
   */
  int getDimension();

  /**
   * Check if the service is available
   */
  boolean isAvailable();
}