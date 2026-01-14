# POM Configuration Verification Checklist

## Pre-Build Verification

### 1. Validate POM XML Structure
```bash
mvn validate
```
**Expected**: BUILD SUCCESS with no errors

### 2. Check Active Profiles
```bash
mvn help:active-profiles
```
**Expected Output**:
```
Active Profiles for Project 'org.sourceforge.pebble:pebble:war:2.6.7-SNAPSHOT':

The following profiles are active:
 - java8 (source: pom)
```

### 3. Display Effective POM
```bash
mvn help:effective-pom | grep -A 5 "maven.compiler"
```
**Expected Output**:
```xml
<maven.compiler.source>1.8</maven.compiler.source>
<maven.compiler.target>1.8</maven.compiler.target>
<maven.compiler.release>8</maven.compiler.release>
```

### 4. Check Plugin Versions
```bash
mvn help:effective-pom | grep -A 2 "maven-compiler-plugin"
```
**Expected**: Version 3.11.0

## Build Verification

### 5. Clean Build Test
```bash
mvn clean compile -DskipTests
```
**Expected**:
- BUILD SUCCESS
- No compilation errors
- Java 8 bytecode generated (version 52.0)

### 6. Verify Java 8 Bytecode
```bash
javap -v target/classes/net/sourceforge/pebble/Configuration.class | head -20
```
**Expected**: `major version: 52` (Java 8)

### 7. Full Build with Tests
```bash
mvn clean install
```
**Expected**: BUILD SUCCESS (tests may fail initially due to dependency updates)

## Multi-Version Profile Testing

### 8. Test Java 11 Profile
```bash
mvn clean compile -Pjava11 -DskipTests
```
**Expected**: BUILD SUCCESS with Java 11 bytecode

### 9. Test Java 17 Profile
```bash
mvn clean compile -Pjava17 -DskipTests
```
**Expected**: BUILD SUCCESS with Java 17 bytecode

### 10. Test Java 21 Profile
```bash
mvn clean compile -Pjava21 -DskipTests
```
**Expected**: BUILD SUCCESS with Java 21 bytecode

## Configuration Verification

### 11. Verify Properties Section
Check that pom.xml contains:
```xml
<properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
    <maven.compiler.release>8</maven.compiler.release>
    <maven-compiler-plugin.version>3.11.0</maven-compiler-plugin.version>
    <!-- ... other properties ... -->
</properties>
```

### 12. Verify Compiler Plugin Configuration
Check that compiler plugin uses:
- Version: ${maven-compiler-plugin.version}
- Source: ${maven.compiler.source}
- Target: ${maven.compiler.target}
- Release: ${maven.compiler.release}
- showDeprecation: true
- showWarnings: true

### 13. Verify All Profiles Present
Check pom.xml contains profiles:
- java8 (activeByDefault: true)
- java11
- java17
- java21

### 14. Verify Plugin Versions Updated
All plugins use properties-based versioning:
- maven-compiler-plugin: 3.11.0
- maven-war-plugin: 3.4.0
- maven-source-plugin: 3.3.0
- maven-site-plugin: 3.12.1
- maven-release-plugin: 3.0.1
- maven-scm-plugin: 2.0.1
- maven-antrun-plugin: 3.1.0
- maven-assembly-plugin: 3.6.0

### 15. Verify Antrun Plugin Changes
Check that maven-antrun-plugin uses:
- `<target>` instead of `<tasks>` in configuration
- Updated ant-jsch: 1.10.14
- Updated jsch: com.jcraft:jsch:0.1.55

## Integration Verification

### 16. Verify WAR Packaging
```bash
mvn clean package -DskipTests
ls -lh target/pebble-*.war
```
**Expected**: WAR file successfully created

### 17. Verify Source JAR Generation
```bash
ls -lh target/pebble-*-sources.jar
```
**Expected**: Sources JAR file present

### 18. Check for Warnings
```bash
mvn clean compile 2>&1 | grep -i "warning"
```
**Expected**: Review any deprecation or compilation warnings

### 19. Dependency Tree Analysis
```bash
mvn dependency:tree > dependency-tree.txt
```
**Expected**: No conflicts, all dependencies resolved

### 20. Validate Release Profile
```bash
mvn help:active-profiles -Prelease
```
**Expected**: Both java8 and release profiles active

## Success Criteria

- ✅ POM validates without errors
- ✅ Default profile is java8
- ✅ Compilation succeeds with Java 8 target
- ✅ All plugin versions updated to Java 8 compatible versions
- ✅ Multi-version profiles work correctly
- ✅ WAR and sources JAR generated successfully
- ✅ No XML syntax errors in POM
- ✅ Properties-based version management working

## Troubleshooting

### Issue: "release version 8 not supported"
**Solution**: Ensure Maven compiler plugin is version 3.6.0 or higher

### Issue: "unknown element: target"
**Solution**: Ensure maven-antrun-plugin is version 3.0.0 or higher

### Issue: Multiple profiles active
**Solution**: This is expected - java8 is default, can be overridden with -Pjava11/17/21

### Issue: Compilation fails with Java 6/7 errors
**Solution**: Verify JAVA_HOME points to Java 8+ and maven-compiler-plugin configuration is correct

## Documentation References

- Maven Build Configuration Changes: `maven-java8-migration-changes.md`
- Project POM: `/Users/jgallimore/Projects/pebble/pom.xml`
