#!/bin/bash
# Development start script - avoids unnecessary rebuilds

set -e

echo "🚀 Pebble Development Environment"
echo "================================="

# Check if base image exists
if ! docker image inspect pebble-java6:latest >/dev/null 2>&1; then
    echo "📦 Base image not found. Building..."
    docker build -t pebble-java6 -f Dockerfile.java6 .
else
    echo "✅ Base image exists, skipping build"
fi

# Stop any running dev container
docker stop pebble-dev 2>/dev/null || true
docker rm pebble-dev 2>/dev/null || true

# Ensure target directory exists for volume mounting
mkdir -p target/classes

echo "🔧 Starting development container with live reload..."
docker-compose -f docker-compose.dev.yml up -d

echo ""
echo "🎯 Development Environment Ready!"
echo "================================="
echo "📍 Application: http://localhost:8080/pebble/"
echo "🔍 Debug port: 8000 (for IDE debugging)"
echo "📝 Live reload: Edit files in src/ and compile with 'mvn compile'"
echo ""
echo "📋 Development Commands:"
echo "  View logs:     docker-compose -f docker-compose.dev.yml logs -f"
echo "  Restart:       docker-compose -f docker-compose.dev.yml restart"
echo "  Stop:          docker-compose -f docker-compose.dev.yml down"
echo "  Hot compile:   mvn compile (changes reflect immediately)"
echo ""
echo "💡 Tip: Use 'mvn compile' to recompile Java changes without restarting!"