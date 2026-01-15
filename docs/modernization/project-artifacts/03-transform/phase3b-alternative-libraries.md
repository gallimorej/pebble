# Phase 3B Alternative Libraries Plan

**Date**: January 14, 2026
**Phase**: 3B - Spring 6 + Jakarta EE Migration
**Status**: ✅ **VIABLE - Alternative Replacements Available**

---

## Executive Summary

Phase 3B migration to Spring 6 + Jakarta Servlet is **feasible** by replacing incompatible third-party libraries with Jakarta-compatible alternatives. This document outlines specific replacement strategies for each blocker identified in `phase3b-blockers.md`.

**Key Finding**: All four critical blockers have viable replacement options with low-to-medium implementation complexity.

---

## Library Replacement Strategies

### 1. ✅ Spring Security OpenID → OAuth 2.0 / OIDC

**Original Library**: `spring-security-openid` (removed in Spring Security 6)
**Files Affected**: 1 file (`AddOpenIdAction.java`)
**Usage**: OpenID 2.0 authentication for user login

#### Replacement Option A: Spring Security OAuth 2.0 Client (RECOMMENDED)

**Library**: Built into Spring Security 6.x
**Maven Dependencies**:
```xml
<!-- Already included in Spring Security 6.2.1 -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-client</artifactId>
    <version>${spring-security.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
    <version>${spring-security.version}</version>
</dependency>
```

**Benefits**:
- Modern OAuth 2.0/OIDC standard (replaces deprecated OpenID 2.0)
- Built into Spring Security 6 (no external dependencies)
- Better security (token-based, supports MFA)
- Wider provider support (Google, GitHub, Microsoft, etc.)

**Migration Complexity**: **Medium**
- Replace OpenIDConsumer with OAuth2AuthorizedClientService
- Update authentication flow from OpenID 2.0 → OIDC
- Modify `AddOpenIdAction.java` (~120 lines)
- Update Spring Security configuration

**Code Changes Required**:
- Remove OpenID-specific imports
- Implement OAuth2 authorization code flow
- Update SecurityRealm to store OAuth2 identities instead of OpenID
- Configure OAuth2 client registration in application.properties

**Testing Required**:
- Unit tests for OAuth2 flow
- Integration tests for login/authentication
- Manual testing with Google/GitHub OIDC providers

**Breaking Change**: Yes - OpenID 2.0 URLs will no longer work. Users must re-authenticate with OAuth2/OIDC providers.

---

#### Replacement Option B: Remove OpenID Support

**Complexity**: **Low**
- Simply delete `AddOpenIdAction.java`
- Remove OpenID-related UI/menu items
- Update documentation

**Benefits**: Simplest approach, reduces attack surface
**Trade-off**: Loss of OpenID authentication feature

---

### 2. ✅ commons-fileupload → Spring Native Multipart Support

**Original Library**: `commons-fileupload:1.5` (javax.servlet only)
**Files Affected**: 1 file (`UploadFileAction.java`)
**Usage**: Multipart file upload for blog images, theme files

#### Replacement: Spring's StandardServletMultipartResolver (RECOMMENDED)

**Library**: Built into Spring Framework 6.x (uses Servlet 3.0+ native multipart)
**Maven Dependencies**: None (uses Jakarta Servlet 5.0 native support)

**Benefits**:
- No external dependencies
- Native servlet container support (Tomcat 10+)
- Better performance (no temporary disk writes)
- Fully Jakarta-compatible
- Simpler configuration

**Migration Complexity**: **Low**
- Replace `ServletFileUpload.parseRequest()` with Spring's MultipartFile API
- Update `UploadFileAction.java` (~30 lines changed)
- Add Spring multipart resolver bean configuration

**Code Changes Required**:

**Before (commons-fileupload)**:
```java
DiskFileItemFactory factory = new DiskFileItemFactory();
ServletFileUpload upload = new ServletFileUpload(factory);
List items = upload.parseRequest(request);
Iterator it = items.iterator();
while (it.hasNext()) {
    FileItem item = (FileItem)it.next();
    if (!item.isFormField()) {
        item.write(file);
    }
}
```

**After (Spring MultipartFile)**:
```java
MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
    MultipartFile file = entry.getValue();
    if (!file.isEmpty()) {
        file.transferTo(destinationFile);
    }
}
```

**Configuration Required**:
```java
@Bean
public MultipartResolver multipartResolver() {
    StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
    return resolver;
}
```

**web.xml** (or ServletInitializer):
```xml
<multipart-config>
    <max-file-size>5242880</max-file-size> <!-- 5MB -->
    <max-request-size>20971520</max-request-size> <!-- 20MB -->
</multipart-config>
```

**Testing Required**:
- Unit tests for file upload parsing
- Integration tests for image uploads
- Integration tests for theme file uploads
- Verify file size limits work correctly

**Breaking Change**: No - Same functionality, different implementation

---

### 3. ✅ JSTL Config → Direct Locale Setting

**Original Library**: `javax.servlet.jsp.jstl.core.Config` (missing in Jakarta JSTL 2.0)
**Files Affected**: 2 files (`ExportBlogAction.java`, `FeedAction.java`)
**Usage**: Setting JSTL locale for internationalized content

#### Replacement Option A: Spring LocaleResolver (RECOMMENDED)

**Library**: Built into Spring Framework 6.x
**Maven Dependencies**: None (built-in)

**Benefits**:
- Spring-native approach
- Better integration with Spring i18n
- No JSTL dependency
- Jakarta-compatible

**Migration Complexity**: **Low**
- Replace `Config.set()` calls with Spring LocaleContextHolder
- Update 2 files (~10 lines total)

**Code Changes Required**:

**Before (JSTL Config)**:
```java
import javax.servlet.jsp.jstl.core.Config;

Config.set(request.getSession(), Config.FMT_LOCALE, locale);
```

**After (Spring LocaleContextHolder)**:
```java
import org.springframework.context.i18n.LocaleContextHolder;

LocaleContextHolder.setLocale(locale);
request.getSession().setAttribute("org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE", locale);
```

**Configuration Required**:
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver resolver = new SessionLocaleResolver();
    resolver.setDefaultLocale(Locale.ENGLISH);
    return resolver;
}
```

**Testing Required**:
- Unit tests for locale setting
- Integration tests for feed generation with different locales
- Verify blog export works with i18n

**Breaking Change**: No - Same functionality

---

#### Replacement Option B: Direct Session Attribute

**Complexity**: **Very Low**
- Set locale directly as session attribute
- JSPs read from standard JSTL locale attribute

**Code**:
```java
request.getSession().setAttribute("javax.servlet.jsp.jstl.fmt.locale", locale);
```

**Note**: JSTL still uses `javax.servlet.jsp.jstl.fmt.locale` attribute name even in Jakarta (for backward compatibility)

---

### 4. ✅ rome-propono → Custom Atom Implementation

**Original Library**: `rome-propono:1.5.1` (javax.servlet only, no Jakarta version)
**Files Affected**: 2 files (`PebbleAtomHandlerFactory.java`, `PebbleAtomHandler.java`)
**Usage**: Atom Publishing Protocol (AtomPub) API for blog post publishing

#### Background: What is rome-propono?

Rome-propono implements the Atom Publishing Protocol (RFC 5023), allowing external clients to:
- Create/edit/delete blog posts via HTTP
- Upload media files
- Retrieve blog collections

**Current Usage in Pebble**: Minimal - provides AtomPub endpoint for blog publishing clients (rarely used in practice).

---

#### Replacement Option A: Remove Atom API Support (RECOMMENDED)

**Complexity**: **Very Low**
- Delete `PebbleAtomHandlerFactory.java` and `PebbleAtomHandler.java`
- Remove AtomPub servlet mapping from web.xml
- Update documentation

**Benefits**:
- Simplest approach
- AtomPub is largely obsolete (replaced by modern REST APIs)
- Reduces attack surface
- No external dependencies

**Impact**:
- Loss of AtomPub support (rarely used in modern blogging)
- Atom *feeds* still work (handled by rome library, not propono)
- Users can still publish via web UI or custom REST API

**Trade-off**: Some legacy blog clients may no longer work (but these are rare today)

---

#### Replacement Option B: Implement Custom AtomPub Handler

**Complexity**: **High**
- Implement Atom Publishing Protocol manually
- Create Jakarta-compatible servlet
- Parse/generate Atom XML
- Handle POST/PUT/DELETE operations

**Benefits**:
- Maintains full AtomPub compatibility
- No external dependencies

**Code Required** (~500-800 lines):
- AtomPub request parser
- Atom entry XML generator (use rome library)
- Collection/entry resource handlers
- Authentication/authorization

**Libraries Needed**:
```xml
<!-- Rome for Atom feed generation (Jakarta-compatible) -->
<dependency>
    <groupId>com.rometools</groupId>
    <artifactId>rome</artifactId>
    <version>2.1.0</version>
</dependency>
```

**Testing Required**:
- Unit tests for Atom XML parsing/generation
- Integration tests with AtomPub clients
- Security testing for authentication

**Trade-off**: Significant development effort for rarely-used feature

---

#### Replacement Option C: Wait for rome-propono Jakarta Support

**Status**: No Jakarta migration announced as of Jan 2026
**Likelihood**: Low - AtomPub is largely obsolete
**Recommendation**: Not advised - may never happen

---

## Migration Effort Summary

| Blocker | Replacement | Complexity | Lines Changed | Breaking Change | Recommendation |
|---------|-------------|------------|---------------|-----------------|----------------|
| **OpenID** | OAuth 2.0 Client | Medium | ~150 | Yes (user re-auth) | Migrate to OAuth2/OIDC |
| **commons-fileupload** | Spring Native Multipart | Low | ~30 | No | Use Spring built-in |
| **JSTL Config** | Spring LocaleResolver | Low | ~10 | No | Use Spring i18n |
| **rome-propono** | Remove AtomPub | Very Low | -50 | Yes (rare feature) | Remove feature |
| **TOTAL** | | **Low-Medium** | **~140 net** | **Limited** | **Feasible** |

---

## Recommended Migration Path

### Phase 3B-R (Revised): Replace Incompatible Libraries

**Goal**: Achieve Spring 6 + Jakarta Servlet migration by replacing blocking dependencies

**Steps**:

1. **Replace commons-fileupload** (1-2 days)
   - Implement Spring native multipart support
   - Update UploadFileAction.java
   - Test file uploads (images, theme files)

2. **Replace JSTL Config** (0.5 days)
   - Use Spring LocaleResolver
   - Update ExportBlogAction and FeedAction
   - Test i18n functionality

3. **Remove rome-propono** (0.5 days)
   - Delete Atom API files
   - Remove servlet mappings
   - Update documentation

4. **Replace OpenID with OAuth2** (3-5 days)
   - Implement OAuth2/OIDC support
   - Refactor AddOpenIdAction → OAuth2LoginAction
   - Configure OAuth2 providers (Google, GitHub)
   - Update SecurityRealm for OAuth2 identities
   - Test authentication flow

5. **Full Migration** (1 day)
   - Apply Phase 3B changes (Spring 6 + Jakarta)
   - Update pom.xml dependencies
   - Migrate javax → jakarta imports (229 files)
   - Update Dockerfile for Tomcat 10

6. **Testing** (2-3 days)
   - Run all 775 unit tests
   - Run all 25 integration tests
   - Manual testing of replaced functionality
   - Security testing

**Total Estimated Effort**: 8-12 days

---

## Alternative: Staged Migration

### Option: Phase 3B-Lite (Skip OAuth2 Migration)

If OAuth2 migration is too complex, we can stage the migration:

**Phase 3B-Lite Steps**:
1. Replace commons-fileupload ✅
2. Replace JSTL Config ✅
3. Remove rome-propono ✅
4. Remove OpenID support ❌ (delete feature entirely)
5. Complete Spring 6 + Jakarta migration ✅

**Effort**: 3-5 days (instead of 8-12 days)
**Trade-off**: Loss of OpenID authentication (can add OAuth2 later)

---

## Benefits of Phase 3B with Replacements

### Technical Benefits:
- ✅ Modern Java 17 LTS
- ✅ Spring Framework 6.x (latest features)
- ✅ Jakarta Servlet 5.0 (future-proof namespace)
- ✅ Tomcat 10+ (modern servlet container)
- ✅ Reduced dependencies (built-in Spring features)
- ✅ Better security (OAuth2 > OpenID 2.0)
- ✅ Improved performance (native multipart support)

### Maintenance Benefits:
- ✅ LTS versions across the board
- ✅ Active community support
- ✅ Security patches for Spring 6.x
- ✅ Less technical debt
- ✅ Easier future upgrades

---

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| OAuth2 migration complexity | Medium | High | Use Spring Security 6 built-in support, extensive testing |
| User re-authentication required | High | Medium | Clear communication, migration guide |
| Multipart upload issues | Low | Medium | Thorough testing with various file types/sizes |
| Locale setting breaks i18n | Low | Low | Test with multiple locales |
| Atom API users affected | Low | Low | Feature rarely used, document removal |

---

## Testing Strategy

### Unit Tests (775 tests)
- All existing tests must pass
- Add new tests for replaced libraries:
  - Spring multipart upload tests
  - Spring locale resolver tests
  - OAuth2 authentication flow tests

### Integration Tests (25 tests)
- File upload integration test
- Feed generation with i18n
- Authentication flow (OAuth2)
- End-to-end user journeys

### Manual Testing
- Upload images via web UI
- Upload theme files
- Generate blog export (multiple locales)
- Generate RSS/Atom feeds
- OAuth2 login flow (Google, GitHub)
- Security testing (OWASP Top 10)

---

## Comparison: Phase 3A vs Phase 3B-R

| Feature | Phase 3A | Phase 3B-R | Change |
|---------|----------|------------|--------|
| **Java Version** | 17 LTS ✅ | 17 LTS ✅ | Same |
| **Spring Framework** | 5.3.39 | 6.0.23 ✅ | Upgraded |
| **Spring Security** | 5.8.14 | 6.2.1 ✅ | Upgraded |
| **Servlet API** | javax 3.1 | jakarta 5.0 ✅ | Migrated |
| **Tomcat** | 9.0.85 | 10.1.19 ✅ | Upgraded |
| **Authentication** | OpenID 2.0 | OAuth 2.0/OIDC ✅ | Modern |
| **File Upload** | commons-fileupload | Spring Native ✅ | Improved |
| **Atom API** | rome-propono | Removed ⚠️ | Simplified |
| **Dependencies** | More external | Fewer (built-in) ✅ | Reduced |

---

## Recommendation

✅ **PROCEED WITH PHASE 3B-R** - Replace incompatible libraries and complete Spring 6 + Jakarta migration

**Rationale**:
1. **Feasible effort**: 8-12 days is reasonable for a major framework upgrade
2. **Modern stack**: Future-proof with Spring 6 + Jakarta + Java 17 LTS
3. **Reduced dependencies**: Less external libraries = less maintenance
4. **Better security**: OAuth2 is more secure than OpenID 2.0
5. **Performance**: Native multipart support is faster
6. **Long-term benefits**: Easier upgrades, active community support

**Alternative if timeline is tight**: Phase 3B-Lite (3-5 days, skip OAuth2, remove OpenID entirely)

---

## Next Steps

If proceeding with Phase 3B-R:

1. ✅ User approval of migration plan
2. 🔄 Create feature branch: `phase3b-library-replacement`
3. 🔄 Implement replacements in order:
   - commons-fileupload → Spring Native Multipart
   - JSTL Config → Spring LocaleResolver
   - rome-propono → Remove AtomPub
   - OpenID → OAuth2/OIDC
4. 🔄 Apply Spring 6 + Jakarta migration
5. 🔄 Run full test suite
6. 🔄 Manual testing and security review
7. 🔄 Documentation updates
8. 🔄 Git tag: `phase3b-spring6-jakarta`

---

**Document Version**: 1.0
**Prepared by**: Phase 3B Migration Team
**Date**: January 14, 2026
**Status**: ✅ **Awaiting User Approval**
