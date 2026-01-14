# Phase 2 Java 11 Migration - Architecture & Implementation Design

**Project**: Pebble Blog Modernization
**Phase**: 03-Transform, Phase 2 (Java 8 → Java 11)
**Design Date**: January 14, 2026
**Architect**: System Architecture Designer
**Status**: DESIGN READY

## Executive Summary

This document defines the architecture and implementation strategy for migrating Pebble from Java 8 to Java 11 LTS. Phase 2 builds on the successful Phase 1 completion (96% integration test pass rate, 775 unit tests passing) and introduces critical framework modernizations while maintaining functional equivalence.

### Phase 2 Scope

**Primary Objectives**:
1. Migrate from Java 8 to Java 11 LTS
2. Upgrade Spring Framework 3.0.7 → 5.3.x
3. Upgrade Lucene 1.4.1 → 9.9.2
4. Add explicit JAXB dependencies (Java 11 removal)
5. Maintain 100% functional equivalence

**Key Deliverables**:
- Java 11 compatible codebase
- Spring 5.3.x framework integration
- Lucene 9.x search implementation
- All 775 unit tests passing
- All 25 integration tests passing

### Success Criteria

| Criteria | Target | Status |
|----------|--------|--------|
| Java 11 compilation | Zero errors | TBD |
| Unit tests | 775 passing | TBD |
| Integration tests | 25 passing (96%+) | TBD |
| Performance | ≤ Phase 1 + 10% | TBD |
| Security CVEs | Zero critical | TBD |

---

## 1. Migration Architecture

### 1.1 Technology Stack Transition

#### Current State (Phase 1 - Java 8)
```
Java 8 (1.8)
├── Spring Framework 3.0.7
│   ├── Spring Security 3.0.8
│   └── Spring Web 3.0.7
├── Lucene 1.4.1
├── JAXB 2.0 (bundled in JRE)
├── Servlet API 2.5 (Tomcat 7)
└── Maven 3.x
```

#### Target State (Phase 2 - Java 11)
```
Java 11 LTS
├── Spring Framework 5.3.39 (latest 5.x)
│   ├── Spring Security 5.8.14 (latest 5.x)
│   └── Spring Web 5.3.39
├── Lucene 9.9.2
├── JAXB 2.3.9 (explicit dependency)
│   ├── jakarta.xml.bind-api 2.3.3
│   ├── jaxb-runtime 2.3.9
│   └── jaxb-impl 2.3.9
├── Servlet API 3.1 (Tomcat 9)
└── Maven 3.8+
```

### 1.2 Module System Impact

Java 11 introduces the Java Platform Module System (JPMS) from Java 9. While Pebble will remain a non-modular application, we must address:

**Module Visibility Changes**:
- JAXB removed from JDK → Add explicit dependencies
- JAX-WS removed → Not used by Pebble
- CORBA removed → Not used by Pebble
- JTA removed → Not used by Pebble

**Internal API Access**:
- sun.* packages restricted
- com.sun.* packages restricted
- Need `--add-opens` for reflection-heavy frameworks

---

## 2. Phase 2 Implementation Phases

### Phase 2a: Maven + Java 11 Configuration (Week 1)

**Objectives**:
- Update Maven compiler configuration
- Add explicit JAXB dependencies
- Configure Java 11 runtime parameters
- Verify clean compilation

**Effort**: 1 week (40 hours)

### Phase 2b: Spring Framework Upgrade (Week 2-3)

**Objectives**:
- Migrate Spring 3.0.7 → 5.3.39
- Update Spring Security 3.0.8 → 5.8.14
- Migrate Spring configuration files
- Update servlet configuration

**Effort**: 2 weeks (80 hours)

### Phase 2c: Lucene Upgrade (Week 3-4)

**Objectives**:
- Migrate Lucene 1.4.1 → 9.9.2
- Update search index implementation
- Rewrite query parser logic
- Migrate analyzer configuration

**Effort**: 2 weeks (80 hours)

### Phase 2d: Testing & Validation (Week 5-6)

**Objectives**:
- Execute full test suite (775 unit tests)
- Run integration tests (25 tests)
- Performance benchmarking
- Security vulnerability scanning

**Effort**: 2 weeks (80 hours)

**Total Effort**: 4-6 weeks (280 hours)

---

## 3. Maven Configuration Changes

### 3.1 Java 11 Compiler Settings

**File**: `/pom.xml`

#### Update Properties Section
```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>

    <!-- Java 11 settings -->
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <maven.compiler.release>11</maven.compiler.release>

    <!-- Spring 5.3.x -->
    <spring.version>5.3.39</spring.version>
    <spring-security.version>5.8.14</spring-security.version>

    <!-- Lucene 9.x -->
    <lucene.version>9.9.2</lucene.version>

    <!-- JAXB (Java 11 requirement) -->
    <jaxb.version>2.3.9</jaxb.version>

    <!-- Plugin versions -->
    <maven-compiler-plugin.version>3.11.0</maven-compiler-plugin.version>
    <maven-surefire-plugin.version>3.2.5</maven-surefire-plugin.version>
</properties>
```

#### Update Compiler Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${maven-compiler-plugin.version}</version>
    <configuration>
        <encoding>${project.build.sourceEncoding}</encoding>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
        <release>${maven.compiler.release}</release>
        <showDeprecation>true</showDeprecation>
        <showWarnings>true</showWarnings>
        <compilerArgs>
            <arg>-Xlint:all</arg>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

#### Update Surefire Plugin (Java 11 Module System)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>${maven-surefire-plugin.version}</version>
    <configuration>
        <argLine>
            --add-opens java.base/java.lang=ALL-UNNAMED
            --add-opens java.base/java.util=ALL-UNNAMED
            --add-opens java.base/java.io=ALL-UNNAMED
        </argLine>
    </configuration>
</plugin>
```

### 3.2 JAXB Dependencies (Critical)

**Context**: Java 11 removed JAXB from the JDK. Pebble uses JAXB extensively for XML-based persistence.

#### Add JAXB Runtime Dependencies
```xml
<!-- JAXB API -->
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>2.3.3</version>
</dependency>

<!-- JAXB Runtime (Glassfish) -->
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>${jaxb.version}</version>
    <scope>runtime</scope>
</dependency>

<!-- JAXB Implementation -->
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-impl</artifactId>
    <version>${jaxb.version}</version>
</dependency>

<!-- JAXB Core -->
<dependency>
    <groupId>com.sun.xml.bind</groupId>
    <artifactId>jaxb-core</artifactId>
    <version>${jaxb.version}</version>
</dependency>

<!-- Activation API (JAXB dependency) -->
<dependency>
    <groupId>jakarta.activation</groupId>
    <artifactId>jakarta.activation-api</artifactId>
    <version>1.2.2</version>
</dependency>
```

#### Update JAXB Maven Plugin
```xml
<plugin>
    <groupId>org.jvnet.jaxb2.maven2</groupId>
    <artifactId>maven-jaxb2-plugin</artifactId>
    <version>0.15.3</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <schemaDirectory>${basedir}/src/main/resources</schemaDirectory>
        <schemaIncludes>
            <include>pebble.xsd</include>
        </schemaIncludes>
        <generatePackage>net.sourceforge.pebble.dao.file</generatePackage>
        <generateDirectory>${project.build.directory}/generated-sources/xjc</generateDirectory>
        <strict>false</strict>
        <extension>true</extension>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.glassfish.jaxb</groupId>
            <artifactId>jaxb-runtime</artifactId>
            <version>${jaxb.version}</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jaxb</groupId>
            <artifactId>jaxb-xjc</artifactId>
            <version>${jaxb.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

### 3.3 Java 11 Multi-Version Profile

Add to existing profiles section:

```xml
<profile>
    <id>java11</id>
    <activation>
        <jdk>11</jdk>
        <activeByDefault>true</activeByDefault>
    </activation>
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <maven.compiler.release>11</maven.compiler.release>
    </properties>
    <dependencies>
        <!-- JAXB dependencies only needed for Java 11+ -->
        <dependency>
            <groupId>jakarta.xml.bind</groupId>
            <artifactId>jakarta.xml.bind-api</artifactId>
            <version>2.3.3</version>
        </dependency>
        <dependency>
            <groupId>org.glassfish.jaxb</groupId>
            <artifactId>jaxb-runtime</artifactId>
            <version>${jaxb.version}</version>
        </dependency>
    </dependencies>
</profile>
```

---

## 4. Spring Framework Migration

### 4.1 Spring Framework 3.0.7 → 5.3.39

**Breaking Changes to Address**:

1. **XML Configuration Schema Updates**
2. **Package relocations**
3. **Deprecated API removal**
4. **Spring Security configuration overhaul**

### 4.2 Spring Dependencies Update

#### Remove Old Dependencies
```xml
<!-- DELETE these Spring 3.x dependencies -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>3.0.7.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-web</artifactId>
    <version>3.0.8.RELEASE</version>
</dependency>
```

#### Add Spring 5.3.x Dependencies
```xml
<!-- Spring Framework 5.3.x -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>${spring.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>${spring.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>${spring.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-beans</artifactId>
    <version>${spring.version}</version>
</dependency>

<!-- Spring Security 5.8.x -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-web</artifactId>
    <version>${spring-security.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-config</artifactId>
    <version>${spring-security.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-openid</artifactId>
    <version>${spring-security.version}</version>
</dependency>
```

### 4.3 Spring XML Configuration Migration

**File**: `/src/main/webapp/WEB-INF/applicationContext-pebble.xml`

#### Current (Spring 3.0):
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:security="http://www.springframework.org/schema/security"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
          http://www.springframework.org/schema/beans
          http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
          http://www.springframework.org/schema/security
          http://www.springframework.org/schema/security/spring-security-3.0.3.xsd
          http://www.springframework.org/schema/context
          http://www.springframework.org/schema/context/spring-context.xsd">
```

#### Updated (Spring 5.3):
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:security="http://www.springframework.org/schema/security"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="
          http://www.springframework.org/schema/beans
          http://www.springframework.org/schema/beans/spring-beans-5.3.xsd
          http://www.springframework.org/schema/security
          http://www.springframework.org/schema/security/spring-security-5.8.xsd
          http://www.springframework.org/schema/context
          http://www.springframework.org/schema/context/spring-context-5.3.xsd">
```

### 4.4 Spring Security Configuration Migration

**File**: `/src/main/webapp/WEB-INF/applicationContext-security.xml`

#### Key Migration Changes:

1. **Namespace Update**: `spring-security-3.0.3.xsd` → `spring-security-5.8.xsd`
2. **Authentication Manager**: Configuration pattern changed
3. **CSRF Protection**: Enabled by default in 5.x (verify compatibility)
4. **Password Encoding**: Explicit BCrypt configuration

#### Security Configuration Pattern (5.8.x)
```xml
<security:http auto-config="true" use-expressions="true">
    <security:intercept-url pattern="/admin/**" access="hasRole('ROLE_ADMIN')" />
    <security:intercept-url pattern="/**" access="permitAll" />
    <security:form-login login-page="/login.action"
                         default-target-url="/admin/"
                         authentication-failure-url="/login.action?error=true" />
    <security:logout logout-success-url="/" />
    <security:csrf />
</security:http>

<security:authentication-manager>
    <security:authentication-provider ref="pebbleAuthenticationProvider" />
</security:authentication-manager>
```

### 4.5 Servlet API Update

#### Update Servlet Dependency
```xml
<!-- Remove old Tomcat 7 servlet API -->
<!-- DELETE:
<dependency>
    <groupId>org.apache.tomcat</groupId>
    <artifactId>tomcat-servlet-api</artifactId>
    <version>7.0.88</version>
    <scope>provided</scope>
</dependency>
-->

<!-- Add Servlet 3.1 API (Tomcat 9) -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>3.1.0</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>javax.servlet.jsp</groupId>
    <artifactId>javax.servlet.jsp-api</artifactId>
    <version>2.3.3</version>
    <scope>provided</scope>
</dependency>
```

#### Update web.xml (Servlet 3.1)

**File**: `/src/main/webapp/WEB-INF/web.xml`

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">
```

---

## 5. Lucene Migration (1.4.1 → 9.9.2)

### 5.1 Lucene Architecture Changes

**Critical API Changes**:
1. `IndexWriter` constructor signature changed
2. `IndexReader.open()` → `DirectoryReader.open()`
3. `IndexSearcher` requires `IndexReader` parameter
4. `Hits` class removed → Use `TopDocs`
5. `Field` API completely rewritten
6. `QueryParser` moved to separate module
7. `Analyzer` API changes

### 5.2 Lucene Dependencies

#### Replace Old Dependency
```xml
<!-- DELETE old Lucene 1.4.1 -->
<!--
<dependency>
    <groupId>lucene</groupId>
    <artifactId>lucene</artifactId>
    <version>1.4.1</version>
</dependency>
-->

<!-- Add Lucene 9.9.2 modules -->
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-core</artifactId>
    <version>${lucene.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-queryparser</artifactId>
    <version>${lucene.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-analyzers-common</artifactId>
    <version>${lucene.version}</version>
</dependency>
```

### 5.3 SearchIndex.java Migration

**File**: `/src/main/java/net/sourceforge/pebble/index/SearchIndex.java`

#### Current Implementation (Lucene 1.4.1)
```java
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

// Example: IndexWriter creation
IndexWriter writer = new IndexWriter(searchDirectory, analyzer, true);

// Example: Field creation
document.add(Field.Keyword("id", blogEntry.getId()));
document.add(Field.Text("title", blogEntry.getTitle()));
document.add(Field.UnStored("body", blogEntry.getBody()));

// Example: Search
IndexSearcher searcher = new IndexSearcher(blog.getSearchIndexDirectory());
Query query = QueryParser.parse(queryString, "blogEntry", getAnalyzer());
Hits hits = searcher.search(query);
for (int i = 0; i < hits.length(); i++) {
    Document doc = hits.doc(i);
    float score = hits.score(i);
}
```

#### Target Implementation (Lucene 9.9.2)
```java
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

// Example: IndexWriter creation (NEW)
Directory directory = FSDirectory.open(searchDirectory.toPath());
IndexWriterConfig config = new IndexWriterConfig(analyzer);
config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
IndexWriter writer = new IndexWriter(directory, config);

// Example: Field creation (NEW)
document.add(new StringField("id", blogEntry.getId(), Field.Store.YES));
document.add(new TextField("title", blogEntry.getTitle(), Field.Store.YES));
document.add(new TextField("body", blogEntry.getBody(), Field.Store.NO));

// Date field replacement (DateField removed)
long dateMillis = blogEntry.getDate().getTime();
document.add(new LongPoint("date", dateMillis));
document.add(new StoredField("date", dateMillis));

// Example: Search (NEW)
Directory directory = FSDirectory.open(Paths.get(blog.getSearchIndexDirectory()));
DirectoryReader reader = DirectoryReader.open(directory);
IndexSearcher searcher = new IndexSearcher(reader);
QueryParser parser = new QueryParser("blogEntry", getAnalyzer());
Query query = parser.parse(queryString);
TopDocs topDocs = searcher.search(query, 100); // Max 100 results

for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
    Document doc = searcher.doc(scoreDoc.doc);
    float score = scoreDoc.score;
}

reader.close();
directory.close();
```

### 5.4 Lucene Migration API Mapping

| Lucene 1.4.1 | Lucene 9.9.2 | Notes |
|--------------|--------------|-------|
| `Field.Keyword()` | `new StringField()` | Indexed, not tokenized, stored |
| `Field.Text()` | `new TextField()` | Indexed, tokenized, stored |
| `Field.UnStored()` | `new TextField(..., Store.NO)` | Indexed, not stored |
| `Field.UnIndexed()` | `new StoredField()` | Stored, not indexed |
| `DateField.dateToString()` | `LongPoint + StoredField` | Use milliseconds |
| `IndexWriter(dir, analyzer, create)` | `IndexWriter(dir, config)` | Use IndexWriterConfig |
| `IndexReader.open(dir)` | `DirectoryReader.open(dir)` | Different reader types |
| `new IndexSearcher(path)` | `new IndexSearcher(reader)` | Requires IndexReader |
| `Hits` | `TopDocs` | Different iteration pattern |
| `hits.doc(i)` | `searcher.doc(scoreDoc.doc)` | Two-step retrieval |
| `hits.score(i)` | `scoreDoc.score` | Direct access |

### 5.5 Lucene Compatibility Layer

To minimize code changes, create an abstraction layer:

**File**: `/src/main/java/net/sourceforge/pebble/index/LuceneCompatibility.java`

```java
package net.sourceforge.pebble.index;

import org.apache.lucene.document.*;
import java.util.Date;

/**
 * Compatibility layer for Lucene 1.4.1 → 9.9.2 migration.
 * Provides factory methods that mimic old Field API.
 */
public class LuceneCompatibility {

    /**
     * Replacement for Field.Keyword() - indexed, not tokenized, stored
     */
    public static Field Keyword(String name, String value) {
        return new StringField(name, value, Field.Store.YES);
    }

    /**
     * Replacement for Field.Text() - indexed, tokenized, stored
     */
    public static Field Text(String name, String value) {
        return new TextField(name, value, Field.Store.YES);
    }

    /**
     * Replacement for Field.UnStored() - indexed, tokenized, not stored
     */
    public static Field UnStored(String name, String value) {
        return new TextField(name, value, Field.Store.NO);
    }

    /**
     * Replacement for Field.UnIndexed() - not indexed, stored
     */
    public static Field UnIndexed(String name, String value) {
        return new StoredField(name, value);
    }

    /**
     * Replacement for DateField - store date as long milliseconds
     */
    public static void addDateField(Document doc, String name, Date date) {
        long millis = date.getTime();
        doc.add(new LongPoint(name, millis));
        doc.add(new StoredField(name, millis));
    }

    /**
     * Retrieve date from document
     */
    public static Date getDateField(Document doc, String name) {
        long millis = doc.getField(name).numericValue().longValue();
        return new Date(millis);
    }
}
```

#### Updated SearchIndex.java with Compatibility Layer
```java
// Minimal changes to existing code
import static net.sourceforge.pebble.index.LuceneCompatibility.*;

// Field creation now uses compatibility methods
document.add(Keyword("id", blogEntry.getId()));
document.add(Text("title", blogEntry.getTitle()));
document.add(UnStored("body", blogEntry.getBody()));
addDateField(document, "date", blogEntry.getDate());
```

---

## 6. Risk Assessment & Mitigation

### 6.1 Critical Risk Areas

#### Risk 1: JAXB Compatibility (CRITICAL)
**Risk**: Java 11 removed JAXB, could break XML persistence
**Impact**: Blog entries unreadable, data loss risk
**Probability**: High
**Mitigation**:
1. Add explicit JAXB 2.3.9 dependencies BEFORE testing
2. Test XML read/write with Phase 1 data
3. Backup all XML files before Phase 2
4. Verify character encoding (UTF-8)
5. Test with international characters (Chinese, Arabic, emoji)

**Validation**:
- Unit test: `FileBlogEntryDAOTest` (existing)
- Integration test: Create blog entry → verify XML file
- Data migration test: Read Phase 1 XML files

#### Risk 2: Spring Security Configuration (HIGH)
**Risk**: Authentication system may break with Spring 5.8.x
**Impact**: Users cannot log in, admin panel inaccessible
**Probability**: Medium
**Mitigation**:
1. Review Spring Security 3.0 → 5.8 migration guide
2. Update XML configuration incrementally
3. Test authentication before enabling CSRF
4. Maintain backward-compatible password encoding

**Validation**:
- Unit test: Security tests (existing)
- Integration test: Login with username/password
- Integration test: CSRF token validation

#### Risk 3: Lucene Search Functionality (HIGH)
**Risk**: Search API complete rewrite could break search
**Impact**: Search functionality non-functional
**Probability**: High
**Mitigation**:
1. Create compatibility layer (LuceneCompatibility.java)
2. Migrate incrementally (write first, then read)
3. Test with existing search indexes
4. Rebuild indexes if format incompatible

**Validation**:
- Unit test: `SearchIndexTest` (existing)
- Integration test: Index blog entry → search → verify results
- Regression test: Compare search results Phase 1 vs Phase 2

#### Risk 4: Java Module System (MEDIUM)
**Risk**: Reflection-heavy frameworks may fail with JPMS
**Impact**: Runtime errors, ClassNotFoundException
**Probability**: Medium
**Mitigation**:
1. Add `--add-opens` flags to Surefire (done)
2. Test with all 775 unit tests
3. Monitor runtime for IllegalAccessError

**Validation**:
- Execute full test suite on Java 11
- Check for module access warnings in logs

#### Risk 5: Character Encoding (MEDIUM)
**Risk**: Java 11 UTF-8 default could corrupt XML
**Impact**: Blog entries corrupted, data loss
**Probability**: Low (if JAXB configured correctly)
**Mitigation**:
1. Explicitly set UTF-8 in all XML operations
2. Test with Phase 1 XML files
3. Byte-level comparison of XML output

**Validation**:
- Integration test: XML file byte comparison
- Test with special characters: `<>&"'`, emoji, non-ASCII

### 6.2 Performance Risks

#### Risk 6: Spring 5.x Performance (LOW)
**Risk**: Spring 5.x overhead could slow requests
**Impact**: Request latency increase
**Probability**: Low (Spring 5.x generally faster)
**Mitigation**:
1. Benchmark Phase 1 baseline (P95 latency)
2. Compare Phase 2 performance
3. Target: ≤ Phase 1 + 10%

**Validation**:
- Performance test: Homepage load time
- Performance test: Blog entry creation
- Performance test: Search query execution

---

## 7. Testing Strategy

### 7.1 Test Execution Plan

#### Stage 1: Unit Tests (775 tests)
**Objective**: Verify all existing functionality unchanged

**Execution**:
```bash
mvn clean test -Pjava11
```

**Expected**: 775 tests pass, 0 failures, 0 errors

**Critical Test Classes**:
- `BlogEntryTest` (38KB, largest test)
- `FileBlogEntryDAOTest` (JAXB persistence)
- `SearchIndexTest` (Lucene search)
- `BlogServiceTest` (business logic)
- `CommentTest` (comment system)

#### Stage 2: Integration Tests (25 tests)
**Objective**: Verify live application functionality

**Execution**:
```bash
# Build and deploy
mvn clean package -Pjava11
docker build -t pebble-java11:latest .
docker run -d -p 8080:8080 pebble-java11:latest

# Run integration tests
./run-integration-tests.sh
```

**Expected**: 24-25 tests pass (96%+ success rate)

**Critical Integration Tests**:
1. Homepage accessible
2. Blog entry creation (JAXB write)
3. Blog entry display (JAXB read)
4. User login (Spring Security)
5. Search functionality (Lucene)
6. RSS/Atom feed generation
7. XML persistence validation

#### Stage 3: Performance Benchmarking
**Objective**: Ensure performance within acceptable thresholds

**Metrics to Collect**:
- Application startup time
- P95 latency for homepage
- P95 latency for blog entry view
- Search query execution time
- Memory heap usage
- GC pause time

**Acceptance Criteria**:
- Startup time: ≤ Phase 1 + 10%
- P95 latency: ≤ Phase 1 baseline
- Memory usage: ≤ Phase 1 + 20%
- Search latency: ≤ Phase 1 baseline

#### Stage 4: Security Scanning
**Objective**: Verify no new vulnerabilities introduced

**Tools**:
- OWASP Dependency Check
- Snyk vulnerability scanner
- Manual penetration testing

**Acceptance Criteria**:
- Zero critical CVEs (CVSS ≥ 9.0)
- Zero high CVEs (CVSS ≥ 7.0)
- All dependencies < 2 years old

### 7.2 Rollback Procedure

**Rollback Trigger Conditions**:
1. > 10% unit test failures
2. > 20% integration test failures
3. Critical functionality broken (login, persistence, search)
4. Performance degradation > 50%
5. Data corruption detected

**Rollback Steps** (< 30 minutes):
```bash
# 1. Stop Phase 2 container
docker stop pebble-java11

# 2. Restart Phase 1 container
docker start pebble-java8

# 3. Restore XML files (if corrupted)
cp -r /backup/pebble-data/* /app/pebble/

# 4. Verify Phase 1 functionality
curl http://localhost:8080/pebble/ping
```

**Rollback Validation**:
- Homepage loads correctly
- Can log in as admin
- Can create blog entry
- XML files readable

---

## 8. Success Criteria & Phase Gate

### 8.1 Phase 2 Sign-Off Criteria

To approve Phase 2 completion, ALL criteria must be met:

#### Functional Criteria (MANDATORY)
- [ ] **F1**: All 775 unit tests pass on Java 11
- [ ] **F2**: 24+ of 25 integration tests pass (96%+)
- [ ] **F3**: Blog entry creation/editing works (JAXB)
- [ ] **F4**: User authentication works (Spring Security)
- [ ] **F5**: Search functionality works (Lucene 9.x)
- [ ] **F6**: RSS/Atom feeds generate correctly
- [ ] **F7**: All Phase 1 XML files readable

#### Performance Criteria
- [ ] **P1**: Application startup ≤ Phase 1 + 10%
- [ ] **P2**: P95 latency ≤ Phase 1 baseline
- [ ] **P3**: Memory usage ≤ Phase 1 + 20%
- [ ] **P4**: Search latency ≤ Phase 1 baseline

#### Security Criteria
- [ ] **S1**: Zero critical CVEs (CVSS ≥ 9.0)
- [ ] **S2**: Zero high CVEs (CVSS ≥ 7.0)
- [ ] **S3**: Spring Security 5.8.x authentication works

#### Quality Criteria
- [ ] **Q1**: Zero compilation errors
- [ ] **Q2**: Zero runtime ClassNotFoundException
- [ ] **Q3**: Zero XML encoding issues

### 8.2 Phase Gate Review

**Phase 2 → Phase 3 Decision Points**:

1. **Technical Readiness**
   - All success criteria met
   - Performance acceptable
   - Security validated
   - Rollback tested

2. **Business Readiness**
   - Stakeholder approval
   - Documentation complete
   - Operations team trained
   - Deployment plan approved

3. **Risk Assessment**
   - No critical unresolved issues
   - Known issues documented and accepted
   - Mitigation strategies in place

**Approval Authority**: Project Sponsor + Technical Lead

---

## 9. Implementation Checklist

### Week 1: Maven + Java 11 Configuration

- [ ] Update `pom.xml` properties (Java 11)
- [ ] Update maven-compiler-plugin (3.11.0)
- [ ] Update maven-surefire-plugin (module system flags)
- [ ] Add JAXB dependencies (jakarta.xml.bind, glassfish)
- [ ] Add Activation API dependency
- [ ] Update JAXB maven plugin
- [ ] Add Java 11 profile
- [ ] Build with `mvn clean compile -Pjava11`
- [ ] Verify zero compilation errors
- [ ] Run unit tests: `mvn test -Pjava11`
- [ ] Document any deprecation warnings

### Week 2: Spring Framework Upgrade

- [ ] Update Spring dependencies (5.3.39)
- [ ] Update Spring Security dependencies (5.8.14)
- [ ] Update applicationContext-pebble.xml (schema 5.3)
- [ ] Update applicationContext-security.xml (schema 5.8)
- [ ] Update web.xml (Servlet 3.1)
- [ ] Update Servlet API dependency
- [ ] Test Spring context loading
- [ ] Run unit tests: `mvn test -Pjava11`
- [ ] Test authentication (integration test)
- [ ] Verify CSRF protection works

### Week 3: Lucene Upgrade (Part 1)

- [ ] Add Lucene 9.9.2 dependencies
- [ ] Create LuceneCompatibility.java (compatibility layer)
- [ ] Update SearchIndex.java imports
- [ ] Migrate IndexWriter creation
- [ ] Migrate Field creation (Keyword, Text, UnStored)
- [ ] Migrate DateField usage
- [ ] Test index writing (unit test)
- [ ] Run unit tests: `mvn test -Pjava11`

### Week 4: Lucene Upgrade (Part 2)

- [ ] Migrate IndexReader usage (DirectoryReader)
- [ ] Migrate IndexSearcher creation
- [ ] Migrate QueryParser usage
- [ ] Migrate Hits → TopDocs
- [ ] Update search result iteration
- [ ] Test search functionality (unit test)
- [ ] Test search with existing indexes
- [ ] Rebuild search indexes if needed
- [ ] Run full test suite: `mvn clean test -Pjava11`

### Week 5: Integration Testing

- [ ] Build Docker image (Java 11)
- [ ] Deploy to test environment
- [ ] Run 25 integration tests
- [ ] Test blog entry creation (JAXB write)
- [ ] Test blog entry display (JAXB read)
- [ ] Test user authentication
- [ ] Test search functionality
- [ ] Test RSS/Atom feeds
- [ ] Verify XML file format unchanged
- [ ] Test with Phase 1 XML files

### Week 6: Performance & Validation

- [ ] Benchmark application startup time
- [ ] Benchmark P95 latency (homepage, blog view)
- [ ] Benchmark search query execution
- [ ] Measure memory heap usage
- [ ] Measure GC pause time
- [ ] Run OWASP Dependency Check
- [ ] Run Snyk vulnerability scan
- [ ] Document performance metrics
- [ ] Compare Phase 1 vs Phase 2 metrics
- [ ] Phase gate review with stakeholders

---

## 10. Deliverables

### 10.1 Technical Deliverables

1. **Updated pom.xml**
   - Java 11 configuration
   - Spring 5.3.39 dependencies
   - Lucene 9.9.2 dependencies
   - JAXB explicit dependencies

2. **Spring Configuration Files**
   - applicationContext-pebble.xml (Spring 5.3)
   - applicationContext-security.xml (Spring Security 5.8)
   - web.xml (Servlet 3.1)

3. **Lucene Compatibility Layer**
   - LuceneCompatibility.java (abstraction)
   - Updated SearchIndex.java (Lucene 9.x)

4. **Docker Image**
   - pebble-java11:latest (Tomcat 9 + Java 11)

5. **Test Results**
   - Unit test report (775 tests)
   - Integration test report (25 tests)
   - Performance benchmark report
   - Security scan report

### 10.2 Documentation Deliverables

1. **Phase 2 Design Document** (this document)
2. **Phase 2 Implementation Report**
3. **Phase 2 Validation Report**
4. **Spring Migration Guide** (3.0 → 5.3)
5. **Lucene Migration Guide** (1.4.1 → 9.9.2)

---

## 11. Next Steps

### Immediate Actions (Upon Approval)

1. **Backup Phase 1 State**
   - Create git branch: `phase2-java11`
   - Backup all XML data files
   - Document Phase 1 baseline metrics

2. **Setup Development Environment**
   - Install Java 11 JDK
   - Configure Maven for Java 11
   - Setup IDE (Eclipse/IntelliJ) for Java 11

3. **Begin Week 1 Implementation**
   - Update pom.xml (Maven configuration)
   - Add JAXB dependencies
   - Test compilation on Java 11

### Phase Gate Approval

**Required Approvals**:
- [ ] Project Sponsor sign-off
- [ ] Technical Lead approval
- [ ] Security review approval
- [ ] Operations team readiness

**Phase 2 Start Date**: TBD (upon approval)
**Phase 2 Target Completion**: 4-6 weeks from start

---

## Appendix A: Dependency Version Matrix

| Component | Phase 1 (Java 8) | Phase 2 (Java 11) | Notes |
|-----------|------------------|-------------------|-------|
| Java | 8 | 11 LTS | Major version upgrade |
| Spring Framework | 3.0.7 | 5.3.39 | Major upgrade |
| Spring Security | 3.0.8 | 5.8.14 | Authentication overhaul |
| Lucene | 1.4.1 | 9.9.2 | Complete API rewrite |
| JAXB API | 2.0 (bundled) | 2.3.3 (explicit) | Moved out of JDK |
| JAXB Runtime | N/A | 2.3.9 | Required for Java 11 |
| Servlet API | 2.5 (Tomcat 7) | 3.1 (Tomcat 9) | Minor upgrade |
| Tomcat | 7.0.109 | 9.0.x | Application server |
| Maven Compiler | 3.11.0 | 3.11.0 | No change |
| Maven Surefire | 3.2.5 | 3.2.5 | Module flags added |

---

## Appendix B: Breaking Changes Reference

### Java 11 Breaking Changes

1. **Modules**: JAXB, JAX-WS, CORBA, JTA removed from JDK
2. **Internal APIs**: sun.* and com.sun.* restricted
3. **Deployment**: Java Web Start removed
4. **CORBA**: Removed (not used by Pebble)
5. **Nashorn**: JavaScript engine deprecated

### Spring 5.x Breaking Changes

1. **XML Schema**: Update to spring-beans-5.3.xsd
2. **Servlet**: Requires Servlet 3.1+ (was 2.5)
3. **CGLIB**: No longer bundled, use JDK proxies
4. **Commons FileUpload**: API changes
5. **MockHttpServletRequest**: Package relocated

### Lucene 9.x Breaking Changes

1. **Field API**: Complete rewrite (Field.Keyword → StringField)
2. **IndexWriter**: Constructor requires IndexWriterConfig
3. **IndexReader**: open() → DirectoryReader.open()
4. **Hits**: Removed, use TopDocs
5. **DateField**: Removed, use LongPoint
6. **QueryParser**: Moved to separate module

---

## Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-14 | System Architect | Initial design document |

---

**Document Status**: READY FOR REVIEW
**Next Review Date**: Upon stakeholder feedback
**Contact**: System Architecture Designer
