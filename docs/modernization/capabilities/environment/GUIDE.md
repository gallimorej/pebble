````markdown
# Environment Discovery Guide - v2

## 🚨 CRITICAL SAFETY CONSTRAINT
**READ-ONLY DISCOVERY - NO SOURCE MODIFICATIONS**

**MANDATORY RULE**: During environment discovery, you must ONLY read and analyze existing files. 
- **NEVER modify source application files**
- **NEVER create files in the source application directories**  
- **ONLY create documentation in `project-artifacts/01-discover/environment/`**
- **Use read-only analysis tools and techniques only**

Any violation of this constraint requires immediate restart of the discovery phase.

## Overview
This guide provides comprehensive instructions for discovering and documenting the complete environment setup of a legacy application. Environment discovery is critical for successful modernization as it ensures all dependencies, configurations, and integrations are understood before transformation begins.

## Discovery Objectives
- Document runtime environments and versions
- Catalog all dependencies and libraries
- Identify configuration files and settings
- Map environment variables and their usage
- Discover external system integrations
- Assess infrastructure requirements
- Understand deployment patterns
- **Assess containerization readiness and requirements**
- **Analyze multi-architecture compatibility** ✨ **NEW**
- **Evaluate Java runtime compatibility and decisions** ✨ **NEW**

## Key Discovery Guides

### Core Environment Discovery
This guide covers the fundamental environment analysis.

### Containerization Discovery
For applications with legacy dependencies that need isolation, see:
**[Legacy Containerization Discovery Guide](CONTAINERIZATION-GUIDE.md)**

This specialized guide provides systematic steps for:
- Isolating problematic legacy dependencies within containers
- Preserving exact current application behavior and runtime
- Planning container strategy for applications with old dependencies  
- Preventing legacy dependency conflicts with modern host systems
- Creating implementation roadmap for legacy application containerization

### **NEW: Multi-Architecture Analysis** ✨
Modern development environments require consideration of different CPU architectures (ARM64 vs AMD64).

### **NEW: Java Runtime Decision Framework** ✨
Legacy Java applications require specific guidance for runtime version selection.

## Discovery Areas

### 1. Runtime Environments
**What to Document:**
- Operating system and version
- Runtime platforms (Java, .NET, Python, etc.) and versions
- Web servers and application servers
- Database systems and versions
- Container environments (if applicable)

**Discovery Techniques:**
- System information commands
- Version checking utilities
- Configuration file analysis
- Server documentation review

### 2. Dependencies and Libraries
**What to Document:**
- Application dependencies (JAR files, NuGet packages, npm modules, etc.)
- System libraries and shared components
- Third-party integrations and SDKs
- Version numbers and compatibility requirements

**Discovery Techniques:**
- Dependency manifest analysis (pom.xml, package.json, requirements.txt, etc.)
- Library scanning tools
- Build script examination
- Runtime dependency analysis

### 3. Configuration Management
**What to Document:**
- Application configuration files and formats
- Environment-specific configurations
- Configuration management patterns
- Security configurations and credentials management

**Discovery Techniques:**
- File system scanning for config files
- Environment variable enumeration
- Configuration management tool analysis
- Security configuration review

### 4. External Integrations
**What to Document:**
- Database connections and schemas
- Web service endpoints and APIs
- Message queues and event systems
- File system dependencies
- Network connections and protocols

**Discovery Techniques:**
- Network traffic analysis
- Configuration file parsing
- Database connection string analysis
- API endpoint discovery
- Integration documentation review

### 5. Infrastructure Requirements
**What to Document:**
- Hardware requirements and sizing
- Network topology and security
- Storage requirements and patterns
- Backup and recovery procedures
- Monitoring and logging systems

**Discovery Techniques:**
- Infrastructure documentation review
- Performance monitoring analysis
- Capacity planning assessment
- Architecture diagram analysis

### 6. **NEW: Multi-Architecture Compatibility Analysis** ✨

#### What to Document:
- **Development Architecture**: ARM64 (Apple Silicon) vs AMD64 (Intel) considerations
- **Production Architecture**: Target deployment architecture requirements  
- **Java Installation Paths**: Architecture-specific runtime paths and compatibility
- **Container Base Images**: Architecture-specific container compatibility
- **Build Tool Compatibility**: Maven/Gradle/npm architecture requirements

#### Discovery Techniques:
```bash
# Architecture detection and documentation
cat > project-artifacts/01-discover/environment/architecture-analysis.md << 'EOF'
# Multi-Architecture Compatibility Analysis

## Current Development Environment
- **Architecture**: $(uname -m)  # arm64, x86_64, etc.
- **OS**: $(uname -s) $(uname -r)
- **Java Path Discovery**:
  - AMD64: $(find /usr/lib/jvm -name "*amd64*" 2>/dev/null || echo "Not found")
  - ARM64: $(find /usr/lib/jvm -name "*arm64*" 2>/dev/null || echo "Not found")

## Architecture-Specific Considerations
- [ ] **Java Runtime Paths**: Vary by architecture (document both)
- [ ] **Container Base Images**: Must support target architectures
- [ ] **Build Tools**: Architecture compatibility verified
- [ ] **Native Dependencies**: Any architecture-specific native libraries

## Multi-Platform Build Requirements
- [ ] **Cross-compilation needed**: [Yes/No]
- [ ] **Architecture-specific configurations**: [Document any differences]
- [ ] **Container platform targets**: [linux/amd64, linux/arm64, etc.]
EOF
```

#### Critical Questions:
- Does the application use native libraries that are architecture-specific?
- Are there hardcoded paths that assume specific Java installation locations?
- Will the application be deployed on different architectures than development?
- Do container base images support all required architectures?

### 7. **NEW: Java Runtime Decision Analysis** ✨

#### What to Document:
- **Current Java Requirements**: Exact Java version currently required
- **Bytecode Compatibility**: Java version that compiled the application
- **Framework Compatibility**: Spring/Hibernate/etc. Java version requirements
- **Migration Options**: Compatibility with newer Java versions
- **Security Implications**: CVE analysis of current vs. newer Java versions

#### Java Runtime Decision Framework:
```bash
cat > project-artifacts/01-discover/environment/java-runtime-analysis.md << 'EOF'
# Java Runtime Decision Analysis

## Current Java Environment
- **Current Runtime**: $(java -version 2>&1 | head -1)
- **JAVA_HOME**: $JAVA_HOME
- **Compilation Target**: [Check JAR/class file versions]
- **Framework Versions**: [Spring, Hibernate, etc. versions and their Java requirements]

## Compatibility Assessment
| Java Version | Bytecode Compatibility | Security Status | Framework Support | Recommendation |
|-------------|----------------------|-----------------|-------------------|----------------|
| Java 6 | [Check if app was compiled for Java 6] | 🔴 EOL 2013 | ⚠️ Limited | **AVOID** |
| Java 8 | ✅ Runs Java 6+ bytecode | 🟡 Extended support | ✅ Good | **RECOMMENDED** |
| Java 11 | ✅ Runs Java 6+ bytecode | ✅ LTS Support | ✅ Modern | Consider for modernization |
| Java 17 | ✅ Runs Java 6+ bytecode | ✅ Latest LTS | ✅ Latest | Future target |

## Decision Criteria
- **Bytecode Compatibility**: ✅ Java 8+ can run Java 6+ bytecode
- **Security Posture**: ✅ Newer Java versions have better security  
- **Framework Support**: [Check if current frameworks work with newer Java]
- **Migration Effort**: [Assess code changes needed for newer Java]

## Recommended Approach
1. **Immediate**: [Java 8 runtime with existing bytecode - minimal risk]
2. **Short-term**: [Java 8 compilation target - moderate effort]
3. **Long-term**: [Java 11/17 - full modernization effort]

## Implementation Strategy
- **Phase 1**: Container with Java 8 runtime + existing Java 6 bytecode
- **Phase 2**: Recompile for Java 8 target (if needed)
- **Phase 3**: Framework and language modernization
EOF
```

#### Java Version Selection Decision Tree:
```
Application requires Java 6?
├── Yes → Can run on Java 8 runtime?
│   ├── Yes → ✅ **Use Java 8 runtime (RECOMMENDED)**
│   └── No → ⚠️ Document compatibility issues
├── No → Java 7?
│   ├── Yes → ✅ **Use Java 8 runtime (RECOMMENDED)**
│   └── No → ✅ **Use current Java version or newer**
```

## Tools and Techniques

### Automated Discovery Tools
- Dependency scanners (Maven dependency plugin, npm audit, etc.)
- System information utilities
- Configuration analysis tools
- Network discovery tools
- **Architecture detection scripts** ✨ **NEW**
- **Java version compatibility tools** ✨ **NEW**

### Manual Discovery Methods
- Documentation review
- Configuration file analysis
- Interview stakeholders
- Code review for environment dependencies

### **NEW: Enhanced Discovery Scripts** ✨

#### Multi-Architecture Environment Discovery:
```bash
#!/bin/bash
# Enhanced environment discovery with architecture awareness

echo "=== ENHANCED ENVIRONMENT DISCOVERY ===" > project-artifacts/01-discover/environment/enhanced-env-discovery.txt

# Architecture detection
echo "--- Architecture Information ---" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
echo "Architecture: $(uname -m)" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
echo "Kernel: $(uname -s)" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
echo "OS Release: $(uname -r)" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt

# Java environment analysis
if command -v java &> /dev/null; then
    echo "--- Java Environment ---" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
    java -version >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt 2>&1
    echo "JAVA_HOME: $JAVA_HOME" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
    
    # Architecture-specific Java paths
    echo "--- Java Installation Paths by Architecture ---" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
    find /usr/lib/jvm -name "*amd64*" 2>/dev/null | head -5 >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
    find /usr/lib/jvm -name "*arm64*" 2>/dev/null | head -5 >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
    find /usr/lib/jvm -name "*default*" 2>/dev/null | head -5 >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
fi

# Container compatibility check
if command -v docker &> /dev/null; then
    echo "--- Container Platform Information ---" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
    docker version --format "Client: {{.Client.Version}}, Server: {{.Server.Version}}" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt 2>/dev/null || echo "Docker not running" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
    echo "Docker platform: $(docker version --format '{{.Server.Os}}/{{.Server.Arch}}' 2>/dev/null)" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
fi

# Build tool versions
echo "--- Build Tools ---" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
command -v mvn &> /dev/null && echo "Maven: $(mvn -version | head -1)" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
command -v gradle &> /dev/null && echo "Gradle: $(gradle -version | grep Gradle)" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
command -v npm &> /dev/null && echo "npm: $(npm -version)" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
command -v node &> /dev/null && echo "Node.js: $(node -version)" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt

echo "Enhanced discovery completed: $(date)" >> project-artifacts/01-discover/environment/enhanced-env-discovery.txt
```

### Documentation Templates

#### Enhanced Environment Discovery Report Template ✨

```markdown
# Environment Discovery Report - Enhanced

**Application**: [Application Name]
**Discovery Date**: [Date]
**Discoverer**: [Name/Team]

## Runtime Environment
- **OS**: [Details]
- **Architecture**: [ARM64/AMD64/x86_64]
- **Runtime Platform**: [Details]
- **Web/App Server**: [Details]
- **Database**: [Details]

## Multi-Architecture Considerations ✨ NEW
- **Development Architecture**: [ARM64/AMD64]
- **Target Deployment Architecture**: [ARM64/AMD64/both]
- **Java Runtime Paths**:
  - AMD64: [/usr/lib/jvm/java-8-openjdk-amd64]
  - ARM64: [/usr/lib/jvm/java-8-openjdk-arm64]
- **Container Base Image Strategy**: [Architecture-specific considerations]

## Java Runtime Analysis ✨ NEW
- **Current Java Version**: [1.6.0_45, 1.8.0_251, etc.]
- **Compiled Bytecode Version**: [Check class file version]
- **Backward Compatibility**: [Can run on newer Java versions?]
- **Security Assessment**: [CVE count for current vs newer Java]
- **Recommended Java Strategy**: [Java 8 runtime recommended for Java 6 apps]

## Dependencies
[Structured list of all dependencies with versions]

## Configurations
[Key configuration files and settings]

## External Integrations
[List of external systems and connection details]

## Infrastructure Requirements
[Hardware, network, and infrastructure needs]

## Containerization Readiness ✨ NEW
- **Legacy Dependency Isolation Needed**: [Yes/No - why]
- **Multi-Architecture Support Required**: [Yes/No]
- **Container Base Image Recommendation**: [Specific image:tag]
- **Environment Variable Strategy**: [How to handle JAVA_HOME, etc.]

## Security and Compliance Analysis ✨ NEW
- **EOL Software Identified**: [Java 6, Tomcat 7, etc.]
- **Known CVEs**: [Security vulnerabilities in current stack]
- **Isolation Benefits**: [How containerization improves security posture]
- **Compliance Impact**: [Any regulatory considerations]

## Implementation Risk Assessment ✨ NEW
- **Technical Complexity**: [Low/Medium/High]
- **Architecture Compatibility Issues**: [Specific issues identified]
- **Estimated Implementation Time**: [Based on dependency complexity]
- **Critical Success Factors**: [What must work for success]

## Recommendations for Next Phases
- **Design Phase Priorities**: [Container architecture, Java runtime strategy]
- **Transform Phase Considerations**: [Implementation approach]
- **Validation Requirements**: [What must be tested]

## Action Items for Implementation
- [ ] Create architecture-aware Dockerfile
- [ ] Plan Java runtime migration strategy  
- [ ] Design environment variable strategy
- [ ] Plan multi-architecture testing approach
```

#### Enhanced Build and Run Instructions Template ✨

Add these sections to the existing build instructions template:

```markdown
## Multi-Architecture Considerations ✨ NEW

### Current Architecture
```bash
echo "Current development architecture: $(uname -m)"
echo "Current platform: $(uname -s)"
```

### Architecture-Specific Setup
```bash
# Architecture-aware Java setup (if needed)
if [ "$(uname -m)" = "arm64" ]; then
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-arm64
elif [ "$(uname -m)" = "x86_64" ]; then  
    export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
fi
```

### Cross-Platform Build Verification
```bash
# Test on multiple architectures (if available)
docker buildx build --platform linux/amd64,linux/arm64 -t myapp:test .
```

## Java Runtime Compatibility ✨ NEW

### Java Version Check
```bash
# Check current Java environment
java -version
javac -version  # If source compilation needed

# Check bytecode compatibility
file target/*.jar | grep -o "Java [0-9]\+\.[0-9]\+"  # Shows compiled Java version
```

### Java Runtime Options
```bash
# Option 1: Use current Java version (if compatible)
java -jar target/myapp.jar

# Option 2: Use specific Java version (if multiple installed)
/usr/lib/jvm/java-8-openjdk/bin/java -jar target/myapp.jar

# Option 3: Container with specific Java version
docker run --rm -v $(pwd):/app openjdk:8 java -jar /app/target/myapp.jar
```

### Java Migration Testing ✨ NEW
```bash
# Test application with different Java versions
echo "Testing with Java 8:"
docker run --rm -v $(pwd):/app openjdk:8 java -jar /app/target/myapp.jar &
sleep 10 && curl http://localhost:8080/health && echo "Java 8: OK" || echo "Java 8: FAIL"

echo "Testing with Java 11:"  
docker run --rm -v $(pwd):/app openjdk:11 java -jar /app/target/myapp.jar &
sleep 10 && curl http://localhost:8080/health && echo "Java 11: OK" || echo "Java 11: FAIL"
```

## Container Environment Issues ✨ NEW

### Common Container Problems and Solutions

#### Issue: Java Runtime Not Found
**Symptoms**: `java: command not found` in container
**Solution**: 
```bash
# Ensure Java is in PATH and JAVA_HOME is set correctly
export JAVA_HOME=/usr/lib/jvm/default-java
export PATH=$PATH:$JAVA_HOME/bin
```

#### Issue: Architecture Mismatch
**Symptoms**: Container builds on AMD64 but fails on ARM64
**Solution**:
```dockerfile
# Architecture-aware setup in Dockerfile
RUN arch=$(dpkg --print-architecture) && \
    if [ "$arch" = "amd64" ]; then \
        ln -s /usr/lib/jvm/java-8-openjdk-amd64 /usr/lib/jvm/default-java; \
    elif [ "$arch" = "arm64" ]; then \
        ln -s /usr/lib/jvm/java-8-openjdk-arm64 /usr/lib/jvm/default-java; \
    fi
```

#### Issue: Environment Variables Not Propagating
**Symptoms**: JAVA_HOME works in shell but not in application startup
**Solution**:
```dockerfile
# Ensure environment variables are set for all contexts
ENV JAVA_HOME=/usr/lib/jvm/default-java
ENV PATH=$PATH:$JAVA_HOME/bin
RUN echo 'export JAVA_HOME=/usr/lib/jvm/default-java' >> /etc/environment
```
```

## Best Practices

### 1. Systematic Approach
- Follow a consistent discovery methodology
- Document everything, even seemingly minor details
- Cross-reference findings across different sources
- Validate discoveries with multiple stakeholders

### 2. Version Tracking
- Record exact versions of all components
- Note version compatibility requirements
- Identify version upgrade paths
- Document any version-specific configurations

### 3. Security Considerations
- Identify security-sensitive configurations
- Document authentication and authorization patterns
- Note encryption and credential management
- Assess security implications of discovered integrations

### 4. Documentation Quality
- Use consistent naming and formatting
- Include discovery date and source
- Provide context for complex configurations
- Create visual diagrams where helpful

### 5. **NEW: Architecture and Runtime Best Practices** ✨
- **Always document target architectures** for deployment environments
- **Test Java runtime compatibility** before committing to specific versions
- **Document architecture-specific paths** and configurations
- **Plan for cross-platform compatibility** from the beginning
- **Validate environment variables** propagate correctly in containerized environments

## Common Pitfalls to Avoid

### Traditional Pitfalls
- Assuming standard configurations without verification
- Missing environment-specific variations
- Overlooking implicit dependencies
- Incomplete integration mapping
- Insufficient version documentation

### **NEW: Enhanced Pitfalls to Avoid** ✨
- **Architecture assumptions**: Don't assume AMD64 everywhere (ARM64 increasingly common)
- **Java version confusion**: Don't mix runtime version with bytecode version
- **Container environment differences**: Environment variables may not propagate the same way
- **Hardcoded paths**: Avoid hardcoding architecture-specific paths like `/usr/lib/jvm/java-8-openjdk-amd64`
- **Single-platform testing**: Always test on multiple architectures if possible

## Success Criteria
✅ All runtime environments documented with versions  
✅ Complete dependency catalog created  
✅ Configuration management patterns understood  
✅ External integrations mapped and documented  
✅ Infrastructure requirements specified  
✅ Security considerations identified  
✅ Documentation validated by stakeholders
✅ **Multi-architecture compatibility assessed** ✨ **NEW**
✅ **Java runtime strategy defined** ✨ **NEW**
✅ **Container environment considerations documented** ✨ **NEW**

## Output Location
Document all findings in `project-artifacts/01-discover/environment/` with organized subdirectories for different discovery areas.

### Enhanced Directory Structure ✨
```
project-artifacts/01-discover/environment/
├── environment-discovery-report.md
├── build-run-instructions.md
├── architecture-analysis.md ✨ NEW
├── java-runtime-analysis.md ✨ NEW
├── enhanced-env-discovery.txt ✨ NEW
└── containerization/
    ├── containerization-assessment.md
    ├── multi-arch-considerations.md ✨ NEW
    └── container-troubleshooting.md ✨ NEW
```

---

**Guide Version**: 2.0 - Enhanced with Multi-Architecture and Java Runtime Analysis  
**Key Enhancements**: Architecture compatibility analysis, Java runtime decision framework, container environment troubleshooting  
**Recommended For**: All containerization projects and legacy Java applications

````