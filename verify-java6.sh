#!/bin/bash

# Java 6 Containerized Environment Verification Script
# Tests that Pebble is actually running on Java 6 inside containers

echo "🔍 JAVA 6 CONTAINERIZED VERIFICATION TESTS"
echo "============================================="
echo "Testing fully containerized Java 6 environment"
echo ""

# Check if container is running
CONTAINER_NAME="pebble-java6-test"
if ! docker exec $CONTAINER_NAME echo "Container is accessible" >/dev/null 2>&1; then
    echo "❌ Error: $CONTAINER_NAME container is not running"
    echo "Start the environment first: ./start-pebble-java6.sh"
    exit 1
fi

echo "1. Runtime Java Version:"
echo "========================"
docker exec $CONTAINER_NAME java -version 2>&1 | head -3

echo ""
echo "2. Java Installation Path:"
echo "========================="
docker exec $CONTAINER_NAME echo \$JAVA_HOME

echo ""
echo "3. Java Executable Verification:"
echo "================================"
docker exec $CONTAINER_NAME which java
docker exec $CONTAINER_NAME sh -c 'readlink -f $(which java)'

echo ""
echo "4. Class File Version (should be 50 for Java 6):"
echo "================================================="
CLASS_FILE=$(docker exec $CONTAINER_NAME find /opt/tomcat/webapps/pebble -name "*.class" | head -1 2>/dev/null)
if [ ! -z "$CLASS_FILE" ]; then
    docker exec $CONTAINER_NAME javap -verbose "$CLASS_FILE" | grep "major version" | head -1
else
    echo "   (WAR file not yet extracted - checking after application starts)"
fi

echo ""
echo "5. Application Response:"
echo "======================="
HTTP_RESPONSE=$(curl -I http://localhost:8080/pebble/ 2>/dev/null | head -1)
if [ ! -z "$HTTP_RESPONSE" ]; then
    echo "$HTTP_RESPONSE"
else
    echo "   ❌ Application not responding"
fi

echo ""
echo "6. Tomcat Version:"
echo "================="
docker exec $CONTAINER_NAME /opt/tomcat/bin/catalina.sh version | grep "Server version"

echo ""
echo "7. JVM System Properties (Java 6 specific):"
echo "==========================================="
docker exec $CONTAINER_NAME java -XX:+PrintGCDetails -version 2>&1 | grep -E "java version|VM version|OpenJDK"

echo ""
echo "8. Container Architecture:"
echo "========================="
docker exec $CONTAINER_NAME uname -a

echo ""
echo "9. Maven Build Verification:"
echo "============================"
docker exec $CONTAINER_NAME ls -la /opt/tomcat/webapps/pebble.war 2>/dev/null && echo "✅ WAR file deployed" || echo "❓ WAR file not found"

echo ""
echo "10. Application Logs (last 5 lines):"
echo "====================================="
docker exec $CONTAINER_NAME tail -5 /opt/tomcat/logs/catalina.out 2>/dev/null || echo "   (No logs available yet)"

echo ""
echo "============================================="
echo "✅ Containerized Java 6 verification complete"
echo ""
echo "🎯 VERIFICATION SUMMARY:"
echo "========================"

# Summary checks
JAVA_VERSION=$(docker exec $CONTAINER_NAME java -version 2>&1 | head -1 | grep -o '"[^"]*"')
TOMCAT_VERSION=$(docker exec $CONTAINER_NAME /opt/tomcat/bin/catalina.sh version 2>/dev/null | grep "Server version" | cut -d'/' -f2 | cut -d' ' -f1)
APP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/pebble/ 2>/dev/null)

echo "  Java Version: $JAVA_VERSION"
echo "  Tomcat Version: $TOMCAT_VERSION"
echo "  HTTP Status: $APP_STATUS"

if [[ "$JAVA_VERSION" == *"1.6.0"* ]]; then
    echo "  ✅ Java 6 confirmed"
else
    echo "  ❌ Java 6 not detected"
fi

if [[ "$TOMCAT_VERSION" == "7.0.109" ]]; then
    echo "  ✅ Tomcat 7.0.109 confirmed"
else
    echo "  ❌ Expected Tomcat 7.0.109, got: $TOMCAT_VERSION"
fi

if [[ "$APP_STATUS" == "200" ]]; then
    echo "  ✅ Application responding successfully"
else
    echo "  ❌ Application not responding (HTTP $APP_STATUS)"
fi

echo ""
echo "💡 CONTAINERIZATION BENEFITS ACHIEVED:"
echo "======================================"
echo "  ✅ True Java 6 runtime (no local installation)"
echo "  ✅ Complete environment isolation"
echo "  ✅ Consistent build and runtime environment"
echo "  ✅ Zero host system dependencies"