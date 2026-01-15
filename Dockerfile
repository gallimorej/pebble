# Dockerfile for Pebble Application
# Phase 3A: Java 17 LTS + Tomcat 9.0.x
# Spring 5.3.x + Lucene 9.x

FROM ubuntu:20.04

# Avoid interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Update package lists and install basic dependencies
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    ca-certificates \
    unzip \
    tzdata \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

# Install OpenJDK 17 (Java 17 LTS)
RUN apt-get update && apt-get install -y openjdk-17-jdk && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME (auto-detect Java installation)
RUN echo "export JAVA_HOME=\$(dirname \$(dirname \$(readlink -f \$(which java))))" >> /etc/profile
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-arm64
ENV PATH=$PATH:$JAVA_HOME/bin

# Create application user for security
RUN groupadd -r pebble && useradd -r -g pebble -d /app pebble

# Create required directories
RUN mkdir -p /app/data /app/logs /opt/tomcat
RUN chown -R pebble:pebble /app

# Download and install Tomcat 9.0.85 (Java 11 and Spring 5 compatible)
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-9/v9.0.85/bin/apache-tomcat-9.0.85.tar.gz \
    && tar -xzf apache-tomcat-9.0.85.tar.gz -C /opt/tomcat --strip-components=1 \
    && rm apache-tomcat-9.0.85.tar.gz \
    && chown -R pebble:pebble /opt/tomcat \
    && chmod +x /opt/tomcat/bin/*.sh

# Set environment variables
ENV CATALINA_HOME=/opt/tomcat
ENV CATALINA_BASE=/opt/tomcat
ENV CATALINA_TMPDIR=/tmp
ENV JRE_HOME=$JAVA_HOME
ENV CLASSPATH=$CATALINA_HOME/bin/bootstrap.jar:$CATALINA_HOME/bin/tomcat-juli.jar

# Phase 3A: Java 17 memory settings with G1GC optimization
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"
ENV CATALINA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=UTC"

# Pebble-specific configuration
ENV PEBBLE_DATA_DIR=/app/data

# Copy WAR file if it exists (will be created during build process)
# Note: This step will fail if WAR doesn't exist - build application first with Maven
COPY target/*.war /opt/tomcat/webapps/pebble.war

# Switch to application user
USER pebble

# Expose Tomcat port
EXPOSE 8080

# Health check - using the existing /ping servlet
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/pebble/ping || exit 1

# Start Tomcat
CMD ["/opt/tomcat/bin/catalina.sh", "run"]