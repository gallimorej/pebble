# Java Modernization Summary - Pebble

**Quick Reference Guide**

---

## Current State

- **Java Version**: Java 1.6 (released 2006, EOL 2013)
- **19 years behind** current Java versions
- **516 Java source files**
- **Packaging**: WAR file for servlet containers
- **Primary Dependencies**: Spring 3.0.x, Spring Security 3.0.x, Lucene 1.4.1

## Target State

- **Recommended**: **Java 21 LTS** (September 2023, supported until 2031)
- **Conservative Alternative**: Java 17 LTS (September 2021, supported until 2029)
- **Updated Dependencies**: Spring 6.x, modern libraries
- **Modern Build**: Maven 3.9+, updated plugins

---

## Migration Path

### Three-Phase Approach (Recommended)

```
Phase 1: Java 6 → Java 11  (12-18 weeks) - FOUNDATION
    ├─ Update build configuration
    ├─ Add JAXB dependencies (removed from JDK)
    ├─ Update Servlet API (Tomcat 9)
    ├─ Migrate Spring 3 → Spring 5.3
    ├─ Migrate Spring Security 3 → 5.8
    ├─ Replace commons-httpclient
    ├─ Migrate Lucene 1.4 → 9.x
    └─ Testing & validation

Phase 2: Java 11 → Java 17  (4-6 weeks) - STABILIZATION
    ├─ Jakarta EE namespace migration (javax → jakarta)
    ├─ Migrate Spring 5.3 → Spring 6.x
    ├─ Migrate Spring Security 5.8 → 6.x
    ├─ Update Servlet API (Tomcat 10.1)
    └─ Testing & validation

Phase 3: Java 17 → Java 21  (2-3 weeks) - MODERNIZATION
    ├─ Update to Java 21
    ├─ Final dependency updates
    ├─ Adopt Java 21 features
    └─ Testing & validation

Total Duration: 18-27 weeks (4.5-7 months)
```

---

## Critical Challenges

### 🔴 High-Risk Migrations

1. **Spring Framework 3 → 6**
   - Configuration changes (XML → Java config)
   - Major API changes
   - Effort: 2 weeks

2. **Spring Security 3 → 6**
   - Security configuration overhaul
   - Authentication/authorization changes
   - Effort: 1 week

3. **Lucene 1.4 → 9.x**
   - Complete API rewrite
   - Index format incompatible - **requires reindexing**
   - Effort: 2 weeks

4. **Jakarta EE Migration**
   - Namespace change: `javax.*` → `jakarta.*`
   - Affects all servlets, filters, JSPs
   - Effort: 1-2 weeks

5. **Apache HttpClient 3 → 5**
   - Complete API rewrite
   - All HTTP integration code must be updated
   - Effort: 1 week

---

## Dependencies Requiring Updates

### Critical Security Updates

| Library | Current | Target | Risk |
|---------|---------|--------|------|
| Spring Framework | 3.0.7 | 6.1.x | 🔴 Critical |
| Spring Security | 3.0.8 | 6.2.x | 🔴 Critical |
| commons-collections | 3.2.2 | 4.4 | 🔴 Critical |
| commons-httpclient | 3.1 | HttpClient 5.3 | 🔴 Critical |
| Lucene | 1.4.1 | 9.9.x | 🔴 Critical |
| Guava | r07 | 33.0.0-jre | 🔴 Critical |

### Major Version Updates

| Library | Current | Target | Notes |
|---------|---------|--------|-------|
| Servlet API | 3.0 | 6.0 | Jakarta namespace |
| JSP API | 2.2 | 3.1 | Jakarta namespace |
| Tomcat | 7.0.88 | 10.1.x | Jakarta EE 10 |
| JUnit | 4.6 | 5.10.x | New API |
| Mockito | 1.8.4 | 5.9.0 | Compatible |
| ehcache | 2.10.5 | 3.10.8 | Config changes |

### Libraries Requiring Replacement

| Old | New | Reason |
|-----|-----|--------|
| commons-lang 2.6 | commons-lang3 3.14.0 | EOL |
| commons-httpclient 3.1 | HttpClient 5.3 | EOL |
| xmlrpc 1.2-b1 | Apache XML-RPC 3.1.3 | Ancient |
| JTidy 4aug2000r7 | jsoup 1.17.1 | Unmaintained |

---

## What Changes in Each Java Version

### Java 7 (2011)
- ✅ try-with-resources
- ✅ Diamond operator `<>`
- ✅ String in switch
- ✅ Multi-catch exceptions

### Java 8 (2014) - **MAJOR**
- ✅ Lambda expressions
- ✅ Stream API
- ✅ Optional
- ✅ New Date/Time API
- ✅ Default methods in interfaces

### Java 9-10 (2017-2018)
- ✅ Module system
- ✅ Collection factory methods
- ✅ Local variable type inference (var)

### Java 11 (2018) - **LTS**
- ✅ String methods (isBlank, lines, strip)
- ✅ Files.readString() / writeString()
- ✅ HTTP Client API
- ⚠️ **JAXB removed from JDK** (must add dependency)

### Java 17 (2021) - **LTS**
- ✅ Pattern matching for instanceof
- ✅ Text blocks
- ✅ Records
- ✅ Sealed classes
- ✅ Switch expressions

### Java 21 (2023) - **LTS** ⭐
- ✅ Virtual threads
- ✅ Pattern matching for switch
- ✅ Sequenced collections
- ✅ Record patterns

---

## Effort Estimates

### By Phase

| Phase | Duration | Complexity | Team Size |
|-------|----------|------------|-----------|
| Java 6 → 11 | 12-18 weeks | High | 2-3 developers |
| Java 11 → 17 | 4-6 weeks | Medium | 2-3 developers |
| Java 17 → 21 | 2-3 weeks | Low | 2-3 developers |
| **Total** | **18-27 weeks** | **High** | **2-3 developers** |

### By Activity Type

| Activity | Weeks | % of Total |
|----------|-------|-----------|
| Dependency Updates | 3-4 | 15% |
| Spring Migration | 3-4 | 18% |
| Lucene Migration | 2 | 10% |
| Jakarta Migration | 1-2 | 7% |
| Code Fixes | 3-4 | 16% |
| Testing | 5-8 | 30% |
| Documentation | 1-2 | 4% |

---

## Success Criteria

### Must Have ✅

- [ ] Application builds with target Java version
- [ ] All existing tests pass (100%)
- [ ] No high/critical security vulnerabilities
- [ ] All features work identically
- [ ] Performance within 10% (or better)
- [ ] Production deployment successful
- [ ] Rollback plan tested

### Should Have 🎯

- [ ] Code modernization (lambdas, streams)
- [ ] Improved test coverage
- [ ] Updated documentation
- [ ] Team training completed
- [ ] Monitoring/alerting configured

### Nice to Have 💡

- [ ] Performance improvements
- [ ] Code quality improvements
- [ ] Additional test automation
- [ ] CI/CD pipeline enhancements

---

## Quick Decision Matrix

### Choose Java 21 if:
- ✅ Organization supports latest LTS
- ✅ Want longest support timeline (until 2031)
- ✅ Need virtual threads for scalability
- ✅ Want all modern Java features
- ✅ Team comfortable with newer tech

### Choose Java 17 if:
- ✅ Organization prefers proven technology
- ✅ More conservative approach needed
- ✅ Still get 4+ years support
- ✅ Good balance of modern + stable
- ✅ Stepping stone strategy

### Choose Java 11 if:
- ⚠️ Only if absolutely necessary
- ⚠️ Support ends 2026 (< 1 year)
- ⚠️ Missing many modern features
- ⚠️ Not recommended for new migrations

---

## Recommended Approach

**✅ RECOMMENDED: Phased Migration to Java 21**

1. **Phase 1**: Migrate Java 6 → Java 11 (Foundation)
   - Establish stable baseline
   - Major dependency updates
   - Keep javax namespace
   - **Checkpoint**: Can deploy to production

2. **Phase 2**: Migrate Java 11 → Java 17 (Stabilization)
   - Jakarta EE migration
   - Spring 6 upgrade
   - Modern servlet container
   - **Checkpoint**: Modern, stable platform

3. **Phase 3**: Migrate Java 17 → Java 21 (Modernization)
   - Latest LTS features
   - Virtual threads
   - Final polish
   - **Checkpoint**: Future-proof for 8 years

**Duration**: 6-7 months with 2-3 developers

**Risk**: Medium (manageable with phased approach)

**Benefit**: Can stop at any phase if needed

---

## Immediate Next Steps

### Week 1 - Preparation

1. **Environment Setup**
   - [ ] Install JDK 11, 17, 21
   - [ ] Configure development IDE
   - [ ] Set up Git branch strategy

2. **Baseline Documentation**
   - [ ] Run all current tests (record results)
   - [ ] Document current build process
   - [ ] Measure performance benchmarks
   - [ ] Export search indexes

3. **Team Alignment**
   - [ ] Review this plan with stakeholders
   - [ ] Get approval for approach
   - [ ] Identify required training
   - [ ] Assign responsibilities

### Week 2 - Phase 1 Kickoff

1. **Start Java 11 Migration**
   - [ ] Create feature branch
   - [ ] Update pom.xml to Java 11
   - [ ] Add JAXB dependencies
   - [ ] Update maven-compiler-plugin

2. **Dependency Analysis**
   - [ ] Run dependency:tree
   - [ ] Identify conflicts
   - [ ] Plan update order

---

## Key Resources

- **Full Analysis**: `java-modernization-analysis.md`
- **Spring Migration Guide**: https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-6.x
- **Jakarta EE Migration**: https://eclipse-ee4j.github.io/jakartaee-platform/
- **Java 21 Features**: https://openjdk.org/projects/jdk/21/

---

## Contact & Support

- **Technical Lead**: [To be assigned]
- **Architecture Review**: [To be assigned]
- **Project Manager**: [To be assigned]

---

**Document Status**: Draft for Review
**Last Updated**: November 7, 2025
**Version**: 1.0
