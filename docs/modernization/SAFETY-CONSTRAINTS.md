# SAFETY CONSTRAINTS FOR AI AGENTS

## CRITICAL PHASE-BASED RESTRICTIONS

### Phase 1: Discovery - READ ONLY MODE
**🚨 ABSOLUTE PROHIBITION: NO SOURCE CODE MODIFICATIONS**

**What AI agents MUST NOT do:**
- Edit any source application files
- Create files in source application directories
- Modify configuration files in the source application
- Run any commands that could alter source code
- Use file editing tools on source application files

**What AI agents CAN do:**
- Read source application files for analysis
- Create documentation in `project-artifacts/01-discover/`
- Run read-only analysis commands
- Copy source files to artifacts directory for annotation
- Create diagrams and documentation

**Example FORBIDDEN actions:**
```bash
# ❌ FORBIDDEN - These modify source files
vi src/main.py
echo "comment" >> app.config
sed -i 's/old/new/g' source.java
git add . && git commit -m "discovery changes"
```

**Example ALLOWED actions:**
```bash
# ✅ ALLOWED - These only read and document
cat src/main.py > project-artifacts/01-discover/source-analysis.txt
find . -name "*.config" | tee project-artifacts/01-discover/config-files.txt
grep -r "database" src/ > project-artifacts/01-discover/db-references.txt
```

### Phase 2: Design - READ ONLY MODE  
**🚨 ABSOLUTE PROHIBITION: NO SOURCE CODE MODIFICATIONS**

Same restrictions as Phase 1. Source application remains untouchable.
All design work goes in `project-artifacts/02-design/`

### Phase 3: Transform - MODIFICATION PERMITTED
**✅ MODIFICATION ALLOWED**

Only in this phase are AI agents permitted to modify source application files.

### Phase 4: Validate - CONTEXT DEPENDENT
**⚠️ LIMITED MODIFICATIONS**

Agents may create test files and validation scripts but should not modify core application logic except for bug fixes.

## ENFORCEMENT MECHANISMS

### 1. Directory Protection
- Source application directories should be read-only during Phases 1 & 2
- Only `project-artifacts/` directories should be writable

### 2. Version Control Guards
- Source application should be committed and protected
- All agent work during discovery/design should be in separate directories

### 3. Agent Instructions
AI agents must be explicitly instructed:
- "You are in Discovery/Design phase - READ ONLY mode"
- "Never edit source application files"
- "All outputs go to project-artifacts directory"

### 4. Quality Gates
- Discovery cannot proceed to Design if any source files were modified
- Design cannot proceed to Transform if any source files were modified

## VIOLATION HANDLING

If source code modifications occur during Discovery or Design:
1. **Immediate Stop**: Halt all agent activity
2. **Revert Changes**: Restore source application to clean state
3. **Restart Phase**: Begin discovery/design again with clean baseline
4. **Strengthen Guards**: Implement additional protections

## RATIONALE

This strict separation ensures:
- **Source Integrity**: Original application remains unchanged during analysis
- **Clean Baseline**: Transformation begins from known good state  
- **Confidence Building**: Stakeholders trust the analysis reflects reality
- **Risk Mitigation**: No chance of accidentally breaking working system
- **Clear Audit Trail**: Sharp distinction between analysis and modification phases

---

**Remember: The goal is to build confidence in the modernization process. Strict adherence to phase boundaries is essential for organizational trust.**