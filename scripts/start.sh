#!/bin/bash
# Copyright 2024-2026 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# Java Mem4j Startup Script

echo "Starting Java Mem4j..."

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Error: Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven 3.6 or higher."
    exit 1
fi

# Set environment variables
export OPENAI_API_KEY=${OPENAI_API_KEY:-"your-openai-api-key"}
export ANTHROPIC_API_KEY=${ANTHROPIC_API_KEY:-""}
export QDRANT_URL=${QDRANT_URL:-"http://localhost:6333"}
export NEO4J_URI=${NEO4J_URI:-"bolt://localhost:7687"}
export NEO4J_USERNAME=${NEO4J_USERNAME:-"neo4j"}
export NEO4J_PASSWORD=${NEO4J_PASSWORD:-"password"}

echo "Environment variables:"
echo "  OPENAI_API_KEY: $OPENAI_API_KEY"
echo "  QDRANT_URL: $QDRANT_URL"
echo "  NEO4J_URI: $NEO4J_URI"

# Build the project
echo "Building project..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

# Start the application
echo "Starting application..."
mvn spring-boot:run
