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

package io.github.mem4j.memory;

/**
 * Enumeration of actions that can be taken on memories
 */
public enum MemoryAction {

	/**
	 * Insert a new memory
	 */
	INSERT,

	/**
	 * Update an existing memory
	 */
	UPDATE,

	/**
	 * Delete an existing memory
	 */
	DELETE,

	/**
	 * Skip - no action needed
	 */
	SKIP

}
