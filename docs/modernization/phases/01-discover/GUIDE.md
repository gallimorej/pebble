# Discovery Phase Guide

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

### Process Steps
1. **Identify Discovery Areas**: Determine what aspects of the system need to be discovered
2. **Execute Discovery**: Use the appropriate capability guides for detailed methodology
3. **Document Findings**: Create comprehensive documentation of discoveries
4. **Validate Completeness**: Ensure all critical aspects are captured
5. **Prepare for Design**: Organize findings to inform the design phase

## Activities
1. **Planning**: Review capability guides and plan discovery approach
2. **Execution**: Follow capability-specific guidance for thorough analysis
3. **Documentation**: Create structured documentation of all findings
4. **Review**: Validate completeness and accuracy of discoveries

## Discovery Areas and Guides

| Area | Guide | Status |
|------|-------|---------|
| Environment | [Environment Discovery Guide](../../capabilities/environment/GUIDE.md) | Active |
| Containerization | [Containerization Discovery Guide](../../capabilities/environment/CONTAINERIZATION-GUIDE.md) | Active |
| UI Patterns | [UI Discovery Guide](../../capabilities/ui/GUIDE.md) | Future |

## Deliverables
- Complete discovery documentation for each active area
- **Environment analysis** including build/run instructions
- **Containerization assessment** and implementation roadmap
- Consolidated findings summary
- Recommendations for design phase

## Output Location
All discovery artifacts should be placed in `project-artifacts/01-discover/`

## Quality Gates
Before proceeding to the design phase:
- [ ] All active discovery areas completed
- [ ] Documentation reviewed and validated
- [ ] Findings organized for design phase consumption

## Next Steps
Once discovery is complete, findings inform the design phase to establish the target state.
