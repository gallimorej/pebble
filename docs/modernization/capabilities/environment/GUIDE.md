# Environment Discovery Guide

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

## Tools and Techniques

### Automated Discovery Tools
- Dependency scanners (Maven dependency plugin, npm audit, etc.)
- System information utilities
- Configuration analysis tools
- Network discovery tools

### Manual Discovery Methods
- Documentation review
- Configuration file analysis
- Interview stakeholders
- Code review for environment dependencies

### Documentation Templates
Create structured documentation using these categories:
- **Environment Summary**: High-level overview
- **Runtime Details**: Specific versions and configurations
- **Dependency Catalog**: Complete dependency list with versions
- **Integration Map**: External system connections
- **Infrastructure Specification**: Hardware and network requirements

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

## Output Templates

### Environment Discovery Report
```
# Environment Discovery Report
**Application**: [Application Name]
**Discovery Date**: [Date]
**Discoverer**: [Name/Team]

## Runtime Environment
- OS: [Details]
- Runtime Platform: [Details]
- Web/App Server: [Details]
- Database: [Details]

## Dependencies
[Structured list of all dependencies with versions]

## Configurations
[Key configuration files and settings]

## External Integrations
[List of external systems and connection details]

## Infrastructure Requirements
[Hardware, network, and infrastructure needs]

## Recommendations
[Notes for modernization planning]
```

### Build and Run Instructions Template
For documenting the specific commands needed to build and run the existing application, use this template:

```markdown
# Build and Run Instructions

**Application**: [Your Application Name]  
**Discovery Date**: [Current Date]  
**Environment**: [Development/Staging/Production]  

## Prerequisites

### Required Software
- [List required software and versions]
- [e.g., Java 11+, Node.js 16+, Maven 3.8+, etc.]

### Environment Setup
```bash
# Any environment variables that need to be set
export JAVA_HOME=/path/to/java
export NODE_ENV=development
# Add other required environment variables
```

## Build Instructions

### Clean Build
```bash
# Command to clean previous builds
# Example: mvn clean
```

### Dependencies Installation
```bash
# Commands to install/download dependencies
# Example: npm install
# Example: mvn dependency:resolve
```

### Compile/Build
```bash
# Commands to compile and build the application
# Example: mvn package
# Example: npm run build
```

### Build Verification
```bash
# Commands to verify build was successful
# Example: ls target/*.jar
# Example: npm run test
```

## Run Instructions

### Database Setup (if applicable)
```bash
# Commands to set up database
# Example: docker run -d --name mydb -p 5432:5432 postgres
# Example: mysql -u root -p < schema.sql
```

### Application Startup
```bash
# Commands to start the application
# Example: java -jar target/myapp.jar
# Example: npm start
# Example: mvn spring-boot:run
```

### Verification Steps
```bash
# Commands to verify application is running
# Example: curl http://localhost:8080/health
# Example: netstat -an | grep 8080
```

## Development Mode

### Hot Reload/Watch Mode
```bash
# Commands for development with hot reload
# Example: mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=dev"
# Example: npm run dev
```

### Debug Mode
```bash
# Commands to run in debug mode
# Example: java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar target/myapp.jar
```

## Testing

### Unit Tests
```bash
# Commands to run unit tests
# Example: mvn test
# Example: npm test
```

### Integration Tests
```bash
# Commands to run integration tests
# Example: mvn verify
# Example: npm run test:integration
```

## Common Issues and Troubleshooting

### Issue 1: [Common Problem]
**Problem**: [Description]  
**Solution**: 
```bash
# Commands to resolve
```

### Issue 2: [Another Common Problem]
**Problem**: [Description]  
**Solution**: 
```bash
# Commands to resolve
```

## Port Information
- **Application Port**: [port number]
- **Debug Port**: [port number if applicable]
- **Database Port**: [port number if applicable]
- **Other Services**: [list other ports used]

## Log Locations
- **Application Logs**: [path to log files]
- **Error Logs**: [path to error log files]
- **Debug Logs**: [path to debug log files]

## Performance Notes
- **Startup Time**: [typical startup time]
- **Memory Usage**: [typical memory requirements]
- **Build Time**: [typical build duration]

## Notes for Modernization
- [Any specific notes about build complexity]
- [Dependencies that might be problematic]
- [Build steps that could be simplified]
- [Performance bottlenecks observed]

---

*This document captures the current build and run process as discovered during the environment analysis phase.*
```

## Common Pitfalls to Avoid
- Assuming standard configurations without verification
- Missing environment-specific variations
- Overlooking implicit dependencies
- Incomplete integration mapping
- Insufficient version documentation

## Success Criteria
✅ All runtime environments documented with versions  
✅ Complete dependency catalog created  
✅ Configuration management patterns understood  
✅ External integrations mapped and documented  
✅ Infrastructure requirements specified  
✅ Security considerations identified  
✅ Documentation validated by stakeholders

## Output Location
Document all findings in `project-artifacts/01-discover/environment/` with organized subdirectories for different discovery areas.
