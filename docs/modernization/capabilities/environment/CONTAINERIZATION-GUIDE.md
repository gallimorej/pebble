# Legacy Application Containerization Guide

## 🚨 CRITICAL SAFETY CONSTRAINT
**READ-ONLY DISCOVERY - NO SOURCE MODIFICATIONS**

**MANDATORY RULE**: During containerization discovery, you must ONLY read and analyze existing files. 
- **NEVER modify source application files**
- **NEVER create Docker files in the source application directories**  
- **ONLY create containerization documentation and artifacts in `project-artifacts/01-discover/environment/containerization/`**
- **Use read-only analysis tools and techniques only**

## Overview

This guide provides systematic steps for **containerizing the existing legacy application exactly as it is today**. The primary goal is **dependency isolation** - preventing old, potentially problematic dependencies from contaminating the host system environment.

### Key Objectives:
- **Isolate legacy dependencies** within container boundaries
- **Preserve exact current behavior** of the existing application
- **Enable consistent deployment** across different environments
- **Prevent dependency conflicts** with modern system packages
- **Create a clean baseline** for future modernization efforts
- **Document the legacy runtime environment** for transformation planning

### What This Is NOT:
- ❌ Modernizing the application architecture
- ❌ Upgrading dependencies or frameworks  
- ❌ Changing the application's technology stack
- ❌ Optimizing for container-native patterns

### What This IS:
- ✅ Wrapping the existing application in a container
- ✅ Preserving all current dependencies and versions
- ✅ Maintaining identical runtime behavior
- ✅ Isolating legacy components from the host system

## Legacy Dependency Isolation Process

### 1. Legacy Runtime Environment Capture

#### A. Exact Version Documentation
**Capture the precise versions of everything the legacy app uses:**

```markdown
# Legacy Runtime Environment Snapshot

## Current Production Environment
- **Operating System**: [Exact OS and version currently used]
- **Runtime Platform**: [e.g., Java 8 build 251, Python 2.7.16, .NET Framework 4.5.2]
- **Web/App Server**: [e.g., Tomcat 7.0.47, IIS 7.5, Apache 2.2.15]
- **Database Client Libraries**: [e.g., Oracle JDBC 11.2.0.4, MySQL Connector 5.1.40]
- **System Libraries**: [Any specific system dependencies]

⚠️ **Critical**: Document EXACT versions, not "latest" or "compatible" versions
```

#### B. Legacy Dependency Inventory
**Catalog all dependencies that must be preserved in the container:**

```bash
# Comprehensive dependency capture
echo "=== LEGACY DEPENDENCY INVENTORY ===" > project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt

# Java applications - capture exact JAR versions
if [ -d "lib" ] || find . -name "*.jar" | grep -q .; then
    echo "--- Java Dependencies ---" >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
    find . -name "*.jar" -exec ls -la {} \; >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
    
    # Maven dependencies (if applicable)
    if [ -f "pom.xml" ]; then
        echo "--- Maven Dependencies ---" >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
        cat pom.xml >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
    fi
fi

# Node.js applications - capture exact package versions
if [ -f "package.json" ]; then
    echo "--- Node.js Dependencies ---" >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
    cat package.json >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
    
    # Also capture package-lock.json for exact versions
    if [ -f "package-lock.json" ]; then
        echo "--- Exact Package Versions ---" >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
        cat package-lock.json >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
    fi
fi

# Python applications - capture exact versions
if [ -f "requirements.txt" ]; then
    echo "--- Python Dependencies ---" >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
    cat requirements.txt >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
fi

# .NET applications
if find . -name "*.csproj" | grep -q .; then
    echo "--- .NET Dependencies ---" >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
    find . -name "*.csproj" -exec cat {} \; >> project-artifacts/01-discover/environment/containerization/legacy-deps-inventory.txt
fi
```

### 2. Legacy Build Environment Replication

#### A. Build Tool Version Capture
**Document the exact build environment that works today:**

```bash
# Create legacy build environment snapshot
cat > project-artifacts/01-discover/environment/containerization/legacy-build-env.md << 'EOF'
# Legacy Build Environment Snapshot

## Build Tools and Versions (EXACT VERSIONS REQUIRED)
- **Build Tool**: [e.g., Maven 3.3.9, Gradle 4.10.3, npm 6.14.8]
- **Runtime for Build**: [e.g., OpenJDK 8u251, Python 2.7.16, Node.js 10.24.1]
- **Additional Tools**: [e.g., specific compiler versions, build utilities]

## Build Environment OS Requirements
- **Base OS**: [e.g., Ubuntu 16.04, CentOS 7.9, Windows Server 2016]
- **Required System Packages**: [any OS packages needed for build]

## Build Process (Current Working Commands)
```bash
# Document the EXACT commands that work today
# Example:
# export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
# mvn clean package -Dmaven.test.skip=true
# [Add the actual commands here]
```

## Critical Build Dependencies
- **Build-time Dependencies**: [things only needed during build]
- **Runtime Dependencies**: [things needed when running]
- **System Libraries**: [OS-level dependencies]

## Environment Variables Required for Build
- [List any environment variables that must be set]

## Build Artifacts That Must Work in Container
- **Primary Artifact**: [e.g., myapp.war, target/myapp.jar, dist/]
- **Configuration Files**: [files that must be present at runtime]
- **Assets/Resources**: [static files, templates, etc.]
EOF
```

#### B. Legacy Runtime Requirements
**Capture what the application needs to run as-is:**

```bash
cat > project-artifacts/01-discover/environment/containerization/legacy-runtime-reqs.md << 'EOF'
# Legacy Runtime Requirements (AS-IS)

## Runtime Platform Requirements
- **Primary Runtime**: [e.g., Java 8 JRE build 251, Python 2.7.16, .NET Framework 4.5.2]
- **Application Server**: [e.g., Tomcat 7.0.94, IIS 7.5 - EXACT versions]
- **Web Server**: [if separate from app server]

## System-Level Dependencies
- **OS Type**: [Linux distro/version that app currently runs on]
- **System Libraries**: [any shared libraries required]
- **System Packages**: [OS packages that must be installed]

## Legacy Configuration Patterns
- **Config File Locations**: [where app expects config files]
- **Log File Locations**: [where app writes logs]
- **Data Directory Locations**: [any file system paths app uses]
- **Environment Variables**: [variables app expects to be set]

## Legacy Network/Port Configuration
- **Application Port**: [port app currently uses]
- **Admin/Management Ports**: [any additional ports]
- **Protocol Requirements**: [HTTP, HTTPS, custom protocols]

## Legacy Security/User Requirements
- **User/Group**: [what user app runs as]
- **File Permissions**: [any specific permission requirements]
- **Security Context**: [any special security requirements]
EOF
```

### 3. Container Isolation Strategy

#### A. Base Image Selection for Legacy Apps
**Choose base images that can support old dependencies:**

```bash
cat > project-artifacts/01-discover/environment/containerization/legacy-base-image.md << 'EOF'
# Legacy Application Base Image Strategy

## Legacy Runtime Compatibility Matrix

### For Java Legacy Apps (Java 6-8)
- **Option 1**: `openjdk:8u252-jdk` (for Java 8 apps)
- **Option 2**: `openjdk:7u261-jdk` (for Java 7 apps)  
- **Option 3**: Custom image based on `ubuntu:16.04` + manual Java install
- **Rationale**: Matches production environment, supports legacy frameworks

### For Python Legacy Apps (Python 2.x)
- **Option 1**: `python:2.7.18-slim` (official Python 2.7 - now deprecated)
- **Option 2**: `ubuntu:18.04` + manual Python 2.7 install
- **Option 3**: Custom build with old pip/setuptools versions
- **Rationale**: Many legacy Python apps require Python 2.7 and old packages

### For .NET Legacy Apps (.NET Framework)
- **Option 1**: `mcr.microsoft.com/dotnet/framework/runtime:4.8`
- **Option 2**: `mcr.microsoft.com/dotnet/framework/runtime:4.5.2`
- **Option 3**: Windows Server Core base images
- **Rationale**: .NET Framework apps cannot run on .NET Core/modern runtimes

### For Node.js Legacy Apps (Node 6-10)
- **Option 1**: `node:10.24.1-alpine` (for Node 10 apps)
- **Option 2**: `node:8.17.0-alpine` (for older Node 8 apps)
- **Option 3**: Custom Ubuntu with specific Node version
- **Rationale**: Modern Node versions break legacy JavaScript dependencies

## Recommended Base Image: [Document choice]
- **Image**: [specific tag]
- **Version Lock Rationale**: [why this exact version]
- **Legacy Support**: [what old dependencies it supports]
- **Known Issues**: [any compatibility issues identified]
EOF
```

#### B. Dependency Isolation Plan
**Strategy for containing problematic legacy dependencies:**

```bash
cat > project-artifacts/01-discover/environment/containerization/dependency-isolation.md << 'EOF'
# Legacy Dependency Isolation Strategy

## Problematic Dependencies Identified
- **Old Framework Versions**: [e.g., Spring 2.x, Angular 1.x, jQuery 1.x]
- **Deprecated Libraries**: [libraries no longer maintained]
- **Security Vulnerabilities**: [known CVEs in legacy deps - document but don't fix yet]
- **Conflicting Versions**: [deps that conflict with modern system packages]

## Container Isolation Benefits
✅ **System Protection**: Legacy deps cannot affect host OS
✅ **Version Conflicts Avoided**: No conflicts with modern system packages  
✅ **Security Containment**: Vulnerable legacy deps contained within container
✅ **Reproducible Environment**: Exact legacy environment preserved
✅ **Safe Experimentation**: Can test changes without affecting host

## Isolation Boundaries
- **Network Isolation**: Container runs on isolated network
- **File System Isolation**: Legacy app cannot access host file system
- **Process Isolation**: Legacy processes contained within container
- **Resource Isolation**: Memory/CPU limits prevent resource exhaustion

## Legacy Environment Preservation
- **Package Versions**: Pin exact versions of all packages
- **System Configuration**: Replicate current OS config in container
- **Environment Variables**: Preserve all current env var settings
- **File Permissions**: Maintain current permission structure
EOF
```

### 4. Legacy Application "As-Is" Containerization Plan

#### A. Minimal Change Containerization
**Strategy for containerizing without changing the app:**

```bash
cat > project-artifacts/01-discover/environment/containerization/minimal-change-plan.md << 'EOF'
# Legacy Application "As-Is" Containerization Plan

## Core Principle: ZERO APPLICATION CHANGES
The legacy application should run in the container EXACTLY as it runs today, with no code or configuration changes.

## Containerization Approach: Lift-and-Shift
1. **Replicate Current Environment**: Create container that matches current runtime exactly
2. **Preserve File Paths**: Keep all file paths exactly as they are now
3. **Maintain Port Configuration**: Use same ports application currently uses  
4. **Copy Configuration As-Is**: No config changes, just copy existing files
5. **Preserve Startup Process**: Use same startup commands/scripts

## Container Structure Plan
```
/app/                          # Application root (matches current deployment)
├── [current-app-structure]    # Copy current app structure exactly
├── lib/                       # All JARs/libraries (existing versions)
├── config/                    # Configuration files (current versions)
├── logs/                      # Log directory (if app writes logs)
└── data/                      # Data directory (if app uses local storage)
```

## Build Process: Copy Existing Build
1. **Use Current Build Output**: Take WAR/JAR/executable as built today
2. **Copy Dependencies**: Include all current dependencies with exact versions
3. **Copy Configuration**: Include all config files from current deployment
4. **Copy Assets**: Include all static files, templates, etc.

## NO Modern Container Optimizations (Yet)
❌ Don't optimize image size
❌ Don't use multi-stage builds  
❌ Don't change logging to stdout/stderr
❌ Don't externalize configuration
❌ Don't add health checks
❌ Don't change startup process

✅ Just make it run in a container exactly as it runs now
EOF
```

#### B. Legacy Runtime Replication
**Exact steps to replicate current runtime:**

```bash
cat > project-artifacts/01-discover/environment/containerization/runtime-replication.md << 'EOF'
# Legacy Runtime Replication Steps

## Step 1: Environment Replication
```dockerfile
# Preliminary Dockerfile structure (goes in design phase)
# FROM [legacy-compatible-base-image]
# 
# Install exact runtime versions
# RUN [commands to install exact Java/Python/.NET version]
# 
# Install system dependencies
# RUN [commands to install exact system packages]
# 
# Create application directory structure
# RUN mkdir -p /app/[replicate-current-structure]
# 
# Copy application exactly as built
# COPY [current-build-output] /app/
# 
# Set working directory to match current
# WORKDIR /app
# 
# Use exact startup command currently working
# CMD ["[current-startup-command]"]
```

## Step 2: Configuration Replication
- **Copy All Config Files**: Exactly as they exist today
- **Preserve File Paths**: Use same paths inside container
- **Maintain Environment Variables**: Set same env vars as current deployment
- **Keep Same User/Permissions**: Run as same user/group as current

## Step 3: Startup Replication  
- **Same Startup Script**: Use current startup script/command
- **Same JVM Args**: Use current Java arguments (if Java app)
- **Same Environment**: Set same environment variables
- **Same Working Directory**: Start from same directory

## Step 4: Validation That It's Identical
- [ ] Application starts with same startup time
- [ ] Same port responds to requests
- [ ] Same log output format and location
- [ ] Same behavior for all current test cases
- [ ] Same resource usage patterns
EOF
```

### 5. Legacy Containerization Success Criteria

#### A. Container Readiness Checklist
**Verify the legacy app is ready for containerization:**

```bash
cat > project-artifacts/01-discover/environment/containerization/container-readiness.md << 'EOF'
# Legacy Application Container Readiness Assessment

## Current Application Assessment
- [ ] **Portable Build**: Can build app on different machines with consistent results
- [ ] **Known Dependencies**: All runtime dependencies documented and available
- [ ] **Reproducible Startup**: Startup process documented and can be scripted
- [ ] **External Configs**: Know where all config files are located
- [ ] **Port Configuration**: Application port is configurable or documented
- [ ] **Database Connections**: External database connections work from any machine

## Containerization Readiness Indicators
✅ **READY**: Application uses relative paths or configurable absolute paths
✅ **READY**: All dependencies are in standard package repositories or available as files
✅ **READY**: Application can run without GUI/desktop environment
✅ **READY**: No hardcoded localhost connections to external services
✅ **READY**: Startup process doesn't require interactive input

## Potential Blockers (Need Resolution)
⚠️ **BLOCKER**: Hardcoded absolute file paths that don't exist in container
⚠️ **BLOCKER**: Dependencies on Windows-specific APIs (for Linux containers)
⚠️ **BLOCKER**: Dependencies on specific hostname/machine identity
⚠️ **BLOCKER**: Required GUI components or desktop environment
⚠️ **BLOCKER**: Licensing tied to specific machine/MAC address

## Legacy-Specific Considerations
- **Old Dependency Availability**: Are old package versions still available?
- **System Library Compatibility**: Do old system libraries work on container OS?
- **User/Permission Requirements**: Does app require specific users/groups?
- **Legacy Protocol Support**: Does container OS support old network protocols?
EOF
```

#### B. Implementation Timeline for Legacy App
**Realistic timeline for containerizing legacy application:**

```bash
cat > project-artifacts/01-discover/environment/containerization/legacy-implementation-timeline.md << 'EOF'
# Legacy Application Containerization Timeline

## Phase 1: Proof of Concept (1-3 days)
**Goal**: Get legacy app running in a container locally

### Day 1: Environment Replication
- [ ] Choose base image that supports legacy runtime
- [ ] Install exact runtime versions (Java 8, Python 2.7, etc.)
- [ ] Test that legacy dependencies can be installed

### Day 2: Application Integration  
- [ ] Copy built application into container
- [ ] Copy all configuration files
- [ ] Attempt first container startup

### Day 3: Troubleshooting
- [ ] Fix path issues, permission problems
- [ ] Resolve missing dependencies
- [ ] Get application responding on expected port

## Phase 2: Production-Ready Container (3-5 days)
**Goal**: Container that can replace current deployment

### Days 1-2: Hardening
- [ ] Document exact Dockerfile that works
- [ ] Add proper logging directory mounts
- [ ] Configure data persistence (if needed)
- [ ] Set up proper user/group permissions

### Days 3-5: Validation
- [ ] Test all current application functionality
- [ ] Verify performance matches current deployment  
- [ ] Test restart/recovery behavior
- [ ] Document any differences from current deployment

## Phase 3: Deployment Integration (2-3 days)
**Goal**: Deploy containerized version alongside current version

### Days 1-2: Deployment Setup
- [ ] Create container registry push process
- [ ] Set up container orchestration (if needed)
- [ ] Configure monitoring for containerized version

### Day 3: Side-by-Side Testing
- [ ] Run containerized version parallel to current
- [ ] Compare behavior, performance, logs
- [ ] Validate that containerized version is identical

## Success Criteria for Legacy Container
✅ **Functional Equivalence**: All features work exactly as they do currently
✅ **Performance Equivalence**: Same or better performance than current deployment
✅ **Operational Equivalence**: Same logging, monitoring, management capabilities
✅ **Dependency Isolation**: Legacy dependencies contained and don't affect host
✅ **Reproducible Deployment**: Can deploy to any Docker-capable environment
EOF
```

## Integration with Build-and-Run Discovery

### Adding Legacy Containerization to Build Instructions Template

Add this section to your existing `build-and-run-instructions.md`:

```markdown
## Legacy Application Containerization Assessment

### Current Deployment Isolation Needs
- [ ] **Legacy Dependencies**: Does app use old/deprecated dependencies?
- [ ] **System Conflicts**: Would dependencies conflict with modern system packages?
- [ ] **Version Lock Requirements**: Must specific old versions be preserved?
- [ ] **Security Isolation**: Are there known vulnerabilities in legacy dependencies?
- [ ] **Environment Consistency**: Does app behave differently across environments?

### Legacy Container Strategy
- **Isolation Goal**: [Why containerization is needed - dependency isolation, consistency, etc.]
- **Legacy Runtime**: [Exact runtime versions that must be preserved]
- **Migration Approach**: Lift-and-shift with zero application changes
- **Timeline**: [Estimated time to get legacy app running in container]

### Container Readiness for Legacy App
- [ ] All dependencies documented and available
- [ ] Build process can be replicated in container
- [ ] Application runs without GUI/desktop requirements
- [ ] No hardcoded machine-specific configurations
- [ ] External database connections work from any machine
- [ ] File paths are relative or can be made configurable

### Legacy Containerization Deliverables
1. **Legacy Dependency Inventory**: Complete list of old dependencies with exact versions
2. **Runtime Environment Snapshot**: Documentation of exact current runtime setup
3. **Container Base Image Plan**: Strategy for base image that supports legacy runtime
4. **Minimal Change Plan**: Approach to containerize with zero application changes
5. **Implementation Timeline**: Realistic timeline for legacy container deployment

### Next Steps for Legacy Container
1. Complete legacy dependency analysis in `project-artifacts/01-discover/environment/containerization/`
2. Create proof-of-concept Dockerfile in design phase (no source changes)
3. Validate container runs identical to current deployment
4. Plan production container deployment strategy

### Legacy Containerization Benefits
✅ **Dependency Isolation**: Old dependencies can't affect modern host systems
✅ **Consistent Deployment**: Same container works across all environments  
✅ **Risk Reduction**: Legacy app isolated from host system vulnerabilities
✅ **Modern Infrastructure**: Can deploy legacy app on modern container platforms
✅ **Preservation**: Exact current behavior preserved while enabling future modernization

### Containerization Risks for Legacy Apps
⚠️ **Compatibility Issues**: Old dependencies may not work on modern container OS
⚠️ **Performance Changes**: Container overhead might affect legacy app performance  
⚠️ **Complexity**: Additional layer of complexity in deployment pipeline
⚠️ **Limited Support**: Old runtime versions may have limited container image support
```

## Quality Gates

Before proceeding to design phase, ensure:
- [ ] All containerization analysis documents completed
- [ ] Current deployment pattern fully understood
- [ ] Dependencies and requirements documented
- [ ] Security considerations identified
- [ ] Implementation roadmap created
- [ ] Resource requirements estimated
- [ ] Integration points with other systems documented

## Output Location

All containerization discovery artifacts go in:
`project-artifacts/01-discover/environment/containerization/`

## Next Phase Integration

The containerization discovery findings will inform:
- **Design Phase**: Container architecture and deployment strategy
- **Transform Phase**: Actual Dockerfile and manifest creation  
- **Validate Phase**: Container-specific testing and validation

---

*This containerization analysis ensures systematic preparation for modern container-based deployment as part of the application modernization process.*