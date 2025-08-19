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

package io.github.mem4j.example;

import io.github.mem4j.memory.Memory;
import io.github.mem4j.memory.MemoryItem;
import io.github.mem4j.memory.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Example controller showing how to use Memory in a web application
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private Memory memory;

    @PostMapping("/send")
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        String userId = request.getUserId();
        String message = request.getMessage();

        // 搜索相关记忆
        List<MemoryItem> relevantMemories = memory.search(message, userId);

        // 构建简单响应（实际应用中这里会调用LLM）
        String response = generateResponse(message, relevantMemories);

        // 存储对话
        List<Message> conversation = Arrays.asList(
                new Message("user", message),
                new Message("assistant", response));
        memory.add(conversation, userId);

        return new ChatResponse(response, relevantMemories.size());
    }

    @GetMapping("/memories/{userId}")
    public List<MemoryItem> getMemories(@PathVariable("userId") String userId) {
        return memory.getAll(userId, null, 50);
    }

    @DeleteMapping("/memories/{userId}")
    public Map<String, String> clearMemories(@PathVariable("userId") String userId) {
        memory.deleteAll(userId);
        return Map.of("message", "All memories cleared for user: " + userId);
    }

    private String generateResponse(String message, List<MemoryItem> memories) {
        if (memories.isEmpty()) {
            return "Hello! This is my first time talking with you. How can I help?";
        }

        return String.format(
                "I remember we've talked before (%d previous conversations). Regarding '%s', let me help you with that.",
                memories.size(), message);
    }

    // Request/Response DTOs
    public static class ChatRequest {
        private String userId;
        private String message;

        // Getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class ChatResponse {
        private String response;
        private int memoryCount;

        public ChatResponse(String response, int memoryCount) {
            this.response = response;
            this.memoryCount = memoryCount;
        }

        // Getters
        public String getResponse() {
            return response;
        }

        public int getMemoryCount() {
            return memoryCount;
        }
    }
}
