````markdown
# Dependency Analysis Guide

## 🚨 CRITICAL SAFETY CONSTRAINT
**READ-ONLY DISCOVERY - NO SOURCE MODIFICATIONS**

**MANDATORY RULE**: During dependency analysis, you must ONLY read and analyze existing files. 
- **NEVER modify source application files**
- **NEVER create files in the source application directories**  
- **ONLY create documentation in `project-artifacts/01-discover/dependencies/`**
- **Use read-only analysis tools and techniques only**

Any violation of this constraint requires immediate restart of the discovery phase.

## Overview
This guide provides comprehensive instructions for analyzing and documenting all dependencies within a legacy application. Dependency analysis is critical for successful modernization as it identifies:
- Direct and transitive dependencies
- Dependency conflicts and security vulnerabilities
- License compliance issues
- Modernization path and compatibility matrix
- Risk assessment for dependency updates

## Discovery Objectives
- Map complete dependency tree (direct and transitive)
- Identify security vulnerabilities and EOL dependencies
- Assess license compatibility and compliance risks
- Determine modernization paths and update strategies
- Document dependency conflicts and resolution strategies
- Analyze build tool compatibility and requirements
- **Assess containerization impact on dependency management** ✨ **NEW**
- **Evaluate multi-architecture dependency compatibility** ✨ **NEW**

## Dependency Analysis Areas

### 1. Direct Dependency Inventory
**What to Document:**
- Application-declared dependencies (Maven, npm, pip, etc.)
- Version numbers and version ranges
- Scope of dependencies (compile, runtime, test, provided)
- Optional vs. required dependencies
- Custom or internal libraries

**Discovery Techniques:**
```bash
# Maven dependencies
mvn dependency:list > project-artifacts/01-discover/dependencies/maven-dependencies.txt
mvn dependency:tree > project-artifacts/01-discover/dependencies/maven-dependency-tree.txt

# npm dependencies  
npm list --depth=0 > project-artifacts/01-discover/dependencies/npm-dependencies.txt
npm list > project-artifacts/01-discover/dependencies/npm-dependency-tree.txt

# Python dependencies
pip list > project-artifacts/01-discover/dependencies/pip-dependencies.txt
pip show [package-name] > project-artifacts/01-discover/dependencies/pip-package-details.txt
```

### 2. Transitive Dependency Analysis
**What to Document:**
- Complete dependency graph with all transitive dependencies
- Dependency version conflicts and resolutions
- Circular dependencies
- Dependency exclusions and overrides

**Discovery Techniques:**
```bash
# Maven transitive analysis
mvn dependency:tree -Dverbose > project-artifacts/01-discover/dependencies/maven-verbose-tree.txt
mvn dependency:analyze > project-artifacts/01-discover/dependencies/maven-analysis.txt

# Gradle dependencies (if applicable)
gradle dependencies > project-artifacts/01-discover/dependencies/gradle-dependencies.txt

# npm dependency analysis
npm ls --all > project-artifacts/01-discover/dependencies/npm-all-dependencies.txt
```

### 3. Security Vulnerability Assessment
**What to Document:**
- Known CVEs in current dependencies
- Security severity ratings (Critical, High, Medium, Low)
- Available patches and update paths
- Dependencies without security support (EOL)

**Discovery Techniques:**
```bash
# Maven security audit
mvn org.owasp:dependency-check-maven:check

# npm security audit
npm audit > project-artifacts/01-discover/dependencies/npm-security-audit.txt
npm audit --json > project-artifacts/01-discover/dependencies/npm-security-audit.json

# Python security audit (if using pip-audit)
pip-audit > project-artifacts/01-discover/dependencies/pip-security-audit.txt
```

### 4. License Compliance Analysis
**What to Document:**
- License types for all dependencies (Apache, MIT, GPL, etc.)
- License compatibility matrix
- Copyleft vs. permissive license implications
- Commercial license requirements
- License conflicts requiring resolution

**Discovery Techniques:**
```bash
# Maven license analysis (with license plugin)
mvn license:aggregate-third-party-report

# npm license check
npx license-checker > project-artifacts/01-discover/dependencies/npm-licenses.txt

# Manual license documentation
echo "# License Analysis" > project-artifacts/01-discover/dependencies/license-analysis.md
```

### 5. **NEW: Multi-Architecture Dependency Compatibility** ✨
**What to Document:**
- Native dependencies requiring architecture-specific versions
- JNI libraries and their architecture requirements
- Platform-specific dependency variations (ARM64 vs AMD64)
- Container base image compatibility for dependencies

**Discovery Techniques:**
```bash
# Identify native dependencies
find . -name "*.so" -o -name "*.dll" -o -name "*.dylib" 2>/dev/null > project-artifacts/01-discover/dependencies/native-libraries.txt

# Check for architecture-specific JAR files
find . -name "*amd64*" -o -name "*arm64*" -o -name "*x86*" 2>/dev/null > project-artifacts/01-discover/dependencies/arch-specific-files.txt

# Document in analysis
cat > project-artifacts/01-discover/dependencies/architecture-compatibility.md << 'EOF'
# Multi-Architecture Dependency Compatibility

## Native Dependencies Identified
[List of native libraries that may be architecture-specific]

## Architecture-Specific Concerns
- [ ] **JNI Libraries**: [Any Java Native Interface dependencies]
- [ ] **Native Binaries**: [Command-line tools or native executables]
- [ ] **Platform Libraries**: [OS-specific dependency requirements]

## Container Implications
- **Base Image Requirements**: [Architecture-specific base images needed]
- **Multi-Platform Build**: [Dependencies that require different handling per architecture]
EOF
```

### 6. End-of-Life (EOL) and Maintenance Status
**What to Document:**
- Dependencies no longer maintained or supported
- Last update dates for each dependency
- Migration paths to maintained alternatives
- Risk assessment for continuing with EOL dependencies

**Discovery Techniques:**
```bash
# Check last update dates
mvn versions:display-dependency-updates > project-artifacts/01-discover/dependencies/maven-updates.txt

# npm outdated check
npm outdated > project-artifacts/01-discover/dependencies/npm-outdated.txt
```

### 7. Build Tool and Repository Analysis
**What to Document:**
- Build tool versions and compatibility
- Repository sources and accessibility
- Custom repositories and internal dependencies
- Build cache and offline build capabilities

**Discovery Techniques:**
```bash
# Maven repository analysis
mvn help:effective-settings > project-artifacts/01-discover/dependencies/maven-settings.txt
mvn dependency:sources > project-artifacts/01-discover/dependencies/maven-sources.txt

# Document repositories
grep -r "repository" pom.xml > project-artifacts/01-discover/dependencies/maven-repositories.txt 2>/dev/null || true
```

## Dependency Discovery Tools

### Automated Analysis Tools
- **Maven**: `dependency:tree`, `dependency:analyze`, OWASP Dependency Check
- **npm**: `npm audit`, `npm list`, `license-checker`
- **Python**: `pip list`, `pip-audit`, `safety`
- **Gradle**: `dependencies`, `dependencyInsight`
- **Universal**: Snyk, FOSSA, WhiteSource

## Automated Discovery Approach ✨

### Overview
To streamline the dependency analysis process, we provide a comprehensive automated discovery script that intelligently detects your project type and runs appropriate analysis tools. This script eliminates the manual work of running individual commands across different technology stacks.

### Script Purpose and Benefits

#### What the Automated Script Does:
The dependency discovery script performs **comprehensive automated analysis** across multiple technology stacks:

1. **Multi-Platform Detection**: Automatically identifies Maven (Java), npm (Node.js), or Python projects
2. **Comprehensive Analysis**: Runs all relevant dependency commands for each detected platform
3. **Security Assessment**: Automatically performs security vulnerability scanning
4. **Architecture Awareness**: Identifies platform-specific and native dependencies
5. **Organized Output**: Creates structured documentation in the project artifacts directory
6. **Error Resilience**: Continues analysis even if individual tools fail or aren't configured

#### Analysis Coverage by Platform:

**Maven Projects (Java)** - When `pom.xml` is detected:
- `mvn dependency:list` → Lists all direct dependencies with versions
- `mvn dependency:tree` → Complete dependency hierarchy visualization
- `mvn dependency:analyze` → Identifies unused/undeclared dependencies  
- `mvn versions:display-dependency-updates` → Available version updates
- `mvn org.owasp:dependency-check-maven:check` → Security vulnerability scan

**npm Projects (Node.js)** - When `package.json` is detected:
- `npm list --depth=0` → Direct dependencies only
- `npm list` → Complete dependency tree with all transitive dependencies
- `npm audit` → Security vulnerability assessment (text and JSON formats)
- `npm outdated` → Shows outdated packages requiring updates
- `npx license-checker` → Comprehensive license compliance analysis

**Python Projects** - When `requirements.txt`, `setup.py`, or `pyproject.toml` is detected:
- `pip list` → Currently installed packages and versions
- `pip-audit` → Security vulnerability scanner for Python packages
- `safety check` → Additional security scanning with different vulnerability database

**Architecture-Specific Analysis** - For all projects:
- **Native Library Detection**: Searches for `.so`, `.dll`, `.dylib` files
- **Architecture Files**: Identifies AMD64/ARM64/x86 specific components  
- **Platform Documentation**: Records analysis environment and architecture

#### Generated Output Files:
The script creates organized documentation in `project-artifacts/01-discover/dependencies/`:

**Maven Analysis Results:**
- `maven-dependencies.txt` - Direct dependency list
- `maven-dependency-tree.txt` - Complete dependency hierarchy
- `maven-analysis.txt` - Unused/undeclared dependency analysis
- `maven-updates.txt` - Available version updates
- `maven-security-audit.txt` - Security vulnerability report

**npm Analysis Results:**
- `npm-dependencies.txt` - Direct dependencies
- `npm-dependency-tree.txt` - Complete dependency tree
- `npm-security-audit.txt` & `.json` - Security vulnerability reports
- `npm-outdated.txt` - Outdated package report
- `npm-licenses.txt` - License compliance analysis

**Python Analysis Results:**
- `pip-dependencies.txt` - Installed package list
- `pip-security-audit.txt` - Security vulnerability report
- `pip-safety-check.txt` - Additional security analysis

**Architecture Analysis Results:**
- `native-libraries.txt` - Native library inventory
- `arch-specific-files.txt` - Architecture-specific file listing
- `dependency-analysis-summary.txt` - Master summary with timestamps and metadata

#### Error Handling and Resilience:
The script is designed for **production reliability**:
- **Graceful Degradation**: Uses `|| echo "Failed"` to continue if individual commands fail
- **Comprehensive Logging**: Redirects both output and errors (`2>&1`) for complete analysis
- **Missing Tool Handling**: Continues analysis even if optional tools aren't installed
- **Configuration Flexibility**: Works regardless of whether security plugins are configured

#### Usage Instructions:
```bash
# Make the script executable
chmod +x project-artifacts/01-discover/dependencies/dependency-discovery-script.sh

# Run from your project root directory
./project-artifacts/01-discover/dependencies/dependency-discovery-script.sh

# The script will automatically:
# 1. Detect your project type(s)
# 2. Run appropriate analysis tools
# 3. Generate organized documentation
# 4. Provide a summary of results
```

#### Value for Modernization Planning:
This automated approach provides **critical modernization intelligence**:

1. **Security Risk Assessment**: Identifies vulnerabilities that must be addressed
2. **License Compliance Validation**: Prevents legal issues during modernization
3. **Update Planning**: Shows compatibility paths and update requirements
4. **Architecture Planning**: Identifies platform-specific dependencies for containerization
5. **Risk Prioritization**: Helps focus modernization efforts on highest-impact areas

The script eliminates the tedious manual work of dependency discovery while ensuring comprehensive coverage across all technology platforms in your project.

### Enhanced Discovery Scripts ✨

#### Comprehensive Dependency Analysis Script:
```bash
#!/bin/bash
# Comprehensive dependency discovery script

DISCOVERY_DIR="project-artifacts/01-discover/dependencies"
mkdir -p "$DISCOVERY_DIR"

echo "=== COMPREHENSIVE DEPENDENCY ANALYSIS ===" > "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Analysis Date: $(date)" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Analysis Architecture: $(uname -m)" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"

# Maven analysis (if pom.xml exists)
if [ -f "pom.xml" ]; then
    echo "--- Maven Dependency Analysis ---" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
    
    # Basic dependency list
    mvn dependency:list > "$DISCOVERY_DIR/maven-dependencies.txt" 2>&1 || echo "Maven dependency:list failed"
    
    # Dependency tree
    mvn dependency:tree > "$DISCOVERY_DIR/maven-dependency-tree.txt" 2>&1 || echo "Maven dependency:tree failed"
    
    # Dependency analysis
    mvn dependency:analyze > "$DISCOVERY_DIR/maven-analysis.txt" 2>&1 || echo "Maven dependency:analyze failed"
    
    # Check for updates
    mvn versions:display-dependency-updates > "$DISCOVERY_DIR/maven-updates.txt" 2>&1 || echo "Maven versions check failed"
    
    # Security audit (if OWASP plugin configured)
    mvn org.owasp:dependency-check-maven:check > "$DISCOVERY_DIR/maven-security-audit.txt" 2>&1 || echo "OWASP security check not configured"
    
    echo "Maven analysis completed" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

# npm analysis (if package.json exists)
if [ -f "package.json" ]; then
    echo "--- npm Dependency Analysis ---" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
    
    # Direct dependencies
    npm list --depth=0 > "$DISCOVERY_DIR/npm-dependencies.txt" 2>&1 || echo "npm list failed"
    
    # All dependencies
    npm list > "$DISCOVERY_DIR/npm-dependency-tree.txt" 2>&1 || echo "npm list (full) failed"
    
    # Security audit
    npm audit > "$DISCOVERY_DIR/npm-security-audit.txt" 2>&1 || echo "npm audit failed"
    npm audit --json > "$DISCOVERY_DIR/npm-security-audit.json" 2>&1 || echo "npm audit JSON failed"
    
    # Outdated packages
    npm outdated > "$DISCOVERY_DIR/npm-outdated.txt" 2>&1 || echo "npm outdated failed"
    
    # License check
    npx license-checker > "$DISCOVERY_DIR/npm-licenses.txt" 2>&1 || echo "License checker not available"
    
    echo "npm analysis completed" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

# Python analysis (if requirements.txt or setup.py exists)
if [ -f "requirements.txt" ] || [ -f "setup.py" ] || [ -f "pyproject.toml" ]; then
    echo "--- Python Dependency Analysis ---" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
    
    # Current packages
    pip list > "$DISCOVERY_DIR/pip-dependencies.txt" 2>&1 || echo "pip list failed"
    
    # Security audit
    pip-audit > "$DISCOVERY_DIR/pip-security-audit.txt" 2>&1 || echo "pip-audit not available"
    safety check > "$DISCOVERY_DIR/pip-safety-check.txt" 2>&1 || echo "safety not available"
    
    echo "Python analysis completed" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

# Architecture-specific analysis
echo "--- Architecture-Specific Analysis ---" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"

# Find native libraries
find . -name "*.so" -o -name "*.dll" -o -name "*.dylib" 2>/dev/null > "$DISCOVERY_DIR/native-libraries.txt" || echo "No native libraries found"

# Find architecture-specific files
find . -name "*amd64*" -o -name "*arm64*" -o -name "*x86*" 2>/dev/null > "$DISCOVERY_DIR/arch-specific-files.txt" || echo "No architecture-specific files found"

echo "Architecture analysis completed" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"

# Generate summary
echo "" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "--- Analysis Complete ---" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Results saved to: $DISCOVERY_DIR/" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Files generated:" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
ls -la "$DISCOVERY_DIR/" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"

echo "Dependency analysis complete. Results in: $DISCOVERY_DIR/"
```

### Interpreting Automated Analysis Results ✨

#### Understanding the Output Files:

**Master Summary File** (`dependency-analysis-summary.txt`):
- **Purpose**: Central record of what analysis was performed and when
- **Contents**: Analysis timestamp, architecture, and completion status for each platform
- **Usage**: Start here to understand what was analyzed and identify any failed components

**Maven Analysis Interpretation**:
- `maven-dependencies.txt`: Review for unexpected or unknown dependencies
- `maven-dependency-tree.txt`: Look for version conflicts (marked with `(version managed from X)`)
- `maven-analysis.txt`: Check "Used undeclared dependencies" and "Unused declared dependencies" sections
- `maven-updates.txt`: Prioritize security updates over feature updates
- `maven-security-audit.txt`: Focus on HIGH and CRITICAL severity vulnerabilities first

**npm Analysis Interpretation**:
- `npm-dependencies.txt`: Verify all direct dependencies are intentional
- `npm-security-audit.txt`: Check vulnerability count and severity distribution
- `npm-outdated.txt`: Focus on packages marked as "Wanted" vs "Latest"
- `npm-licenses.txt`: Look for GPL, LGPL, or unknown licenses that may require legal review

**Architecture Analysis Interpretation**:
- `native-libraries.txt`: Any files listed here may cause cross-platform compatibility issues
- `arch-specific-files.txt`: Files with architecture in the name need special container handling

#### Common Red Flags to Look For:

🔴 **Critical Security Issues**:
- CVE scores above 7.0 (High/Critical severity)
- Known vulnerabilities in core frameworks (Spring, Express, Django, etc.)
- Dependencies that haven't been updated in 2+ years

🔴 **License Compliance Risks**:
- GPL or LGPL licenses in commercial applications
- Unknown or missing license information
- Incompatible license combinations

🔴 **Modernization Blockers**:
- EOL (End of Life) dependencies with no maintained alternatives
- Native dependencies that don't support target architectures
- Circular dependencies or complex version conflicts

#### Next Steps After Analysis:
1. **Review the master summary** to ensure all expected platforms were analyzed
2. **Prioritize security vulnerabilities** by severity and exploitability
3. **Identify license compliance issues** that need legal review
4. **Document architecture-specific dependencies** for container strategy
5. **Create modernization timeline** based on dependency complexity and risk

## Documentation Templates

### Comprehensive Dependency Analysis Report Template:
```markdown
# Dependency Analysis Report

**Application**: [Application Name]
**Analysis Date**: [Date]
**Analyzer**: [Name/Team]
**Build Tool**: [Maven/npm/pip/Gradle]

## Executive Summary
- **Total Dependencies**: [Direct: X, Transitive: Y]
- **Security Status**: [X Critical, Y High, Z Medium vulnerabilities]
- **License Compliance**: [Compliant/Issues Identified]
- **EOL Dependencies**: [Count and critical ones]
- **Modernization Readiness**: [Ready/Moderate Risk/High Risk]

## Direct Dependencies Analysis
| Dependency | Current Version | Latest Version | License | Security Issues | Status |
|------------|-----------------|----------------|---------|-----------------|---------|
| spring-core | 4.3.21 | 5.3.23 | Apache 2.0 | 2 High | ⚠️ Update Needed |
| hibernate-core | 5.0.12 | 6.1.4 | LGPL 2.1 | 1 Critical | 🔴 Critical Update |

## Security Vulnerability Summary
### Critical Vulnerabilities (Immediate Action Required)
- **CVE-YYYY-XXXX**: [Dependency] - [Description] - [CVSS Score]
  - **Impact**: [Description of security impact]
  - **Resolution**: [Upgrade to version X.Y.Z]

### High Priority Vulnerabilities
[List high-priority vulnerabilities with resolution paths]

## License Compliance Assessment
### License Distribution
- **Apache 2.0**: X dependencies (✅ Compatible)
- **MIT**: Y dependencies (✅ Compatible)
- **GPL**: Z dependencies (⚠️ Review Required)
- **Unknown**: N dependencies (🔴 Investigation Required)

### License Conflicts
[Any identified license compatibility issues]

## End-of-Life Dependencies
| Dependency | Last Update | EOL Date | Recommended Alternative | Migration Effort |
|------------|-------------|----------|------------------------|------------------|
| commons-lang | 2.6 | 2011 | commons-lang3 | Low |
| log4j | 1.2.17 | 2015 | logback/log4j2 | Medium |

## Multi-Architecture Compatibility ✨ NEW
### Native Dependencies
- **JNI Libraries**: [List any Java Native Interface dependencies]
- **Architecture-Specific Files**: [Files that may not work across architectures]
- **Container Implications**: [Impact on containerization strategy]

### Architecture Compatibility Matrix
| Dependency | AMD64 | ARM64 | Notes |
|------------|-------|--------|-------|
| [dependency] | ✅ | ❌ | ARM64 version not available |

## Modernization Recommendations

### Phase 1: Critical Security Updates
- [ ] Update [dependency] to [version] (Critical CVE fix)
- [ ] Replace EOL [dependency] with [alternative]

### Phase 2: Compatibility Updates  
- [ ] Update [dependency] for newer Java version compatibility
- [ ] Resolve license compliance issues

### Phase 3: Modernization Updates
- [ ] Migrate to modern alternatives for legacy dependencies
- [ ] Update to latest stable versions

## Implementation Risk Assessment
### High Risk Dependencies
[Dependencies that pose significant modernization risks]

### Dependency Update Strategy
1. **Security-First**: Address critical vulnerabilities immediately
2. **Compatibility-Second**: Update for platform compatibility
3. **Modernization-Third**: Move to modern alternatives

## Container Strategy Impact ✨ NEW
### Dependency Isolation Strategy
- **Container Benefits**: [How containerization helps with dependency management]
- **Base Image Selection**: [Recommended base images for dependency compatibility]
- **Multi-Stage Build Considerations**: [Dependencies that benefit from multi-stage builds]

## Build Tool Modernization
### Current Build Configuration
- **Build Tool**: [Maven 3.6.3]
- **Repository Sources**: [Central, custom repos]
- **Plugin Versions**: [List critical plugin versions]

### Modernization Recommendations
- [ ] Update build tool to [version]
- [ ] Modernize plugin versions
- [ ] Review repository configurations

## Next Steps for Design Phase
1. **Prioritize Security Updates**: Address critical vulnerabilities first
2. **Plan Compatibility Updates**: Prepare for platform modernization
3. **Design Container Strategy**: Plan dependency isolation approach
4. **Create Update Timeline**: Phased approach for dependency modernization

## Appendices
- **Appendix A**: Complete dependency tree
- **Appendix B**: Detailed security audit results
- **Appendix C**: License compliance details
- **Appendix D**: Architecture compatibility test results ✨ NEW
```

### Enhanced Build Configuration Analysis Template:
```markdown
# Build Configuration Analysis

## Build Tool Configuration
### Maven Configuration (if applicable)
```xml
<!-- Key configuration excerpts -->
<properties>
    <java.version>[discovered version]</java.version>
    <spring.version>[discovered version]</spring.version>
</properties>
```

### Repository Configuration
- **Central Repository**: [Usage and accessibility]
- **Custom Repositories**: [Internal/external repositories]
- **Mirror Configuration**: [Any repository mirrors]

## Dependency Management Strategy
### Version Management
- **Property-based versions**: [Usage of properties for version management]
- **BOM (Bill of Materials)**: [Any BOM usage for version coordination]
- **Version ranges**: [Usage of version ranges and implications]

### Scope Management
- **Compile Dependencies**: [Count and key dependencies]
- **Runtime Dependencies**: [Runtime-only dependencies]
- **Test Dependencies**: [Test-scoped dependencies]
- **Provided Dependencies**: [Container-provided dependencies]

## Build Process Analysis
### Multi-Architecture Build Considerations ✨ NEW
```bash
# Architecture-aware build configuration
<profiles>
    <profile>
        <id>arm64</id>
        <activation>
            <os><arch>aarch64</arch></os>
        </activation>
        <!-- ARM64-specific configuration -->
    </profile>
    <profile>
        <id>amd64</id>
        <activation>
            <os><arch>x86_64</arch></os>
        </activation>
        <!-- AMD64-specific configuration -->
    </profile>
</profiles>
```

### Container Build Impact
- **Base Image Compatibility**: [How dependencies affect base image choice]
- **Layer Optimization**: [Dependency caching strategy]
- **Build Time Optimization**: [Dependency download and cache strategy]
```

## Integration with Existing Framework

### 2. Update the Discovery Phase Guide

I recommend updating your existing discovery phase guide to include dependency analysis:

<function_calls>
<invoke name="replace_string_in_file">
<parameter name="filePath">/Users/jgallimore/Projects/bennu/docs/modernization/phases/01-discover/GUIDE.md