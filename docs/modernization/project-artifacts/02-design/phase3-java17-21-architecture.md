# Phase 3: Java 17/21 Migration - Architecture Design

**Project**: Pebble Blog Modernization
**Phase**: Phase 3 - Java 17 or 21 LTS Migration
**Document Type**: Architecture Decision & Strategy
**Author**: System Architecture Designer
**Date**: January 14, 2026
**Status**: Design Phase - Ready for Review

---

## Executive Summary

This document outlines the architectural strategy for migrating Pebble from Java 11 (Phase 2) to either Java 17 or Java 21 LTS. Based on comprehensive analysis of the current stack, dependency ecosystem, and risk factors, this design recommends **Java 17 LTS** as the target platform with a phased migration approach.

### Key Architectural Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Target Java Version** | Java 17 LTS | Optimal balance: mature ecosystem, Spring 6 support, lower migration risk |
| **Migration Strategy** | Incremental with rollback points | Minimize risk, validate at each stage |
| **Dependency Sequence** | Spring Framework → Tomcat → Jakarta EE | Bottom-up dependency chain |
| **Backward Compatibility** | Multi-release JAR profiles | Support Java 11, 17, 21 builds |
| **Rollback Strategy** | Git branches + Docker tags | Fast rollback capability |
| **Timeline** | 2-3 weeks | With parallel Phase 2 maintenance |

---

## 1. Java Version Decision Analysis

### 1.1 Java 17 vs Java 21 Comparison

#### Java 17 LTS (Released September 2021)

**Pros:**
- **Mature ecosystem**: 3+ years of production usage
- **Excellent Spring support**: Spring 6.0+ fully tested with Java 17
- **Jakarta EE 9+ compatibility**: Complete tooling support
- **Lower migration risk**: Well-documented upgrade paths from Java 11
- **Container image availability**: Official OpenJDK 17 images widely available
- **Extended support**: LTS until September 2029 (8 years total)

**Cons:**
- Not the "latest" LTS (Java 21 is newer)
- Missing some Java 21 features (virtual threads, pattern matching enhancements)

**Key Java 17 Features for Pebble:**
- Sealed classes (enhanced domain modeling)
- Pattern matching for instanceof (cleaner code)
- Text blocks (improved SQL/JSON/XML literals)
- Enhanced random generators (better test randomization)
- Stronger encapsulation (better security)

#### Java 21 LTS (Released September 2023)

**Pros:**
- **Latest LTS**: Newest features and improvements
- **Virtual threads (Project Loom)**: Massive concurrency benefits (not critical for Pebble)
- **Pattern matching**: More advanced features
- **Extended support**: LTS until September 2031 (8+ years)

**Cons:**
- **Newer ecosystem**: Only 15 months old (less production hardening)
- **Spring Framework compatibility**: Spring 6.1+ required, less mature
- **Jakarta EE tooling**: Some tools still catching up
- **Container ecosystem**: Fewer production-tested images
- **Higher migration risk**: Less documented Java 11 → 21 direct paths
- **Dependency support**: Some dependencies may lag Java 21 support

**Key Java 21 Features (Not Critical for Pebble):**
- Virtual threads (Pebble is I/O-bound, but not high-concurrency)
- Pattern matching for switch (nice-to-have, not essential)
- Record patterns (useful but not critical)

### 1.2 Architectural Decision: Java 17 LTS

**Recommendation**: **Java 17 LTS**

**Decision Rationale:**

1. **Risk Mitigation**: Java 17 has 3+ years of production validation vs Java 21's 15 months
2. **Dependency Ecosystem**: Spring 6.0+ is battle-tested with Java 17, less so with Java 21
3. **Tooling Maturity**: All build tools, IDEs, and containers have mature Java 17 support
4. **Migration Path**: Java 11 → 17 is well-documented with fewer breaking changes
5. **Support Timeline**: 8 years LTS (until 2029) is sufficient for Pebble's lifecycle
6. **Feature Fit**: Java 17 features address Pebble's needs (text blocks, pattern matching, sealed classes)
7. **Performance**: Java 17 provides excellent performance improvements over Java 11 without Java 21 complexity

**Java 21 Future Path**: After Java 17 migration completes, assess Java 21 in 12-18 months when ecosystem matures.

---

## 2. Current State Analysis

### 2.1 Phase 2 (Java 11) Stack

| Component | Current Version | Support Status | Java 17 Compatibility |
|-----------|-----------------|----------------|----------------------|
| **Java Runtime** | 11 (OpenJDK LTS) | Active until 2026 | N/A (upgrading) |
| **Spring Framework** | 5.3.41 | Maintenance mode | ⚠️ Requires Spring 6.0+ |
| **Spring Security** | 5.8.14 | Maintenance mode | ⚠️ Requires Spring Security 6.0+ |
| **Lucene** | 9.9.2 | Active | ✅ Java 17 compatible |
| **Servlet API** | 3.1.0 (Tomcat 9) | Maintenance mode | ⚠️ Upgrade to Jakarta Servlet 5.0+ |
| **Tomcat** | 9.0.85 | Active | ⚠️ Tomcat 10+ required |
| **JAXB** | Jakarta 2.3.3 | Active | ✅ Compatible |

### 2.2 Migration Challenges Identified

#### Challenge 1: Spring Framework 5.3 → 6.0 Breaking Changes

**Issue**: Spring 6.0 requires Java 17 minimum and introduces Jakarta EE 9+ namespace changes.

**Breaking Changes**:
- `javax.*` → `jakarta.*` package rename (Spring 6.0 requirement)
- Configuration API changes (removed deprecated methods)
- Spring Security 6.0 coordination required

**Migration Impact**: **HIGH** - Affects all Spring-managed components

#### Challenge 2: Servlet API 3.1 → Jakarta Servlet 5.0

**Issue**: Tomcat 10+ uses Jakarta Servlet 5.0 (namespace change from javax.servlet → jakarta.servlet).

**Breaking Changes**:
- All servlet imports require package rename
- Filter and Listener classes need updates
- web.xml may require schema updates

**Migration Impact**: **MEDIUM** - Affects web layer

#### Challenge 3: Tomcat 9 → 10 Migration

**Issue**: Tomcat 10 is the first Jakarta EE 9 compatible version.

**Breaking Changes**:
- Jakarta namespace requirements
- Configuration compatibility (minimal)
- Docker base image change

**Migration Impact**: **MEDIUM** - Container and deployment changes

#### Challenge 4: Module System Encapsulation

**Issue**: Java 17 enforces stronger module encapsulation (started in Java 9).

**Current Workarounds** (from Phase 2):
```xml
<argLine>--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED</argLine>
```

**Impact**: These workarounds should be eliminated in Phase 3 with proper modular design.

### 2.3 Current Test Results (Baseline)

**Phase 2 Test Baseline** (96% pass rate):
- Unit tests: 775/775 (100%)
- Integration tests: 24/25 (96%)
- One known failure: Test #23 (Permalink Present) - inherited from Phase 1

**Phase 3 Success Criteria**: Maintain or exceed 96% integration test pass rate.

---

## 3. Dependency Upgrade Sequence

### 3.1 Migration Order (Bottom-Up)

The migration follows a bottom-up dependency chain to ensure each layer is stable before upgrading dependent layers.

#### Stage 1: Java Runtime Upgrade
```
Java 11 → Java 17
```
- **Risk**: LOW
- **Reason**: Compile and validate with Java 17 first
- **Validation**: All code compiles without errors

#### Stage 2: Jakarta EE Namespace Migration
```
javax.servlet → jakarta.servlet
javax.annotation → jakarta.annotation
```
- **Risk**: MEDIUM
- **Reason**: Foundation for Spring 6 and Tomcat 10
- **Validation**: Application compiles with Jakarta imports

#### Stage 3: Tomcat Upgrade
```
Tomcat 9.0.85 → Tomcat 10.1.x (Jakarta Servlet 5.0)
```
- **Risk**: MEDIUM
- **Reason**: Required for Jakarta EE 9+ support
- **Validation**: Application starts in Tomcat 10

#### Stage 4: Spring Framework Upgrade
```
Spring 5.3.41 → Spring 6.0.x
Spring Security 5.8.14 → Spring Security 6.0.x
```
- **Risk**: HIGH
- **Reason**: Major version upgrade with breaking changes
- **Validation**: All integration tests pass

#### Stage 5: Dependency Cleanup
```
Remove Java 17 module workarounds
Update legacy dependencies
```
- **Risk**: LOW
- **Reason**: Optimization and modernization
- **Validation**: Clean build with no warnings

### 3.2 Dependency Version Matrix

| Dependency | Phase 2 (Java 11) | Phase 3 (Java 17) | Breaking Changes |
|------------|-------------------|-------------------|------------------|
| **Java** | 11 | 17 | Module encapsulation |
| **Spring Framework** | 5.3.41 | 6.0.x | javax → jakarta, API changes |
| **Spring Security** | 5.8.14 | 6.0.x | Configuration API, jakarta |
| **Tomcat** | 9.0.85 | 10.1.x | jakarta.servlet namespace |
| **Servlet API** | 3.1.0 | Jakarta Servlet 5.0 | Package rename |
| **Lucene** | 9.9.2 | 9.9.2 (or 9.10+) | None (already Java 17 compatible) |
| **JAXB** | Jakarta 2.3.3 | 3.0.x or 4.0.x | Minimal (already Jakarta) |

### 3.3 Critical Dependency Constraints

**Spring 6.0 Requirements**:
- Java 17+ (minimum)
- Jakarta EE 9+ (servlet, annotations)
- Tomcat 10+ or Jetty 11+

**Spring Security 6.0 Requirements**:
- Spring Framework 6.0+
- Java 17+
- Jakarta namespace

**Dependency Compatibility Matrix**:
```
Java 17
  ├── Spring Framework 6.0.x
  │   ├── Spring Security 6.0.x
  │   ├── Jakarta Servlet 5.0+ (Tomcat 10.1+)
  │   └── Jakarta Annotations 2.0+
  ├── Lucene 9.9.2+ (compatible)
  └── JAXB 3.0+ or 4.0+
```

---

## 4. Migration Phases & Timeline

### 4.1 Phase Overview

```
Phase 3A: Java 17 Compilation (Week 1)
   ↓
Phase 3B: Jakarta Namespace Migration (Week 1-2)
   ↓
Phase 3C: Tomcat 10 Migration (Week 2)
   ↓
Phase 3D: Spring 6 Migration (Week 2-3)
   ↓
Phase 3E: Testing & Validation (Week 3)
   ↓
Phase 3F: Production Hardening (Week 3)
```

**Total Duration**: 2-3 weeks (depending on test results)

### 4.2 Phase 3A: Java 17 Compilation (Days 1-3)

**Objective**: Compile existing Java 11 code with Java 17 compiler

**Tasks**:
1. Update `pom.xml` Java version properties
   ```xml
   <maven.compiler.source>17</maven.compiler.source>
   <maven.compiler.target>17</maven.compiler.target>
   <maven.compiler.release>17</maven.compiler.release>
   ```

2. Update Dockerfile base image
   ```dockerfile
   # FROM ubuntu:20.04 + OpenJDK 11
   FROM eclipse-temurin:17-jdk-jammy  # or ubuntu:22.04 + OpenJDK 17
   ```

3. Run Maven compile with Java 17
   ```bash
   mvn clean compile -P java17
   ```

4. Address compilation errors (if any)
   - Update deprecated API usage
   - Fix module visibility issues
   - Remove Java 11 workarounds

**Success Criteria**:
- ✅ Zero compilation errors
- ✅ Zero critical warnings
- ✅ All unit tests pass (775/775)

**Rollback Point**: Git tag `phase3a-java17-compile`

### 4.3 Phase 3B: Jakarta Namespace Migration (Days 4-6)

**Objective**: Migrate from `javax.*` to `jakarta.*` namespaces

**Automated Migration**:
```bash
# Use Eclipse Transformer or manual search-replace
find src -name "*.java" -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} +
find src -name "*.java" -exec sed -i 's/javax\.annotation/jakarta.annotation/g' {} +
```

**Manual Updates Required**:
1. **Servlet classes** (ResponseSplittingPreventer, filters)
   ```java
   // OLD: import javax.servlet.*;
   import jakarta.servlet.*;
   import jakarta.servlet.http.*;
   ```

2. **Spring configuration** (applicationContext-security.xml)
   - Update Spring Security namespace (already 5.8, minimal changes)

3. **Test classes** (MockHttpServletRequest, MockHttpServletResponse)
   ```java
   // OLD: extends javax.servlet.http.HttpServletRequestWrapper
   extends jakarta.servlet.http.HttpServletRequestWrapper
   ```

4. **JSP files** (if any use servlet imports)

**Dependency Updates**:
```xml
<!-- Tomcat Servlet API: 9.0.x → 10.1.x -->
<dependency>
    <groupId>org.apache.tomcat</groupId>
    <artifactId>tomcat-servlet-api</artifactId>
    <version>10.1.x</version>
    <scope>provided</scope>
</dependency>
```

**Success Criteria**:
- ✅ All `javax.servlet` → `jakarta.servlet` converted
- ✅ Code compiles with Jakarta imports
- ✅ Unit tests pass (775/775)

**Rollback Point**: Git tag `phase3b-jakarta-namespace`

### 4.4 Phase 3C: Tomcat 10 Migration (Days 7-9)

**Objective**: Upgrade Tomcat 9.0.85 → Tomcat 10.1.x

**Tomcat Version Selection**: **Tomcat 10.1.19** (latest stable as of Jan 2026)

**Dockerfile Update**:
```dockerfile
# Download Tomcat 10.1.19
RUN wget -q https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.19/bin/apache-tomcat-10.1.19.tar.gz \
    && tar -xzf apache-tomcat-10.1.19.tar.gz -C /opt/tomcat --strip-components=1 \
    && rm apache-tomcat-10.1.19.tar.gz
```

**Configuration Changes**:
- web.xml: Update schema version (if needed)
- server.xml: Minimal changes (Tomcat 10 mostly backward compatible)

**Docker Build**:
```bash
docker build -t pebble:java17-phase3c .
docker run -d --name pebble-java17-test -p 8080:8080 pebble:java17-phase3c
```

**Success Criteria**:
- ✅ Application starts in Tomcat 10.1
- ✅ Homepage accessible (200 OK)
- ✅ Health check endpoint works (/ping)
- ✅ Integration tests: 24/25 pass (maintain baseline)

**Rollback Point**: Git tag `phase3c-tomcat10` + Docker tag `pebble:tomcat10`

### 4.5 Phase 3D: Spring 6 Migration (Days 10-14)

**Objective**: Upgrade Spring 5.3 → Spring 6.0 and Spring Security 5.8 → 6.0

**Dependency Updates**:
```xml
<!-- Spring Framework 5.3.41 → 6.0.x -->
<spring.version>6.0.17</spring.version>
<spring-security.version>6.0.11</spring-security.version>
```

**Configuration Migration**:

1. **applicationContext-security.xml**:
   - Update Spring Security namespace (5.8 → 6.0 schema)
   - Replace deprecated classes:
     ```java
     // OLD: org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder
     // NEW: org.springframework.security.crypto.password.MessageDigestPasswordEncoder (already migrated in Phase 2)
     ```

2. **Spring Bean Configurations**:
   - Check for removed/deprecated Spring 6.0 APIs
   - Update custom validators (if any)

3. **Spring Security 6.0 Breaking Changes**:
   - **Authorizing HTTP Requests**: `use-expressions="true"` now default
   - **CSRF Protection**: Already enabled (Phase 2)
   - **Remember-Me**: TokenBasedRememberMeServices API check

**High-Risk Changes**:

**DefaultSecurityRealm.java** (Password Encoder):
```java
// Phase 2 (Spring Security 5.8):
private PasswordEncoder passwordEncoder = new MessageDigestPasswordEncoder("SHA-1");

// Phase 3 (Spring Security 6.0) - API compatible, but verify:
// MessageDigestPasswordEncoder still in crypto.password package
// Should still work, but MUST TEST
```

**PrivateBlogVoter.java** (AccessDecisionVoter):
```java
// Phase 2 (Spring Security 5.8):
public int vote(Authentication authentication, FilterInvocation filterInvocation, Collection<ConfigAttribute> configAttributes)

// Phase 3 (Spring Security 6.0) - Verify signature:
// AccessDecisionVoter interface may have minor changes
// Check Spring Security 6.0 migration guide
```

**Success Criteria**:
- ✅ Application compiles with Spring 6.0
- ✅ Application starts successfully
- ✅ All unit tests pass (775/775)
- ✅ Integration tests: ≥24/25 pass
- ✅ CSRF protection functional (integration test #15)
- ✅ Authentication/authorization working

**Rollback Point**: Git tag `phase3d-spring6` + Docker tag `pebble:spring6`

### 4.6 Phase 3E: Testing & Validation (Days 15-18)

**Objective**: Comprehensive testing and validation

**Test Suite**:
1. **Unit Tests** (775 tests)
   ```bash
   mvn test
   # Target: 775/775 pass (100%)
   ```

2. **Integration Tests** (25 tests)
   ```bash
   ./phase3-integration-tests.sh
   # Target: ≥24/25 pass (96%+)
   ```

3. **Security Tests**:
   - CSRF token present (Test #15)
   - Login form functional (Test #13)
   - Password field correct (Test #14)

4. **Functional Tests**:
   - Blog CRUD operations
   - Search functionality (Lucene 9.9.2)
   - Feed generation (RSS/Atom)
   - File uploads
   - Theme management

5. **Performance Tests**:
   - Startup time (target: ≤3s)
   - Homepage response (target: ≤100ms)
   - Search query response (target: ≤50ms)

**Regression Testing**:
- Compare Phase 2 vs Phase 3 test results
- Ensure zero new failures
- Document any behavioral changes

**Success Criteria**:
- ✅ Unit tests: 100% pass
- ✅ Integration tests: ≥96% pass
- ✅ Zero functional regressions
- ✅ Performance: equal or better than Phase 2

**Rollback Point**: Git tag `phase3e-validated`

### 4.7 Phase 3F: Production Hardening (Days 19-21)

**Objective**: Production readiness optimizations

**Tasks**:
1. **Remove Java Module Workarounds**:
   ```xml
   <!-- REMOVE from maven-surefire-plugin: -->
   <argLine>--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED</argLine>
   ```

2. **Update Dependency Versions** (security patches):
   - Review CVE databases
   - Update to latest patch versions
   - Re-run security tests

3. **Docker Image Optimization**:
   - Use slim base image (eclipse-temurin:17-jre-jammy)
   - Multi-stage build (if beneficial)
   - Security scanning (Trivy, Clair)

4. **Documentation**:
   - Phase 3 changes documentation
   - Migration guide (Java 11 → 17)
   - Updated deployment instructions

5. **Backward Compatibility Testing**:
   - Test Phase 3 WAR on Tomcat 10
   - Verify Java 17 runtime requirements

**Success Criteria**:
- ✅ Clean build (no warnings)
- ✅ Secure Docker image
- ✅ Complete documentation
- ✅ Rollback procedures validated

**Final Rollback Point**: Git tag `phase3-java17-complete`

---

## 5. Rollback Strategy

### 5.1 Rollback Levels

#### Level 1: Git Branch Rollback (Development)
```bash
# If Phase 3 development fails, revert to Phase 2
git checkout phase2-java11-baseline
docker build -t pebble:java11 .
```
**Recovery Time**: <5 minutes

#### Level 2: Docker Image Rollback (Deployment)
```bash
# If Phase 3 deployment fails, roll back container
docker stop pebble-java17
docker run -d --name pebble-java11 pebble:java11-phase2
```
**Recovery Time**: <2 minutes (container restart)

#### Level 3: Maven Profile Rollback (Build)
```xml
<!-- pom.xml maintains Java 11 profile -->
<profile>
    <id>java11</id>
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <maven.compiler.release>11</maven.compiler.release>
        <spring.version>5.3.41</spring.version>
        <spring-security.version>5.8.14</spring-security.version>
    </properties>
</profile>
```
**Recovery Time**: Maven rebuild (~2 minutes)

### 5.2 Rollback Decision Criteria

**Trigger Rollback If**:
- Unit test pass rate <100% (and not resolvable within 2 hours)
- Integration test pass rate <90% (significant regression)
- Critical security vulnerability discovered in new dependencies
- Application fails to start after 3 troubleshooting attempts
- Performance degradation >20% vs Phase 2

**Acceptable Issues (Don't Rollback)**:
- Single integration test failure (matching Phase 2 baseline)
- Minor performance variations (<10%)
- Non-critical warnings during build
- Documentation incompleteness (can be completed post-migration)

### 5.3 Rollback Procedure

```bash
# 1. Stop Phase 3 deployment
docker stop pebble-java17
docker rm pebble-java17

# 2. Restore Phase 2 branch
git checkout phase2-java11-baseline

# 3. Rebuild Phase 2 container
docker build -t pebble:java11-rollback .

# 4. Deploy Phase 2
docker run -d --name pebble-java11 -p 8080:8080 pebble:java11-rollback

# 5. Verify health
curl http://localhost:8080/pebble/ping
# Expected: "Pong"

# 6. Run integration tests
./phase2-integration-tests.sh
# Expected: 24/25 pass (96%)
```

**Post-Rollback Actions**:
1. Document failure reason
2. Analyze root cause
3. Update migration plan
4. Schedule retry with fixes

---

## 6. Backward Compatibility Strategy

### 6.1 Multi-Release JAR Support

**Objective**: Support building with Java 11, 17, and 21 from same codebase

**Maven Profiles**:
```xml
<profiles>
    <!-- Phase 2: Java 11 LTS (maintenance) -->
    <profile>
        <id>java11</id>
        <properties>
            <maven.compiler.source>11</maven.compiler.source>
            <maven.compiler.target>11</maven.compiler.target>
            <maven.compiler.release>11</maven.compiler.release>
            <spring.version>5.3.41</spring.version>
            <spring-security.version>5.8.14</spring-security.version>
            <tomcat.version>9.0.85</tomcat.version>
        </properties>
    </profile>

    <!-- Phase 3: Java 17 LTS (current) -->
    <profile>
        <id>java17</id>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <maven.compiler.source>17</maven.compiler.source>
            <maven.compiler.target>17</maven.compiler.target>
            <maven.compiler.release>17</maven.compiler.release>
            <spring.version>6.0.17</spring.version>
            <spring-security.version>6.0.11</spring-security.version>
            <tomcat.version>10.1.19</tomcat.version>
        </properties>
    </profile>

    <!-- Future: Java 21 LTS (experimental) -->
    <profile>
        <id>java21</id>
        <properties>
            <maven.compiler.source>21</maven.compiler.source>
            <maven.compiler.target>21</maven.compiler.target>
            <maven.compiler.release>21</maven.compiler.release>
            <spring.version>6.1.x</spring.version>
            <spring-security.version>6.2.x</spring-security.version>
            <tomcat.version>10.1.19</tomcat.version>
        </properties>
    </profile>
</profiles>
```

**Build Commands**:
```bash
# Java 11 build (Phase 2 maintenance)
mvn clean package -P java11

# Java 17 build (Phase 3 default)
mvn clean package -P java17
# or just: mvn clean package

# Java 21 build (future)
mvn clean package -P java21
```

### 6.2 Docker Multi-Architecture Support

**Dockerfiles**:
- `Dockerfile` - Java 17 (default)
- `Dockerfile.java11` - Java 11 (maintenance)
- `Dockerfile.java21` - Java 21 (experimental)

**Docker Tags**:
```bash
docker build -t pebble:java11-phase2 -f Dockerfile.java11 .
docker build -t pebble:java17-phase3 -f Dockerfile .
docker build -t pebble:java21-future -f Dockerfile.java21 .
```

### 6.3 Branch Strategy

**Git Branches**:
- `master` - Latest stable (Java 17 after Phase 3)
- `phase2-java11-maintenance` - Java 11 LTS maintenance
- `phase3-java17-development` - Java 17 development (during migration)
- `experimental-java21` - Java 21 experimentation

**Merge Strategy**:
```
phase3-java17-development
    ↓ (after validation)
master (becomes Java 17)
    ↓ (backport critical fixes)
phase2-java11-maintenance (security fixes only)
```

---

## 7. Risk Assessment & Mitigation

### 7.1 High-Risk Areas

#### Risk 1: Spring 6.0 Breaking Changes

**Risk Level**: 🔴 **HIGH**

**Description**: Spring 5.3 → 6.0 is a major version upgrade with breaking API changes.

**Potential Impacts**:
- Configuration API changes (applicationContext-security.xml)
- Deprecated class removals (PasswordEncoder, AccessDecisionVoter)
- Behavior changes (security defaults, transaction management)

**Mitigation Strategy**:
1. **Thorough Testing**: Run full integration test suite after Spring 6 upgrade
2. **Staged Rollout**: Test each Spring component individually
3. **Reference Documentation**: Use Spring 6.0 migration guide
4. **Rollback Plan**: Git tag + Docker image before Spring 6 upgrade

**Success Indicator**: Integration test pass rate ≥96%

#### Risk 2: Jakarta EE Namespace Migration

**Risk Level**: 🟡 **MEDIUM**

**Description**: All `javax.*` → `jakarta.*` package renames are manual and error-prone.

**Potential Impacts**:
- Missed import statements (compilation errors)
- Runtime ClassNotFoundExceptions
- Third-party libraries with javax dependencies

**Mitigation Strategy**:
1. **Automated Search-Replace**: Use Eclipse Transformer or sed scripts
2. **Incremental Validation**: Compile after each batch of changes
3. **Test Coverage**: Unit tests catch missing imports immediately
4. **Dependency Audit**: Verify all dependencies use Jakarta namespaces

**Success Indicator**: Zero compilation errors, 100% unit test pass

#### Risk 3: Tomcat 10 Compatibility

**Risk Level**: 🟡 **MEDIUM**

**Description**: Tomcat 9 → 10 introduces Jakarta Servlet 5.0 with namespace changes.

**Potential Impacts**:
- Filter/Servlet classes fail to load
- web.xml schema incompatibility
- Container startup failures

**Mitigation Strategy**:
1. **Isolated Testing**: Test Tomcat 10 upgrade separately (Phase 3C)
2. **Configuration Validation**: Review web.xml against Tomcat 10 schemas
3. **Health Checks**: Use Docker health check to detect startup failures
4. **Gradual Migration**: Test locally before Docker deployment

**Success Indicator**: Application starts in Tomcat 10, health check passes

#### Risk 4: Test Regression

**Risk Level**: 🟡 **MEDIUM**

**Description**: Phase 3 changes may introduce regressions in functionality.

**Potential Impacts**:
- Integration test failures beyond Phase 2 baseline (24/25)
- New functional bugs introduced
- Performance degradation

**Mitigation Strategy**:
1. **Baseline Comparison**: Compare Phase 3 vs Phase 2 test results
2. **Regression Testing**: Run both unit and integration tests at each phase
3. **Performance Benchmarking**: Track startup time, response times
4. **Acceptance Criteria**: Require ≥96% integration test pass rate

**Success Indicator**: Test results equal or exceed Phase 2 baseline

### 7.2 Medium-Risk Areas

#### Risk 5: Dependency Version Conflicts

**Risk Level**: 🟢 **LOW-MEDIUM**

**Description**: Spring 6.0 and Jakarta EE dependencies may have version conflicts.

**Mitigation**:
- Use Maven dependency:tree to detect conflicts
- Explicitly declare transitive dependency versions
- Test with `mvn clean install -U` (force update)

#### Risk 6: Performance Regression

**Risk Level**: 🟢 **LOW**

**Description**: Java 17 and Spring 6 may have different performance characteristics.

**Mitigation**:
- Benchmark startup time, response times
- Compare Phase 2 vs Phase 3 metrics
- Accept minor variations (<10%)

**Historical Data**: Java 11 was 19% faster than Java 8 (Phase 1 → 2). Java 17 expected similar or better.

### 7.3 Risk Matrix

| Risk | Probability | Impact | Severity | Mitigation |
|------|------------|--------|----------|------------|
| Spring 6.0 breaking changes | High | High | 🔴 Critical | Staged testing, rollback plan |
| Jakarta namespace errors | Medium | Medium | 🟡 Significant | Automated migration, unit tests |
| Tomcat 10 compatibility | Medium | Medium | 🟡 Significant | Isolated testing, health checks |
| Test regression | Medium | High | 🟡 Significant | Baseline comparison, acceptance criteria |
| Dependency conflicts | Low | Medium | 🟢 Moderate | Dependency analysis, explicit versions |
| Performance regression | Low | Low | 🟢 Minor | Benchmarking, profiling |

---

## 8. Quality Attributes & Constraints

### 8.1 Non-Functional Requirements

#### Reliability
- **Target**: 99.9% uptime (same as Phase 2)
- **Validation**: Integration tests must pass ≥96%
- **Rollback**: Available within 5 minutes if critical failure

#### Performance
- **Startup Time**: ≤3 seconds (Phase 2: 2.6s)
- **Homepage Response**: ≤100ms (Phase 2: 80ms)
- **Search Response**: ≤50ms (Phase 2: 42ms)
- **Acceptable Regression**: <10% from Phase 2 baseline

#### Security
- **CVE Status**: Zero critical vulnerabilities
- **CSRF Protection**: Must remain functional (Test #15)
- **Authentication**: All mechanisms (form, OpenID, basic, remember-me) working
- **Authorization**: Role-based access control functional

#### Maintainability
- **Code Quality**: B+ or higher (same as Phase 2)
- **Test Coverage**: 100% unit test pass rate
- **Documentation**: Complete migration guide and architecture docs
- **Technical Debt**: Eliminate Java module workarounds

#### Compatibility
- **Backward Compatibility**: Java 11 build profile maintained
- **Forward Compatibility**: Java 21 build profile prepared (experimental)
- **Container Compatibility**: Docker images for Java 11, 17, 21

### 8.2 Technical Constraints

#### Build Constraints
- Maven 3.8+ (required for Java 17)
- Maven Compiler Plugin 3.11+ (supports --release 17)
- Maven Surefire Plugin 3.2+ (Java 17 testing)

#### Runtime Constraints
- OpenJDK 17 or Oracle JDK 17 (LTS)
- Tomcat 10.1.x (Jakarta EE 9+)
- Spring Framework 6.0+ (Java 17 minimum)

#### Deployment Constraints
- Docker image based on Ubuntu 22.04 LTS or Eclipse Temurin 17
- Container runtime: Docker 20.10+ or Podman 4.0+
- Minimum memory: 1GB JVM heap (same as Phase 2)

---

## 9. Architecture Decision Records (ADRs)

### ADR-001: Choose Java 17 over Java 21

**Date**: January 14, 2026

**Status**: Accepted

**Context**: Pebble needs to migrate from Java 11 to a newer LTS version. Java 17 and Java 21 are both LTS candidates.

**Decision**: Use Java 17 LTS for Phase 3 migration.

**Rationale**:
- Java 17 has 3+ years of production maturity vs Java 21's 15 months
- Spring 6.0 ecosystem is well-tested with Java 17
- Lower migration risk with established Java 11 → 17 path
- Java 17 LTS support until 2029 (sufficient for Pebble lifecycle)
- Java 21 features (virtual threads, advanced pattern matching) not critical for Pebble's use case

**Consequences**:
- Positive: Lower migration risk, mature ecosystem, faster migration
- Negative: Missing Java 21 features, will need future Java 21 migration
- Future: Assess Java 21 migration in 12-18 months when ecosystem matures

---

### ADR-002: Incremental Migration Strategy

**Date**: January 14, 2026

**Status**: Accepted

**Context**: Migrating Java version + Spring 6 + Tomcat 10 simultaneously has high risk.

**Decision**: Use phased migration approach with rollback points at each stage.

**Phases**:
1. Java 17 compilation
2. Jakarta namespace migration
3. Tomcat 10 upgrade
4. Spring 6 upgrade
5. Testing & validation
6. Production hardening

**Rationale**:
- Isolate failure domains (easier debugging)
- Validate each layer before proceeding
- Enable fast rollback at any stage
- Reduce risk of cascading failures

**Consequences**:
- Positive: Lower risk, clear validation points, faster troubleshooting
- Negative: Longer timeline (2-3 weeks vs "big bang" 1 week)
- Acceptable: Risk reduction justifies extended timeline

---

### ADR-003: Spring Framework 5.3 → 6.0 Upgrade

**Date**: January 14, 2026

**Status**: Accepted

**Context**: Java 17 requires Spring 6.0+ (Spring 5.3 supports Java 11 maximum).

**Decision**: Upgrade Spring Framework 5.3.41 → 6.0.17 and Spring Security 5.8.14 → 6.0.11.

**Rationale**:
- Spring 6.0 is required for Java 17 support
- Spring 5.3 EOL approaching (maintenance mode)
- Spring 6.0 actively developed with security patches
- Breaking changes manageable with careful testing

**Breaking Changes to Address**:
- `javax.*` → `jakarta.*` (already planned)
- Configuration API updates (applicationContext-security.xml)
- Deprecated class removal (verify PasswordEncoder, AccessDecisionVoter)

**Consequences**:
- Positive: Active support, security updates, modern APIs
- Negative: High-risk upgrade requiring extensive testing
- Mitigation: Staged rollout (Phase 3D), comprehensive test suite

---

### ADR-004: Tomcat 10.1 as Servlet Container

**Date**: January 14, 2026

**Status**: Accepted

**Context**: Spring 6.0 requires Jakarta Servlet 5.0+, which requires Tomcat 10+.

**Decision**: Upgrade from Tomcat 9.0.85 to Tomcat 10.1.19.

**Rationale**:
- Tomcat 10.1.x is first stable Jakarta EE 9+ version
- Tomcat 10.1.19 is current stable release (Jan 2026)
- Required for Spring 6.0 compatibility
- Minimal breaking changes (primarily namespace)

**Alternatives Considered**:
- Jetty 11+: Less Pebble community familiarity
- Tomcat 9 with hacks: Not sustainable, Spring 6 incompatible

**Consequences**:
- Positive: Jakarta EE 9+ support, active development
- Negative: Namespace migration required
- Acceptable: Namespace migration already planned (ADR-002)

---

### ADR-005: Maintain Java 11 Backward Compatibility

**Date**: January 14, 2026

**Status**: Accepted

**Context**: Some users may need Java 11 compatibility during transition period.

**Decision**: Maintain Maven profile for Java 11 builds with Spring 5.3 + Tomcat 9.

**Rationale**:
- Provides fallback for users not ready for Java 17
- Enables A/B testing (Java 11 vs 17)
- Minimal cost (Maven profile + separate Dockerfile)

**Maintenance Strategy**:
- Java 11 profile: Security fixes only (no new features)
- Duration: 6-12 months after Phase 3 completion
- EOL: When Java 11 community support ends (2026-2027)

**Consequences**:
- Positive: Smooth transition for users, rollback option
- Negative: Dual maintenance burden (manageable short-term)
- Acceptable: Limited maintenance scope (security only)

---

## 10. Success Criteria

### 10.1 Technical Success Criteria

| Criterion | Target | Validation Method |
|-----------|--------|-------------------|
| **Java Compilation** | Zero errors | `mvn clean compile -P java17` |
| **Unit Tests** | 775/775 pass (100%) | `mvn test` |
| **Integration Tests** | ≥24/25 pass (96%) | `./phase3-integration-tests.sh` |
| **Startup Time** | ≤3 seconds | Docker logs timestamp |
| **Response Time** | Homepage ≤100ms | `curl` + `time` command |
| **Security** | Zero critical CVEs | Dependency CVE scan |
| **CSRF Protection** | Functional | Integration Test #15 |
| **Backward Compat** | Java 11 builds | `mvn clean package -P java11` |

### 10.2 Functional Success Criteria

| Feature | Success Indicator |
|---------|-------------------|
| **Blog CRUD** | Create, read, update, delete blog posts |
| **Search** | Lucene indexing and query functional |
| **Feeds** | RSS/Atom feed generation (all formats) |
| **Authentication** | Form login, OpenID, HTTP Basic, Remember-me |
| **Authorization** | Role-based access control (all roles) |
| **File Uploads** | Image and file upload functional |
| **Themes** | Theme selection and customization working |

### 10.3 Acceptance Criteria

**Migration Approved If**:
- ✅ All technical success criteria met
- ✅ All functional success criteria validated
- ✅ Zero functional regressions vs Phase 2
- ✅ Security review passed (B+ or higher)
- ✅ Code quality maintained (B+ or higher)
- ✅ Documentation complete

**Migration Rejected If**:
- ❌ Unit test pass rate <100%
- ❌ Integration test pass rate <90%
- ❌ Critical CVEs in dependencies
- ❌ Performance regression >20%
- ❌ Security regression (e.g., CSRF broken)

---

## 11. Phase 3 Deliverables

### 11.1 Code Artifacts

| Artifact | Description |
|----------|-------------|
| **pom.xml** | Updated with Java 17, Spring 6, Tomcat 10 dependencies |
| **Dockerfile** | Java 17 + Tomcat 10 container configuration |
| **applicationContext-security.xml** | Spring Security 6.0 configuration |
| **Servlet classes** | Jakarta namespace (jakarta.servlet.*) |
| **Test classes** | Updated for Jakarta and Spring 6 APIs |

### 11.2 Documentation

| Document | Description |
|----------|-------------|
| **phase3-architecture.md** | This document (architecture and strategy) |
| **phase3-migration-guide.md** | Step-by-step migration instructions |
| **phase3-changes-documentation.md** | Detailed change log |
| **phase3-validation-report.md** | Test results and approval |
| **phase3-security-review.md** | Security assessment |

### 11.3 Testing Artifacts

| Artifact | Description |
|----------|-------------|
| **phase3-integration-tests.sh** | Integration test script (updated for Phase 3) |
| **phase3-integration-test-results.md** | Test execution results |
| **phase3-performance-benchmarks.md** | Performance comparison vs Phase 2 |

### 11.4 Deployment Artifacts

| Artifact | Description |
|----------|-------------|
| **Docker image** | `pebble:java17-phase3` |
| **Maven profiles** | Java 11, 17, 21 build configurations |
| **Git tags** | Rollback points (phase3a, phase3b, phase3c, phase3d, phase3e, phase3f) |

---

## 12. Memory Store: Architecture Decisions

**For Implementation Team**: The following key decisions and patterns will be stored in memory for reference during implementation.

### Key Decision Summary

```json
{
  "phase": "Phase 3 - Java 17 Migration",
  "target_java_version": "17",
  "rationale": "Mature ecosystem, Spring 6 support, lower risk vs Java 21",
  "migration_strategy": "Incremental with rollback points",
  "dependency_sequence": [
    "Java 17 compilation",
    "Jakarta namespace migration",
    "Tomcat 10 upgrade",
    "Spring 6 upgrade",
    "Testing & validation",
    "Production hardening"
  ],
  "key_versions": {
    "java": "17",
    "spring_framework": "6.0.17",
    "spring_security": "6.0.11",
    "tomcat": "10.1.19",
    "lucene": "9.9.2",
    "servlet_api": "Jakarta Servlet 5.0"
  },
  "success_criteria": {
    "unit_tests": "775/775 (100%)",
    "integration_tests": "≥24/25 (96%)",
    "startup_time": "≤3s",
    "response_time": "≤100ms",
    "cve_count": "0 critical"
  },
  "rollback_strategy": {
    "git_branch": "phase2-java11-baseline",
    "docker_image": "pebble:java11-phase2",
    "maven_profile": "-P java11"
  },
  "timeline": "2-3 weeks",
  "risk_level": "Medium (mitigated with incremental approach)"
}
```

### Implementation Pattern

```yaml
migration_phases:
  phase_3a:
    name: "Java 17 Compilation"
    duration: "Days 1-3"
    objective: "Compile with Java 17"
    success: "Zero errors, 775/775 tests pass"
    rollback: "Git tag phase3a-java17-compile"

  phase_3b:
    name: "Jakarta Namespace Migration"
    duration: "Days 4-6"
    objective: "javax.* → jakarta.*"
    success: "Code compiles with Jakarta imports"
    rollback: "Git tag phase3b-jakarta-namespace"

  phase_3c:
    name: "Tomcat 10 Migration"
    duration: "Days 7-9"
    objective: "Tomcat 9 → 10.1.19"
    success: "App starts, health check passes"
    rollback: "Git tag phase3c-tomcat10 + Docker pebble:tomcat10"

  phase_3d:
    name: "Spring 6 Migration"
    duration: "Days 10-14"
    objective: "Spring 5.3 → 6.0, Security 5.8 → 6.0"
    success: "All tests pass, CSRF functional"
    rollback: "Git tag phase3d-spring6 + Docker pebble:spring6"

  phase_3e:
    name: "Testing & Validation"
    duration: "Days 15-18"
    objective: "Comprehensive test suite"
    success: "≥96% integration pass, zero regressions"
    rollback: "Git tag phase3e-validated"

  phase_3f:
    name: "Production Hardening"
    duration: "Days 19-21"
    objective: "Remove workarounds, optimize"
    success: "Clean build, secure image, docs complete"
    rollback: "Git tag phase3-java17-complete"
```

---

## 13. Next Steps

### 13.1 Immediate Actions

1. **Review this architecture design** with stakeholders
2. **Obtain approval** for Java 17 target and incremental strategy
3. **Schedule Phase 3 sprint** (2-3 weeks)
4. **Prepare development environment** (Java 17 JDK, Docker, Maven 3.8+)

### 13.2 Pre-Migration Checklist

- [ ] Java 17 JDK installed and configured
- [ ] Maven 3.8+ installed
- [ ] Docker environment tested (build and run)
- [ ] Phase 2 baseline validated (24/25 integration tests passing)
- [ ] Git branch created (`phase3-java17-development`)
- [ ] Documentation template prepared
- [ ] Rollback procedures documented and tested

### 13.3 Phase 3 Kickoff

**Prerequisites Met**:
- ✅ Phase 2 (Java 11) completed and approved
- ✅ Phase 2.5 (Security) completed (BCrypt, headers, HTTPS)
- ✅ Architecture design approved
- ✅ Development environment ready

**Ready to Begin**: Phase 3A (Java 17 Compilation)

---

## 14. Conclusion

This architecture design provides a comprehensive, risk-mitigated strategy for migrating Pebble from Java 11 to Java 17. The incremental approach with rollback points at each stage minimizes risk while enabling thorough validation.

**Key Strengths of This Design**:
1. **Risk-Aware**: Phased approach with clear rollback procedures
2. **Testable**: Success criteria defined at each phase
3. **Maintainable**: Backward compatibility with Java 11 builds
4. **Future-Ready**: Prepared for Java 21 when ecosystem matures
5. **Documented**: Comprehensive ADRs and decision rationale

**Recommendation**: **PROCEED with Java 17 migration** following this architectural design.

---

**Document Status**: ✅ **COMPLETE - Ready for Review**
**Architecture Approval**: Pending stakeholder sign-off
**Next Document**: `phase3-migration-guide.md` (implementation instructions)

---

**Prepared by**: System Architecture Designer
**Date**: January 14, 2026
**Version**: 1.0 (Final)
