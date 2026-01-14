# Dockerfile for Pebble Legacy Application
# Based on containerization assessment - preserves exact legacy dependencies
# Java 6 + Tomcat 7.0.x for dependency isolation

FROM ubuntu:18.04

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

# Install OpenJDK 8 (closest available to Java 6 in Ubuntu 18.04)
# Note: Java 6 is no longer available in modern repositories
RUN apt-get update && apt-get install -y openjdk-8-jdk && rm -rf /var/lib/apt/lists/*

# Set JAVA_HOME (auto-detect Java installation)
RUN echo "export JAVA_HOME=\$(dirname \$(dirname \$(readlink -f \$(which java))))" >> /etc/profile
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-arm64
ENV PATH=$PATH:$JAVA_HOME/bin

# Create application user for security
RUN groupadd -r pebble && useradd -r -g pebble -d /app pebble

# Create required directories
RUN mkdir -p /app/data /app/logs /opt/tomcat
RUN chown -R pebble:pebble /app

# Download and install Tomcat 7.0.109 (latest 7.x version)
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-7/v7.0.109/bin/apache-tomcat-7.0.109.tar.gz \
    && tar -xzf apache-tomcat-7.0.109.tar.gz -C /opt/tomcat --strip-components=1 \
    && rm apache-tomcat-7.0.109.tar.gz \
    && chown -R pebble:pebble /opt/tomcat \
    && chmod +x /opt/tomcat/bin/*.sh

# Set environment variables
ENV CATALINA_HOME=/opt/tomcat
ENV CATALINA_BASE=/opt/tomcat
ENV CATALINA_TMPDIR=/tmp
ENV JRE_HOME=$JAVA_HOME
ENV CLASSPATH=$CATALINA_HOME/bin/bootstrap.jar:$CATALINA_HOME/bin/tomcat-juli.jar

# Java memory settings optimized for container
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:MaxPermSize=256m -Djava.security.egd=file:/dev/./urandom"
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