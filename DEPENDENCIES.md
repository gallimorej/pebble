# Pebble Dependencies

This document lists all dependencies used in the Pebble project, including their current versions and information about available updates.

> **Note**: Pebble is no longer maintained. Many dependencies are outdated, and updating them may require significant code changes due to API incompatibilities and the age of the codebase (Java 6, Servlet 3.0).

## Legend
- ✅ **Safe to update**: Minor version changes, backward compatible
- ⚠️ **Update with caution**: May require code changes
- ❌ **Not practical to update**: Major breaking changes or deprecated/discontinued

---

## Runtime Dependencies

### Apache Commons Libraries

#### commons-logging
- **Current Version**: 1.2
- **Latest Version**: 1.3.4
- **Maven Central**: [1.2](https://mvnrepository.com/artifact/commons-logging/commons-logging/1.2) | [Latest](https://mvnrepository.com/artifact/commons-logging/commons-logging)
- **Status**: ✅ Safe to update
- **Notes**: Minor updates available, fully backward compatible

#### commons-collections
- **Current Version**: 3.2.2
- **Latest Version**: 4.5.0-M2 (or 3.2.2 for legacy)
- **Maven Central**: [3.2.2](https://mvnrepository.com/artifact/commons-collections/commons-collections/3.2.2) | [Latest 4.x](https://mvnrepository.com/artifact/org.apache.commons/commons-collections4)
- **Status**: ❌ Not practical to update
- **Notes**: Version 4.x uses a different package name (`org.apache.commons.collections4`) requiring significant refactoring. Version 3.2.2 is the final release of the 3.x line.

#### commons-lang
- **Current Version**: 2.6
- **Latest Version**: 3.17.0
- **Maven Central**: [2.6](https://mvnrepository.com/artifact/commons-lang/commons-lang/2.6) | [Latest 3.x](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3)
- **Status**: ❌ Not practical to update
- **Notes**: Version 3.x uses different package name (`org.apache.commons.lang3`). Updating requires extensive code refactoring.

#### commons-httpclient
- **Current Version**: 3.1
- **Latest Version**: Deprecated - use Apache HttpClient 5.x
- **Maven Central**: [3.1](https://mvnrepository.com/artifact/commons-httpclient/commons-httpclient/3.1)
- **Status**: ❌ Not practical to update
- **Notes**: This library is EOL. Replacement is Apache HttpClient 5.x with completely different API.

#### commons-fileupload
- **Current Version**: 1.3.3
- **Latest Version**: 1.5
- **Maven Central**: [1.3.3](https://mvnrepository.com/artifact/commons-fileupload/commons-fileupload/1.3.3) | [Latest](https://mvnrepository.com/artifact/commons-fileupload/commons-fileupload)
- **Status**: ⚠️ Update with caution
- **Notes**: Has known CVE vulnerabilities in 1.3.3. Update recommended but test thoroughly. Note: 1.5 requires Java 8+.

#### commons-io
- **Current Version**: 1.4
- **Latest Version**: 2.18.0
- **Maven Central**: [1.4](https://mvnrepository.com/artifact/commons-io/commons-io/1.4) | [Latest](https://mvnrepository.com/artifact/commons-io/commons-io)
- **Status**: ⚠️ Update with caution
- **Notes**: Version 2.x has API changes but mostly backward compatible. Test thoroughly.

---

### Spring Framework

#### spring-security-web
- **Current Version**: 3.0.8.RELEASE
- **Latest Version**: 6.4.2 (or 5.8.15 for Java 8 compatibility)
- **Maven Central**: [3.0.8](https://mvnrepository.com/artifact/org.springframework.security/spring-security-web/3.0.8.RELEASE) | [Latest](https://mvnrepository.com/artifact/org.springframework.security/spring-security-web)
- **Status**: ❌ Not practical to update
- **Notes**: Major version changes with significant API breaking changes. Would require complete security layer rewrite.

#### spring-security-config
- **Current Version**: 3.0.8.RELEASE
- **Latest Version**: 6.4.2 (or 5.8.15 for Java 8 compatibility)
- **Maven Central**: [3.0.8](https://mvnrepository.com/artifact/org.springframework.security/spring-security-config/3.0.8.RELEASE) | [Latest](https://mvnrepository.com/artifact/org.springframework.security/spring-security-config)
- **Status**: ❌ Not practical to update
- **Notes**: Major version changes with significant API breaking changes.

#### spring-security-openid
- **Current Version**: 3.0.8.RELEASE
- **Latest Version**: Deprecated in Spring Security 5.2+
- **Maven Central**: [3.0.8](https://mvnrepository.com/artifact/org.springframework.security/spring-security-openid/3.0.8.RELEASE)
- **Status**: ❌ Not practical to update
- **Notes**: OpenID 2.0 support was removed. Modern replacement is OAuth2/OpenID Connect.

#### spring-web
- **Current Version**: 3.0.7.RELEASE
- **Latest Version**: 6.2.1 (or 5.3.39 for Java 8 compatibility)
- **Maven Central**: [3.0.7](https://mvnrepository.com/artifact/org.springframework/spring-web/3.0.7.RELEASE) | [Latest](https://mvnrepository.com/artifact/org.springframework/spring-web)
- **Status**: ❌ Not practical to update
- **Notes**: Major API changes across versions. Would require significant refactoring.

---

### XML & RSS/Atom Processing

#### rome (ROME RSS/Atom)
- **Current Version**: 1.5.1
- **Latest Version**: 2.1.0
- **Maven Central**: [1.5.1](https://mvnrepository.com/artifact/com.rometools/rome/1.5.1) | [Latest](https://mvnrepository.com/artifact/com.rometools/rome)
- **Status**: ✅ Safe to update
- **Notes**: ROME Tools continues to be maintained. Update should be straightforward.

#### rome-propono
- **Current Version**: 1.5.1
- **Latest Version**: 1.5.1 (no newer releases)
- **Maven Central**: [1.5.1](https://mvnrepository.com/artifact/com.rometools/rome-propono/1.5.1)
- **Status**: ✅ Already on latest
- **Notes**: No updates needed.

#### rome-modules
- **Current Version**: 1.5.1
- **Latest Version**: 2.1.0
- **Maven Central**: [1.5.1](https://mvnrepository.com/artifact/com.rometools/rome-modules/1.5.1) | [Latest](https://mvnrepository.com/artifact/com.rometools/rome-modules)
- **Status**: ✅ Safe to update
- **Notes**: Update together with rome library.

#### jdom
- **Current Version**: 2.0.2
- **Latest Version**: 2.0.6.1
- **Maven Central**: [2.0.2](https://mvnrepository.com/artifact/org.jdom/jdom/2.0.2) | [Latest](https://mvnrepository.com/artifact/org.jdom/jdom2)
- **Status**: ✅ Safe to update
- **Notes**: Minor updates available, backward compatible within 2.x line.

#### jaxb-api
- **Current Version**: 2.0
- **Latest Version**: 2.3.1
- **Maven Central**: [2.0](https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api/2.0) | [Latest](https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api)
- **Status**: ⚠️ Update with caution
- **Notes**: JAXB was removed from JDK 11+. For Java 8+, update to 2.3.1. For Java 11+, need Jakarta EE versions.

#### jaxb-impl
- **Current Version**: 2.0.5
- **Latest Version**: 2.3.9 (javax) or 4.0.5 (jakarta)
- **Maven Central**: [2.0.5](https://mvnrepository.com/artifact/com.sun.xml.bind/jaxb-impl/2.0.5) | [Latest javax](https://mvnrepository.com/artifact/com.sun.xml.bind/jaxb-impl)
- **Status**: ⚠️ Update with caution
- **Notes**: Consider runtime Java version. Java 11+ requires explicit JAXB dependency.

#### xmlrpc
- **Current Version**: 1.2-b1
- **Latest Version**: 3.1.3 (Apache XML-RPC)
- **Maven Central**: [1.2-b1](https://mvnrepository.com/artifact/xmlrpc/xmlrpc/1.2-b1) | [Latest 3.x](https://mvnrepository.com/artifact/org.apache.xmlrpc/xmlrpc)
- **Status**: ❌ Not practical to update
- **Notes**: Version 3.x is a complete rewrite with different package structure and API.

---

### Web & Servlet Technologies

#### tomcat-servlet-api (provided)
- **Current Version**: 7.0.88
- **Latest Version**: 10.1.33 (or 9.0.97 for Servlet 4.0, 11.0.2 for Servlet 6.1)
- **Maven Central**: [7.0.88](https://mvnrepository.com/artifact/org.apache.tomcat/tomcat-servlet-api/7.0.88) | [Latest](https://mvnrepository.com/artifact/org.apache.tomcat/tomcat-servlet-api)
- **Status**: ⚠️ Update with caution
- **Notes**: Update depends on target server. Tomcat 7 is EOL. Consider Tomcat 9 (Servlet 4.0) or Tomcat 10+ (Jakarta EE).

#### tomcat-jsp-api (provided)
- **Current Version**: 7.0.88
- **Latest Version**: 10.1.33 (or 9.0.97)
- **Maven Central**: [7.0.88](https://mvnrepository.com/artifact/org.apache.tomcat/tomcat-jsp-api/7.0.88) | [Latest](https://mvnrepository.com/artifact/org.apache.tomcat/tomcat-jsp-api)
- **Status**: ⚠️ Update with caution
- **Notes**: Update together with servlet API. Tomcat 10+ uses Jakarta namespace.

#### taglibs-standard-spec
- **Current Version**: 1.2.3
- **Latest Version**: 1.2.5
- **Maven Central**: [1.2.3](https://mvnrepository.com/artifact/org.apache.taglibs/taglibs-standard-spec/1.2.3) | [Latest](https://mvnrepository.com/artifact/org.apache.taglibs/taglibs-standard-spec)
- **Status**: ✅ Safe to update
- **Notes**: Minor version update, backward compatible.

#### taglibs-standard-impl
- **Current Version**: 1.2.3
- **Latest Version**: 1.2.5
- **Maven Central**: [1.2.3](https://mvnrepository.com/artifact/org.apache.taglibs/taglibs-standard-impl/1.2.3) | [Latest](https://mvnrepository.com/artifact/org.apache.taglibs/taglibs-standard-impl)
- **Status**: ✅ Safe to update
- **Notes**: Minor version update, backward compatible.

#### dwr (Direct Web Remoting)
- **Current Version**: 2.0.rc2
- **Latest Version**: 3.0.2
- **Maven Central**: [2.0.rc2](https://mvnrepository.com/artifact/org.directwebremoting/dwr/2.0.rc2) | [Latest](https://mvnrepository.com/artifact/org.directwebremoting/dwr)
- **Status**: ⚠️ Update with caution
- **Notes**: DWR 3.0 has configuration changes. Project development has slowed significantly.

---

### Security & Captcha

#### jcaptcha-all
- **Current Version**: 1.0-RC6
- **Latest Version**: 1.0-RC6 (project abandoned)
- **Maven Central**: [1.0-RC6](https://mvnrepository.com/artifact/com.octo.captcha/jcaptcha-all/1.0-RC6)
- **Status**: ❌ Not practical to update
- **Notes**: Project is abandoned. Consider replacing with Google reCAPTCHA or other modern captcha solutions.

#### recaptcha4j
- **Current Version**: 0.0.7
- **Latest Version**: 0.0.7 (project discontinued)
- **Maven Central**: [0.0.7](https://mvnrepository.com/artifact/net.tanesha.recaptcha4j/recaptcha4j/0.0.7)
- **Status**: ❌ Not practical to update
- **Notes**: This library is obsolete. ReCAPTCHA v1 was shut down. Use official Google reCAPTCHA library for v2/v3.

---

### PDF & Document Generation

#### itext
- **Current Version**: 2.0.8
- **Latest Version**: 8.0.5 (or 5.5.13.4 for LGPL/MPL version)
- **Maven Central**: [2.0.8](https://mvnrepository.com/artifact/com.lowagie/itext/2.0.8) | [Latest 5.x](https://mvnrepository.com/artifact/com.itextpdf/itextpdf)
- **Status**: ❌ Not practical to update
- **Notes**: Version 5+ uses different package (`com.itextpdf`). Version 7+ changed to AGPL license. Major API changes required.

#### core-renderer (Flying Saucer)
- **Current Version**: R8
- **Latest Version**: 9.11.2
- **Maven Central**: [R8](https://mvnrepository.com/artifact/org.xhtmlrenderer/core-renderer/R8) | [Latest](https://mvnrepository.com/artifact/org.xhtmlrenderer/flying-saucer-core)
- **Status**: ⚠️ Update with caution
- **Notes**: Artifact name changed to `flying-saucer-core`. API mostly compatible but test thoroughly.

---

### Caching

#### ehcache
- **Current Version**: 2.10.5
- **Latest Version**: 3.10.8 (or 2.10.9.2 for 2.x line)
- **Maven Central**: [2.10.5](https://mvnrepository.com/artifact/net.sf.ehcache/ehcache/2.10.5) | [Latest 2.x](https://mvnrepository.com/artifact/net.sf.ehcache/ehcache) | [Latest 3.x](https://mvnrepository.com/artifact/org.ehcache/ehcache)
- **Status**: ⚠️ Update with caution
- **Notes**: Version 2.10.9.2 is safe update. Version 3.x is JSR-107 compliant with API changes.

---

### Search

#### lucene
- **Current Version**: 1.4.1
- **Latest Version**: 9.12.0
- **Maven Central**: [1.4.1](https://mvnrepository.com/artifact/lucene/lucene/1.4.1) | [Latest](https://mvnrepository.com/artifact/org.apache.lucene/lucene-core)
- **Status**: ❌ Not practical to update
- **Notes**: Ancient version (2003). Modern Lucene 9.x requires complete rewrite. Package changed to `org.apache.lucene`.

---

### Social Media & External Services

#### twitter4j
- **Current Version**: 2.0.10
- **Latest Version**: 4.0.7
- **Maven Central**: [2.0.10](https://mvnrepository.com/artifact/net.homeip.yusuke/twitter4j/2.0.10) | [Latest 4.x](https://mvnrepository.com/artifact/org.twitter4j/twitter4j-core)
- **Status**: ❌ Not practical to update
- **Notes**: Package changed to `org.twitter4j`. Twitter API has changed significantly. Library development stopped in 2016. Consider using official Twitter API v2.

#### geoip-api (MaxMind GeoIP)
- **Current Version**: 1.2.10
- **Latest Version**: Deprecated - use GeoIP2 Java API
- **Maven Central**: [1.2.10](https://mvnrepository.com/artifact/com.maxmind.geoip/geoip-api/1.2.10)
- **Status**: ❌ Not practical to update
- **Notes**: GeoIP Legacy is discontinued. Replacement is GeoIP2 with completely different API.

---

### Utilities & Other

#### guava
- **Current Version**: r07 (version 7)
- **Latest Version**: 33.3.1-jre
- **Maven Central**: [r07](https://mvnrepository.com/artifact/com.google.guava/guava/r07) | [Latest](https://mvnrepository.com/artifact/com.google.guava/guava)
- **Status**: ⚠️ Update with caution
- **Notes**: Very old version. Guava has maintained backward compatibility reasonably well, but test thoroughly. Use `-jre` variant for Java 8+.

#### javax.inject
- **Current Version**: 1
- **Latest Version**: 1 (javax) or 2.0.1 (jakarta)
- **Maven Central**: [1](https://mvnrepository.com/artifact/javax.inject/javax.inject) | [Jakarta](https://mvnrepository.com/artifact/jakarta.inject/jakarta.inject-api)
- **Status**: ✅ Already on latest (javax)
- **Notes**: For Java EE 8, version 1 is correct. For Jakarta EE 9+, migrate to jakarta.inject.

#### jsr250-api
- **Current Version**: 1.0
- **Latest Version**: 1.0 (javax) or 2.1.1 (jakarta)
- **Maven Central**: [1.0](https://mvnrepository.com/artifact/javax.annotation/jsr250-api) | [Jakarta](https://mvnrepository.com/artifact/jakarta.annotation/jakarta.annotation-api)
- **Status**: ✅ Already on latest (javax)
- **Notes**: For Java EE 8, version 1.0 is correct. For Jakarta EE 9+, migrate to jakarta.annotation.

#### javax.mail (provided)
- **Current Version**: 1.4
- **Latest Version**: 1.6.2 (javax) or 2.0.1 (jakarta)
- **Maven Central**: [1.4](https://mvnrepository.com/artifact/javax.mail/mail/1.4) | [Latest javax](https://mvnrepository.com/artifact/com.sun.mail/javax.mail) | [Jakarta](https://mvnrepository.com/artifact/com.sun.mail/jakarta.mail)
- **Status**: ⚠️ Update with caution
- **Notes**: Update to 1.6.2 for Java 7+. For Java 11+, consider Jakarta Mail.

#### jtidy
- **Current Version**: 4aug2000r7-dev
- **Latest Version**: r938 (unofficial)
- **Maven Central**: [4aug2000r7-dev](https://mvnrepository.com/artifact/jtidy/jtidy/4aug2000r7-dev) | [Latest](https://mvnrepository.com/artifact/net.sf.jtidy/jtidy)
- **Status**: ⚠️ Update with caution
- **Notes**: Project nearly abandoned. Consider replacing with jsoup for HTML parsing.

#### radeox
- **Current Version**: 1.0-b2
- **Latest Version**: 1.0-b2 (project discontinued)
- **Maven Central**: [1.0-b2](https://mvnrepository.com/artifact/radeox/radeox/1.0-b2)
- **Status**: ❌ Not practical to update
- **Notes**: Project abandoned circa 2006. No updates available. Consider replacing with modern markdown/wiki parsers.

#### ant (provided)
- **Current Version**: 1.6.2
- **Latest Version**: 1.10.15
- **Maven Central**: [1.6.2](https://mvnrepository.com/artifact/ant/ant/1.6.2) | [Latest](https://mvnrepository.com/artifact/org.apache.ant/ant)
- **Status**: ⚠️ Update with caution
- **Notes**: Only needed for build. Package changed to `org.apache.ant`. Update if used at runtime.

---

## Test Dependencies

#### junit
- **Current Version**: 4.6
- **Latest Version**: 4.13.2 (or JUnit 5.11.3)
- **Maven Central**: [4.6](https://mvnrepository.com/artifact/junit/junit/4.6) | [Latest 4.x](https://mvnrepository.com/artifact/junit/junit) | [JUnit 5](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter)
- **Status**: ✅ Safe to update (within 4.x)
- **Notes**: Update to 4.13.2 is safe. JUnit 5 migration requires more work but is recommended.

#### mockito-core
- **Current Version**: 1.8.4
- **Latest Version**: 5.14.2
- **Maven Central**: [1.8.4](https://mvnrepository.com/artifact/org.mockito/mockito-core/1.8.4) | [Latest](https://mvnrepository.com/artifact/org.mockito/mockito-core)
- **Status**: ⚠️ Update with caution
- **Notes**: Major API improvements in newer versions. Update to 3.x for Java 8 compatibility, 4.x+ for Java 11+.

---

## Maven Plugins

#### maven-compiler-plugin
- **Current Version**: 2.5.1
- **Latest Version**: 3.13.0
- **Maven Central**: [2.5.1](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-compiler-plugin/2.5.1) | [Latest](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-compiler-plugin)
- **Status**: ✅ Safe to update
- **Notes**: Update recommended for better Java version support.

#### maven-source-plugin
- **Current Version**: 3.0.1
- **Latest Version**: 3.3.1
- **Maven Central**: [3.0.1](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-source-plugin/3.0.1) | [Latest](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-source-plugin)
- **Status**: ✅ Safe to update
- **Notes**: Minor improvements, fully compatible.

#### maven-war-plugin
- **Current Version**: 3.2.1
- **Latest Version**: 3.4.0
- **Maven Central**: [3.2.1](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-war-plugin/3.2.1) | [Latest](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-war-plugin)
- **Status**: ✅ Safe to update
- **Notes**: Minor improvements, fully compatible.

#### maven-site-plugin
- **Current Version**: 2.1
- **Latest Version**: 4.0.0-M16
- **Maven Central**: [2.1](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-site-plugin/2.1) | [Latest](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-site-plugin)
- **Status**: ⚠️ Update with caution
- **Notes**: Major version jump with configuration changes.

#### maven-scm-plugin
- **Current Version**: 1.1
- **Latest Version**: 2.1.0
- **Maven Central**: [1.1](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-scm-plugin/1.1) | [Latest](https://mvnrepository.com/artifact/org.apache.maven.plugins/maven-scm-plugin)
- **Status**: ✅ Safe to update
- **Notes**: If still using for SCM operations, safe to update.

#### cobertura-maven-plugin
- **Current Version**: 2.4
- **Latest Version**: 2.7 (project discontinued)
- **Maven Central**: [2.4](https://mvnrepository.com/artifact/org.codehaus.mojo/cobertura-maven-plugin/2.4) | [Latest](https://mvnrepository.com/artifact/org.codehaus.mojo/cobertura-maven-plugin)
- **Status**: ❌ Not practical to update
- **Notes**: Cobertura is no longer maintained. Replace with JaCoCo for code coverage.

---

## Summary

### Update Priority

**Critical Security Updates Needed:**
- commons-fileupload (CVE vulnerabilities)
- All Spring Security components (version 3.x has known vulnerabilities)
- Tomcat APIs (Tomcat 7 is EOL)

**Quick Wins (Safe Updates):**
- JSTL libraries (taglibs-standard-spec, taglibs-standard-impl)
- ROME libraries (rome, rome-modules)
- commons-logging
- JDOM
- Maven plugins (compiler, war, source)
- JUnit (stay on 4.x for minimal changes)

**Major Refactoring Required:**
- Spring Framework (3.x → 5.x/6.x)
- Apache Commons libraries (package name changes)
- Lucene (1.4.1 → 9.x)
- iText (license and API changes)

**Consider Replacing:**
- JCaptcha → Google reCAPTCHA
- recaptcha4j → Official Google reCAPTCHA library
- twitter4j → Twitter API v2
- GeoIP → GeoIP2 or alternative
- commons-httpclient → Apache HttpClient 5.x
- Radeox → Modern markdown/wiki parser
- Cobertura → JaCoCo

### Modernization Path

Given that Pebble targets Java 6 and is no longer maintained, a complete modernization would require:

1. **Java Version Upgrade**: Move to at least Java 8 (ideally Java 11 LTS or Java 17 LTS)
2. **Servlet Container**: Upgrade from Tomcat 7 to Tomcat 9+ or Tomcat 10+ (Jakarta EE)
3. **Framework Migration**: Update Spring Framework and Spring Security
4. **Dependency Cleanup**: Replace discontinued libraries with modern alternatives
5. **Build System**: Continue using Maven but update plugins
6. **Testing**: Update JUnit to 5.x and Mockito to latest version

This level of modernization is **not practical** for a maintenance-only codebase and would effectively be a rewrite.
