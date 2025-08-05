package com.mem0.llms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mem0.configs.MemoryConfig;
import com.mem0.memory.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DashScope implementation of LLMService using direct HTTP calls
 */
@Service
public class DashScopeLLMService implements LLMService {

  private static final Logger logger = LoggerFactory.getLogger(DashScopeLLMService.class);
  private static final String DASHSCOPE_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String model;

  public DashScopeLLMService(MemoryConfig config) {
    this.restTemplate = new RestTemplate();
    this.objectMapper = new ObjectMapper();
    this.apiKey = config.getLlm().getApiKey();
    this.model = config.getLlm().getModel();
  }

  @Override
  public String generate(String prompt) {
    try {
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", model);

      Map<String, Object> input = new HashMap<>();
      input.put("messages", List.of(Map.of("role", "user", "content", prompt)));
      requestBody.put("input", input);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + apiKey);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      ResponseEntity<String> response = restTemplate.postForEntity(DASHSCOPE_API_URL, request, String.class);

      JsonNode responseJson = objectMapper.readTree(response.getBody());
      return responseJson.path("output").path("text").asText();
    } catch (Exception e) {
      logger.error("Error generating response from DashScope", e);
      throw new RuntimeException("Failed to generate response", e);
    }
  }

  @Override
  public String generate(List<Message> messages) {
    try {
      List<Map<String, String>> apiMessages = messages.stream()
          .map(msg -> Map.of("role", msg.getRole(), "content", msg.getContent()))
          .collect(Collectors.toList());

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", model);

      Map<String, Object> input = new HashMap<>();
      input.put("messages", apiMessages);
      requestBody.put("input", input);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + apiKey);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      ResponseEntity<String> response = restTemplate.postForEntity(DASHSCOPE_API_URL, request, String.class);

      JsonNode responseJson = objectMapper.readTree(response.getBody());
      return responseJson.path("output").path("text").asText();
    } catch (Exception e) {
      logger.error("Error generating response from messages", e);
      throw new RuntimeException("Failed to generate response", e);
    }
  }

  @Override
  public String generate(String systemPrompt, String userMessage) {
    try {
      List<Map<String, String>> messages = List.of(
          Map.of("role", "system", "content", systemPrompt),
          Map.of("role", "user", "content", userMessage));

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", model);

      Map<String, Object> input = new HashMap<>();
      input.put("messages", messages);
      requestBody.put("input", input);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + apiKey);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      ResponseEntity<String> response = restTemplate.postForEntity(DASHSCOPE_API_URL, request, String.class);

      JsonNode responseJson = objectMapper.readTree(response.getBody());
      return responseJson.path("output").path("text").asText();
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

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("model", model);

      Map<String, Object> input = new HashMap<>();
      input.put("messages", List.of(Map.of("role", "user", "content", structuredPrompt)));
      requestBody.put("input", input);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.set("Authorization", "Bearer " + apiKey);

      HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
      ResponseEntity<String> response = restTemplate.postForEntity(DASHSCOPE_API_URL, request, String.class);

      JsonNode responseJson = objectMapper.readTree(response.getBody());
      return responseJson.path("output").path("text").asText();
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
      logger.warn("DashScope service is not available", e);
      return false;
    }
  }
}