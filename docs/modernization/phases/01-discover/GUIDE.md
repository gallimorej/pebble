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

## Enhanced Discovery Process ✨ **NEW**

### Current Scope
The discovery phase currently covers:
- **Environment Discovery** - See [Environment Discovery Guide](../../capabilities/environment/GUIDE.md)
- **Implementation Validation** ✨ **NEW** - Proof-of-concept validation of technical assumptions

### Process Steps
1. **Identify Discovery Areas**: Determine what aspects of the system need to be discovered
2. **Execute Discovery**: Use the appropriate capability guides for detailed methodology
3. **Document Findings**: Create comprehensive documentation of discoveries
4. **Validate Technical Assumptions** ✨ **NEW**: Execute proof-of-concept validation (optional but recommended)
5. **Validate Completeness**: Ensure all critical aspects are captured
6. **Prepare for Design**: Organize findings to inform the design phase

## Activities
1. **Planning**: Review capability guides and plan discovery approach
2. **Execution**: Follow capability-specific guidance for thorough analysis
3. **Documentation**: Create structured documentation of all findings
4. **Technical Validation** ✨ **NEW**: Validate critical technical assumptions with minimal implementation
5. **Review**: Validate completeness and accuracy of discoveries

## Discovery Areas and Guides

| Area | Guide | Status | Validation Required |
|------|-------|---------|-------------------|
| Environment | [Environment Discovery Guide](../../capabilities/environment/GUIDE.md) | Active | ✅ Architecture Compatibility |
| Containerization | [Containerization Discovery Guide](../../capabilities/environment/CONTAINERIZATION-GUIDE.md) | Active | ✅ Container Build Test |
| UI Patterns | [UI Discovery Guide](../../capabilities/ui/GUIDE.md) | Future | TBD |

## **NEW: Implementation Validation Step** ✨

### Objective
Validate discovery assumptions with minimal implementation to catch technical issues early and improve implementation success rates.

### Scope
Limited proof-of-concept to verify critical technical assumptions:
- [ ] **Container Build Test**: Verify Dockerfile builds successfully
- [ ] **Basic Runtime Test**: Confirm application starts in container
- [ ] **Architecture Test**: Build on different architectures (ARM64/AMD64)
- [ ] **Environment Test**: Verify environment variables are properly set

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

## Deliverables
- Complete discovery documentation for each active area
- **Environment analysis** including build/run instructions
- **Containerization assessment** and implementation roadmap  
- **Technical validation results** ✨ **NEW** (if validation step executed)
- Consolidated findings summary
- Recommendations for design phase

## Enhanced Quality Gates ✨ **NEW**

### Discovery Completeness Gates
Before proceeding to the design phase:
- [ ] All active discovery areas completed
- [ ] Documentation reviewed and validated
- [ ] Findings organized for design phase consumption

### **NEW: Technical Validation Gates** (Optional but Recommended)
- [ ] **Build Validation**: Critical build processes verified to work
- [ ] **Runtime Validation**: Application startup process confirmed  
- [ ] **Architecture Validation**: Multi-architecture compatibility verified
- [ ] **Environment Validation**: Critical environment setup confirmed

### Quality Gate Decision Matrix
| Scenario | Discovery Complete | Validation Required | Can Proceed to Design |
|----------|-------------------|--------------------|--------------------|
| Standard Application | ✅ | Optional | ✅ Yes |
| Legacy Application | ✅ | ⚠️ Recommended | ✅ Yes (with caution) |
| Complex Dependencies | ✅ | ✅ Required | ✅ Yes (after validation) |
| Unknown Architecture | ✅ | ✅ Required | ❌ No (must validate first) |

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
├── validation-results.md ✨ NEW
├── technical-issues.md ✨ NEW (if validation finds issues)
├── architecture-compatibility.md ✨ NEW (for multi-arch apps)
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
- **Implementation Reality Check**: Realistic timeline estimates based on validation
- **Risk Mitigation**: Specific technical risks identified and mitigation strategies

### Design Phase Prerequisites Enhanced
- Technical understanding complete ✅
- **Implementation assumptions validated** ✅ ✨ **NEW**
- Modernization strategy defined ✅  
- **Architecture compatibility confirmed** ✅ ✨ **NEW**
- Risk assessment complete ✅

## Next Steps
Once discovery is complete, findings inform the design phase to establish the target state.

---

**Guide Version**: 2.0 - Enhanced with Implementation Reality Validation  
**Key Enhancement**: Addition of proof-of-concept validation to improve implementation success rates  
**Recommended For**: All complex legacy applications and applications with containerization plans