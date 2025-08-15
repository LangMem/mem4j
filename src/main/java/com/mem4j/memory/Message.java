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

package com.mem4j.memory;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a message in a conversation
 */
public class Message {

	@NotBlank
	private String role;

	@NotBlank
	private String content;

	private String name;

	@JsonProperty("function_call")
	private FunctionCall functionCall;

	@JsonProperty("tool_calls")
	private ToolCall[] toolCalls;

	@JsonProperty("tool_call_id")
	private String toolCallId;

	private Map<String, Object> metadata;

	@JsonProperty("created_at")
	private Instant createdAt;

	public Message() {
		this.createdAt = Instant.now();
	}

	public Message(String role, String content) {
		this.role = role;
		this.content = content;
		this.createdAt = Instant.now();
	}

	public Message(String role, String content, String name) {
		this.role = role;
		this.content = content;
		this.name = name;
		this.createdAt = Instant.now();
	}

	// Getters and Setters
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FunctionCall getFunctionCall() {
		return functionCall;
	}

	public void setFunctionCall(FunctionCall functionCall) {
		this.functionCall = functionCall;
	}

	public ToolCall[] getToolCalls() {
		return toolCalls;
	}

	public void setToolCalls(ToolCall[] toolCalls) {
		this.toolCalls = toolCalls;
	}

	public String getToolCallId() {
		return toolCallId;
	}

	public void setToolCallId(String toolCallId) {
		this.toolCallId = toolCallId;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public static class FunctionCall {

		private String name;

		private String arguments;

		public FunctionCall() {
		}

		public FunctionCall(String name, String arguments) {
			this.name = name;
			this.arguments = arguments;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getArguments() {
			return arguments;
		}

		public void setArguments(String arguments) {
			this.arguments = arguments;
		}

	}

	public static class ToolCall {

		private String id;

		private String type;

		private FunctionCall function;

		public ToolCall() {
		}

		public ToolCall(String id, String type, FunctionCall function) {
			this.id = id;
			this.type = type;
			this.function = function;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public FunctionCall getFunction() {
			return function;
		}

		public void setFunction(FunctionCall function) {
			this.function = function;
		}

	}

}
