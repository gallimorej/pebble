#!/bin/bash
# Pebble Docker Deployment Script
# Based on containerization assessment

set -e

echo "🚀 Starting Pebble Blog Containerization"
echo "========================================"

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Choose build method based on available tools
echo "📋 Checking build options..."

if command -v mvn &> /dev/null; then
    echo "✅ Maven found - using local build"
    BUILD_METHOD="local"
else
    echo "⚠️  Maven not found - using Docker multi-stage build"
    BUILD_METHOD="docker"
fi

# Build the application
echo ""
echo "🔨 Building Pebble application..."

if [ "$BUILD_METHOD" = "local" ]; then
    echo "Building with local Maven..."
    mvn clean package -DskipTests
    
    if [ ! -f "target/pebble-*.war" ]; then
        echo "❌ WAR file not found after build. Build may have failed."
        exit 1
    fi
    
    echo "✅ Application built successfully"
    
    # Use simple Dockerfile
    echo "🐳 Building Docker image..."
    docker build -t pebble-blog:latest .
    
else
    echo "Building with Docker multi-stage build..."
    # Use multi-stage Dockerfile
    docker build -f Dockerfile.multistage -t pebble-blog:latest .
fi

echo ""
echo "🚀 Starting Pebble with Docker Compose..."

# Update docker-compose.yml to use our built image
cat > docker-compose.yml << EOF
version: '3.8'

services:
  pebble:
    image: pebble-blog:latest
    container_name: pebble-blog
    ports:
      - "8080:8080"
    volumes:
      # Persistent data storage
      - pebble-data:/app/data
      - pebble-logs:/app/logs
    environment:
      # Java runtime configuration
      - JAVA_OPTS=-Xmx1024m -Xms512m -XX:MaxPermSize=256m -Djava.security.egd=file:/dev/./urandom
      - CATALINA_OPTS=-Dfile.encoding=UTF-8 -Duser.timezone=UTC
      # Pebble configuration
      - PEBBLE_DATA_DIR=/app/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/pebble/ping"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    networks:
      - pebble-network

volumes:
  pebble-data:
    driver: local
  pebble-logs:
    driver: local

networks:
  pebble-network:
    driver: bridge
EOF

# Start the application
docker-compose up -d

echo ""
echo "🎉 Pebble Blog is starting up!"
echo ""
echo "📊 Status:"
echo "  - Container: pebble-blog"
echo "  - URL: http://localhost:8080/pebble/"
echo "  - Default login: username / password"
echo ""
echo "📋 Useful commands:"
echo "  - View logs: docker-compose logs -f"
echo "  - Stop: docker-compose down"
echo "  - Restart: docker-compose restart"
echo "  - Shell access: docker-compose exec pebble bash"
echo ""
echo "⏱️  Please wait 60-90 seconds for the application to fully start..."

# Wait a moment and check status
sleep 10
echo ""
echo "🔍 Container status:"
docker-compose ps

echo ""
echo "📊 Checking application health..."
echo "   Waiting for health check to pass..."

# Wait for health check
for i in {1..30}; do
    if docker-compose ps pebble | grep -q "healthy"; then
        echo "✅ Application is healthy and ready!"
        echo "🌐 Access your blog at: http://localhost:8080/pebble/"
        break
    elif docker-compose ps pebble | grep -q "unhealthy"; then
        echo "❌ Application health check failed. Check logs with: docker-compose logs pebble"
        break
    else
        echo "   ⏳ Health check in progress... ($i/30)"
        sleep 5
    fi
done

echo ""
echo "🎯 Next steps:"
echo "  1. Open http://localhost:8080/pebble/ in your browser"
echo "  2. Login with username: 'username', password: 'password'"
echo "  3. Configure your blog settings"
echo "  4. Start blogging!"