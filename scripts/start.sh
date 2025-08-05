#!/bin/bash

# Java Mem0 Startup Script

echo "Starting Java Mem0..."

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