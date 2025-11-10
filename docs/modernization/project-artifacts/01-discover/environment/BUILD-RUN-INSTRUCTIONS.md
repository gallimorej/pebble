# Build and Run Instructions

**Application**: Pebble - Java EE Blogging Tool  
**Discovery Date**: November 10, 2025  
**Environment**: Legacy Java 6/Development Environment  
**Build System**: Maven (Primary) + Apache Ant (Legacy)  

## Current System Analysis

### Available Build Tools
- **Apache Maven**: Not currently installed (mvn command not found)
- **Apache Ant**: Not currently installed (ant command not found) 
- **Java Runtime**: Java 10.0.2 (Compatible but newer than target Java 6)

⚠️ **COMPATIBILITY WARNING**: This application targets Java 6, but Java 10+ is installed. May cause compatibility issues.

## Prerequisites

### Required Software and Versions
- **Java Development Kit**: Java 6 (JDK 1.6) - **CRITICAL REQUIREMENT**
- **Build Tool**: Apache Maven 3.8+ OR Apache Ant 1.7+
- **Application Server**: Apache Tomcat 7.0.x
- **Memory**: 512MB+ heap space recommended

### Current Environment Issues
❌ **Missing Maven**: Required for primary build process  
❌ **Missing Ant**: Required for legacy build process  
❌ **Java Version Mismatch**: Java 10 vs required Java 6  
❌ **Missing Tomcat**: Required for deployment and testing  

## Build System Options

### Option 1: Maven Build (Recommended)
This is the primary build system as indicated by the comprehensive `pom.xml`.

#### Maven Prerequisites Installation
```bash
# Install Maven (macOS with Homebrew)
brew install maven

# Verify installation
mvn -version
```

#### Maven Build Process
```bash
# Set JAVA_HOME to Java 6 if available
export JAVA_HOME=/path/to/java6

# Clean previous builds
mvn clean

# Install dependencies and compile
mvn compile

# Run tests
mvn test

# Package WAR file
mvn package

# Install to local repository
mvn install
```

#### Maven Build Verification
```bash
# Check if WAR file was created
ls -la target/pebble-*.war

# Verify dependencies were downloaded
ls -la ~/.m2/repository/
```

### Option 2: Apache Ant Build (Legacy)
The project includes a comprehensive `build.xml` for older build environments.

#### Ant Prerequisites Installation
```bash
# Install Ant (macOS with Homebrew)
brew install ant

# Verify installation
ant -version
```

#### Ant Build Process
```bash
# Clean previous builds
ant clean

# Initialize build environment
ant init

# Compile schema bindings (JAXB)
ant compileschema

# Compile Java sources
ant compile

# Build complete application
ant build

# Run unit tests
ant test

# Build distribution
ant dist
```

#### Ant Build Verification
```bash
# Check build artifacts
ls -la build/pebble-*.jar
ls -la web/WEB-INF/lib/
ls -la dist/pebble-*.zip
```

## Environment Setup

### Java Environment Variables
```bash
# Set Java 6 environment (if available)
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.6.0_65.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Verify Java version
java -version
javac -version
```

### Memory Configuration
```bash
# Set Maven memory settings
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

# Set Ant memory settings
export ANT_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
```

## Application Deployment

### Tomcat 7 Setup
```bash
# Download and install Tomcat 7.0.x
# Extract to /opt/tomcat7 or similar

# Set CATALINA_HOME
export CATALINA_HOME=/opt/tomcat7
export PATH=$CATALINA_HOME/bin:$PATH

# Start Tomcat
catalina.sh start
# or
startup.sh
```

### WAR Deployment
```bash
# Deploy to Tomcat webapps directory
cp target/pebble-2.6.7-SNAPSHOT.war $CATALINA_HOME/webapps/pebble.war

# OR deploy using Tomcat manager
# Access: http://localhost:8080/manager/html
```

### Application Startup Verification
```bash
# Check if Tomcat is running
netstat -an | grep 8080

# Check application logs
tail -f $CATALINA_HOME/logs/catalina.out

# Test application access
curl http://localhost:8080/pebble/
```

## Development Mode

### Hot Reload Development (Not Supported)
⚠️ **Limited Development Support**: This legacy application does not have modern hot-reload capabilities.

**Alternative Development Workflow:**
1. Make code changes
2. Run `mvn compile` or `ant compile` 
3. Copy updated classes to Tomcat deployment
4. Restart Tomcat or redeploy WAR

### Debug Mode Setup
```bash
# Start Tomcat with debug port
export CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000"
catalina.sh jpda start

# Connect IDE debugger to localhost:8000
```

## Testing

### Unit Tests (Maven)
```bash
# Run all unit tests
mvn test

# Run specific test class
mvn test -Dtest=SpecificTestClass

# Generate test reports
mvn surefire-report:report
```

### Unit Tests (Ant)
```bash
# Run all unit tests
ant test

# View test reports
open build/test-report/html/index.html
```

### Integration Tests
```bash
# Web tests (if webtest directory exists)
ant webtest
```

## Common Issues and Troubleshooting

### Issue 1: Java Version Compatibility
**Problem**: Modern Java version incompatible with Java 6 bytecode  
**Solution**: 
```bash
# Install and use Java 6
# Use jenv or similar to manage Java versions
jenv global 1.6
```

### Issue 2: Missing Dependencies
**Problem**: Build fails due to missing JAR dependencies  
**Maven Solution**: 
```bash
# Force dependency download
mvn dependency:resolve
mvn dependency:copy-dependencies
```
**Ant Solution**: 
```bash
# Check lib directory for required JARs
ls -la lib/
# Download missing dependencies manually if needed
```

### Issue 3: Out of Memory Errors
**Problem**: Build fails with OutOfMemoryError  
**Solution**: 
```bash
# Increase heap size
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
export ANT_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
```

### Issue 4: Schema Compilation Issues
**Problem**: JAXB schema compilation fails  
**Solution**: 
```bash
# Ensure JAXB tools are available
# Check etc/pebble.xsd exists
ls -la src/main/resources/pebble.xsd
```

### Issue 5: Web Application Context Issues
**Problem**: Application fails to start due to context configuration  
**Solution**: 
```bash
# Check Tomcat logs
tail -f $CATALINA_HOME/logs/localhost.*.log

# Verify web.xml configuration
cat src/main/webapp/WEB-INF/web.xml
```

## Port Information
- **Tomcat HTTP Port**: 8080 (default)
- **Tomcat HTTPS Port**: 8443 (if configured)
- **Debug Port**: 8000 (when debugging enabled)
- **Application Context**: http://localhost:8080/pebble/

## File Locations

### Build Outputs
- **Maven WAR**: `target/pebble-2.6.7-SNAPSHOT.war`
- **Ant JAR**: `build/pebble-2.4.jar` 
- **Ant Distribution**: `dist/pebble-2.4.zip`

### Configuration Files
- **Application Config**: `src/main/resources/`
- **Web Configuration**: `src/main/webapp/WEB-INF/`
- **Build Properties**: `pebble-build.properties` (Ant)

### Log Locations
- **Application Logs**: `$CATALINA_HOME/logs/`
- **Build Logs**: Console output during build
- **Test Reports**: 
  - Maven: `target/surefire-reports/`
  - Ant: `build/test-report/html/`

## Performance Notes
- **Build Time**: 2-5 minutes (depending on system and network)
- **Startup Time**: 30-60 seconds (Tomcat + application initialization)
- **Memory Usage**: 256MB+ for application, 512MB+ recommended
- **WAR Size**: ~15-25MB (estimated with all dependencies)

## Modern Development Challenges

### Compatibility Issues
1. **Java 6 EOL**: Security and compatibility risks
2. **Tomcat 7 EOL**: No security updates available
3. **Legacy Dependencies**: Many libraries are 10+ years old
4. **Maven Central**: Some old dependencies may no longer be available

### Build System Conflicts
1. **Dual Build Systems**: Maven POM and Ant build.xml may have different versions
2. **Dependency Versions**: Possible mismatches between Maven and Ant dependency lists
3. **Generated Sources**: JAXB schema compilation in both build systems

## Notes for Modernization

### Critical Modernization Requirements
1. **Java Runtime Upgrade**: Minimum Java 8, preferably Java 17+ LTS
2. **Application Server**: Migrate to Tomcat 9+, or Spring Boot embedded server
3. **Build System**: Standardize on Maven, remove Ant build
4. **Dependencies**: Comprehensive security updates required
5. **Testing**: Modern testing framework integration

### Build System Modernization
- Remove duplicate Ant build system
- Update Maven plugins to current versions
- Implement modern testing and quality gates
- Add Docker support for consistent development environments
- Implement CI/CD pipeline compatibility

### Containerization Opportunity
This application is an excellent candidate for containerization:
- Isolate legacy Java 6 requirements
- Standardize build and runtime environments
- Simplify dependency management
- Enable modern deployment patterns

---

**Status**: Build system analysis complete - requires tool installation  
**Next Step**: Install Maven/Ant and attempt build process  
**Recommendation**: Prioritize containerization strategy for safe modernization  