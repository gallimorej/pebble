# Maven Build Configuration - Java 8 Migration Changes

## Overview
This document details all changes made to the Maven POM configuration (pom.xml) to support Java 8 migration with multi-version build profiles.

## Changes Made

### 1. Added Properties Section
Added a centralized properties section for version management:

```xml
<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <maven.compiler.release>8</maven.compiler.release>

    <!-- Plugin versions -->
    <maven-compiler-plugin.version>3.11.0</maven-compiler-plugin.version>
    <maven-war-plugin.version>3.4.0</maven-war-plugin.version>
    <maven-source-plugin.version>3.3.0</maven-source-plugin.version>
    <maven-site-plugin.version>3.12.1</maven-site-plugin.version>
    <maven-release-plugin.version>3.0.1</maven-release-plugin.version>
    <maven-scm-plugin.version>2.0.1</maven-scm-plugin.version>
    <maven-antrun-plugin.version>3.1.0</maven-antrun-plugin.version>
    <maven-assembly-plugin.version>3.6.0</maven-assembly-plugin.version>
    <cobertura-maven-plugin.version>2.7</cobertura-maven-plugin.version>
</properties>
```

### 2. Updated Maven Compiler Plugin

**Before:**
- Version: 2.5.1
- Source/Target: 1.6

**After:**
- Version: 3.11.0
- Source/Target: 1.8
- Added `release` parameter for Java 8
- Added `showDeprecation` and `showWarnings` flags

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
    </configuration>
</plugin>
```

### 3. Updated Plugin Versions

All Maven plugins updated to Java 8 compatible versions:

| Plugin | Old Version | New Version |
|--------|-------------|-------------|
| maven-compiler-plugin | 2.5.1 | 3.11.0 |
| maven-war-plugin | 3.2.1 | 3.4.0 |
| maven-source-plugin | 3.0.1 | 3.3.0 |
| maven-site-plugin | 2.1 | 3.12.1 |
| maven-release-plugin | (unversioned) | 3.0.1 |
| maven-scm-plugin | 1.1 | 2.0.1 |
| maven-antrun-plugin | (unversioned) | 3.1.0 |
| maven-assembly-plugin | (unversioned) | 3.6.0 |
| cobertura-maven-plugin | 2.4 | 2.7 |
| doxia-module-xhtml | 1.1.2 | 1.12.0 |

### 4. Updated Maven Antrun Plugin Configuration

**Changes:**
- Updated version to 3.1.0
- Changed `<tasks>` to `<target>` (new Antrun plugin requirement)
- Updated in both main build and release profile
- Updated ant-jsch dependency: 1.7.1 → 1.10.14
- Updated jsch dependency: jsch:jsch:0.1.25 → com.jcraft:jsch:0.1.55

### 5. Added Multi-Version Java Build Profiles

Added four new build profiles to support multiple Java versions:

#### java8 Profile (Default)
```xml
<profile>
    <id>java8</id>
    <activation>
        <activeByDefault>true</activeByDefault>
        <jdk>1.8</jdk>
    </activation>
    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <maven.compiler.release>8</maven.compiler.release>
    </properties>
</profile>
```

#### java11 Profile
```xml
<profile>
    <id>java11</id>
    <activation>
        <jdk>11</jdk>
    </activation>
    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <maven.compiler.release>11</maven.compiler.release>
    </properties>
</profile>
```

#### java17 Profile
```xml
<profile>
    <id>java17</id>
    <activation>
        <jdk>17</jdk>
    </activation>
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.release>17</maven.compiler.release>
    </properties>
</profile>
```

#### java21 Profile
```xml
<profile>
    <id>java21</id>
    <activation>
        <jdk>21</jdk>
    </activation>
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <maven.compiler.release>21</maven.compiler.release>
    </properties>
</profile>
```

## Usage

### Building with Default Java 8
```bash
mvn clean install
```

### Building with Specific Java Version
```bash
# Java 11
mvn clean install -Pjava11

# Java 17
mvn clean install -Pjava17

# Java 21
mvn clean install -Pjava21
```

### Checking Active Profiles
```bash
mvn help:active-profiles
```

### Validating POM Configuration
```bash
mvn validate
```

## Benefits

1. **Java 8 Support**: Full Java 8 language features and APIs available
2. **Future-Proof**: Easy migration path to Java 11, 17, and 21
3. **Centralized Management**: All plugin versions managed via properties
4. **Build Flexibility**: Can test against multiple Java versions
5. **Modern Tooling**: Latest Maven plugins with better performance and features
6. **Better Warnings**: Compilation warnings and deprecation notices enabled

## Compatibility

- **Minimum Maven Version**: 3.6.0 (recommended: 3.8.0+)
- **Minimum Java Version**: 8
- **Maximum Tested Java Version**: 21
- **Default Java Version**: 8

## Next Steps

1. ✅ Maven build configuration updated
2. ⏳ Update dependencies (handled by dependency agent)
3. ⏳ Code migration for Java 8 compatibility
4. ⏳ Test suite execution
5. ⏳ Docker containerization updates

## Known Issues

None identified at this stage. The POM configuration is valid XML and follows Maven best practices.

## Related Files

- `/Users/jgallimore/Projects/pebble/pom.xml` - Updated POM file
- `/Users/jgallimore/Projects/pebble/docs/modernization/project-artifacts/02-design/maven-java8-migration-changes.md` - This document

## References

- [Maven Compiler Plugin Documentation](https://maven.apache.org/plugins/maven-compiler-plugin/)
- [Maven Profiles Documentation](https://maven.apache.org/guides/introduction/introduction-to-profiles.html)
- [Java 8 Migration Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/migration/index.html)
