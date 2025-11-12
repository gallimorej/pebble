# Discovery Phase Guide - v2

## 🚨 CRITICAL SAFETY CONSTRAINT
**NO SOURCE CODE MODIFICATIONS ALLOWED**

During the discovery phase, **absolutely NO modifications** to source application files are permitted:
- Read and analyze source code ONLY
- Create documentation in `project-artifacts/01-discover/` ONLY  
- Use read-only tools and techniques
- Any file creation must be in the artifacts directory, never in source code

## Overview
This phase focuses on systematically analyzing and documenting the source application as it exists today. The discovery process ensures comprehensive understanding before beginning modernization.

## Discovery Process

### Current Scope
The discovery phase currently covers:
- **Environment Discovery** - See [Environment Discovery Guide](../../capabilities/environment/GUIDE.md)
- **Dependency Analysis** ✨ **NEW** - Comprehensive dependency mapping, security assessment, and modernization planning - See [Dependency Analysis Guide](../../capabilities/dependencies/GUIDE.md)
- **Implementation Validation** ✨ **NEW** - Proof-of-concept validation of technical assumptions

### Process Steps
1. **Identify Discovery Areas**: Determine what aspects of the system need to be discovered
2. **Execute Discovery**: Use the appropriate capability guides for detailed methodology
3. **Document Findings**: Create comprehensive documentation of discoveries
4. **Validate Technical Assumptions** ✨ **NEW**: Execute proof-of-concept validation (optional but recommended)
5. **AI Agent Documentation Consistency Check** ✨ **NEW**: Automated agent verification of documentation consistency
6. **Validate Completeness**: Ensure all critical aspects are captured
7. **Prepare for Design**: Organize findings to inform the design phase

## Activities
1. **Planning**: Review capability guides and plan discovery approach
2. **Execution**: Follow capability-specific guidance for thorough analysis
3. **Documentation**: Create structured documentation of all findings
4. **Technical Validation** ✨ **NEW**: Validate critical technical assumptions with minimal implementation
5. **AI Agent Consistency Verification** ✨ **NEW**: Automated agent review and validation of documentation consistency
6. **Review**: Validate completeness and accuracy of discoveries

## Discovery Areas and Guides

| Area | Guide | Status | Validation Required |
|------|-------|---------|-------------------|
| Environment | [Environment Discovery Guide](../../capabilities/environment/GUIDE.md) | Active | ✅ Architecture Compatibility |
| Containerization | [Containerization Discovery Guide](../../capabilities/environment/CONTAINERIZATION-GUIDE.md) | Active | ✅ Container Build Test |
| Dependencies | [Dependency Analysis Guide](../../capabilities/dependencies/GUIDE.md) | ✨ **NEW** | ✅ Security & Compatibility Validation |
| UI Patterns | [UI Discovery Guide](../../capabilities/ui/GUIDE.md) | Future | TBD |
| Documentation Consistency | [Documentation Consistency Guide](#documentation-consistency-verification-new) | ✨ **NEW** | ✅ Automated Validation |

## **NEW: Implementation Validation Step** ✨

### Objective
Validate discovery assumptions with minimal implementation to catch technical issues early and improve implementation success rates.

### Scope
Limited proof-of-concept to verify critical technical assumptions:
- [ ] **Container Build Test**: Verify Dockerfile builds successfully
- [ ] **Basic Runtime Test**: Confirm application starts in container
- [ ] **Architecture Test**: Build on different architectures (ARM64/AMD64)
- [ ] **Environment Test**: Verify environment variables are properly set
- [ ] **Dependency Test**: Verify critical dependencies resolve and are compatible ✨ **NEW**

### Implementation Approach
```bash
# Quick validation build (example for containerized applications)
docker build -f Dockerfile.minimal -t app-validation:test .

# Basic runtime test
docker run --rm -d --name validation-test -p 8080:8080 app-validation:test
sleep 30
curl -I http://localhost:8080/health || echo "Health check failed - document for design phase"
docker stop validation-test 2>/dev/null || true
```

### Time Investment
- **Duration**: 1-2 hours maximum
- **Benefit**: Prevents 4-6 hours of debugging during full implementation
- **ROI**: Very high - catches architecture and environment issues early

### Success Criteria
- [ ] Container builds without critical errors
- [ ] Application starts successfully (even if not fully functional)
- [ ] Health check responds or failure reason documented
- [ ] No critical environment variable issues
- [ ] Multi-architecture compatibility verified
- [ ] **Critical dependencies resolve successfully** ✨ **NEW**
- [ ] **No blocking security vulnerabilities identified** ✨ **NEW**

### When to Skip Validation
- Simple applications with standard technology stacks
- When containerization is not part of the modernization plan
- Time constraints requiring immediate progression to design phase

### Validation Documentation
Document validation results in: `project-artifacts/01-discover/validation-results.md`

```markdown
# Discovery Validation Results

## Validation Tests Executed
- [x] Container build test
- [x] Basic runtime test  
- [x] Architecture compatibility test
- [x] Environment variable test

## Results Summary
- **Overall Success**: [Yes/No/Partial]
- **Critical Issues Found**: [List any blocking issues]
- **Warnings/Notes**: [Non-critical issues for design phase]

## Technical Issues Identified
1. **Issue**: [Description]
   **Impact**: [High/Medium/Low]
   **Recommended Resolution**: [How to address in design/transform phases]

## Architecture-Specific Findings
- **ARM64 Compatibility**: [Pass/Fail/Notes]
- **AMD64 Compatibility**: [Pass/Fail/Notes]
- **Environment Variables**: [Working/Issues identified]

## Recommendations for Design Phase
- [Specific technical recommendations based on validation]
```

## **NEW: AI Agent Documentation Consistency Verification** ✨

### Objective
An AI agent automatically performs comprehensive verification of discovery documentation to ensure:
- **Consistency**: Version numbers, names, and configurations match across documents
- **Completeness**: All required sections and information are present
- **Accuracy**: Cross-references between documents are valid
- **Standards Compliance**: Documentation follows framework standards

### Agent-Driven Process
The AI agent is automatically triggered as the final step of the discovery process and performs:

1. **Document Analysis**: Reads all discovery documents in `project-artifacts/01-discover/`
2. **Consistency Verification**: Checks for inconsistencies across documents
3. **Completeness Assessment**: Validates required sections are present
4. **Cross-Reference Validation**: Verifies all internal links and references
5. **Report Generation**: Creates detailed consistency validation report
6. **Issue Resolution**: Provides specific guidance for fixing any issues found

### Agent Consistency Check Categories

#### 1. Version Number Consistency
The agent automatically identifies and validates:
- Java versions mentioned consistently across all documents
- Framework versions (Spring, Hibernate, etc.) match between discovery areas
- Database versions consistent between environment and integration documents
- Application server versions aligned across all references

**Agent Analysis Example**:
```
✅ Java Version Consistency: Java 1.8.0_251 found consistently across:
   - environment-discovery-report.md
   - containerization-assessment.md
   - java-runtime-analysis.md

❌ Spring Version Inconsistency: 
   - environment-discovery-report.md: "Spring Framework 4.3.21"
   - dependency-analysis.md: "Spring 4.3.22"
   → RECOMMENDATION: Standardize to Spring Framework 4.3.21
```

#### 2. Application Naming Consistency
The agent validates:
- Application name spelled consistently across all documents
- Component names match across architectural diagrams and text
- Database schema names consistent
- Service endpoint names aligned

**Agent Analysis Example**:
```
⚠️ Application Name Variations Found:
   - environment-discovery-report.md: "Legacy Order Management System"
   - containerization-assessment.md: "Order Management App"
   - validation-results.md: "Legacy OMS"
   → RECOMMENDATION: Standardize to "Legacy Order Management System"
```

#### 3. Configuration Consistency
The agent checks:
- Port numbers consistent across network and deployment documentation
- File paths match between environment and containerization documents
- Environment variable names consistent
- Connection strings and URLs aligned

#### 4. Cross-Reference and Completeness Validation
The agent verifies:
- Internal document links resolve correctly
- Referenced artifacts exist in project-artifacts directory
- All required sections present in each document type
- Document format compliance

### Agent Integration with Discovery Workflow

#### Automatic Trigger Points
The AI agent consistency check is automatically invoked:

1. **After Technical Validation** (if performed)
2. **Before Discovery Completion** (mandatory gate)
3. **When Discovery Summary is Generated**

#### Agent Output
The agent generates:
- **Consistency Validation Report**: `consistency-validation-report.md`
- **Issue Summary**: High-level overview of any problems found
- **Resolution Guidance**: Specific steps to fix each issue
- **Quality Gate Status**: Pass/Fail determination for proceeding to design phase

#### Agent Workflow Integration
```
Discovery Activities Complete
           ↓
Technical Validation (optional)
           ↓
🤖 AI Agent Consistency Check (automatic)
           ↓
Issues Found? → Yes → Agent provides resolution guidance → Fix issues → Re-run agent check
           ↓ No
Quality Gate: PASSED
           ↓
Proceed to Design Phase
```

### Agent-Generated Consistency Report Format

The AI agent automatically creates a comprehensive report:

```markdown
# AI Agent Documentation Consistency Verification Report

## Analysis Summary
- **Documents Analyzed**: 8 files
- **Consistency Checks**: 12 validation rules
- **Issues Found**: 3 (2 medium, 1 low priority)
- **Quality Gate Status**: ⚠️ PASSED WITH WARNINGS

## Version Consistency Analysis
✅ **Java Versions**: Consistent (Java 1.8.0_251)
❌ **Spring Framework**: Inconsistent (4.3.21 vs 4.3.22)
✅ **Database**: Consistent (MySQL 5.7.32)

## Naming Consistency Analysis
⚠️ **Application Name**: 3 variations found
✅ **Component Names**: Consistent
✅ **Database Schemas**: Consistent

## Cross-Reference Validation
✅ **Internal Links**: All valid
✅ **Artifact References**: All exist
❌ **Diagram Reference**: architecture-diagram.png not found

## Required Sections Completeness
✅ **Environment Report**: All required sections present
✅ **Containerization Assessment**: Complete
⚠️ **Validation Results**: Missing "Recommendations for Design Phase" section

## Agent Recommendations
1. **HIGH PRIORITY**: Standardize Spring Framework version references
2. **MEDIUM**: Standardize application name to "Legacy Order Management System"
3. **LOW**: Add missing diagram file or remove reference

## Quality Gate Decision
⚠️ **PASSED WITH WARNINGS** - Minor issues should be resolved but do not block progression to design phase.
```

### Agent Success Criteria
The AI agent validates:
- [ ] **Version Consistency**: All version numbers consistent across documents
- [ ] **Naming Consistency**: Application and component names standardized  
- [ ] **Cross-Reference Validation**: All internal links and references valid
- [ ] **Completeness Check**: All required sections present in documentation
- [ ] **Format Compliance**: Documentation follows framework standards

### Agent Issue Resolution Process
When the agent finds issues:

1. **Categorizes by Priority**: High/Medium/Low severity
2. **Provides Specific Guidance**: Exact changes needed to resolve issues
3. **Generates Action Items**: Clear checklist for fixing problems
4. **Re-validates After Changes**: Can be re-run to confirm fixes
5. **Updates Quality Gate Status**: Pass/Fail determination for design phase progression

### Invoking the AI Agent
To trigger the AI agent consistency check:

1. **Automatic Trigger**: The agent is automatically invoked when discovery activities are marked complete
2. **Manual Trigger**: Request "Please perform documentation consistency verification on the discovery artifacts"
3. **Re-validation**: After fixing issues, request "Please re-run the consistency check to verify fixes"

**Example Agent Request**:
> "I have completed all discovery activities in project-artifacts/01-discover/. Please perform a comprehensive documentation consistency verification and generate the consistency validation report."

The agent will:
- Analyze all files in the discovery artifacts directory
- Generate detailed consistency validation report
- Provide specific guidance for any issues found
- Determine quality gate pass/fail status for design phase progression

## Deliverables
- Complete discovery documentation for each active area
- **Environment analysis** including build/run instructions
- **Containerization assessment** and implementation roadmap
- **Dependency analysis report** ✨ **NEW** including security vulnerabilities, license compliance, and modernization recommendations
- **Technical validation results** ✨ **NEW** (if validation step executed)
- **AI Agent consistency validation report** ✨ **NEW** (automatically generated)
- Consolidated findings summary
- Recommendations for design phase

## Enhanced Quality Gates ✨ **NEW**

### Discovery Completeness Gates
Before proceeding to the design phase:
- [ ] All active discovery areas completed
- [ ] Documentation reviewed and validated for consistency
- [ ] **AI Agent consistency validation completed and passed** ✨ **NEW**
- [ ] Findings organized for design phase consumption

### **NEW: Technical Validation Gates** (Optional but Recommended)
- [ ] **Build Validation**: Critical build processes verified to work
- [ ] **Runtime Validation**: Application startup process confirmed  
- [ ] **Architecture Validation**: Multi-architecture compatibility verified
- [ ] **Environment Validation**: Critical environment setup confirmed
- [ ] **Dependency Validation**: Critical security vulnerabilities identified and documented ✨ **NEW**

### Quality Gate Decision Matrix
| Scenario | Discovery Complete | Validation Required | AI Agent Check | Can Proceed to Design |
|----------|-------------------|--------------------|--------------|-----------------------|
| Standard Application | ✅ | Optional | ✅ Pass | ✅ Yes |
| Legacy Application | ✅ | ⚠️ Recommended | ✅ Pass | ✅ Yes (with caution) |
| Complex Dependencies | ✅ | ✅ Required | ✅ Pass | ✅ Yes (after validation) |
| Unknown Architecture | ✅ | ✅ Required | ✅ Pass | ❌ No (must validate first) |
| Documentation Issues | ✅ | Any | ❌ Fail | ❌ No (must fix issues first) |

## Output Location
All discovery artifacts should be placed in `project-artifacts/01-discover/`

### Enhanced Directory Structure ✨ **NEW**
```
project-artifacts/01-discover/
├── environment/
│   ├── environment-discovery-report.md
│   ├── build-run-instructions.md
│   └── containerization/
│       └── containerization-assessment.md
├── dependencies/ ✨ NEW
│   ├── dependency-analysis-report.md
│   ├── dependency-discovery-script.sh
│   ├── maven-dependencies.txt
│   ├── maven-dependency-tree.txt
│   ├── maven-security-audit.txt
│   ├── npm-security-audit.txt
│   ├── license-analysis.md
│   └── architecture-compatibility.md
├── validation-results.md ✨ NEW
├── technical-issues.md ✨ NEW (if validation finds issues)
├── architecture-compatibility.md ✨ NEW (for multi-arch apps)
├── consistency-validation-report.md ✨ NEW (AI agent generated)
├── agent-consistency-summary.md ✨ NEW (AI agent summary)
└── discovery-summary.md
```

## Common Technical Issues and Framework Responses ✨ **NEW**

### Issue: Multi-Architecture Compatibility Problems
**Symptoms**: Container builds fail on ARM64 vs AMD64
**Discovery Enhancement**: Architecture compatibility assessment added to environment discovery
**Validation Test**: Build container on multiple architectures during validation step

### Issue: Java Runtime Version Confusion  
**Symptoms**: Unclear whether to use Java 6, 8, or newer versions
**Discovery Enhancement**: Java runtime decision framework with compatibility matrix
**Validation Test**: Verify chosen Java version works with application bytecode

### Issue: Environment Variable Propagation
**Symptoms**: Environment variables not available in container runtime context
**Discovery Enhancement**: Container environment assessment templates
**Validation Test**: Verify critical environment variables in validation container

### Issue: Dependency Build Complexity
**Symptoms**: Build process takes much longer than expected due to legacy dependencies
**Discovery Enhancement**: Build complexity assessment and realistic timeline estimation
**Validation Test**: Basic build process verification during validation step

## Integration with Design Phase ✨ **NEW**

### Enhanced Handoff to Design Phase
Discovery findings now include:
- **Technical Validation Results**: Known working configurations and identified issues
- **Architecture-Specific Notes**: Multi-platform considerations and requirements
- **Dependency Risk Assessment**: Security vulnerabilities, license compliance issues, and modernization path recommendations ✨ **NEW**
- **Implementation Reality Check**: Realistic timeline estimates based on validation
- **Risk Mitigation**: Specific technical risks identified and mitigation strategies

### Design Phase Prerequisites Enhanced
- Technical understanding complete ✅
- **Implementation assumptions validated** ✅ ✨ **NEW**
- **AI Agent consistency verification completed** ✅ ✨ **NEW**
- **Dependency security and compliance assessment complete** ✅ ✨ **NEW**
- Modernization strategy defined ✅  
- **Architecture compatibility confirmed** ✅ ✨ **NEW**
- Risk assessment complete ✅

## Next Steps
Once discovery is complete, findings inform the design phase to establish the target state.

---

**Guide Version**: 2.0 - Enhanced with Implementation Reality Validation  
**Key Enhancement**: Addition of proof-of-concept validation to improve implementation success rates
**Recommended For**: All complex legacy applications and applications with containerization
