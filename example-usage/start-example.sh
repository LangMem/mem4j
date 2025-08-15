#!/bin/bash

# Mem4j Example Usage Startup Script

echo "🚀 Mem4j Example Usage Startup Script"
echo "====================================="

# Check if in correct directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: This script must be run from the example-usage directory"
    echo "   Please run: cd example-usage && ./start-example.sh"
    exit 1
fi

# Check if parent project is built
if [ ! -f "../target/mem4j-0.1.0.jar" ]; then
    echo "📦 Building parent project first..."
    cd .. && mvn clean install -DskipTests -q
    if [ $? -ne 0 ]; then
        echo "❌ Failed to build parent project"
        exit 1
    fi
    cd example-usage
    echo "✅ Parent project built successfully"
fi

# Check for API key
if [ -z "$DASHSCOPE_API_KEY" ]; then
    echo "⚠️  Warning: DASHSCOPE_API_KEY environment variable is not set"
    echo "   The application will use the default placeholder value"
    echo "   To use real DashScope services, set your API key:"
    echo "   export DASHSCOPE_API_KEY=\"your-api-key\""
    echo ""
    echo "   You can also edit src/main/resources/application.yml directly"
    echo ""
fi

echo "🔧 Starting Mem4j Example Application..."
echo "   Application will be available at: http://localhost:9090"
echo "   Press Ctrl+C to stop"
echo ""

# Start the application
mvn spring-boot:run

echo ""
echo "👋 Application stopped"
