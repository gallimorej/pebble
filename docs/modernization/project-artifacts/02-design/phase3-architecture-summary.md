# Phase 3 Architecture - Quick Reference Summary

**Date**: January 14, 2026
**Status**: Design Complete - Ready for Implementation

---

## Key Decisions

### 1. Target Platform: **Java 17 LTS** (Not Java 21)

**Why Java 17?**
- ✅ Mature ecosystem (3+ years production)
- ✅ Spring 6.0 fully tested and stable
- ✅ Lower migration risk
- ✅ LTS support until 2029
- ✅ Excellent tooling and container support

**Why Not Java 21?**
- ⚠️ Only 15 months old (less battle-tested)
- ⚠️ Some dependencies still catching up
- ⚠️ Higher risk for production
- ℹ️ Defer to future phase (12-18 months)

---

## Migration Strategy: Incremental Phased Approach

### Phase Sequence

```
Week 1: Java 17 Foundation
├─ Phase 3A: Java 17 Compilation (Days 1-3)
└─ Phase 3B: Jakarta Namespace Migration (Days 4-6)

Week 2: Container & Framework
├─ Phase 3C: Tomcat 10 Migration (Days 7-9)
└─ Phase 3D: Spring 6 Migration (Days 10-14)

Week 3: Validation & Production
├─ Phase 3E: Testing & Validation (Days 15-18)
└─ Phase 3F: Production Hardening (Days 19-21)
```

### Rollback Points

Each phase has a Git tag for fast rollback:
- `phase3a-java17-compile`
- `phase3b-jakarta-namespace`
- `phase3c-tomcat10` (+ Docker image)
- `phase3d-spring6` (+ Docker image)
- `phase3e-validated`
- `phase3-java17-complete` (final)

**Recovery Time**: <5 minutes at any stage

---

## Dependency Upgrade Matrix

| Component | Phase 2 (Java 11) | Phase 3 (Java 17) | Breaking Changes |
|-----------|-------------------|-------------------|------------------|
| **Java** | 11 | **17** | Module encapsulation |
| **Spring Framework** | 5.3.41 | **6.0.17** | javax → jakarta, API changes |
| **Spring Security** | 5.8.14 | **6.0.11** | Configuration API, jakarta |
| **Tomcat** | 9.0.85 | **10.1.19** | jakarta.servlet namespace |
| **Servlet API** | 3.1.0 | **Jakarta 5.0** | Package rename |
| **Lucene** | 9.9.2 | **9.9.2** | None (already compatible) |
| **JAXB** | Jakarta 2.3.3 | **3.0.x or 4.0.x** | Minimal |

---

## Success Criteria

### Technical Metrics

| Metric | Target | Validation |
|--------|--------|------------|
| **Unit Tests** | 775/775 (100%) | `mvn test` |
| **Integration Tests** | ≥24/25 (96%) | `./phase3-integration-tests.sh` |
| **Startup Time** | ≤3 seconds | Docker logs |
| **Response Time** | ≤100ms (homepage) | `curl` + `time` |
| **CVE Count** | 0 critical | Dependency scan |
| **CSRF Protection** | Functional | Integration Test #15 |

### Functional Validation

- ✅ Blog CRUD operations
- ✅ Search (Lucene indexing)
- ✅ Feed generation (RSS/Atom)
- ✅ Authentication (all methods)
- ✅ Authorization (RBAC)
- ✅ File uploads
- ✅ Theme management

---

## High-Risk Areas

### Risk 1: Spring 6.0 Breaking Changes 🔴 **HIGH**

**Impact**: Configuration API changes, deprecated class removal

**Mitigation**:
- Staged rollout (isolated testing)
- Comprehensive integration tests
- Rollback plan (Git tag + Docker image)

**Success Indicator**: Integration test pass rate ≥96%

### Risk 2: Jakarta EE Namespace Migration 🟡 **MEDIUM**

**Impact**: All `javax.*` → `jakarta.*` package renames

**Mitigation**:
- Automated search-replace (Eclipse Transformer)
- Incremental validation (compile after each batch)
- Unit test coverage (catches missing imports)

**Success Indicator**: Zero compilation errors, 100% unit test pass

### Risk 3: Tomcat 10 Compatibility 🟡 **MEDIUM**

**Impact**: Jakarta Servlet 5.0 namespace changes

**Mitigation**:
- Isolated testing (Phase 3C separate from Spring 6)
- Configuration validation (web.xml schema)
- Docker health checks

**Success Indicator**: Application starts, health check passes

---

## Rollback Strategy

### Three-Level Rollback

#### Level 1: Git Branch Rollback
```bash
git checkout phase2-java11-baseline
docker build -t pebble:java11 .
```
**Recovery Time**: <5 minutes

#### Level 2: Docker Image Rollback
```bash
docker stop pebble-java17
docker run -d --name pebble-java11 pebble:java11-phase2
```
**Recovery Time**: <2 minutes

#### Level 3: Maven Profile Rollback
```bash
mvn clean package -P java11
```
**Recovery Time**: ~2 minutes (rebuild)

### Rollback Triggers

**Immediate Rollback If**:
- Unit test pass rate <100%
- Integration test pass rate <90%
- Critical CVE in new dependencies
- Application fails to start (3+ attempts)
- Performance degradation >20%

---

## Backward Compatibility

### Maven Multi-Profile Support

```bash
# Java 11 build (Phase 2 maintenance)
mvn clean package -P java11

# Java 17 build (Phase 3 default)
mvn clean package -P java17
# or just: mvn clean package

# Java 21 build (future experimental)
mvn clean package -P java21
```

### Docker Multi-Architecture

- `Dockerfile` → Java 17 (default)
- `Dockerfile.java11` → Java 11 (maintenance)
- `Dockerfile.java21` → Java 21 (experimental)

### Git Branch Strategy

- `master` → Java 17 (after Phase 3)
- `phase2-java11-maintenance` → Security fixes only
- `experimental-java21` → Future exploration

---

## Critical Breaking Changes to Address

### 1. Jakarta Namespace (All Phases)

**Before (Phase 2)**:
```java
import javax.servlet.*;
import javax.servlet.http.*;
import javax.annotation.*;
```

**After (Phase 3)**:
```java
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.annotation.*;
```

**Affected Files**:
- All servlet/filter classes (ResponseSplittingPreventer, etc.)
- Test mock classes (MockHttpServletRequest, etc.)
- Spring configuration (minimal - Spring 6 handles internally)

### 2. Spring Security Configuration (Phase 3D)

**Verify These Classes**:
- `DefaultSecurityRealm.java` → PasswordEncoder API (should be compatible)
- `PrivateBlogVoter.java` → AccessDecisionVoter interface (check Spring 6 signature)
- `applicationContext-security.xml` → Spring Security 6.0 namespace

### 3. Remove Java Module Workarounds (Phase 3F)

**Delete from maven-surefire-plugin**:
```xml
<!-- REMOVE THIS: -->
<argLine>--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.io=ALL-UNNAMED</argLine>
```

---

## Implementation Checklist

### Pre-Migration
- [ ] Java 17 JDK installed
- [ ] Maven 3.8+ verified
- [ ] Docker environment tested
- [ ] Phase 2 baseline validated (24/25 integration tests)
- [ ] Git branch created (`phase3-java17-development`)
- [ ] Rollback procedures documented

### Phase 3A: Java 17 Compilation
- [ ] Update pom.xml (compiler source/target/release to 17)
- [ ] Update Dockerfile (OpenJDK 17)
- [ ] Maven compile success
- [ ] Unit tests pass (775/775)
- [ ] Git tag: `phase3a-java17-compile`

### Phase 3B: Jakarta Namespace
- [ ] Automated search-replace (javax → jakarta)
- [ ] Manual review of servlet classes
- [ ] Update test mocks
- [ ] Code compiles
- [ ] Unit tests pass (775/775)
- [ ] Git tag: `phase3b-jakarta-namespace`

### Phase 3C: Tomcat 10
- [ ] Update pom.xml (Tomcat 10.1.19 servlet-api)
- [ ] Update Dockerfile (Tomcat 10.1.19)
- [ ] Docker build success
- [ ] Application starts
- [ ] Health check passes (/ping)
- [ ] Integration tests: ≥24/25
- [ ] Git tag: `phase3c-tomcat10`
- [ ] Docker tag: `pebble:tomcat10`

### Phase 3D: Spring 6
- [ ] Update pom.xml (Spring 6.0.17, Spring Security 6.0.11)
- [ ] Review applicationContext-security.xml
- [ ] Verify DefaultSecurityRealm.java
- [ ] Verify PrivateBlogVoter.java
- [ ] Maven compile success
- [ ] Application starts
- [ ] Unit tests pass (775/775)
- [ ] Integration tests: ≥24/25
- [ ] CSRF protection functional (Test #15)
- [ ] Git tag: `phase3d-spring6`
- [ ] Docker tag: `pebble:spring6`

### Phase 3E: Testing & Validation
- [ ] Full integration test suite (25 tests)
- [ ] Security tests (CSRF, login, auth)
- [ ] Functional tests (CRUD, search, feeds)
- [ ] Performance benchmarks (startup, response times)
- [ ] Regression comparison (Phase 2 vs Phase 3)
- [ ] Git tag: `phase3e-validated`

### Phase 3F: Production Hardening
- [ ] Remove Java module workarounds
- [ ] Update dependency versions (security patches)
- [ ] Docker image optimization
- [ ] Security scanning (Trivy, CVE check)
- [ ] Documentation complete
- [ ] Backward compatibility verified
- [ ] Git tag: `phase3-java17-complete`

---

## Key Files to Modify

### Build Configuration
- `pom.xml` → Java version, Spring 6, Tomcat 10 dependencies
- `Dockerfile` → Java 17, Tomcat 10.1.19

### Spring Security Configuration
- `applicationContext-security.xml` → Spring Security 6.0 namespace
- `DefaultSecurityRealm.java` → Verify PasswordEncoder
- `PrivateBlogVoter.java` → Verify AccessDecisionVoter

### Servlet Layer (Jakarta Namespace)
- `ResponseSplittingPreventer.java` → jakarta.servlet
- All Filter classes → jakarta.servlet
- All Servlet classes → jakarta.servlet

### Test Layer (Jakarta Namespace)
- `MockHttpServletRequest.java` → jakarta.servlet.http
- `MockHttpServletResponse.java` → jakarta.servlet.http
- Other test mocks using servlet API

---

## Performance Expectations

### Historical Data

| Migration | Startup Time | Response Time | Change |
|-----------|--------------|---------------|--------|
| Phase 1 (Java 8) | 3.2s | 85ms | Baseline |
| Phase 2 (Java 11) | 2.6s | 80ms | -19%, -6% ✅ |
| Phase 3 (Java 17) | ≤3.0s | ≤100ms | Target |

**Expected Result**: Java 17 typically equals or improves Java 11 performance (G1GC enhancements, JIT improvements).

---

## Documentation Deliverables

### Architecture Phase (Complete)
- ✅ `phase3-java17-21-architecture.md` (this is the comprehensive design)
- ✅ `phase3-architecture-summary.md` (this quick reference)

### Implementation Phase (To Be Created)
- 📝 `phase3-migration-guide.md` (step-by-step instructions)
- 📝 `phase3-changes-documentation.md` (detailed change log)
- 📝 `phase3-integration-tests.sh` (test script)

### Validation Phase (To Be Created)
- 📝 `phase3-integration-test-results.md` (test execution results)
- 📝 `phase3-validation-report.md` (final approval document)
- 📝 `phase3-security-review.md` (security assessment)
- 📝 `phase3-performance-benchmarks.md` (performance comparison)

---

## Quick Commands

### Build & Test
```bash
# Compile with Java 17
mvn clean compile -P java17

# Run unit tests
mvn test

# Build WAR
mvn clean package -P java17

# Run integration tests
./phase3-integration-tests.sh

# Build Docker image
docker build -t pebble:java17-phase3 .

# Run container
docker run -d --name pebble-java17 -p 8080:8080 pebble:java17-phase3

# Check health
curl http://localhost:8080/pebble/ping
```

### Rollback
```bash
# Level 1: Git branch rollback
git checkout phase2-java11-baseline
mvn clean package -P java11
docker build -t pebble:java11-rollback .

# Level 2: Docker image rollback
docker stop pebble-java17
docker run -d --name pebble-java11 pebble:java11-phase2

# Level 3: Maven profile rollback
mvn clean package -P java11
```

---

## Memory Store Commands

Store these decisions in Claude Flow memory:

```bash
# Java version decision
npx @claude-flow/cli@latest memory store \
  --namespace "architecture-decisions" \
  --key "phase3-java-version" \
  --value "Java 17 LTS selected for mature ecosystem, Spring 6 support, lower migration risk"

# Migration strategy
npx @claude-flow/cli@latest memory store \
  --namespace "architecture-decisions" \
  --key "phase3-migration-strategy" \
  --value "Incremental phased: Java 17 compile → Jakarta namespace → Tomcat 10 → Spring 6 → Testing → Hardening"

# Success criteria
npx @claude-flow/cli@latest memory store \
  --namespace "architecture-decisions" \
  --key "phase3-success-criteria" \
  --value "Unit: 775/775 (100%), Integration: ≥24/25 (96%), Startup: ≤3s, Response: ≤100ms, CVEs: 0 critical"

# Retrieve decisions
npx @claude-flow/cli@latest memory search \
  --query "phase3" \
  --namespace "architecture-decisions"
```

---

## Contact & References

**Full Architecture Document**: `phase3-java17-21-architecture.md`
**Repository**: `/Users/jgallimore/Projects/pebble`
**Documentation**: `docs/modernization/project-artifacts/02-design/`

**Phase 2 Baseline**:
- Test Results: 775/775 unit, 24/25 integration (96%)
- Validation Report: `phase2-validation-report.md`
- Security Review: `phase2-security-review.md`

**Next Step**: Create `phase3-migration-guide.md` with step-by-step implementation instructions.

---

**Status**: ✅ **Architecture Design Complete - Ready for Implementation**
**Approval**: Pending stakeholder review
**Timeline**: 2-3 weeks from kickoff

---

**Prepared by**: System Architecture Designer
**Date**: January 14, 2026
**Version**: 1.0
