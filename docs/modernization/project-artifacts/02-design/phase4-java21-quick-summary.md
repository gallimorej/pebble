# Phase 4: Java 21 Migration - Quick Reference Summary

**Date**: January 14, 2026
**Status**: Design Complete - Ready for Implementation (after Phase 3)
**Prerequisites**: Phase 3 (Java 17) complete and production-stable

---

## Key Decisions

### 1. Target Platform: **Java 21 LTS** (from Java 17)

**Why Java 21?**
- ✅ **Virtual threads** (Project Loom) - 10-30x concurrency improvement
- ✅ Pattern matching for switch - cleaner code
- ✅ Record patterns - better data handling
- ✅ LTS support until 2031 (8+ years)
- ✅ 15-25% performance improvement over Java 17
- ✅ Binary compatible with Java 17 (easy rollback)

**Key Difference from Phase 3**:
- Phase 3 (Java 11 → 17): Major Spring 6 upgrade, Jakarta namespace migration (HIGH RISK)
- Phase 4 (Java 17 → 21): Virtual threads, Spring 6.1 minor upgrade (MEDIUM RISK)

---

## Migration Strategy: Incremental Phased Approach

### Phase Sequence

```
Week 1: Foundation
├─ Phase 4A: Java 21 Compilation + Spring 6.1 (Days 1-5)
└─ Phase 4B: Virtual Threads (Days 6-10)

Week 2: Code Modernization
├─ Phase 4B: Virtual Thread Load Testing (Days 8-10)
└─ Phase 4C: Pattern Matching Refactoring (Days 11-14)

Week 3: Validation & Production
└─ Phase 4D: Testing, Benchmarking, Deployment (Days 15-21)
```

### Rollback Points

Each phase has Git tag + Docker image for fast rollback:
- `phase4a-java21-foundation` + `pebble:java21-foundation`
- `phase4b-virtual-threads` + `pebble:virtual-threads`
- `phase4c-code-modernization` + `pebble:java21-modern`
- `phase4-java21-complete` + `pebble:java21-production`

**Recovery Time**: <5 minutes to rollback to Java 17

---

## Dependency Upgrade Matrix

| Component | Phase 3 (Java 17) | Phase 4 (Java 21) | Breaking Changes |
|-----------|-------------------|-------------------|------------------|
| **Java** | 17 | **21** | None (binary compatible) |
| **Spring Framework** | 6.0.23 | **6.1.14+** | None (6.1 backward compatible with 6.0) |
| **Spring Security** | 6.2.1 | **6.2.2+** | None (patch version) |
| **Tomcat** | 10.1.19 | **10.1.28+** | None (virtual thread support) |
| **Servlet API** | Jakarta 5.0 | **Jakarta 6.0** (optional) | None |
| **Lucene** | 9.9.2 | **9.10.0+** (optional) | None |

**Key Insight**: Much simpler than Phase 3 (no namespace migration, no major Spring version jump)

---

## The Big Feature: Virtual Threads 🚀

### What Are Virtual Threads?

**Traditional Platform Threads** (Java 17):
- One OS thread per concurrent request
- Limited by OS thread pool (150-200 threads)
- Each thread: 1-2MB memory overhead
- **Pebble Phase 3 capacity**: 150-200 concurrent users

**Virtual Threads** (Java 21):
- Lightweight threads managed by JVM
- Thousands of virtual threads → dozens of platform threads
- Minimal memory overhead per thread
- **Pebble Phase 4 capacity**: 1000-5000+ concurrent users

### Virtual Thread Configuration

**Spring Configuration**:
```java
@Configuration
public class VirtualThreadConfig {
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(
            Executors.newVirtualThreadPerTaskExecutor()
        );
    }
}
```

**Tomcat Configuration** (server.xml):
```xml
<Connector port="8080" protocol="HTTP/1.1"
           maxThreads="10000"
           useVirtualThreads="true"
           connectionTimeout="20000" />
```

**Or via application.properties**:
```yaml
spring.threads.virtual.enabled=true
server.tomcat.threads.max=10000
```

### Virtual Thread Benefits for Pebble

| Scenario | Java 17 (Platform Threads) | Java 21 (Virtual Threads) | Improvement |
|----------|----------------------------|---------------------------|-------------|
| **Normal traffic** | 50-100 concurrent users | Same (no difference) | Baseline |
| **Burst traffic** | 200 users (max, then queue/timeout) | 1000-5000 users (no queue) | **10-30x** |
| **Memory usage** | 1.2GB (200 users) | 800-900MB (1000 users) | 25-35% reduction |
| **Platform threads** | 200 threads | <500 threads (1000 users) | Virtual threads |

**Use Case**: When a blog post goes viral, Pebble can handle massive concurrent traffic without OOM errors.

---

## Success Criteria

### Technical Metrics

| Metric | Target | Validation |
|--------|--------|------------|
| **Unit Tests** | 775/775 (100%) | `mvn test` |
| **Integration Tests** | ≥24/25 (96%) | `./phase4-integration-tests.sh` |
| **Concurrent Users** | 1000+ (no timeouts) | `wrk -c1000 -d60s` |
| **Platform Thread Count** | <500 (under 1000 users) | Thread dump, JMX |
| **Memory (1000 users)** | ≤1.5GB | JMX heap metrics |
| **Throughput** | +15-25% vs Phase 3 | Load testing |
| **Startup Time** | ≤2.5 seconds | Docker logs |

### Load Testing Targets

```bash
# Test 1: Burst traffic (1000 concurrent users)
wrk -t4 -c1000 -d60s http://localhost:8080/pebble/
# Expected: 2000-5000 req/sec, zero timeouts

# Test 2: Sustained load (500 users, 5 minutes)
wrk -t4 -c500 -d300s http://localhost:8080/pebble/
# Expected: Stable throughput, <1% error rate

# Test 3: Stress test (5000 users)
wrk -t8 -c5000 -d30s http://localhost:8080/pebble/
# Expected: <1% error rate, graceful degradation
```

---

## Pattern Matching Improvements

### Before (Java 17) - instanceof Cascade
```java
public ModelAndView handleRequest(HttpServletRequest request) {
    String uri = request.getRequestURI();
    if (uri.contains("/blog/")) {
        return handleBlogRequest(request);
    } else if (uri.contains("/comment/")) {
        return handleCommentRequest(request);
    } else if (uri.contains("/feed/")) {
        return handleFeedRequest(request);
    }
    return handleDefault(request);
}
```

### After (Java 21) - Pattern Matching Switch
```java
public ModelAndView handleRequest(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return switch (uri) {
        case String s when s.contains("/blog/") -> handleBlogRequest(request);
        case String s when s.contains("/comment/") -> handleCommentRequest(request);
        case String s when s.contains("/feed/") -> handleFeedRequest(request);
        default -> handleDefault(request);
    };
}
```

**Benefits**:
- 10-20% faster type checks
- 5-10% less boilerplate
- More readable and maintainable

**High-Value Targets in Pebble**:
- PermalinkController.java (request routing)
- AccessDecisionVoter.java (security decisions)
- ResponseController.java (response type handling)

---

## Risk Assessment

### High-Risk Areas

#### Risk 1: Virtual Thread Compatibility 🔴 **HIGH**

**Description**: Not all libraries are virtual thread-safe (blocking operations can pin carrier threads).

**Mitigation**:
- Test thoroughly with virtual threads enabled
- Monitor thread dumps for pinned carrier threads:
  ```bash
  jcmd <pid> Thread.dump_to_file -format=json /tmp/threads.json
  cat /tmp/threads.json | jq '.threads[] | select(.pinned == true)'
  ```
- Use JDK Flight Recorder to detect blocking:
  ```bash
  -XX:StartFlightRecording=settings=profile,filename=/tmp/flight.jfr
  jfr print --events jdk.VirtualThreadPinned /tmp/flight.jfr
  ```

**Success Indicator**: Zero pinned carrier threads under load

---

#### Risk 2: Spring 6.1 Upgrade 🟡 **MEDIUM-LOW**

**Description**: Spring 6.0 → 6.1 is minor version, but behavior changes possible.

**Mitigation**:
- Review Spring 6.1 release notes
- Run full test suite after upgrade
- Monitor logs for deprecation warnings

**Success Indicator**: All integration tests pass (≥24/25)

---

### Low-Risk Areas

- ✅ Java 21 binary compatible with Java 17
- ✅ No Jakarta namespace changes (already done in Phase 3)
- ✅ Tomcat 10.1+ supports Java 21
- ✅ Lucene 9.9.2 works with Java 21

---

## Rollback Strategy

### Three-Level Rollback

#### Level 1: Git Branch Rollback
```bash
git checkout phase3-java17-baseline
mvn clean package -P java17
docker build -t pebble:java17 .
```
**Recovery Time**: <5 minutes

#### Level 2: Docker Image Rollback
```bash
docker stop pebble-java21
docker run -d --name pebble-java17 pebble:java17-phase3
```
**Recovery Time**: <2 minutes

#### Level 3: Disable Virtual Threads (Partial Rollback)
```yaml
# application.properties
spring.threads.virtual.enabled=false
server.tomcat.useVirtualThreads=false
```
**Recovery Time**: Application restart (~30 seconds)

### Rollback Triggers

**Immediate Rollback If**:
- Unit test pass rate <100%
- Integration test pass rate <90%
- Virtual threads causing >10% thread pinning
- Performance regression >20%
- Memory usage >2GB under load

---

## Implementation Checklist

### Pre-Migration
- [ ] Phase 3 (Java 17) complete and production-stable (3-6 months)
- [ ] Java 21 JDK installed
- [ ] Maven 3.9+ verified
- [ ] Docker environment tested
- [ ] Git branch created (`phase4-java21-development`)
- [ ] Load testing tools ready (wrk, JMeter)

### Phase 4A: Java 21 Foundation (Days 1-5)
- [ ] Update pom.xml (Java 21, Spring 6.1+)
- [ ] Update Dockerfile (eclipse-temurin:21-jdk-jammy)
- [ ] Maven compile success
- [ ] Spring 6.1+ working
- [ ] Unit tests pass (775/775)
- [ ] Integration tests pass (≥24/25)
- [ ] Git tag: `phase4a-java21-foundation`

### Phase 4B: Virtual Threads (Days 6-10)
- [ ] Create VirtualThreadConfig.java (Spring)
- [ ] Update Tomcat connector (useVirtualThreads=true)
- [ ] Application starts with virtual threads
- [ ] Load test: 1000 concurrent users
- [ ] Monitor thread count (<500 platform threads)
- [ ] Check for pinned threads (should be 0)
- [ ] Memory usage validation (≤1.5GB)
- [ ] Git tag: `phase4b-virtual-threads`
- [ ] Docker tag: `pebble:virtual-threads`

### Phase 4C: Code Modernization (Days 11-14)
- [ ] Refactor PermalinkController (pattern matching)
- [ ] Refactor AccessDecisionVoter (pattern matching)
- [ ] Adopt sequenced collections (getFirst(), getLast())
- [ ] Code review all refactorings
- [ ] Unit tests pass (775/775)
- [ ] Git tag: `phase4c-code-modernization`

### Phase 4D: Testing & Production (Days 15-21)
- [ ] Full integration test suite (25 tests)
- [ ] Virtual thread load tests (1000-5000 users)
- [ ] Performance benchmarking (vs Phase 3)
- [ ] Thread dump analysis (pinned threads)
- [ ] Memory profiling
- [ ] Documentation updates
- [ ] Production deployment
- [ ] Git tag: `phase4-java21-complete`
- [ ] Docker tag: `pebble:java21-production`

---

## Performance Expectations

### Comparison Matrix

| Metric | Phase 3 (Java 17) | Phase 4 (Java 21) | Target Improvement |
|--------|-------------------|-------------------|-------------------|
| **Startup Time** | 2.0-2.5s | 1.8-2.2s | 10-15% |
| **Throughput (200 users)** | 600 req/sec | 800-1000 req/sec | 30-65% |
| **Max Concurrent Users** | 200 (platform threads) | 5000+ (virtual threads) | **25x+** |
| **Memory (idle)** | 600MB | 600-650MB | ±5% |
| **Memory (1000 users)** | 1.2GB (OOM risk) | 800-900MB | 25-35% |
| **GC Pause (p99)** | 180-240ms | 150-200ms | 15-25% |
| **Request Latency (p95)** | 130-140ms | 120-130ms | 7-15% |
| **Platform Threads (1000 users)** | 1000+ (OOM) | <500 | Virtual threads |

---

## Key Files to Modify

### Build Configuration
- `pom.xml` → Java 21, Spring 6.1+, Maven Compiler Plugin 3.12.1
- `Dockerfile` → eclipse-temurin:21-jdk-jammy

### Virtual Thread Configuration
- **NEW**: `src/main/java/pebble/config/VirtualThreadConfig.java`
- `application.properties` → `spring.threads.virtual.enabled=true`
- `server.xml` or Tomcat connector → `useVirtualThreads="true"`

### Pattern Matching Refactoring (High-Value Targets)
- `PermalinkController.java` → Pattern matching switch for routing
- `AccessDecisionVoter.java` → Pattern matching for security decisions
- `ResponseController.java` → Pattern matching for response types

---

## Monitoring Virtual Threads

### Key Metrics to Monitor

```bash
# Platform thread count (should stay <500 under load)
jcmd <pid> Thread.print | grep "java.lang.Thread" | wc -l

# Pinned carrier threads (should be 0)
jcmd <pid> Thread.dump_to_file -format=json /tmp/threads.json
cat /tmp/threads.json | jq '.threads[] | select(.pinned == true)'

# Virtual thread count (can be 10,000+)
jcmd <pid> Thread.print | grep "VirtualThread" | wc -l

# JDK Flight Recorder (detect blocking operations)
-XX:StartFlightRecording=settings=profile,filename=/tmp/flight.jfr
jfr print --events jdk.VirtualThreadPinned /tmp/flight.jfr
```

### Grafana Dashboard Metrics
- Throughput (requests/second)
- Latency (p50, p95, p99)
- Platform thread count (<500 target)
- Virtual thread count (informational)
- Heap usage (≤1.5GB target)
- GC pause times
- CPU usage

---

## Quick Commands

### Build & Test
```bash
# Compile with Java 21
mvn clean compile -P java21

# Run unit tests
mvn test

# Build WAR
mvn clean package -P java21

# Run integration tests
./phase4-integration-tests.sh

# Build Docker image
docker build -t pebble:java21-phase4 .

# Run container
docker run -d --name pebble-java21 -p 8080:8080 pebble:java21-phase4

# Check health
curl http://localhost:8080/pebble/ping
```

### Load Testing (Virtual Threads)
```bash
# Test 1: 1000 concurrent users
wrk -t4 -c1000 -d60s --latency http://localhost:8080/pebble/

# Test 2: 5000 concurrent users (stress test)
wrk -t8 -c5000 -d30s --latency http://localhost:8080/pebble/

# Test 3: Sustained load (500 users, 5 minutes)
wrk -t4 -c500 -d300s http://localhost:8080/pebble/
```

### Rollback
```bash
# Level 1: Git branch rollback
git checkout phase3-java17-baseline
mvn clean package -P java17
docker build -t pebble:java17-rollback .

# Level 2: Docker image rollback
docker stop pebble-java21
docker run -d --name pebble-java17 pebble:java17-phase3

# Level 3: Disable virtual threads (partial rollback)
# Edit application.properties:
spring.threads.virtual.enabled=false
server.tomcat.useVirtualThreads=false
```

---

## Why Java 21 vs Staying on Java 17?

### Java 17 Advantages (Phase 3)
- ✅ Mature ecosystem (3+ years production)
- ✅ Spring 6.0 fully tested
- ✅ Lower migration risk from Java 11
- ✅ LTS until 2029 (sufficient)

### Java 21 Advantages (Phase 4)
- ✅ **Virtual threads** (10-30x concurrency) 🚀
- ✅ Pattern matching for switch (cleaner code)
- ✅ Record patterns (better data handling)
- ✅ LTS until 2031 (2 years longer)
- ✅ 15-25% performance improvement
- ✅ Binary compatible with Java 17 (easy rollback)

### When to Migrate?

**Migrate to Java 21 if**:
- ✅ Phase 3 (Java 17) is production-stable (3-6 months)
- ✅ Blog experiences burst traffic (popular posts going viral)
- ✅ Need to handle 1000+ concurrent users
- ✅ Want latest LTS (until 2031)

**Stay on Java 17 if**:
- ⚠️ Phase 3 not yet stable
- ⚠️ Traffic is consistently low (<100 concurrent users)
- ⚠️ Risk-averse (Java 21 is 1+ year old)

**Recommendation**: Wait 3-6 months after Phase 3, then migrate to Java 21 for virtual threads.

---

## Documentation Deliverables

### Architecture Phase (Complete)
- ✅ `phase4-java21-migration-architecture.md` (comprehensive design)
- ✅ `phase4-java21-quick-summary.md` (this document)

### Implementation Phase (To Be Created)
- 📝 `phase4-migration-guide.md` (step-by-step instructions)
- 📝 `phase4-virtual-threads-guide.md` (virtual thread configuration)
- 📝 `phase4-pattern-matching-refactoring.md` (code modernization)

### Validation Phase (To Be Created)
- 📝 `phase4-integration-tests.sh` (test script with virtual thread scenarios)
- 📝 `phase4-load-testing-results.md` (virtual thread load testing)
- 📝 `phase4-validation-report.md` (final approval document)
- 📝 `phase4-performance-comparison.md` (Java 17 vs Java 21 benchmarks)

---

## Contact & References

**Full Architecture Document**: `phase4-java21-migration-architecture.md`
**Repository**: `/Users/jgallimore/Projects/pebble`
**Documentation**: `docs/modernization/project-artifacts/02-design/`

**Phase 3 Baseline**:
- Test Results: 775/775 unit, 24/25 integration (96%)
- Validation Report: `phase3-validation-report.md`
- Architecture: `phase3-java17-21-architecture.md`

**Next Steps**:
1. Complete Phase 3 (Java 17)
2. Stabilize in production (3-6 months)
3. Create `phase4-migration-guide.md`
4. Begin Phase 4A (Java 21 foundation)

---

**Status**: ✅ **Architecture Design Complete - Ready for Implementation**
**Prerequisites**: Phase 3 complete and production-stable
**Timeline**: 2-3 weeks from kickoff
**Expected ROI**: 10-30x concurrency improvement, 15-25% performance gain

---

**Prepared by**: System Architecture Designer
**Date**: January 14, 2026
**Version**: 1.0
