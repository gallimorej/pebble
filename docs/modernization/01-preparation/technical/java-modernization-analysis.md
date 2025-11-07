# Java Modernization Analysis for Pebble

**Analysis Date**: November 7, 2025
**Analyzed By**: AI Analysis
**Current Java Version**: Java 1.6 (declared in pom.xml line 312-313)
**Target Java Version**: Java 21 LTS (recommended) or Java 17 LTS (conservative)

---

## Executive Summary

Pebble is a Java EE blogging application currently targeting Java 1.6 (released December 2006, **end-of-life since February 2013**). The application consists of 516 Java source files building into a WAR file for deployment on servlet containers. This analysis identifies the current state, dependencies, and provides a comprehensive modernization roadmap.

**Critical Finding**: The codebase is **19 years behind** on the Java platform and contains numerous dependencies with known security vulnerabilities.

---

## Table of Contents

1. [Current State Analysis](#current-state-analysis)
2. [Dependency Analysis](#dependency-analysis)
3. [Java Version Evolution Path](#java-version-evolution-path)
4. [Modernization Approach](#modernization-approach)
5. [Step-by-Step Modernization Plan](#step-by-step-modernization-plan)
6. [Risk Assessment](#risk-assessment)
7. [Effort Estimation](#effort-estimation)

---

## Current State Analysis

### 1. Java Version Configuration

**pom.xml (Maven Build)**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>2.5.1</version>
    <configuration>
        <encoding>UTF-8</encoding>
        <source>1.6</source>
        <target>1.6</target>
    </configuration>
</plugin>
```
- **Source**: Java 1.6
- **Target**: Java 1.6
- **Compiler Plugin**: 2.5.1 (released 2012, very outdated)

**build.xml (Ant Build)**
```xml
<javac srcdir="${src.dir}" destdir="${classes.dir}"
       debug="yes" source="1.5" target="1.5">
```
- **Source**: Java 1.5
- **Target**: Java 1.5
- **Note**: Inconsistent with pom.xml

### 2. Application Architecture

- **Type**: Java EE Web Application (WAR packaging)
- **Servlet API**: 3.0 (Tomcat 7.0.88 API)
- **JSP Version**: 2.2
- **Web Container**: Apache Tomcat 7.0.x (documented requirement)
- **Build System**: Maven (primary) + Ant (legacy)
- **Java Files**: 516 source files
- **Framework**: Spring Framework 3.0.x + Spring Security 3.0.x

### 3. Code Patterns Observed

Based on source analysis:

✅ **Modern Patterns Already Used**:
- Generic types: `Comparator<StaticPage>`
- `@Override` annotations (Java 5+)
- Enhanced for-loops (Java 5+)
- Autoboxing/unboxing (Java 5+)

❌ **Missing Modern Features**:
- No lambda expressions (Java 8+)
- No Stream API usage (Java 8+)
- No try-with-resources (Java 7+)
- No Optional usage (Java 8+)
- Raw types still used in some comparators
- Verbose collection initialization

### 4. Package Structure

```
net.sourceforge.pebble/
├── aggregator/          # RSS/Atom feed aggregation
├── api/                 # Plugin APIs (decorators, events, permalinks)
├── audit/               # Audit trail functionality
├── comparator/          # Comparator implementations
├── confirmation/        # CAPTCHA and spam prevention
├── dao/                 # Data Access Objects (file-based)
├── decorator/           # Content decoration pipeline
├── domain/              # Core domain model
├── event/               # Event handling
├── index/               # Search indexing (Lucene)
├── permalink/           # Permalink providers
├── search/              # Search functionality
├── security/            # Security infrastructure
├── util/                # Utility classes
└── web/                 # Web layer (controllers, filters, views)
```

---

## Dependency Analysis

### Core Dependencies with Security/EOL Concerns

| Dependency | Current Version | Latest Version | Status | Risk Level |
|------------|----------------|----------------|--------|------------|
| **Spring Framework** | 3.0.7.RELEASE | 6.1.x | EOL (2016) | 🔴 CRITICAL |
| **Spring Security** | 3.0.8.RELEASE | 6.2.x | EOL (2016) | 🔴 CRITICAL |
| **commons-collections** | 3.2.2 | 4.4 | Known vulns | 🔴 CRITICAL |
| **commons-lang** | 2.6 | 3.14.0 | EOL | 🟠 HIGH |
| **commons-httpclient** | 3.1 | N/A (replaced) | EOL (2007) | 🔴 CRITICAL |
| **Lucene** | 1.4.1 | 9.9.x | Ancient | 🔴 CRITICAL |
| **Guava** | r07 | 33.x | Ancient | 🔴 CRITICAL |
| **ehcache** | 2.10.5 | 3.10.x | Major upgrade | 🟠 HIGH |
| **JDOM** | 2.0.2 | 2.0.6.1 | Outdated | 🟡 MEDIUM |
| **ROME** | 1.5.1 | 2.1.0 | Outdated | 🟡 MEDIUM |
| **iText** | 2.0.8 | 8.x.x | Major change | 🔴 CRITICAL |
| **JUnit** | 4.6 | 5.10.x | JUnit 4 | 🟠 HIGH |
| **Mockito** | 1.8.4 | 5.9.x | Very old | 🟠 HIGH |
| **twitter4j** | 2.0.10 | 4.1.2 | Outdated | 🟡 MEDIUM |
| **DWR** | 2.0.rc2 | 3.0.x | RC version | 🟠 HIGH |
| **JAXB** | 2.0.5 | 4.0.x | Removed from JDK | 🔴 CRITICAL |

### Dependencies Requiring Complete Replacement

1. **commons-httpclient 3.1** → Replace with **Apache HttpClient 5.x** or **Java 11+ HttpClient**
2. **xmlrpc 1.2-b1** → Replace with **Apache XML-RPC 3.x** or modern REST API
3. **JTidy (4aug2000r7-dev)** → Replace with **jsoup** or **JTidy 938+**
4. **radeox (1.0-b2)** → Evaluate replacement with modern markdown libraries

### Servlet API Upgrade Path

- **Current**: Servlet 3.0 / JSP 2.2 (Tomcat 7.0.88)
- **Target Option 1**: Jakarta EE 10 - Servlet 6.0 / JSP 3.1 (Tomcat 10.1.x) - **Jakarta namespace**
- **Target Option 2**: Jakarta EE 9 - Servlet 5.0 / JSP 3.0 (Tomcat 10.0.x) - **Jakarta namespace**
- **Conservative Option**: Servlet 4.0 / JSP 2.3 (Tomcat 9.0.x) - **javax namespace**

**Critical Note**: Jakarta EE 9+ requires changing all `javax.*` imports to `jakarta.*`

---

## Java Version Evolution Path

### What Changed Since Java 6

#### Java 7 (2011) - Key Features
- **try-with-resources**: Automatic resource management
- **Diamond operator**: `List<String> list = new ArrayList<>();`
- **String in switch**: Switch statements with String cases
- **Multi-catch**: `catch (IOException | SQLException ex)`
- **Binary literals**: `int binary = 0b1010;`
- **Underscores in numeric literals**: `int million = 1_000_000;`

#### Java 8 (2014) - MAJOR RELEASE
- **Lambda expressions**: `list.forEach(item -> System.out.println(item))`
- **Stream API**: Functional-style operations on collections
- **Optional**: Better null handling
- **Method references**: `String::valueOf`
- **Default methods**: Default implementations in interfaces
- **New Date/Time API**: `java.time` package replaces Date/Calendar
- **Functional interfaces**: `@FunctionalInterface` annotation
- **CompletableFuture**: Async programming support

#### Java 9 (2017)
- **Module system**: Java Platform Module System (JPMS)
- **JShell**: REPL for Java
- **Factory methods for collections**: `List.of()`, `Set.of()`, `Map.of()`
- **Private methods in interfaces**
- **Process API improvements**

#### Java 10 (2018)
- **Local variable type inference**: `var list = new ArrayList<String>();`

#### Java 11 (2018) - LTS RELEASE
- **String methods**: `isBlank()`, `lines()`, `strip()`, `repeat()`
- **Collection to Array**: `list.toArray(String[]::new)`
- **Files methods**: `Files.readString()`, `Files.writeString()`
- **HTTP Client**: New `java.net.http` API
- **Removed modules**: **JAXB removed from JDK** (must add as dependency)

#### Java 12-16 (2019-2021)
- **Switch expressions** (Java 12-14)
- **Text blocks** (Java 13-15): Multi-line strings
- **Pattern matching for instanceof** (Java 14-16)
- **Records** (Java 14-16): Immutable data carriers
- **Sealed classes** (Java 15-17): Restricted inheritance

#### Java 17 (2021) - LTS RELEASE ⭐
- **Pattern matching for switch** (preview)
- **Sealed classes** (finalized)
- **Strong encapsulation of JDK internals**
- **Foreign Function & Memory API** (incubator)
- **Removal of deprecated features**
- **Recommended minimum for new projects**

#### Java 18-20 (2022-2023)
- **UTF-8 by default**
- **Pattern matching improvements**
- **Virtual threads** (preview in 19-20)
- **Structured concurrency** (incubator)

#### Java 21 (2023) - LTS RELEASE ⭐⭐
- **Virtual threads**: Lightweight concurrency (finalized)
- **Sequenced collections**: New collection interfaces
- **Pattern matching for switch**: Finalized
- **Record patterns**: Enhanced pattern matching
- **String templates** (preview)
- **Unnamed patterns and variables**
- **Recommended target for modernization**

---

## Modernization Approach

### Target Version Recommendation

**Primary Recommendation: Java 21 LTS** ⭐
- Latest LTS release (September 2023)
- Support until September 2031 (8 years)
- Best balance of modern features and long-term support
- Virtual threads for improved scalability
- All modern language features available

**Conservative Alternative: Java 17 LTS**
- Released September 2021
- Support until September 2029 (4+ years remaining)
- Proven stability in production
- Good stepping stone if organization cautious

**Minimum Viable: Java 11 LTS**
- Released September 2018
- Support ends September 2026 (less than 1 year remaining)
- Only if organization cannot support newer versions
- **Not recommended** - approaching EOL

### Migration Strategy

We recommend a **phased, incremental approach**:

1. **Phase 1**: Java 6 → Java 11 (Foundation)
2. **Phase 2**: Java 11 → Java 17 (Stabilization)
3. **Phase 3**: Java 17 → Java 21 (Modernization)

**Rationale**:
- Gradual migration reduces risk
- Each phase can be tested and stabilized
- Java 11 is critical milestone (JAXB removal, module system)
- Can stop at Java 17 if needed (still LTS)

---

## Step-by-Step Modernization Plan

### PHASE 1: Java 6 → Java 11 (Foundation Phase)

#### Step 1.1: Environment Setup
**Duration**: 1-2 days
**Risk**: Low

- [ ] Install JDK 11 (e.g., Temurin, Oracle JDK, Amazon Corretto)
- [ ] Update JAVA_HOME environment variable
- [ ] Verify Maven/Ant work with Java 11
- [ ] Update IDE/editor to support Java 11

#### Step 1.2: Build Configuration Updates
**Duration**: 1 day
**Risk**: Low

**Maven (pom.xml)**:
```xml
<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <maven.compiler.release>11</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <release>11</release>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Ant (build.xml)** - Update or deprecate:
```xml
<javac srcdir="${src.dir}" destdir="${classes.dir}"
       debug="yes" source="11" target="11" release="11">
```

#### Step 1.3: Add JAXB Dependencies (CRITICAL)
**Duration**: 1 day
**Risk**: Medium

JAXB was **removed from JDK in Java 11**. Add to pom.xml:

```xml
<!-- JAXB API -->
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- JAXB Runtime -->
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>4.0.3</version>
    <scope>runtime</scope>
</dependency>
```

**Files using JAXB** (need testing):
- `src/main/java/net/sourceforge/pebble/dao/file/*` (JAXB generated classes)
- Any XML marshalling/unmarshalling code

#### Step 1.4: Update Servlet API
**Duration**: 2-3 days
**Risk**: Medium

**Option A: Conservative (Tomcat 9.0.x)** - Recommended for Phase 1
```xml
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>4.0.4</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>jakarta.servlet.jsp</groupId>
    <artifactId>jakarta.servlet.jsp-api</artifactId>
    <version>2.3.6</version>
    <scope>provided</scope>
</dependency>
```
- Still uses `javax.*` namespace
- Tomcat 9.0.x compatible
- Minimal code changes
- **Recommended for Java 11 migration**

**Option B: Modern (Tomcat 10.1.x)** - For later phase
- Requires `javax.*` → `jakarta.*` namespace migration
- Larger refactoring effort
- Better done in separate phase

#### Step 1.5: Update Core Dependencies
**Duration**: 1 week
**Risk**: HIGH

Update to Java 11 compatible versions:

```xml
<!-- Spring Framework 5.3.x (last version with javax) -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>5.3.31</version>
</dependency>

<!-- Spring Security 5.8.x (last version with javax) -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-web</artifactId>
    <version>5.8.8</version>
</dependency>

<!-- Apache Commons Lang3 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.14.0</version>
</dependency>

<!-- Apache Commons Collections4 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-collections4</artifactId>
    <version>4.4</version>
</dependency>

<!-- Replace commons-httpclient with Apache HttpClient 5 -->
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
    <version>5.3</version>
</dependency>

<!-- Ehcache 3.x -->
<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
    <version>3.10.8</version>
</dependency>

<!-- Lucene 9.x -->
<dependency>
    <groupId>org.apache.lucene</groupId>
    <artifactId>lucene-core</artifactId>
    <version>9.9.1</version>
</dependency>

<!-- Guava -->
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>33.0.0-jre</version>
</dependency>

<!-- Commons IO -->
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.15.1</version>
</dependency>

<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.9.0</version>
    <scope>test</scope>
</dependency>
```

**Migration Work Required**:
1. **Spring 3 → Spring 5**: Configuration changes, API updates
2. **Apache HttpClient 3 → 5**: Complete rewrite of HTTP client code
3. **Lucene 1.4 → 9.x**: Index format incompatible, reindex required
4. **Ehcache 2 → 3**: Configuration format changed
5. **JUnit 4 → 5**: Test annotations and assertions changed

#### Step 1.6: Code Compilation Fixes
**Duration**: 2-3 weeks
**Risk**: HIGH

Expected compilation issues:

1. **Removed/Changed APIs**:
   - Java EE APIs (JAXB, JAX-WS, etc.)
   - Deprecated methods removed
   - Internal JDK APIs made inaccessible

2. **Module System Issues**:
   - Automatic module names
   - Split packages
   - Classpath vs module path

3. **Dependency Conflicts**:
   - Transitive dependency versions
   - Incompatible library combinations

**Action Items**:
- [ ] Compile with Java 11
- [ ] Fix all compilation errors
- [ ] Add `--add-modules` flags if needed
- [ ] Update deprecated API usage
- [ ] Fix Spring configuration (XML → Java config recommended)

#### Step 1.7: Testing & Validation
**Duration**: 2-3 weeks
**Risk**: HIGH

- [ ] Update test framework (JUnit 4 → 5)
- [ ] Migrate test assertions
- [ ] Run full test suite
- [ ] Fix failing tests
- [ ] Perform manual smoke testing
- [ ] Test on Tomcat 9.0.x
- [ ] Performance testing
- [ ] Security testing

#### Step 1.8: Adopt Java 7-11 Features (Optional Modernization)
**Duration**: Ongoing (2-4 weeks)
**Risk**: Low-Medium

**Quick Wins** (Java 7):
```java
// Before (Java 6)
BufferedReader br = new BufferedReader(new FileReader("file.txt"));
try {
    String line = br.readLine();
} finally {
    br.close();
}

// After (Java 7)
try (BufferedReader br = new BufferedReader(new FileReader("file.txt"))) {
    String line = br.readLine();
}
```

```java
// Before
List<String> list = new ArrayList<String>();

// After (Java 7)
List<String> list = new ArrayList<>();
```

**High-Value Improvements** (Java 8):
```java
// Before (Java 6)
for (BlogEntry entry : entries) {
    if (entry.isPublished()) {
        publishedEntries.add(entry);
    }
}

// After (Java 8)
List<BlogEntry> publishedEntries = entries.stream()
    .filter(BlogEntry::isPublished)
    .collect(Collectors.toList());
```

```java
// Before
Date now = new Date();
Calendar cal = Calendar.getInstance();

// After (Java 8)
LocalDateTime now = LocalDateTime.now();
ZonedDateTime zdt = ZonedDateTime.now();
```

**Modern Improvements** (Java 11):
```java
// Before
String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

// After (Java 11)
String content = Files.readString(path);
```

### PHASE 2: Java 11 → Java 17 (Stabilization Phase)

#### Step 2.1: Update Build Configuration
**Duration**: 1 day
**Risk**: Low

Update to Java 17:
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <maven.compiler.release>17</maven.compiler.release>
</properties>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <release>17</release>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

#### Step 2.2: Update Dependencies
**Duration**: 3-5 days
**Risk**: Medium

```xml
<!-- Spring Framework 6.0.x (requires Java 17+) -->
<!-- NOTE: This requires jakarta namespace migration -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>6.0.15</version>
</dependency>

<!-- Spring Security 6.x -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-web</artifactId>
    <version>6.2.1</version>
</dependency>
```

**CRITICAL**: Spring 6 requires Jakarta EE 9+ (jakarta.* namespace)

#### Step 2.3: Jakarta EE Migration (javax → jakarta)
**Duration**: 1-2 weeks
**Risk**: HIGH

**Namespace Changes Required**:
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.servlet.http.*` → `jakarta.servlet.http.*`
- `javax.servlet.jsp.*` → `jakarta.servlet.jsp.*`
- `javax.xml.bind.*` → `jakarta.xml.bind.*`
- `javax.annotation.*` → `jakarta.annotation.*`
- `javax.inject.*` → `jakarta.inject.*`

**Tools to Help**:
- Eclipse Transformer: Automated bytecode/source transformation
- OpenRewrite: Recipe-based migration
- Manual find/replace (for small codebases)

**Steps**:
1. [ ] Update Servlet API dependencies to Jakarta 5.0+
2. [ ] Migrate Tomcat 9 → Tomcat 10.1.x
3. [ ] Run migration tool on source code
4. [ ] Update JSP files with new namespace
5. [ ] Update Spring configuration
6. [ ] Recompile and test

#### Step 2.4: Testing & Validation
**Duration**: 2 weeks
**Risk**: HIGH

- [ ] Full regression testing
- [ ] Verify all servlets and filters work
- [ ] Test Spring Security authentication/authorization
- [ ] Validate JSP rendering
- [ ] Performance testing

#### Step 2.5: Adopt Java 12-17 Features
**Duration**: Ongoing
**Risk**: Low

**Records** (Java 14-16):
```java
// Before
public class BlogMetadata {
    private final String title;
    private final String author;

    public BlogMetadata(String title, String author) {
        this.title = title;
        this.author = author;
    }

    // getters, equals, hashCode, toString
}

// After (Java 16+)
public record BlogMetadata(String title, String author) {}
```

**Text Blocks** (Java 15):
```java
// Before
String html = "<html>\n" +
              "  <body>\n" +
              "    <h1>Blog Post</h1>\n" +
              "  </body>\n" +
              "</html>";

// After (Java 15+)
String html = """
    <html>
      <body>
        <h1>Blog Post</h1>
      </body>
    </html>
    """;
```

**Pattern Matching for instanceof** (Java 16):
```java
// Before
if (obj instanceof BlogEntry) {
    BlogEntry entry = (BlogEntry) obj;
    System.out.println(entry.getTitle());
}

// After (Java 16+)
if (obj instanceof BlogEntry entry) {
    System.out.println(entry.getTitle());
}
```

**Switch Expressions** (Java 14):
```java
// Before
String status;
switch (entry.getState()) {
    case PUBLISHED:
        status = "Published";
        break;
    case DRAFT:
        status = "Draft";
        break;
    default:
        status = "Unknown";
}

// After (Java 14+)
String status = switch (entry.getState()) {
    case PUBLISHED -> "Published";
    case DRAFT -> "Draft";
    default -> "Unknown";
};
```

### PHASE 3: Java 17 → Java 21 (Modernization Phase)

#### Step 3.1: Update Build Configuration
**Duration**: 1 day
**Risk**: Low

```xml
<properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <maven.compiler.release>21</maven.compiler.release>
</properties>
```

#### Step 3.2: Update Dependencies
**Duration**: 2-3 days
**Risk**: Low

Ensure all dependencies are Java 21 compatible:
- Spring 6.1.x
- Spring Security 6.2.x
- Latest patch versions of all libraries

#### Step 3.3: Testing & Validation
**Duration**: 1 week
**Risk**: Medium

- [ ] Verify compilation
- [ ] Run full test suite
- [ ] Performance testing
- [ ] Verify on latest Tomcat 10.1.x

#### Step 3.4: Adopt Java 21 Features
**Duration**: Ongoing
**Risk**: Low

**Virtual Threads** (Java 21):
```java
// For high-concurrency operations
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
executor.submit(() -> {
    // Long-running task
});
```

**Pattern Matching for switch** (Java 21):
```java
String formatted = switch (obj) {
    case BlogEntry entry -> "Entry: " + entry.getTitle();
    case Comment comment -> "Comment by: " + comment.getAuthor();
    case null -> "null";
    default -> "Unknown";
};
```

**Sequenced Collections** (Java 21):
```java
// New methods on collections
List<BlogEntry> entries = getEntries();
BlogEntry first = entries.getFirst();  // Instead of get(0)
BlogEntry last = entries.getLast();    // Instead of get(size()-1)
entries.reversed();                     // Reversed view
```

---

## Risk Assessment

### High-Risk Areas

#### 1. Spring Framework Migration (3.0 → 6.0)
**Risk Level**: 🔴 CRITICAL
**Impact**: Core framework, affects entire application

**Risks**:
- Configuration changes (XML → Java config)
- Deprecated API removal
- Bean lifecycle changes
- Security configuration changes

**Mitigation**:
- Review Spring 4, 5, 6 migration guides
- Update incrementally (3→4→5→6)
- Extensive testing at each step
- Keep backup of working version

#### 2. Spring Security Migration (3.0 → 6.0)
**Risk Level**: 🔴 CRITICAL
**Impact**: Authentication, authorization, security

**Risks**:
- Security configuration DSL changed
- Password encoding changes
- CSRF protection changes
- OAuth/OpenID changes

**Mitigation**:
- Security audit after migration
- Test all authentication scenarios
- Verify role-based access control
- Penetration testing

#### 3. Lucene Migration (1.4.1 → 9.x)
**Risk Level**: 🔴 CRITICAL
**Impact**: Search functionality

**Risks**:
- Index format completely incompatible
- Query syntax changes
- API completely rewritten
- Performance characteristics different

**Mitigation**:
- Build index migration tool
- Reindex all content
- Parallel testing (old vs new)
- Consider alternative (Elasticsearch, Solr)

#### 4. Jakarta EE Migration (javax → jakarta)
**Risk Level**: 🟠 HIGH
**Impact**: All servlets, filters, JSPs

**Risks**:
- Namespace changes throughout codebase
- Binary incompatibility
- Third-party library compatibility
- JSP tag library updates

**Mitigation**:
- Use automated transformation tools
- Test on Jakarta-compatible server
- Verify all JSP pages render
- Check all filters and servlets

#### 5. HTTP Client Migration
**Risk Level**: 🟠 HIGH
**Impact**: External service integration

**Risks**:
- API completely different
- Connection pooling changes
- SSL/TLS configuration
- Error handling patterns

**Mitigation**:
- Identify all HTTP client usage
- Create wrapper/facade
- Test external integrations
- Monitor in production

### Medium-Risk Areas

- Ehcache 2 → 3 migration
- Test framework migration (JUnit 4 → 5)
- Date/Time API migration
- JAXB configuration with Java 11+
- Maven plugin updates

### Low-Risk Areas

- Java language syntax updates
- Build configuration changes
- JDK version upgrade (once dependencies resolved)
- Utility library updates (Commons, Guava)

---

## Effort Estimation

### Phase 1: Java 6 → Java 11

| Activity | Effort | Risk |
|----------|--------|------|
| Environment setup | 1-2 days | Low |
| Build configuration | 1 day | Low |
| JAXB dependencies | 1 day | Medium |
| Servlet API update | 2-3 days | Medium |
| Dependency updates | 1 week | High |
| Spring migration | 2 weeks | Critical |
| Spring Security migration | 1 week | Critical |
| HTTP Client migration | 1 week | High |
| Lucene migration | 2 weeks | Critical |
| Code compilation fixes | 2-3 weeks | High |
| Testing & validation | 2-3 weeks | High |
| Optional code modernization | 2-4 weeks | Medium |
| **TOTAL** | **12-18 weeks** | **High** |

### Phase 2: Java 11 → Java 17

| Activity | Effort | Risk |
|----------|--------|------|
| Build configuration | 1 day | Low |
| Dependency updates | 3-5 days | Medium |
| Jakarta EE migration | 1-2 weeks | High |
| Testing & validation | 2 weeks | High |
| Optional code modernization | 1-2 weeks | Low |
| **TOTAL** | **4-6 weeks** | **Medium** |

### Phase 3: Java 17 → Java 21

| Activity | Effort | Risk |
|----------|--------|------|
| Build configuration | 1 day | Low |
| Dependency updates | 2-3 days | Low |
| Testing & validation | 1 week | Medium |
| Optional code modernization | 1-2 weeks | Low |
| **TOTAL** | **2-3 weeks** | **Low** |

### Total Effort Estimate

**Full Migration (Java 6 → Java 21)**:
- **Minimum**: 18-27 weeks (4.5-6.75 months)
- **Recommended**: 24-32 weeks (6-8 months) with buffer
- **Team Size**: 2-3 developers (1 lead + 1-2 developers)

**Critical Path**:
1. Spring Framework migration
2. Spring Security migration
3. Jakarta EE namespace migration
4. Lucene search engine migration
5. Testing and validation

---

## Recommended Execution Plan

### Option A: Aggressive (High Risk, Faster)

**Target**: Java 6 → Java 17 in single phase
**Duration**: 20-24 weeks
**Risk**: Very High
**Not Recommended** - Too many breaking changes at once

### Option B: Phased (Recommended) ⭐

**Phase 1**: Java 6 → Java 11 (12-18 weeks)
- Establish stable baseline
- Major dependency updates
- Keep javax namespace (Tomcat 9)
- **Milestone**: Production-ready Java 11 application

**Phase 2**: Java 11 → Java 17 (4-6 weeks)
- Jakarta EE migration
- Spring 6 upgrade
- Tomcat 10.1 migration
- **Milestone**: Production-ready Java 17 application

**Phase 3**: Java 17 → Java 21 (2-3 weeks)
- Final version bump
- Adopt Java 21 features
- **Milestone**: Modern Java application

**Total Duration**: 18-27 weeks (4.5-7 months)
**Risk**: Medium (manageable)
**Benefit**: Can stop at any phase if needed

### Option C: Conservative

**Phase 1**: Java 6 → Java 11 (12-18 weeks)
**Pause**: Stabilize for 3-6 months in production
**Phase 2**: Java 11 → Java 17 (4-6 weeks)
**Pause**: Stabilize for 3-6 months in production
**Phase 3**: Java 17 → Java 21 (2-3 weeks)

**Total Duration**: 12-18 months (with stabilization periods)
**Risk**: Low
**Benefit**: Maximum production confidence at each stage

---

## Success Criteria

### Technical Criteria

- [ ] Application compiles with target Java version
- [ ] All unit tests pass (100% of original passing tests)
- [ ] All integration tests pass
- [ ] No security vulnerabilities in dependencies
- [ ] Performance within 10% of original (preferably better)
- [ ] Memory usage within acceptable limits
- [ ] No deprecated API warnings
- [ ] All dependencies up-to-date

### Business Criteria

- [ ] All user-facing features work identically
- [ ] No data loss or corruption
- [ ] No regression in user experience
- [ ] Deployment process validated
- [ ] Rollback plan tested
- [ ] Documentation updated
- [ ] Team trained on new technologies

---

## Critical Decisions Required

### Decision 1: Target Java Version
**Options**:
- [ ] Java 17 LTS (conservative, stable)
- [ ] Java 21 LTS (recommended, modern)

**Recommendation**: Java 21 LTS

### Decision 2: Spring Framework Version
**Options**:
- [ ] Spring 5.3.x (javax namespace, Java 11+)
- [ ] Spring 6.x (jakarta namespace, Java 17+)

**Recommendation**: Spring 6.x (aligns with Java 21 target)

### Decision 3: Servlet Container
**Options**:
- [ ] Tomcat 9.0.x (Servlet 4.0, javax namespace)
- [ ] Tomcat 10.1.x (Servlet 6.0, jakarta namespace)

**Recommendation**: Tomcat 10.1.x (aligns with Spring 6 and Java 21)

### Decision 4: Lucene Migration
**Options**:
- [ ] Migrate to Lucene 9.x (high effort)
- [ ] Replace with Elasticsearch (modern, scalable)
- [ ] Replace with Apache Solr (full-featured)
- [ ] Replace with simpler solution (database full-text search)

**Recommendation**: Evaluate based on search requirements and scale

### Decision 5: Migration Pace
**Options**:
- [ ] Aggressive: Single phase (high risk)
- [ ] Phased: Three phases (recommended)
- [ ] Conservative: Phased with production pauses (low risk)

**Recommendation**: Phased approach (Option B)

---

## Next Steps

### Immediate Actions (Week 1)

1. **Set up development environment**:
   - [ ] Install JDK 11, 17, and 21
   - [ ] Configure IDE for Java 21
   - [ ] Set up version control branch strategy

2. **Baseline current state**:
   - [ ] Document current build process
   - [ ] Run all existing tests (establish baseline)
   - [ ] Measure current performance metrics
   - [ ] Export current search index

3. **Dependency analysis**:
   - [ ] Run dependency vulnerability scan
   - [ ] Identify all deprecated dependencies
   - [ ] Research replacement options for EOL libraries

4. **Risk assessment**:
   - [ ] Review all custom code using Spring 3.x APIs
   - [ ] Identify all servlet/filter implementations
   - [ ] Document all external integrations
   - [ ] Catalog all Lucene usage points

5. **Team preparation**:
   - [ ] Review modernization plan with team
   - [ ] Identify knowledge gaps
   - [ ] Schedule training if needed
   - [ ] Assign roles and responsibilities

### Phase 1 Kickoff (Week 2)

- [ ] Create feature branch: `feature/java-11-migration`
- [ ] Begin Phase 1, Step 1.1 (Environment Setup)
- [ ] Set up CI/CD for Java 11
- [ ] Create testing strategy document

---

## Appendix A: Key Dependencies Replacement Matrix

| Old Dependency | Current Version | Replace With | New Version | Effort |
|----------------|----------------|--------------|-------------|--------|
| commons-lang | 2.6 | commons-lang3 | 3.14.0 | Low |
| commons-collections | 3.2.2 | commons-collections4 | 4.4 | Medium |
| commons-httpclient | 3.1 | HttpClient 5 | 5.3 | High |
| xmlrpc | 1.2-b1 | Apache XML-RPC | 3.1.3 | High |
| JTidy | 4aug2000r7-dev | jsoup | 1.17.1 | Medium |
| radeox | 1.0-b2 | CommonMark Java | 0.21.0 | High |
| Lucene | 1.4.1 | Lucene | 9.9.1 | Critical |
| Spring | 3.0.7 | Spring | 6.1.x | Critical |
| Spring Security | 3.0.8 | Spring Security | 6.2.x | Critical |
| ehcache | 2.10.5 | ehcache | 3.10.8 | High |
| Guava | r07 | Guava | 33.0.0-jre | Low |
| ROME | 1.5.1 | ROME | 2.1.0 | Medium |
| iText | 2.0.8 | OpenPDF | 1.3.x | High |
| JUnit | 4.6 | JUnit Jupiter | 5.10.1 | Medium |
| Mockito | 1.8.4 | Mockito | 5.9.0 | Low |

---

## Appendix B: Spring Framework Migration Guide

### Spring 3.0 → 5.3 → 6.0 Breaking Changes

**Configuration**:
- XML configuration deprecated (move to Java config or annotations)
- `<mvc:annotation-driven>` replaced by `@EnableWebMvc`
- Bean definition inheritance changes

**API Changes**:
- `@RequestMapping` → `@GetMapping`, `@PostMapping`, etc.
- Handler interceptors API changed
- Exception handling approach updated

**Security**:
- WebSecurityConfigurerAdapter deprecated (Spring Security 5.7+)
- Use component-based security configuration
- Lambda DSL for HTTP security

**Testing**:
- `@WebAppConfiguration` usage changes
- MockMvc setup simplified
- `@SpringBootTest` patterns (if moving to Spring Boot)

---

## Appendix C: Useful Commands

### Build Commands

```bash
# Clean build with Java 11
mvn clean install -DskipTests

# Build with specific Java version
mvn clean install -Djava.version=11

# Run tests
mvn test

# Generate dependency report
mvn dependency:tree > dependencies.txt

# Check for dependency updates
mvn versions:display-dependency-updates

# Security vulnerability check (requires plugin)
mvn org.owasp:dependency-check-maven:check
```

### Analysis Commands

```bash
# Find usage of deprecated APIs
jdeps --jdk-internals -R --class-path 'lib/*' target/classes

# List module dependencies
jdeps -s target/classes

# Find usages of removed JAXB
grep -r "javax.xml.bind" src/

# Find servlet API usages
grep -r "javax.servlet" src/
```

---

## Document Version

- **Version**: 1.0
- **Date**: November 7, 2025
- **Author**: AI Analysis
- **Status**: Draft for Review
- **Next Review**: Upon completion of Phase 1

---

## Sign-off

- [ ] **Technical Lead Review**: _______________ Date: ___________
- [ ] **Architecture Review**: _______________ Date: ___________
- [ ] **Security Review**: _______________ Date: ___________
- [ ] **Business Stakeholder Review**: _______________ Date: ___________

---

*This document provides a comprehensive analysis and modernization plan for upgrading Pebble from Java 6 to Java 21. All recommendations are based on industry best practices and real-world migration experience. Actual effort and timelines may vary based on team expertise, organizational constraints, and unforeseen technical challenges.*
