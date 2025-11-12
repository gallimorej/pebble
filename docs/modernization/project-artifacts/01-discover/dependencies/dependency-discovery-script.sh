#!/bin/bash
# Comprehensive Dependency Discovery Script for Pebble Project
# This script performs comprehensive dependency analysis for the Pebble blogging application

DISCOVERY_DIR="docs/modernization/project-artifacts/01-discover/dependencies"
PROJECT_ROOT=$(pwd)

# Ensure we're in the project root
if [ ! -f "pom.xml" ]; then
    echo "Error: pom.xml not found. Please run this script from the Pebble project root directory."
    exit 1
fi

echo "=== PEBBLE DEPENDENCY ANALYSIS ==="
echo "Starting comprehensive dependency discovery for Pebble application..."
echo ""

# Create analysis summary file
echo "=== COMPREHENSIVE DEPENDENCY ANALYSIS ===" > "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Application: Pebble Blogging Platform" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Analysis Date: $(date)" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Analysis Host: $(hostname)" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Analysis Architecture: $(uname -m)" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Java Version: $(java -version 2>&1 | head -n 1)" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Maven Version: $(mvn -version 2>&1 | head -n 1)" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"

# Maven Dependency Analysis
echo "--- Maven Dependency Analysis ---" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"

echo "1. Collecting direct dependencies..."
mvn dependency:list > "$DISCOVERY_DIR/maven-dependencies.txt" 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Direct dependencies collected" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
else
    echo "❌ Failed to collect direct dependencies" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

echo "2. Building dependency tree..."
mvn dependency:tree > "$DISCOVERY_DIR/maven-dependency-tree.txt" 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Dependency tree generated" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
else
    echo "❌ Failed to generate dependency tree" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

echo "3. Analyzing dependency usage..."
mvn dependency:analyze > "$DISCOVERY_DIR/maven-analysis.txt" 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Dependency analysis completed" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
else
    echo "❌ Failed to analyze dependency usage" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

echo "4. Checking for dependency updates..."
mvn versions:display-dependency-updates > "$DISCOVERY_DIR/maven-updates.txt" 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Dependency updates checked" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
else
    echo "❌ Failed to check dependency updates" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

echo "5. Running security vulnerability scan..."
mvn org.owasp:dependency-check-maven:check > "$DISCOVERY_DIR/maven-security-audit.txt" 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Security vulnerability scan completed" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
else
    echo "⚠️ OWASP dependency check not configured or failed - manual security review required" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

echo "6. Checking build plugin versions..."
mvn versions:display-plugin-updates > "$DISCOVERY_DIR/maven-plugin-updates.txt" 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Plugin update analysis completed" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
else
    echo "❌ Failed to analyze plugin updates" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

echo "7. Analyzing effective POM..."
mvn help:effective-pom > "$DISCOVERY_DIR/maven-effective-pom.xml" 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Effective POM generated" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
else
    echo "❌ Failed to generate effective POM" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

# Architecture-specific analysis
echo "" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "--- Architecture-Specific Analysis ---" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"

echo "8. Searching for native libraries..."
find . -name "*.so" -o -name "*.dll" -o -name "*.dylib" 2>/dev/null > "$DISCOVERY_DIR/native-libraries.txt"
native_count=$(wc -l < "$DISCOVERY_DIR/native-libraries.txt" 2>/dev/null || echo "0")
echo "Found $native_count native libraries" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"

echo "9. Searching for architecture-specific files..."
find . -name "*amd64*" -o -name "*arm64*" -o -name "*x86*" -o -name "*i386*" 2>/dev/null > "$DISCOVERY_DIR/arch-specific-files.txt"
arch_count=$(wc -l < "$DISCOVERY_DIR/arch-specific-files.txt" 2>/dev/null || echo "0")
echo "Found $arch_count architecture-specific files" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"

# Repository and settings analysis
echo "" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "--- Build Configuration Analysis ---" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"

echo "10. Analyzing repository configuration..."
grep -r "repository" pom.xml > "$DISCOVERY_DIR/maven-repositories.txt" 2>/dev/null
repo_count=$(wc -l < "$DISCOVERY_DIR/maven-repositories.txt" 2>/dev/null || echo "0")
echo "Found $repo_count repository configurations" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"

echo "11. Collecting effective settings..."
mvn help:effective-settings > "$DISCOVERY_DIR/maven-effective-settings.xml" 2>&1
if [ $? -eq 0 ]; then
    echo "✅ Effective settings collected" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
else
    echo "❌ Failed to collect effective settings" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
fi

# Analysis completion and summary
echo "" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "--- Analysis Complete ---" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Analysis completed at: $(date)" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Results saved to: $DISCOVERY_DIR/" | tee -a "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
echo "Generated files:" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"
ls -la "$DISCOVERY_DIR/" >> "$DISCOVERY_DIR/dependency-analysis-summary.txt"

echo ""
echo "=== DEPENDENCY ANALYSIS COMPLETE ==="
echo "📁 Results location: $DISCOVERY_DIR/"
echo "📄 Start with: dependency-analysis-summary.txt"
echo "📊 Key files to review:"
echo "   • maven-dependencies.txt (direct dependencies)"
echo "   • maven-dependency-tree.txt (complete tree)"
echo "   • maven-security-audit.txt (security vulnerabilities)"
echo "   • maven-updates.txt (available updates)"
echo ""
echo "Next step: Review the dependency-analysis-summary.txt file and then examine individual analysis files."