# Phase 4: Java 17 to Java 21 LTS Migration - Architecture Design

**Project**: Pebble Blog Modernization
**Phase**: Phase 4 - Java 21 LTS Migration
**Document Type**: Architecture Decision & Strategy
**Author**: System Architecture Designer
**Date**: January 14, 2026
**Status**: Design Phase - Ready for Review
**Prerequisites**: Phase 3 (Java 17) complete and validated

---

## Executive Summary

This document outlines the architectural strategy for migrating Pebble from Java 17 LTS (Phase 3) to Java 21 LTS. Building on the successful Java 17 foundation, this migration focuses on leveraging Java 21's advanced features for performance, concurrency, and developer productivity.

### Key Architectural Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Target Java Version** | Java 21 LTS | Latest LTS, virtual threads, performance improvements |
| **Migration Strategy** | Incremental with validation gates | Minimize risk, leverage Phase 3 patterns |
| **Spring Framework** | 6.1+ (or 6.2+) | Required for virtual thread support |
| **Virtual Threads** | Enabled selectively | High concurrency benefit for blog workload |
| **Pattern Matching** | Adopt where beneficial | Code modernization, readability |
| **Timeline** | 2-3 weeks | Similar to Phase 3, building on learned patterns |

### Performance Expectations

| Metric | Java 17 Baseline | Java 21 Target | Improvement |
|--------|------------------|----------------|-------------|
| **Throughput** | Baseline | +15-25% | Virtual threads, JIT improvements |
| **Concurrent Requests** | 150-200 | 1000-5000+ | Virtual threads (10-30x) |
| **Startup Time** | 2.0-2.5s | 1.8-2.2s | CDS improvements, faster JIT |
| **GC Pause (p99)** | 180-240ms | 150-200ms | ZGC generational improvements |
| **Memory Efficiency** | Baseline | +5-10% | Better string handling, metaspace |

---

## 1. Java 21 vs Java 17 Feature Analysis

### 1.1 Major Java 21 Features for Pebble

#### 1. Virtual Threads (Project Loom) - JEP 444 🔥 **HIGHEST IMPACT**

**Description**: Lightweight threads that dramatically increase concurrent request handling capacity.

**Impact for Pebble Blog**:
- **Traditional threads (Java 17)**: 150-200 concurrent requests (platform thread pool)
- **Virtual threads (Java 21)**: 1000-5000+ concurrent requests (same memory)
- **Use case**: Handling burst traffic from popular blog posts

**Technical Details**:
```java
// Phase 3 (Java 17) - Platform threads
ExecutorService executor = Executors.newFixedThreadPool(200);

// Phase 4 (Java 21) - Virtual threads
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
```

**Spring Integration** (Spring 6.1+):
```yaml
# application.properties
spring.threads.virtual.enabled=true
server.tomcat.threads.max=10000  # Virtual threads scale much higher
```

**Expected Benefit**: **10-30x more concurrent users** with same memory footprint

---

#### 2. Pattern Matching for Switch - JEP 441 🔥 **HIGH IMPACT**

**Description**: Enhanced switch expressions with pattern matching, type patterns, and guards.

**Pebble Use Cases**:
- Request routing (PermalinkController, ResponseController)
- Content type handling (BlogEntry types, Comment processing)
- Security decision making (AccessDecisionVoter, SecurityRealm)

**Example Migration**:
```java
// Phase 3 (Java 17) - Traditional instanceof cascade
public String handleRequest(Request request) {
    if (request instanceof BlogRequest) {
        BlogRequest blogRequest = (BlogRequest) request;
        return processBlogRequest(blogRequest);
    } else if (request instanceof CommentRequest) {
        CommentRequest commentRequest = (CommentRequest) request;
        return processCommentRequest(commentRequest);
    } else if (request instanceof SearchRequest) {
        SearchRequest searchRequest = (SearchRequest) request;
        return processSearchRequest(searchRequest);
    }
    return handleDefault(request);
}

// Phase 4 (Java 21) - Pattern matching switch
public String handleRequest(Request request) {
    return switch (request) {
        case BlogRequest blogReq -> processBlogRequest(blogReq);
        case CommentRequest commentReq -> processCommentRequest(commentReq);
        case SearchRequest searchReq -> processSearchRequest(searchReq);
        default -> handleDefault(request);
    };
}
```

**Expected Benefit**: 10-20% faster request routing, cleaner code

---

#### 3. Record Patterns - JEP 440 🔥 **MEDIUM-HIGH IMPACT**

**Description**: Deconstruct records in pattern matching for cleaner data extraction.

**Pebble Use Cases**:
- DTO pattern matching (BlogEntry data, Comment data)
- Configuration parsing (SecurityConfig, BlogConfig)
- Event handling (BlogEvent, CommentEvent)

**Example**:
```java
// Phase 4 (Java 21) - Record patterns
public record BlogEntry(String title, String body, Author author) {}

public String formatEntry(Object obj) {
    return switch (obj) {
        case BlogEntry(String title, String body, Author(String name)) ->
            "Post: %s by %s\n%s".formatted(title, name, body);
        default -> "Unknown entry";
    };
}
```

**Expected Benefit**: Cleaner data handling, 5-10% less boilerplate

---

#### 4. String Templates (Preview) - JEP 430 🔥 **MEDIUM IMPACT**

**Description**: Type-safe string interpolation with validation.

**Pebble Use Cases**:
- JSP rendering (blog post templates)
- SQL query building (BlogDAO, CommentDAO)
- Log formatting
- HTML generation

**Example**:
```java
// Phase 3 (Java 17) - String concatenation or formatting
String html = "<div class=\"blog-post\">" +
              "<h1>" + entry.getTitle() + "</h1>" +
              "<p>" + entry.getBody() + "</p>" +
              "</div>";

// Phase 4 (Java 21) - String templates (Preview)
String html = STR."""
    <div class="blog-post">
        <h1>\{entry.getTitle()}</h1>
        <p>\{entry.getBody()}</p>
    </div>
    """;
```

**Expected Benefit**: Safer HTML/SQL generation, 10-15% less string bugs

---

#### 5. Generational ZGC - JEP 439 🔥 **LOW-MEDIUM IMPACT**

**Description**: ZGC with generational collection (young/old generation split).

**Performance Characteristics**:
- 50% lower CPU overhead compared to non-generational ZGC
- <5ms pause times (vs 180-240ms G1GC p99)
- Better suited for smaller heaps in Java 21

**Decision for Pebble**: **Evaluate but likely stick with G1GC**
- G1GC is well-tuned for 1GB heap
- ZGC overhead may not justify <5ms pauses for blog workload
- Consider if GC pauses become user-visible issue

**Test Configuration**:
```bash
# Experimental ZGC testing
-XX:+UseZGC
-XX:+ZGenerational  # Generational mode (Java 21+)
-Xms1g
-Xmx1g
```

---

#### 6. Sequenced Collections - JEP 431 🔥 **LOW IMPACT**

**Description**: New collection interfaces (SequencedCollection, SequencedSet, SequencedMap).

**Pebble Use Cases**:
- Blog post ordering (chronological display)
- Comment thread ordering
- Tag/category ordering

**API Improvements**:
```java
// New methods on List, Set, Map
List<BlogEntry> recentPosts = blog.getRecentEntries();
BlogEntry first = recentPosts.getFirst();  // No need for get(0)
BlogEntry last = recentPosts.getLast();    // No need for get(size()-1)
List<BlogEntry> reversed = recentPosts.reversed();  // Efficient reverse view
```

**Expected Benefit**: Cleaner collection APIs, minor performance gains

---

### 1.2 Java 21 Features NOT Applicable to Pebble

| Feature | JEP | Reason Not Applicable |
|---------|-----|----------------------|
| **Foreign Function & Memory API** | 442 | No native library integration needed |
| **Structured Concurrency (Preview)** | 453 | Virtual threads sufficient for blog workload |
| **Scoped Values (Preview)** | 446 | ThreadLocal works fine for current use case |
| **Vector API (Incubator)** | 438 | Limited SIMD benefit for web application |

---

## 2. Current State Analysis (Phase 3 Baseline)

### 2.1 Phase 3 (Java 17) Stack

| Component | Phase 3 Version | Java 21 Compatibility | Upgrade Required |
|-----------|----------------|----------------------|------------------|
| **Java Runtime** | 17 (OpenJDK LTS) | N/A (upgrading) | ✅ Yes |
| **Spring Framework** | 6.0.23 | ⚠️ Requires 6.1+ | ✅ Yes (virtual threads) |
| **Spring Security** | 6.2.1 | ✅ Compatible | ⚠️ Minor update (6.2.2+) |
| **Tomcat** | 10.1.19 | ✅ Compatible | ℹ️ Optional (10.1.28+) |
| **Jakarta Servlet** | 5.0 | ✅ Compatible | ℹ️ Optional (6.0) |
| **Lucene** | 9.9.2 | ✅ Compatible | ℹ️ Optional (9.10+) |
| **JAXB** | Jakarta 3.0/4.0 | ✅ Compatible | No |

### 2.2 Migration Complexity Assessment

#### Low Complexity Changes
- ✅ Java 21 JDK upgrade (binary compatible)
- ✅ Dockerfile base image update
- ✅ Maven compiler version update

#### Medium Complexity Changes
- ⚠️ Spring Framework 6.0 → 6.1+ (virtual thread support)
- ⚠️ Virtual thread configuration (Tomcat + Spring)
- ⚠️ Pattern matching adoption (code refactoring)

#### Optional/Future Changes
- 💡 String templates adoption (preview feature, requires --enable-preview)
- 💡 Record patterns (requires record-based DTOs)
- 💡 ZGC evaluation (performance testing)

---

## 3. Migration Strategy: Phased Approach

### 3.1 Phase Overview

```
Phase 4A: Java 21 Foundation (Week 1)
   ├─ Java 21 JDK compilation
   ├─ Spring 6.1+ upgrade
   └─ Validation (no virtual threads yet)

Phase 4B: Virtual Threads (Week 1-2)
   ├─ Enable virtual threads in Tomcat
   ├─ Spring async executor configuration
   ├─ Load testing and tuning
   └─ Validation gate

Phase 4C: Code Modernization (Week 2)
   ├─ Pattern matching refactoring
   ├─ Record patterns (where applicable)
   ├─ Sequenced collections adoption
   └─ Code review and testing

Phase 4D: Testing & Production (Week 2-3)
   ├─ Comprehensive test suite
   ├─ Performance benchmarking
   ├─ Load testing (virtual threads)
   └─ Production deployment
```

**Total Duration**: 2-3 weeks (similar to Phase 3)

### 3.2 Phase 4A: Java 21 Foundation (Days 1-5)

**Objective**: Establish Java 21 runtime with Spring 6.1+ baseline

#### 4A.1 Java 21 JDK Upgrade

**pom.xml Changes**:
```xml
<properties>
    <!-- Phase 4: Java 21 LTS -->
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <maven.compiler.release>21</maven.compiler.release>

    <!-- Spring 6.1+ required for virtual thread support -->
    <spring.version>6.1.14</spring.version>  <!-- or 6.2.x latest -->
    <spring-security.version>6.2.2</spring-security.version>

    <!-- Plugin versions -->
    <maven-compiler-plugin.version>3.12.1</maven-compiler-plugin.version>
</properties>
```

**Dockerfile Changes**:
```dockerfile
# FROM eclipse-temurin:17-jdk-jammy (Phase 3)
FROM eclipse-temurin:21-jdk-jammy

# Java 21 runtime optimizations
ENV JAVA_OPTS="-Xms512m \
-Xmx1024m \
-XX:MaxMetaspaceSize=256m \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
..."
```

**Success Criteria**:
- ✅ Code compiles with Java 21 (zero errors)
- ✅ Application starts (no virtual threads yet)
- ✅ Unit tests pass (775/775)
- ✅ Integration tests pass (≥24/25)

**Rollback Point**: Git tag `phase4a-java21-foundation`

---

#### 4A.2 Spring Framework 6.1+ Upgrade

**Dependency Update**:
```xml
<!-- Spring 6.0.23 → 6.1.14+ (virtual thread support) -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>6.1.14</version>
</dependency>
```

**Configuration Changes** (minimal):
- Spring 6.1 is backward compatible with 6.0
- No breaking API changes from 6.0 → 6.1
- Virtual thread support is opt-in

**Validation**:
```bash
mvn clean test
# Expected: All tests pass (no virtual threads active yet)
```

---

### 3.3 Phase 4B: Virtual Threads (Days 6-10)

**Objective**: Enable virtual threads for massive concurrency improvement

#### 4B.1 Spring Async Configuration

**Create**: `src/main/java/pebble/config/VirtualThreadConfig.java`
```java
package pebble.config;

import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfig {

    /**
     * Configure Spring async executor to use virtual threads.
     * Replaces default platform thread pool.
     */
    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public AsyncTaskExecutor asyncTaskExecutor() {
        return new TaskExecutorAdapter(
            Executors.newVirtualThreadPerTaskExecutor()
        );
    }
}
```

#### 4B.2 Tomcat Virtual Thread Configuration

**Update**: `src/main/webapp/WEB-INF/web.xml` (or Tomcat server.xml)

**Option 1: Embedded Tomcat Configuration** (if using Spring Boot)
```yaml
# application.properties
spring.threads.virtual.enabled=true
server.tomcat.threads.max=10000  # Virtual threads scale much higher
server.tomcat.threads.min-spare=100
```

**Option 2: Standalone Tomcat Configuration**
```xml
<!-- server.xml connector configuration -->
<Connector port="8080" protocol="HTTP/1.1"
           maxThreads="10000"           <!-- Virtual threads scale higher -->
           minSpareThreads="100"
           useVirtualThreads="true"     <!-- Enable virtual threads -->
           connectionTimeout="20000" />
```

**Note**: Tomcat 10.1.x supports virtual threads via `useVirtualThreads="true"` flag (Java 21+)

#### 4B.3 Load Testing Virtual Threads

**Test Scenario**: Simulate burst traffic (1000 concurrent users)

```bash
# Phase 3 baseline (platform threads)
wrk -t4 -c1000 -d60s http://localhost:8080/pebble/
# Expected: 400-600 req/sec, some connection timeouts

# Phase 4 (virtual threads)
wrk -t4 -c1000 -d60s http://localhost:8080/pebble/
# Expected: 2000-5000 req/sec, zero timeouts
```

**Metrics to Monitor**:
- Thread count (should stay low: 100-200 platform threads)
- Memory usage (should not increase significantly)
- CPU usage (may increase 5-10% under load)
- Request latency (p50, p95, p99)
- Throughput (requests/second)

**Success Criteria**:
- ✅ Application starts with virtual threads
- ✅ Thread count stays <500 under 1000 concurrent requests
- ✅ Memory usage increases <20% vs Phase 3
- ✅ Throughput increases 10-30x (1000+ concurrent users)
- ✅ Zero functional regressions

**Rollback Point**: Git tag `phase4b-virtual-threads` + Docker tag `pebble:virtual-threads`

---

### 3.4 Phase 4C: Code Modernization (Days 11-14)

**Objective**: Adopt Java 21 language features for cleaner, faster code

#### 4C.1 Pattern Matching Refactoring

**High-Value Targets** (Pebble codebase):

1. **PermalinkController.java** - Request routing
```java
// BEFORE (Java 17)
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

// AFTER (Java 21) - Pattern matching switch
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

2. **AccessDecisionVoter.java** - Security decisions
```java
// AFTER (Java 21)
public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
    return switch (object) {
        case FilterInvocation invocation when isPublicResource(invocation) ->
            ACCESS_GRANTED;
        case FilterInvocation invocation when requiresAuthentication(invocation) ->
            authentication.isAuthenticated() ? ACCESS_GRANTED : ACCESS_DENIED;
        default -> ACCESS_ABSTAIN;
    };
}
```

**Refactoring Strategy**:
1. Identify instanceof cascades (search codebase: `if.*instanceof`)
2. Convert to pattern matching switch (Java 21)
3. Test each conversion (unit + integration tests)
4. Code review for correctness

**Expected Impact**: 5-10% less boilerplate, 10-20% faster type checks

---

#### 4C.2 Sequenced Collections Adoption

**Pebble Use Cases**:

```java
// Blog post chronological ordering
List<BlogEntry> recentPosts = blog.getRecentEntries();

// BEFORE (Java 17)
BlogEntry mostRecent = recentPosts.isEmpty() ? null : recentPosts.get(0);
BlogEntry oldest = recentPosts.isEmpty() ? null : recentPosts.get(recentPosts.size() - 1);

// AFTER (Java 21)
BlogEntry mostRecent = recentPosts.getFirst();  // Throws if empty
BlogEntry oldest = recentPosts.getLast();

// Reverse iteration (efficient view, no copying)
for (BlogEntry post : recentPosts.reversed()) {
    // Process in reverse chronological order
}
```

**Expected Impact**: Cleaner collection APIs, minor readability improvement

---

#### 4C.3 Record Patterns (Optional)

**Opportunity**: If Pebble adopts records for DTOs

```java
// Define records for data transfer
public record BlogEntryDTO(String title, String body, AuthorDTO author) {}
public record AuthorDTO(String name, String email) {}

// Pattern matching with records
public String formatBlogPost(Object obj) {
    return switch (obj) {
        case BlogEntryDTO(String title, String body, AuthorDTO(String name, _)) ->
            "Title: %s\nAuthor: %s\nBody: %s".formatted(title, name, body);
        default -> "Unknown format";
    };
}
```

**Decision**: **LOW PRIORITY**
- Requires converting DTOs to records (medium effort)
- Benefit is primarily code clarity (not performance)
- Consider for Phase 5 (future refactoring)

---

### 3.5 Phase 4D: Testing & Validation (Days 15-21)

**Objective**: Comprehensive testing and production readiness

#### 4D.1 Test Suite

**Unit Tests** (775 tests):
```bash
mvn test
# Target: 775/775 pass (100%)
```

**Integration Tests** (25 tests):
```bash
./phase4-integration-tests.sh
# Target: ≥24/25 pass (96%+)
```

**Virtual Thread Load Tests**:
```bash
# Test 1: Burst traffic (1000 concurrent users)
wrk -t4 -c1000 -d60s http://localhost:8080/pebble/

# Test 2: Sustained load (500 users, 5 minutes)
wrk -t4 -c500 -d300s http://localhost:8080/pebble/

# Test 3: Stress test (5000 concurrent users)
wrk -t8 -c5000 -d30s http://localhost:8080/pebble/
```

**Metrics Collection**:
- Thread count (platform threads should stay <500)
- Memory usage (heap, metaspace)
- GC pause times
- Request latency (p50, p95, p99, max)
- Throughput (requests/second)
- Error rate

**Success Criteria**:
- ✅ Unit tests: 100% pass
- ✅ Integration tests: ≥96% pass
- ✅ 1000 concurrent users: throughput >2000 req/sec, zero timeouts
- ✅ 5000 concurrent users: <1% error rate
- ✅ Memory usage: <1.5GB heap under load
- ✅ Platform thread count: <500 (virtual threads handling load)

---

#### 4D.2 Performance Benchmarking

**Comparison Matrix**:

| Metric | Phase 3 (Java 17) | Phase 4 (Java 21) | Target Improvement |
|--------|-------------------|-------------------|-------------------|
| **Startup Time** | 2.0-2.5s | 1.8-2.2s | 10-15% faster |
| **Throughput (200 users)** | 600 req/sec | 800-1000 req/sec | 30-65% |
| **Max Concurrent Users** | 200 (platform threads) | 5000+ (virtual threads) | 25x+ |
| **Memory (idle)** | 600MB | 600-650MB | ±5% |
| **Memory (1000 users)** | 1.2GB (OOM risk) | 800-900MB | 25-35% reduction |
| **GC Pause (p99)** | 180-240ms | 150-200ms | 15-25% |
| **Request Latency (p95)** | 130-140ms | 120-130ms | 7-15% |

**Benchmarking Tools**:
```bash
# Apache Bench
ab -n 10000 -c 500 http://localhost:8080/pebble/

# wrk (better for sustained load)
wrk -t4 -c1000 -d60s --latency http://localhost:8080/pebble/

# JMeter (GUI-based, comprehensive scenarios)
jmeter -n -t pebble-load-test.jmx -l results.jtl
```

---

## 4. Dependency Upgrade Matrix

### 4.1 Required Upgrades

| Dependency | Phase 3 (Java 17) | Phase 4 (Java 21) | Breaking Changes |
|------------|-------------------|-------------------|------------------|
| **Java** | 17 | **21** | None (binary compatible) |
| **Spring Framework** | 6.0.23 | **6.1.14+** | None (6.1 is backward compatible with 6.0) |
| **Spring Security** | 6.2.1 | **6.2.2+** | None (patch version) |
| **Maven Compiler Plugin** | 3.11.0 | **3.12.1** | None |

### 4.2 Optional Upgrades (Recommended)

| Dependency | Phase 3 Version | Phase 4 Version | Benefit |
|------------|----------------|-----------------|---------|
| **Tomcat** | 10.1.19 | **10.1.28+** | Virtual thread support improvements |
| **Lucene** | 9.9.2 | **9.10.0+** | Java 21 performance optimizations |
| **Jakarta Servlet** | 5.0 | **6.0** | Latest servlet spec (optional) |

### 4.3 No Upgrade Required

- JAXB (Jakarta 3.0/4.0) - Already compatible
- Commons libraries - No changes needed
- Lucene 9.9.2 - Works with Java 21 (optional upgrade)

---

## 5. Risk Assessment & Mitigation

### 5.1 High-Risk Areas

#### Risk 1: Virtual Thread Compatibility 🔴 **HIGH**

**Description**: Not all libraries are virtual thread-safe (blocking operations).

**Potential Issues**:
- Synchronized blocks can pin carrier threads
- Thread-local usage may cause memory leaks
- Native calls (JNI) pin carrier threads

**Pebble-Specific Analysis**:
- ✅ Spring 6.1+ is virtual thread-aware
- ✅ Tomcat 10.1+ supports virtual threads
- ✅ JDBC drivers (if used) mostly compatible
- ⚠️ Lucene indexing (file I/O - verify no pinning)
- ⚠️ EhCache (locking - verify compatibility)

**Mitigation Strategy**:
1. **Test thoroughly** with virtual threads enabled
2. **Monitor thread dump** for pinned carrier threads:
   ```bash
   jcmd <pid> Thread.dump_to_file -format=json /tmp/thread-dump.json
   # Look for "pinned" threads
   ```
3. **Use JDK Flight Recorder** to detect blocking operations:
   ```bash
   -XX:StartFlightRecording=settings=profile,filename=/tmp/flight.jfr
   ```
4. **Rollback option**: Disable virtual threads if issues arise

**Success Indicator**: Zero pinned carrier threads under load

---

#### Risk 2: Spring 6.1 Upgrade Compatibility 🟡 **MEDIUM**

**Description**: Spring 6.0 → 6.1 is minor version, but behavior changes possible.

**Mitigation**:
- Review Spring 6.1 release notes
- Run full test suite after upgrade
- Monitor logs for deprecation warnings

**Success Indicator**: All integration tests pass

---

#### Risk 3: Performance Regression (Edge Cases) 🟡 **MEDIUM-LOW**

**Description**: Virtual threads may perform worse for CPU-bound tasks.

**Pebble Workload Analysis**:
- ✅ Blog serving: I/O-bound (database, file system) → **Benefits from virtual threads**
- ✅ Comment processing: I/O-bound → **Benefits**
- ⚠️ Lucene indexing: CPU-bound → **May not benefit** (but won't regress)
- ⚠️ Search queries: CPU-bound → **May not benefit**

**Mitigation**:
- Benchmark both I/O-bound and CPU-bound operations
- Use platform threads for CPU-intensive tasks if needed:
  ```java
  ExecutorService cpuBoundExecutor = Executors.newFixedThreadPool(
      Runtime.getRuntime().availableProcessors()
  );
  ```

**Success Indicator**: No throughput regression for any operation type

---

### 5.2 Medium-Risk Areas

#### Risk 4: Pattern Matching Bugs 🟡 **MEDIUM-LOW**

**Description**: Pattern matching switch has subtle semantics (fall-through, null handling).

**Mitigation**:
- Code review all pattern matching conversions
- Unit test each refactored method
- Use exhaustiveness checking (compiler warnings)

---

#### Risk 5: JVM Flag Compatibility 🟢 **LOW**

**Description**: Some Java 17 flags may be deprecated in Java 21.

**Current Flags to Review**:
```bash
# Check all JAVA_OPTS in Dockerfile
-XX:+UseG1GC                           # ✅ Still valid
-XX:MaxGCPauseMillis=200               # ✅ Still valid
-XX:G1PeriodicGCInterval=900000        # ✅ Still valid
-XX:+UseStringDeduplication            # ✅ Still valid
```

**Validation**:
```bash
java -XX:+PrintFlagsFinal -version | grep <flag-name>
```

---

## 6. Rollback Strategy

### 6.1 Rollback Levels (Same as Phase 3)

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

### 6.2 Rollback Decision Criteria

**Trigger Rollback If**:
- Unit test pass rate <100%
- Integration test pass rate <90%
- Virtual threads causing thread pinning >10% of threads
- Performance regression >20% for any operation
- Memory usage increase >30%
- Critical bugs discovered in virtual thread implementation

**Acceptable Issues (Don't Rollback)**:
- Single integration test failure (matching Phase 3 baseline)
- Minor performance variations (<10%)
- Non-critical warnings during build

---

## 7. Success Criteria

### 7.1 Technical Success Criteria

| Criterion | Target | Validation Method |
|-----------|--------|-------------------|
| **Java Compilation** | Zero errors | `mvn clean compile -P java21` |
| **Unit Tests** | 775/775 pass (100%) | `mvn test` |
| **Integration Tests** | ≥24/25 pass (96%) | `./phase4-integration-tests.sh` |
| **Virtual Thread Scale** | 1000+ concurrent users | `wrk -c1000 -d60s` |
| **Thread Count** | <500 platform threads (1000 users) | JMX monitoring, thread dump |
| **Memory Usage** | ≤1.5GB under load | JMX heap metrics |
| **Throughput** | +15-25% vs Phase 3 | Load testing comparison |
| **Startup Time** | ≤2.5 seconds | Docker logs timestamp |

### 7.2 Functional Success Criteria

- ✅ All Phase 3 functionality works (blog, comments, search, feeds)
- ✅ Authentication/authorization unchanged
- ✅ CSRF protection functional
- ✅ File uploads working
- ✅ Lucene indexing/search working
- ✅ Theme management working

### 7.3 Acceptance Criteria

**Migration Approved If**:
- ✅ All technical success criteria met
- ✅ All functional success criteria validated
- ✅ Virtual threads handle 1000+ concurrent users
- ✅ No functional regressions vs Phase 3
- ✅ Performance equal or better than Phase 3

**Migration Rejected If**:
- ❌ Unit test pass rate <100%
- ❌ Integration test pass rate <90%
- ❌ Virtual threads cause >10% thread pinning
- ❌ Performance regression >20%
- ❌ Memory usage >2GB under load

---

## 8. Architecture Decision Records (ADRs)

### ADR-006: Choose Java 21 LTS over staying on Java 17

**Date**: January 14, 2026

**Status**: Accepted (subject to Phase 3 completion)

**Context**: Phase 3 (Java 17) provides stable foundation. Java 21 LTS offers significant benefits.

**Decision**: Upgrade to Java 21 LTS after Phase 3 stabilizes.

**Rationale**:
1. **Virtual threads**: 10-30x concurrency improvement for blog burst traffic
2. **LTS support**: 8+ years (until 2031), longer than Java 17 (until 2029)
3. **Performance**: 15-25% throughput improvement (JIT, GC, virtual threads)
4. **Language features**: Pattern matching, record patterns, string templates
5. **Binary compatibility**: Java 21 is binary compatible with Java 17
6. **Spring support**: Spring 6.1+ fully supports Java 21 and virtual threads

**Consequences**:
- Positive: Massive concurrency improvement, future-proof LTS, performance gains
- Negative: Newer ecosystem (1+ year old), requires virtual thread testing
- Acceptable: Benefits outweigh risks, rollback to Java 17 available

---

### ADR-007: Enable Virtual Threads for Pebble Blog

**Date**: January 14, 2026

**Status**: Accepted

**Context**: Pebble blog experiences burst traffic when popular posts go viral.

**Decision**: Enable virtual threads via Spring 6.1+ and Tomcat 10.1+ configuration.

**Rationale**:
1. **Burst traffic handling**: Handle 1000-5000 concurrent users (vs 150-200 with platform threads)
2. **Memory efficiency**: Same memory footprint, 10-30x more concurrent requests
3. **I/O-bound workload**: Blog serving is I/O-bound (perfect for virtual threads)
4. **No code changes**: Spring 6.1+ handles virtual threads automatically (opt-in config)

**Consequences**:
- Positive: Handle viral blog post traffic, better user experience
- Negative: Potential thread pinning issues (mitigated by testing)
- Mitigation: Monitor thread dumps, rollback option available

---

### ADR-008: Adopt Pattern Matching Where Beneficial

**Date**: January 14, 2026

**Status**: Accepted

**Context**: Java 21 pattern matching improves code readability and performance.

**Decision**: Refactor instanceof cascades and type checks to pattern matching switch.

**Rationale**:
1. **Performance**: 10-20% faster type checks
2. **Readability**: Cleaner code, less boilerplate
3. **Maintainability**: Type-safe patterns reduce bugs

**Scope**: High-value targets (request routing, security decisions, type handling)

**Consequences**:
- Positive: Cleaner code, performance improvement
- Negative: Code refactoring effort (medium)
- Acceptable: Benefits justify refactoring, incremental approach

---

### ADR-009: Stick with G1GC (Defer ZGC Evaluation)

**Date**: January 14, 2026

**Status**: Accepted

**Context**: Java 21 introduces generational ZGC with lower overhead.

**Decision**: Continue using G1GC for Phase 4, evaluate ZGC later.

**Rationale**:
1. **G1GC is well-tuned**: Optimized for 1GB heap, 180-240ms p99 pauses acceptable
2. **Blog workload**: GC pauses are not user-visible (<300ms)
3. **ZGC overhead**: 5-10% CPU overhead may not justify <5ms pauses
4. **Risk reduction**: Stick with known-good GC configuration

**Future evaluation**: Test ZGC if GC pauses become user-visible issue

**Consequences**:
- Positive: Lower risk, proven GC configuration
- Negative: Missing <5ms pause times (not needed for blog workload)
- Acceptable: G1GC is appropriate for this use case

---

## 9. Implementation Timeline

### 9.1 Week 1: Foundation (Days 1-7)

| Day | Phase | Tasks | Deliverables |
|-----|-------|-------|-------------|
| 1-2 | **4A** | Java 21 JDK upgrade, pom.xml updates | Java 21 compiles |
| 3-4 | **4A** | Spring 6.1+ upgrade, dependency updates | Spring 6.1 working |
| 5 | **4A** | Testing and validation | All tests pass |
| 6-7 | **4B** | Virtual thread configuration (Spring + Tomcat) | Virtual threads enabled |

**Week 1 Rollback Point**: Git tag `phase4a-foundation`, Docker tag `pebble:java21-foundation`

### 9.2 Week 2: Optimization (Days 8-14)

| Day | Phase | Tasks | Deliverables |
|-----|-------|-------|-------------|
| 8-9 | **4B** | Virtual thread load testing, tuning | Load test results |
| 10 | **4B** | Virtual thread validation | 1000+ concurrent users |
| 11-12 | **4C** | Pattern matching refactoring | Cleaner code |
| 13-14 | **4C** | Sequenced collections, code review | Code modernization |

**Week 2 Rollback Point**: Git tag `phase4c-code-modernization`, Docker tag `pebble:java21-modern`

### 9.3 Week 3: Production (Days 15-21)

| Day | Phase | Tasks | Deliverables |
|-----|-------|-------|-------------|
| 15-16 | **4D** | Comprehensive test suite | Test results |
| 17-18 | **4D** | Performance benchmarking | Benchmark report |
| 19 | **4D** | Documentation updates | Architecture docs |
| 20-21 | **4D** | Production deployment, monitoring | Java 21 in production |

**Week 3 Rollback Point**: Git tag `phase4-java21-complete`, Docker tag `pebble:java21-production`

---

## 10. Monitoring & Observability

### 10.1 Virtual Thread Metrics

**Key Metrics**:
```bash
# Platform thread count (should stay <500)
jcmd <pid> Thread.print | grep "java.lang.Thread" | wc -l

# Pinned carrier threads (should be 0)
jcmd <pid> Thread.dump_to_file -format=json /tmp/threads.json
cat /tmp/threads.json | jq '.threads[] | select(.pinned == true)'

# Virtual thread count (can be 10,000+)
jcmd <pid> Thread.print | grep "VirtualThread" | wc -l
```

**JMX Metrics** (via Spring Boot Actuator):
- `jvm.threads.live` - Platform thread count
- `jvm.threads.peak` - Peak platform threads
- `http.server.requests` - Request throughput

**JDK Flight Recorder**:
```bash
# Start recording
-XX:StartFlightRecording=settings=profile,filename=/tmp/flight.jfr

# Analyze pinned threads
jfr print --events jdk.VirtualThreadPinned /tmp/flight.jfr
```

### 10.2 Performance Dashboards

**Grafana Dashboard** (recommended metrics):
- Throughput (requests/second)
- Latency (p50, p95, p99)
- Platform thread count
- Virtual thread count (if exposed)
- Heap usage
- GC pause times
- CPU usage

---

## 11. Documentation Deliverables

### 11.1 Architecture Phase (This Document)
- ✅ `phase4-java21-migration-architecture.md` (comprehensive design)

### 11.2 Implementation Phase (To Be Created)
- 📝 `phase4-migration-guide.md` (step-by-step instructions)
- 📝 `phase4-virtual-threads-guide.md` (virtual thread configuration)
- 📝 `phase4-pattern-matching-refactoring.md` (code modernization guide)

### 11.3 Validation Phase (To Be Created)
- 📝 `phase4-integration-tests.sh` (test script with virtual thread scenarios)
- 📝 `phase4-load-testing-results.md` (virtual thread load testing)
- 📝 `phase4-validation-report.md` (final approval document)
- 📝 `phase4-performance-comparison.md` (Java 17 vs Java 21 benchmarks)

---

## 12. Comparison: Java 17 vs Java 21 Migration

### 12.1 Similarity to Phase 3

**Shared Patterns** (leverage Phase 3 experience):
- ✅ Incremental phased approach
- ✅ Rollback points at each stage
- ✅ Comprehensive testing strategy
- ✅ Maven profile support (java17, java21)
- ✅ Docker multi-architecture support
- ✅ Git branching strategy

**Phase 3 Learnings Applied**:
- Spring 6.x upgrade process (6.0 → 6.1 easier than 5.3 → 6.0)
- Dependency management patterns
- Test validation gates
- Performance benchmarking methodology

### 12.2 Key Differences from Phase 3

| Aspect | Phase 3 (Java 17) | Phase 4 (Java 21) |
|--------|-------------------|-------------------|
| **Complexity** | High (Spring 6, Jakarta namespace) | Medium (Spring 6.1, virtual threads) |
| **Breaking Changes** | Many (javax → jakarta) | Few (Spring 6.0 → 6.1 compatible) |
| **Major Feature** | Spring 6 modernization | Virtual threads (concurrency) |
| **Performance Gain** | 15-30% (GC, JIT) | 15-25% (virtual threads, JIT) |
| **Concurrency Gain** | None (same platform threads) | **10-30x** (virtual threads) |
| **Risk Level** | High (major Spring upgrade) | Medium (virtual thread testing) |
| **Timeline** | 2-3 weeks | 2-3 weeks (similar) |

**Key Insight**: Phase 4 is **less risky** than Phase 3 because:
- No namespace migration (already done in Phase 3)
- Spring 6.1 is backward compatible with 6.0
- Virtual threads are opt-in (can be disabled)
- Binary compatibility with Java 17 (easy rollback)

---

## 13. Post-Migration Optimization Opportunities

### 13.1 Optional Enhancements (Phase 5)

**After Phase 4 stabilizes, consider**:

1. **String Templates** (once finalized):
   - Safer JSP rendering
   - SQL query building
   - HTML generation

2. **Record-based DTOs**:
   - Convert POJOs to records
   - Leverage record patterns
   - Immutability benefits

3. **Generational ZGC**:
   - If GC pauses become visible
   - Test <5ms pause times

4. **Structured Concurrency** (if finalized):
   - Simplify complex async workflows
   - Better error handling

### 13.2 Long-term Roadmap

**2026-2027**:
- Phase 4: Java 21 LTS (virtual threads, pattern matching)
- Stabilize and optimize virtual thread usage
- Monitor virtual thread ecosystem maturity

**2027-2028**:
- Evaluate GraalVM Native Image (if startup time critical)
- Consider Jakarta EE 10+ migration
- Monitor Java 25 LTS (September 2026 release)

**2028+**:
- Long-term support on Java 21 (until 2031)
- Evaluate next LTS (Java 25 or Java 29)

---

## 14. Conclusion

This architecture design provides a comprehensive, risk-mitigated strategy for migrating Pebble from Java 17 to Java 21. The migration builds on Phase 3 successes and focuses on leveraging Java 21's key feature: **virtual threads for massive concurrency improvement**.

**Key Strengths of This Design**:
1. **High Impact**: 10-30x concurrency improvement (virtual threads)
2. **Lower Risk**: Binary compatible with Java 17, easier than Phase 3
3. **Incremental**: Phased approach with validation gates
4. **Testable**: Comprehensive load testing for virtual threads
5. **Reversible**: Fast rollback to Java 17 if issues arise
6. **Future-proof**: Java 21 LTS support until 2031 (8+ years)

**Recommendation**: **PROCEED with Java 21 migration** after Phase 3 stabilizes (3-6 months).

**Critical Success Factor**: Virtual thread load testing (1000-5000 concurrent users) must validate concurrency claims.

---

**Document Status**: ✅ **COMPLETE - Ready for Review**
**Prerequisites**: Phase 3 (Java 17) complete and stable (3-6 months production)
**Architecture Approval**: Pending stakeholder sign-off
**Next Document**: `phase4-migration-guide.md` (implementation instructions)

---

**Prepared by**: System Architecture Designer
**Date**: January 14, 2026
**Version**: 1.0 (Final)
