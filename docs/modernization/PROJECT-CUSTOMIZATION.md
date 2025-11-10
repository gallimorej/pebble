# Project-Specific Customization Guide

## Project Information
- **Project Name**: pebble
- **Target Location**: /Users/jgallimore/Projects/pebble
- **Framework Copied**: Mon Nov 10 15:30:48 EST 2025

## 🚨 CRITICAL SAFETY SETUP

### Source Code Protection (MANDATORY)
Before starting discovery, implement these safety measures:

1. **Make source code read-only during Phases 1 & 2:**
   ```bash
   # Make source directories read-only (excluding docs/modernization)
   find . -path "./docs/modernization" -prune -o -type f -name "*.py" -o -name "*.java" -o -name "*.js" -o -name "*.ts" -o -name "*.cs" -print0 | xargs -0 chmod 444
   ```

2. **Commit current state to version control:**
   ```bash
   git add -A
   git commit -m "Baseline before modernization discovery"
   git tag "pre-modernization-baseline"
   ```

3. **Review SAFETY-CONSTRAINTS.md:**
   Read `docs/modernization/SAFETY-CONSTRAINTS.md` thoroughly

4. **Brief all team members and agents:**
   - Discovery and Design phases are READ-ONLY
   - No source modifications until Phase 3: Transform
   - All work goes in project-artifacts directories

## Customization Areas

### 1. Update Project-Specific Information
- Update README.md with your project details
- Customize capability guides based on your technology stack
- Add project-specific phases if needed

### 2. Technology Stack Configuration
- Document your current technology stack in discovery phase
- Update capability guides for your specific frameworks
- Add any custom discovery tools or scripts

### 3. Organizational Adaptation
- Adapt phase gates to your approval processes
- Customize validation criteria for your requirements
- Update deliverable templates for your documentation standards

### 4. Project Artifacts Setup
Begin populating the project-artifacts directories:
- `project-artifacts/01-discover/` - Discovery documentation
- `project-artifacts/02-design/` - Design specifications
- `project-artifacts/03-transform/` - Transformation logs
- `project-artifacts/04-validate/` - Validation results

## Next Steps

1. Review `FRAMEWORK.md` for the complete methodology
2. Start with `phases/01-discover/GUIDE.md`
3. Use capability guides as needed for your technology stack
4. Begin documenting your current application in the discovery phase
5. Adapt the framework to your specific organizational needs

## Framework Source
Original framework copied from: /Users/jgallimore/Projects/bennu
