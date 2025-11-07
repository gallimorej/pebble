# Java Modernization Roadmap - Visual Guide

**Pebble Java 6 → Java 21 Migration**

---

## Migration Timeline Overview

```
START                                                                    END
  │                                                                        │
  ├─────────────────┬──────────────┬────────┤
  │                 │              │        │
Java 6          Java 11        Java 17   Java 21
(2006)          (2018)         (2021)    (2023)
19 years old    7 years old    4 years   Current LTS
EOL 2013        EOL 2026       EOL 2029  EOL 2031

  Phase 1         Phase 2       Phase 3
  12-18 wks       4-6 wks       2-3 wks
  =========       =======       =====
  FOUNDATION      STABILIZE     MODERNIZE
```

---

## Three-Phase Migration Path

### Phase 1: Foundation (Java 6 → 11)
**Goal**: Establish modern, stable baseline
**Duration**: 12-18 weeks
**Risk**: 🔴 HIGH

```
┌─────────────────────────────────────────────────────────────────┐
│                    PHASE 1: FOUNDATION                          │
│                    Java 6 → Java 11                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Week 1-2: Environment & Build Setup                           │
│  ├─ Install JDK 11                                            │
│  ├─ Update pom.xml (source/target = 11)                       │
│  ├─ Update maven-compiler-plugin to 3.11.0                    │
│  └─ Add JAXB dependencies (removed from JDK)                  │
│                                                                 │
│  Week 3-4: Core Dependencies                                   │
│  ├─ Update Servlet API (javax namespace, Tomcat 9)            │
│  ├─ Update commons-lang 2.6 → commons-lang3 3.14.0           │
│  ├─ Update commons-collections 3.2.2 → 4.4                    │
│  └─ Update Guava r07 → 33.0.0                                │
│                                                                 │
│  Week 5-8: Major Framework Migrations                          │
│  ├─ Spring 3.0.7 → Spring 5.3.31 ⚠️ CRITICAL                 │
│  ├─ Spring Security 3.0.8 → 5.8.8 ⚠️ CRITICAL                │
│  ├─ Update Spring configuration (XML → Java config)           │
│  └─ Update security configuration                             │
│                                                                 │
│  Week 9-10: External Dependencies                              │
│  ├─ commons-httpclient 3.1 → HttpClient 5.3 ⚠️ HIGH          │
│  ├─ Lucene 1.4.1 → 9.9.x ⚠️ CRITICAL                         │
│  ├─ ehcache 2.10.5 → 3.10.8                                   │
│  └─ JUnit 4.6 → JUnit 5.10.x                                  │
│                                                                 │
│  Week 11-14: Code Fixes & Compilation                          │
│  ├─ Fix all compilation errors                                │
│  ├─ Replace deprecated API calls                              │
│  ├─ Update JAXB usage                                         │
│  └─ Update test code for JUnit 5                             │
│                                                                 │
│  Week 15-18: Testing & Validation                              │
│  ├─ Run all unit tests                                        │
│  ├─ Run integration tests                                     │
│  ├─ Manual testing of key features                            │
│  ├─ Performance testing                                       │
│  ├─ Security testing                                          │
│  └─ Deploy to test environment (Tomcat 9)                     │
│                                                                 │
│  ✅ Milestone: Production-ready Java 11 application            │
│     - Can stay here if needed (EOL 2026)                       │
└─────────────────────────────────────────────────────────────────┘
```

### Phase 2: Stabilization (Java 11 → 17)
**Goal**: Jakarta EE migration, Spring 6
**Duration**: 4-6 weeks
**Risk**: 🟠 MEDIUM

```
┌─────────────────────────────────────────────────────────────────┐
│                   PHASE 2: STABILIZATION                        │
│                   Java 11 → Java 17                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Week 1: Build Configuration                                    │
│  ├─ Update pom.xml (source/target = 17)                       │
│  └─ Update to Java 17 JDK                                     │
│                                                                 │
│  Week 2-3: Jakarta EE Migration ⚠️ HIGH RISK                  │
│  ├─ javax.servlet.* → jakarta.servlet.*                       │
│  ├─ javax.servlet.http.* → jakarta.servlet.http.*             │
│  ├─ javax.servlet.jsp.* → jakarta.servlet.jsp.*               │
│  ├─ javax.xml.bind.* → jakarta.xml.bind.*                     │
│  ├─ javax.annotation.* → jakarta.annotation.*                 │
│  ├─ Update all JSP files                                      │
│  └─ Update web.xml and Spring configs                         │
│                                                                 │
│  Week 3-4: Framework Updates                                   │
│  ├─ Spring 5.3.31 → Spring 6.1.x ⚠️ CRITICAL                 │
│  ├─ Spring Security 5.8.8 → 6.2.x ⚠️ CRITICAL                │
│  ├─ Update Servlet API to Jakarta 5.0+                        │
│  └─ Migrate to Tomcat 10.1.x                                  │
│                                                                 │
│  Week 5-6: Testing & Validation                                │
│  ├─ Run all tests                                             │
│  ├─ Test all servlets and filters                            │
│  ├─ Verify Spring Security authentication                     │
│  ├─ Validate all JSP pages                                   │
│  └─ Deploy to test environment (Tomcat 10.1)                  │
│                                                                 │
│  ✅ Milestone: Production-ready Java 17 application            │
│     - Modern platform with 4+ years support                    │
└─────────────────────────────────────────────────────────────────┘
```

### Phase 3: Modernization (Java 17 → 21)
**Goal**: Latest LTS, modern features
**Duration**: 2-3 weeks
**Risk**: 🟢 LOW

```
┌─────────────────────────────────────────────────────────────────┐
│                   PHASE 3: MODERNIZATION                        │
│                   Java 17 → Java 21                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Week 1: Configuration & Dependencies                           │
│  ├─ Update pom.xml (source/target = 21)                       │
│  ├─ Update to Java 21 JDK                                     │
│  ├─ Update Spring to 6.1.x (latest)                           │
│  ├─ Update all dependencies to latest                         │
│  └─ Update Tomcat to 10.1.x (latest)                          │
│                                                                 │
│  Week 2-3: Testing & Feature Adoption                          │
│  ├─ Run all tests                                             │
│  ├─ Verify on Tomcat 10.1.x                                   │
│  ├─ Performance testing                                       │
│  ├─ Consider virtual threads for async tasks                  │
│  ├─ Adopt pattern matching for switch                         │
│  └─ Deploy to production                                       │
│                                                                 │
│  ✅ Milestone: Modern Java 21 LTS application                  │
│     - Supported until 2031 (8 years!)                          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Dependency Migration Map

### Spring Framework Evolution

```
Phase 1                    Phase 2                    Phase 3
───────────────────────────────────────────────────────────────

Spring 3.0.7               Spring 5.3.31              Spring 6.1.x
(2011)                     (2023)                     (2024)
Java 5+                    Java 8+                    Java 17+
javax namespace            javax namespace            jakarta namespace
    │                          │                          │
    │                          │                          │
    ▼                          ▼                          ▼
┌──────────┐              ┌──────────┐              ┌──────────┐
│  XML     │              │ Java     │              │ Java     │
│  Config  │────────────> │ Config   │────────────> │ Config   │
│          │              │ @Enable  │              │ @Enable  │
└──────────┘              └──────────┘              └──────────┘

EOL: 2016                 EOL: 2024                  Active
```

### Servlet API Evolution

```
Phase 1                    Phase 2
──────────────────────────────────────────────

Servlet 3.0                Servlet 5.0/6.0
(Tomcat 7.0.88)           (Tomcat 10.1.x)
javax.servlet.*            jakarta.servlet.*
    │                          │
    ▼                          ▼
┌──────────┐              ┌──────────┐
│ javax.*  │              │jakarta.* │
│ namespace│────────────> │namespace │
│ Tomcat 9 │              │Tomcat 10 │
└──────────┘              └──────────┘

Java 6+                    Java 11+
```

### Lucene Migration Path

```
Phase 1
───────────────────────────────────────

Lucene 1.4.1               Lucene 9.9.x
(2004)                     (2024)
    │                          │
    ▼                          ▼
┌──────────┐              ┌──────────┐
│Old Index │              │New Index │
│Format    │   REINDEX    │Format    │
│          │──────────────│          │
└──────────┘              └──────────┘
   │                            │
   └─────────────┬──────────────┘
                 │
                 ▼
         ⚠️ NOT COMPATIBLE
         Must rebuild all indexes
```

---

## Critical Path Analysis

### Blocking Dependencies

```
┌───────────────────────────────────────────────────────────────┐
│                    CRITICAL PATH                              │
│                 (Must be done in order)                       │
└───────────────────────────────────────────────────────────────┘

1. Build Configuration
   └─> 2. JAXB Dependencies (Java 11+ requirement)
       └─> 3. Spring Framework Migration
           └─> 4. Spring Security Migration
               └─> 5. Servlet API Update
                   └─> 6. Jakarta Namespace Migration
                       └─> 7. Final Testing

Parallel Tasks (can be done independently):
├─> Lucene Migration
├─> HTTP Client Migration
├─> Commons Library Updates
└─> Test Framework Migration
```

---

## Risk Heat Map

### Phase 1 Risks

```
┌─────────────────────────┬──────────┬─────────┬──────────┐
│ Activity                │ Risk     │ Impact  │ Priority │
├─────────────────────────┼──────────┼─────────┼──────────┤
│ Spring 3→5 Migration    │   🔴     │   🔴    │    P0    │
│ Spring Security 3→5     │   🔴     │   🔴    │    P0    │
│ Lucene 1.4→9.x          │   🔴     │   🟠    │    P1    │
│ HTTP Client Migration   │   🟠     │   🟠    │    P1    │
│ JAXB Configuration      │   🟠     │   🟠    │    P0    │
│ Build System Update     │   🟢     │   🔴    │    P0    │
│ Test Migration          │   🟠     │   🟠    │    P2    │
└─────────────────────────┴──────────┴─────────┴──────────┘

Legend:
🔴 High   🟠 Medium   🟢 Low
P0: Critical Path
P1: High Priority
P2: Medium Priority
```

### Phase 2 Risks

```
┌─────────────────────────┬──────────┬─────────┬──────────┐
│ Activity                │ Risk     │ Impact  │ Priority │
├─────────────────────────┼──────────┼─────────┼──────────┤
│ Jakarta Migration       │   🟠     │   🔴    │    P0    │
│ Spring 5→6 Migration    │   🟠     │   🔴    │    P0    │
│ Tomcat 9→10 Migration   │   🟠     │   🟠    │    P0    │
│ JSP Updates             │   🟢     │   🟠    │    P1    │
└─────────────────────────┴──────────┴─────────┴──────────┘
```

---

## Testing Strategy by Phase

### Phase 1 Testing Pyramid

```
                    ▲
                   ╱│╲
                  ╱ │ ╲
                 ╱  │  ╲
                ╱   │   ╲
               ╱ E2E│    ╲        E2E Tests
              ╱─────┼─────╲       - Key user flows
             ╱      │      ╲      - Critical features
            ╱───────┼───────╲     Effort: 2 weeks
           ╱        │        ╲
          ╱  Integr.│         ╲   Integration Tests
         ╱──────────┼──────────╲  - Spring context
        ╱           │           ╲ - Database operations
       ╱────────────┼────────────╲- External services
      ╱             │             ╲Effort: 1 week
     ╱              │              ╲
    ╱       Unit    │               ╲Unit Tests
   ╱────────────────┼────────────────╲- All classes
  ╱─────────────────┼─────────────────╲- All methods
 ╱                  │                  ╲Effort: 2 weeks
╱___________________│___________________╲
                    │
              Total: 5 weeks testing
```

### Test Coverage Goals

```
Phase 1: Java 6 → 11
├─ Maintain existing coverage: 100% of current tests passing
├─ Add Spring 5 integration tests
├─ Add HTTP client integration tests
└─ Add JAXB marshalling tests

Phase 2: Java 11 → 17
├─ Maintain Phase 1 coverage
├─ Add Jakarta namespace validation tests
└─ Add Spring 6 security tests

Phase 3: Java 17 → 21
├─ Maintain Phase 2 coverage
└─ Add performance benchmarks
```

---

## Team Structure & Responsibilities

```
┌────────────────────────────────────────────────────────┐
│                   PROJECT TEAM                         │
├────────────────────────────────────────────────────────┤
│                                                        │
│  Tech Lead (1)                                         │
│  ├─ Overall architecture decisions                    │
│  ├─ Code review and approval                          │
│  ├─ Risk management                                   │
│  └─ Stakeholder communication                         │
│                                                        │
│  Senior Developer (1)                                  │
│  ├─ Spring Framework migration                        │
│  ├─ Spring Security migration                         │
│  └─ Jakarta EE migration                              │
│                                                        │
│  Developer (1-2)                                       │
│  ├─ Dependency updates                                │
│  ├─ Code fixes and updates                            │
│  ├─ Test migration                                    │
│  └─ Documentation                                     │
│                                                        │
│  QA Engineer (0.5-1)                                   │
│  ├─ Test planning                                     │
│  ├─ Test execution                                    │
│  ├─ Bug tracking                                      │
│  └─ Regression testing                                │
│                                                        │
│  DevOps (0.25-0.5)                                     │
│  ├─ CI/CD updates                                     │
│  ├─ Environment setup                                 │
│  └─ Deployment support                                │
│                                                        │
└────────────────────────────────────────────────────────┘

Total Team: 2.75 - 4.5 FTE
```

---

## Milestone Checklist

### Phase 1 Completion Gate

```
┌─ Technical Criteria ───────────────────────────────────┐
│ [ ] Application builds with Java 11                   │
│ [ ] All unit tests pass (100%)                        │
│ [ ] All integration tests pass                        │
│ [ ] Spring 5 fully integrated                         │
│ [ ] Spring Security 5 functional                      │
│ [ ] Lucene 9 indexing/searching works                 │
│ [ ] HTTP client integrations work                     │
│ [ ] Deploys successfully to Tomcat 9                  │
│ [ ] No critical security vulnerabilities              │
│ [ ] Performance within 10% of baseline                │
└────────────────────────────────────────────────────────┘

┌─ Business Criteria ────────────────────────────────────┐
│ [ ] All key features tested and working               │
│ [ ] User acceptance testing passed                    │
│ [ ] Documentation updated                             │
│ [ ] Rollback plan tested                              │
│ [ ] Stakeholder sign-off obtained                     │
└────────────────────────────────────────────────────────┘

✅ GO / 🛑 NO-GO for Phase 2
```

### Phase 2 Completion Gate

```
┌─ Technical Criteria ───────────────────────────────────┐
│ [ ] Application builds with Java 17                   │
│ [ ] Jakarta namespace migration complete              │
│ [ ] Spring 6 fully integrated                         │
│ [ ] Spring Security 6 functional                      │
│ [ ] All servlets/filters migrated                     │
│ [ ] All JSPs render correctly                         │
│ [ ] Deploys successfully to Tomcat 10.1               │
│ [ ] All tests pass                                    │
│ [ ] Performance benchmarks met                        │
└────────────────────────────────────────────────────────┘

✅ GO / 🛑 NO-GO for Phase 3
```

### Phase 3 Completion Gate

```
┌─ Technical Criteria ───────────────────────────────────┐
│ [ ] Application builds with Java 21                   │
│ [ ] All tests pass                                    │
│ [ ] Performance benchmarks met/improved               │
│ [ ] Production deployment successful                  │
│ [ ] Monitoring/alerting configured                    │
└────────────────────────────────────────────────────────┘

✅ PRODUCTION RELEASE
```

---

## Rollback Strategy

```
┌────────────────────────────────────────────────────────┐
│              ROLLBACK DECISION TREE                    │
└────────────────────────────────────────────────────────┘

Issue Discovered in Phase 1?
    │
    ├─> Minor (< 2 days to fix)
    │   └─> FIX FORWARD
    │
    ├─> Major (2-5 days to fix)
    │   └─> ROLLBACK to Java 6
    │       - Restore from backup
    │       - Re-deploy previous version
    │       - Investigate issue
    │       - Plan fix, re-attempt
    │
    └─> Critical (> 5 days or data loss)
        └─> ROLLBACK + PAUSE
            - Full investigation
            - Risk re-assessment
            - Stakeholder review
            - Revised plan required

Each phase has Git tags for easy rollback:
├─ tag: java-6-baseline
├─ tag: java-11-complete
├─ tag: java-17-complete
└─ tag: java-21-complete
```

---

## Communication Plan

### Status Reporting Frequency

```
Phase 1 (Foundation) - Weekly Updates
├─ Technical progress
├─ Blockers and risks
├─ Upcoming work
└─ Timeline adherence

Phase 2 (Stabilization) - Weekly Updates
├─ Migration progress
├─ Test results
├─ Issues encountered
└─ Go-live readiness

Phase 3 (Modernization) - Bi-weekly Updates
├─ Final status
├─ Production readiness
└─ Lessons learned
```

### Stakeholder Touchpoints

```
Week 0:   Project Kickoff
Week 4:   Phase 1 Progress Review
Week 8:   Phase 1 Mid-point Review
Week 12:  Phase 1 Completion Review
Week 16:  Phase 2 Completion Review
Week 18:  Phase 3 Completion / Go-Live
Week 22:  Post-Production Review
```

---

## Success Metrics Dashboard

```
┌─────────────────────────────────────────────────────────┐
│              KEY PERFORMANCE INDICATORS                 │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Build Time                                             │
│  ├─ Baseline: [__________] 5 min                       │
│  └─ Target:   [_______] ≤ 5 min                        │
│                                                         │
│  Test Execution Time                                    │
│  ├─ Baseline: [_______________] 10 min                 │
│  └─ Target:   [____________] ≤ 10 min                  │
│                                                         │
│  Application Startup                                    │
│  ├─ Baseline: [__________] 30 sec                      │
│  └─ Target:   [________] ≤ 30 sec                      │
│                                                         │
│  Memory Usage                                           │
│  ├─ Baseline: [____________] 512 MB                    │
│  └─ Target:   [___________] ≤ 512 MB                   │
│                                                         │
│  Test Coverage                                          │
│  ├─ Baseline: [_________] 60%                          │
│  └─ Target:   [_________] ≥ 60%                        │
│                                                         │
│  Security Vulnerabilities                               │
│  ├─ Baseline: [████████████] 50+                       │
│  └─ Target:   [__] 0 Critical/High                     │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Quick Reference: What's Changing

### Package Names

```
BEFORE (javax)              AFTER (jakarta)
──────────────────────────────────────────────
javax.servlet.*         →   jakarta.servlet.*
javax.servlet.http.*    →   jakarta.servlet.http.*
javax.servlet.jsp.*     →   jakarta.servlet.jsp.*
javax.xml.bind.*        →   jakarta.xml.bind.*
javax.annotation.*      →   jakarta.annotation.*
javax.inject.*          →   jakarta.inject.*
```

### Server Versions

```
BEFORE                      AFTER
──────────────────────────────────────────────
Tomcat 7.0.88           →   Tomcat 10.1.x
Servlet 3.0             →   Servlet 6.0
JSP 2.2                 →   JSP 3.1
Java 6                  →   Java 21
```

### Build Configuration

```
BEFORE (pom.xml)            AFTER (pom.xml)
──────────────────────────────────────────────
<source>1.6</source>    →   <release>21</release>
<target>1.6</target>    →   (release replaces both)
```

---

**End of Roadmap**

*This visual guide provides a comprehensive overview of the Java modernization journey. For detailed technical information, refer to `java-modernization-analysis.md`.*
