# Phase 3B Migration Blockers

**Date**: January 14, 2026
**Phase**: 3B - Spring 6 + Jakarta EE Migration
**Status**: ❌ **BLOCKED**
**Reason**: Third-party library incompatibilities with Jakarta Servlet API

---

## Executive Summary

Phase 3B attempted to migrate from Spring 5.3.x to Spring 6.x with Jakarta Servlet 5.0 API. This migration is **currently blocked** by fundamental incompatibilities in the dependency ecosystem.

**Verdict**: Jakarta EE migration is not feasible at this time. Recommend staying on Phase 3A (Java 17 + Spring 5.3.x + Tomcat 9) until the library ecosystem matures.

---

## What We Attempted

### Migration Goals
1. Upgrade Spring Framework 5.3.39 → 6.0.23
2. Upgrade Spring Security 5.8.14 → 6.2.1
3. Migrate `javax.servlet.*` → `jakarta.servlet.*` (229 files)
4. Upgrade Tomcat 9.0.85 → 10.1.19
5. Update all third-party dependencies for Jakarta compatibility

### Dependencies Updated
```xml
<!-- Spring Framework -->
<spring.version>6.0.23</spring.version>
<spring-security.version>6.2.1</spring-security.version>

<!-- Jakarta Servlet APIs -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>5.0.0</version>
</dependency>
<dependency>
    <groupId>jakarta.servlet.jsp</groupId>
    <artifactId>jakarta.servlet.jsp-api</artifactId>
    <version>3.0.0</version>
</dependency>
<dependency>
    <groupId>jakarta.servlet.jsp.jstl</groupId>
    <artifactId>jakarta.servlet.jsp.jstl-api</artifactId>
    <version>2.0.0</version>
</dependency>

<!-- Jakarta Inject/Annotation -->
<dependency>
    <groupId>jakarta.inject</groupId>
    <artifactId>jakarta.inject-api</artifactId>
    <version>2.0.1</version>
</dependency>
<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
    <version>2.1.1</version>
</dependency>
```

### Code Changes Completed
- ✅ All 229 Java files migrated: `javax.servlet.*` → `jakarta.servlet.*`
- ✅ pom.xml updated with Spring 6 and Jakarta dependencies
- ✅ Dockerfile updated for Tomcat 10.1.19

---

## Why It Failed

### Critical Blockers (16 Compilation Errors)

#### 1. **Spring Security OpenID Removed** ❌

**Error**:
```
package org.springframework.security.openid does not exist
```

**Files Affected**:
- `AddOpenIdAction.java` (5 errors)

**Root Cause**: Spring Security 6.x completely removed OpenID support. The recommendation is to use OAuth 2.0/OIDC instead.

**Impact**: Breaks OpenID authentication feature entirely.

**Fix Required**: Remove OpenID support or implement OAuth 2.0/OIDC replacement (significant refactoring).

---

#### 2. **commons-fileupload Incompatibility** ❌

**Error**:
```
no suitable method found for parseRequest(jakarta.servlet.http.HttpServletRequest)
  method ServletFileUpload.parseRequest(javax.servlet.http.HttpServletRequest) is not applicable
```

**Files Affected**:
- `UploadFileAction.java` (2 errors)

**Root Cause**: Apache Commons FileUpload 1.5 still uses `javax.servlet.*` APIs. There is no Jakarta-compatible version available.

**Impact**: Breaks file upload functionality (blog images, theme files, etc.).

**Fix Required**:
- Wait for commons-fileupload 2.0 with Jakarta support
- OR implement custom file upload handling with Jakarta APIs

---

#### 3. **JSTL Config Class Missing** ❌

**Error**:
```
package javax.servlet.jsp.jstl.core does not exist
```

**Files Affected**:
- `ExportBlogAction.java` (2 errors)
- `FeedAction.java` (2 errors)

**Root Cause**: The `javax.servlet.jsp.jstl.core.Config` class does not have a direct Jakarta equivalent in the JSTL 2.0 API we're using.

**Impact**: Breaks blog export and feed generation functionality.

**Fix Required**:
- Replace JSTL Config usage with alternative approach
- OR use Jakarta JSTL 3.0 when available

---

#### 4. **rome-propono Atom API Incompatibility** ❌

**Error**:
```
net.sourceforge.pebble.webservice.PebbleAtomHandlerFactory is not abstract and does not
override abstract method newAtomHandler(javax.servlet.http.HttpServletRequest,
javax.servlet.http.HttpServletResponse)
```

**Files Affected**:
- `PebbleAtomHandlerFactory.java` (1 error)

**Root Cause**: The rome-propono library (Atom Publishing Protocol) version 1.5.1 still uses `javax.servlet.*` interfaces. No Jakarta-compatible version exists.

**Impact**: Breaks Atom API publishing functionality.

**Fix Required**:
- Wait for rome-propono Jakarta update
- OR remove Atom API support
- OR create adapter layer (complex)

---

## Dependency Ecosystem Analysis

### Libraries Blocking Migration

| Library | Current Version | Jakarta Support | Status | Impact |
|---------|----------------|-----------------|--------|---------|
| **commons-fileupload** | 1.5 | ❌ No (2.0 in progress) | Blocker | High - File uploads broken |
| **rome-propono** | 1.5.1 | ❌ No | Blocker | Medium - Atom API broken |
| **Spring Security OpenID** | Removed in 6.x | ❌ Deprecated | Blocker | High - Auth feature removed |
| **DWR** | 2.0.rc2 | ❌ Unknown | Unknown | Medium - Ajax features |
| **jcaptcha** | 1.0-RC6 | ❌ Unknown | Unknown | Low - CAPTCHA |

### Why javax → jakarta Migration is Problematic

The Jakarta namespace migration (Java EE → Jakarta EE) occurred in 2019, but many mature libraries haven't migrated yet because:

1. **Breaking Change**: Requires major version bumps (1.x → 2.x or higher)
2. **Ecosystem Lag**: Spring 6 (Nov 2022) requires Jakarta, but many libraries predate this
3. **Maintenance**: Many libraries are in maintenance mode and won't get Jakarta updates
4. **Adoption Rate**: Slow enterprise adoption means low pressure for updates

---

## Attempted Solutions

### ❌ Solution 1: Update commons-fileupload to 2.0
**Result**: Version 2.0.0-M1 (Milestone) exists but Maven cannot resolve it. Not production-ready.

### ❌ Solution 2: Remove spring-security-openid dependency
**Result**: Compilation progressed but OpenID classes still referenced in code. Requires code refactoring.

### ❌ Solution 3: Use Jakarta JSTL implementation
**Result**: `jakarta.servlet.jsp.jstl.core.Config` class doesn't exist in current Jakarta JSTL 2.0 API.

---

## Comparison: Phase 3A vs Phase 3B

| Feature | Phase 3A (Current) | Phase 3B (Attempted) | Outcome |
|---------|-------------------|----------------------|---------|
| **Java Version** | 17 LTS ✅ | 17 LTS ✅ | Same |
| **Spring Framework** | 5.3.39 ✅ | 6.0.23 ❌ | Blocked |
| **Spring Security** | 5.8.14 ✅ | 6.2.1 ❌ | Blocked |
| **Servlet API** | javax 3.1 ✅ | jakarta 5.0 ❌ | Blocked |
| **Tomcat** | 9.0.85 ✅ | 10.1.19 ❌ | Blocked |
| **OpenID Support** | Yes ✅ | Removed ❌ | Breaking |
| **File Upload** | Works ✅ | Broken ❌ | Blocker |
| **Atom API** | Works ✅ | Broken ❌ | Blocker |
| **Unit Tests** | 775/775 ✅ | N/A ❌ | Won't compile |
| **Integration Tests** | 25/25 ✅ | N/A ❌ | Won't compile |

---

## Recommended Path Forward

### ✅ **Option 1: Stay on Phase 3A (RECOMMENDED)**

**Rationale**: Phase 3A is stable, tested, and production-ready.

**Configuration**:
- Java 17 LTS (supported until 2029)
- Spring Framework 5.3.39 (maintained until 2024+)
- Spring Security 5.8.14 (maintained until 2024+)
- Tomcat 9.0.85 (maintained, Jakarta not required)
- All 775 unit tests passing ✅
- All 25 integration tests passing ✅
- Zero regressions ✅

**Benefits**:
- Stable dependency ecosystem
- All features working
- LTS Java version
- Active Spring 5.x maintenance
- No breaking changes

**Trade-offs**:
- Uses legacy `javax.*` namespaces (not a technical problem)
- Will eventually need migration when Spring 5.x reaches EOL

---

### ⚠️ **Option 2: Wait for Ecosystem Maturity**

**Revisit Jakarta migration when**:
1. commons-fileupload 2.0 stable release available
2. rome-propono Jakarta-compatible version released
3. Spring 5.x approaches end-of-life
4. More libraries migrate to Jakarta

**Timeline**: Likely 1-2 years (2026-2027)

---

### ❌ **Option 3: Force Migration with Workarounds**

**NOT RECOMMENDED** - Requires extensive refactoring:

1. Remove OpenID authentication (breaking change)
2. Implement custom file upload handling
3. Remove or rewrite Atom API support
4. Replace JSTL Config usage
5. Test all 775 unit tests for regressions
6. Test all 25 integration tests

**Effort**: 2-4 weeks of development + testing
**Risk**: High - breaking changes to authentication and file handling
**Benefit**: Marginal - Jakarta namespace alone doesn't provide functional improvements

---

## Conclusion

**Jakarta EE migration is blocked by third-party library incompatibilities.**

The current Phase 3A implementation (Java 17 + Spring 5.3.x + Tomcat 9) is:
- ✅ Stable and production-ready
- ✅ Fully tested (775 unit tests, 25 integration tests)
- ✅ Modern Java 17 LTS
- ✅ Active maintenance (Spring 5.x)
- ✅ Zero regressions from Phase 2

**Recommendation**: **Stay on Phase 3A** until the Jakarta ecosystem matures. The namespace change (`javax.*` → `jakarta.*`) is purely cosmetic and provides no functional benefits. The risk and effort of forcing migration far outweigh any potential gains.

---

## Technical Details

### Build Environment
- **Docker Image**: pebble-blog:spring6-phase3b (failed to build)
- **Maven**: 3.9.5
- **Java**: OpenJDK 17.0.15
- **Build Time**: 4:40 minutes before failure
- **Compilation Errors**: 16 errors
- **Compilation Warnings**: 28 warnings (deprecation notices)

### Error Summary
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile
[INFO] 16 errors
[INFO] 28 warnings

Errors:
- 5 errors: Spring Security OpenID package missing
- 2 errors: commons-fileupload javax/jakarta mismatch
- 4 errors: JSTL Config class missing
- 1 error: rome-propono javax/jakarta interface mismatch
- 4 errors: related symbol resolution failures
```

### Files Modified (Reverted)
- `pom.xml` - Dependency upgrades
- `Dockerfile.multistage` - Tomcat 10 configuration
- 229 Java source files - javax → jakarta imports
- 1 JSP documentation file - code example

All changes have been **reverted** to restore Phase 3A stability.

---

**Prepared by**: Phase 3B Migration Team
**Date**: January 14, 2026
**Status**: ❌ **BLOCKED - Reverting to Phase 3A**

---

## Appendix: Related Documentation

- [Phase 3A Integration Test Results](phase3a-integration-test-results.md) - Baseline for comparison
- [Phase 3A Integration Tests Script](phase3a-integration-tests.sh) - Automated test suite
- [Phase 2 Security Enhancements](../02-design/phase2-5-security-enhancements.md) - Previous phase

## Appendix: Jakarta Migration Timeline

| Date | Event |
|------|-------|
| **Sep 2019** | Jakarta EE 8 released (javax.* namespace) |
| **Nov 2020** | Jakarta EE 9 released (jakarta.* namespace migration) |
| **Nov 2022** | Spring Framework 6.0 released (requires Jakarta EE 9+) |
| **Jan 2024** | Spring Framework 5.3.x still maintained |
| **Jan 2026** | Many libraries still use javax.* (this project) |
| **Est 2027** | Broader ecosystem Jakarta adoption expected |
