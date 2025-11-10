#!/bin/bash

# Setup script for modernization documentation structure
# Usage: ./setup-modernization-docs-structure.sh <project-root-directory>
# Project root directory is required

set -e  # Exit on error

# Color output for better visibility
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if project root directory is provided
if [ -z "$1" ]; then
    echo -e "${RED}Error: Project root directory is required${NC}"
    echo "Usage: $0 <project-root-directory>"
    exit 1
fi

# Set project root directory
PROJECT_ROOT_DIR="$1"
DOCS_ROOT="${PROJECT_ROOT_DIR}/docs/modernization"

echo -e "${GREEN}Creating modernization documentation structure...${NC}"
echo "Target location: ${DOCS_ROOT}"
echo ""

# Check if directory already exists
if [ -d "${DOCS_ROOT}" ]; then
    echo -e "${YELLOW}Warning: ${DOCS_ROOT} already exists.${NC}"
    read -p "Do you want to continue? This will create missing subdirectories. (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Aborted."
        exit 1
    fi
fi

# Create main documentation directory
mkdir -p "${DOCS_ROOT}"

# Create phases directory structure
echo "Creating phases structure..."
mkdir -p "${DOCS_ROOT}/phases/01-discover"
mkdir -p "${DOCS_ROOT}/phases/02-design"
mkdir -p "${DOCS_ROOT}/phases/03-transform"
mkdir -p "${DOCS_ROOT}/phases/04-validate"

# Create capabilities directory structure
echo "Creating capabilities structure..."
mkdir -p "${DOCS_ROOT}/capabilities/environment"
mkdir -p "${DOCS_ROOT}/capabilities/ui"

# Create project-artifacts directory structure
echo "Creating project-artifacts structure..."
mkdir -p "${DOCS_ROOT}/project-artifacts/01-discover"
mkdir -p "${DOCS_ROOT}/project-artifacts/02-design"
mkdir -p "${DOCS_ROOT}/project-artifacts/03-transform"
mkdir -p "${DOCS_ROOT}/project-artifacts/04-validate"

# Create phase guide files
echo "Creating phase guide files..."
cat > "${DOCS_ROOT}/phases/01-discover/GUIDE.md" << 'EOF'
# Discovery Phase Guide

## Overview
This phase focuses on analyzing and documenting the source application as it exists today.

## Objectives
- Document current architecture and components
- Identify dependencies and integrations
- Catalog business logic and data flows
- Assess technical debt and risks
- Create comprehensive baseline documentation

## Activities
1. System architecture analysis
2. Component inventory
3. Dependency mapping
4. Risk assessment
5. Knowledge capture

## Deliverables
- System architecture documentation
- Component inventory
- Dependency mapping
- Risk assessment
- Knowledge capture artifacts

## Output Location
All discovery artifacts should be placed in `project-artifacts/01-discover/`
EOF

cat > "${DOCS_ROOT}/phases/02-design/GUIDE.md" << 'EOF'
# Design Phase Guide

## Overview
This phase focuses on designing the target application architecture and modernization approach.

## Objectives
- Define target architecture
- Plan migration strategy
- Design new components and interfaces
- Create transformation roadmap
- Establish validation criteria

## Activities
1. Target architecture definition
2. Migration strategy planning
3. Component design
4. Roadmap creation
5. Acceptance criteria establishment

## Deliverables
- Target architecture specification
- Migration plan
- Component design documents
- Acceptance criteria
- Risk mitigation strategies

## Output Location
All design artifacts should be placed in `project-artifacts/02-design/`
EOF

cat > "${DOCS_ROOT}/phases/03-transform/GUIDE.md" << 'EOF'
# Transform Phase Guide

## Overview
This phase focuses on converting the source application to the target technology stack.

## Objectives
- Execute code transformation
- Implement new components
- Migrate data structures
- Update configurations
- Document all changes and decisions

## Activities
1. Code transformation execution
2. New component implementation
3. Data structure migration
4. Configuration updates
5. Change documentation

## Deliverables
- Converted application code
- Migration scripts
- Configuration updates
- Transformation logs
- Decision documentation

## Output Location
All transformation artifacts should be placed in `project-artifacts/03-transform/`
EOF

cat > "${DOCS_ROOT}/phases/04-validate/GUIDE.md" << 'EOF'
# Validate Phase Guide

## Overview
This phase focuses on verifying the converted application is production-ready and functionally equivalent.

## Objectives
- Execute comprehensive testing
- Validate functional equivalence
- Performance testing
- Security assessment
- Production readiness review

## Activities
1. Comprehensive testing execution
2. Functional equivalence validation
3. Performance testing
4. Security assessment
5. Production readiness review

## Deliverables
- Test results and reports
- Performance benchmarks
- Security assessment
- Production deployment plan
- Sign-off documentation

## Output Location
All validation artifacts should be placed in `project-artifacts/04-validate/`
EOF

# Create capability guide files
echo "Creating capability guide files..."
cat > "${DOCS_ROOT}/capabilities/environment/GUIDE.md" << 'EOF'
# Environment Discovery Guide

## Overview
This guide provides instructions for discovering and documenting the environment setup of legacy applications.

## Key Areas to Document
1. Runtime environments
2. Dependencies and libraries
3. Configuration files
4. Environment variables
5. External integrations
6. Infrastructure requirements

## Tools and Techniques
- Dependency scanners
- Configuration analysis
- Environment mapping
- Integration discovery

## Output
Document findings in the appropriate project-artifacts phase directory.
EOF

cat > "${DOCS_ROOT}/capabilities/ui/GUIDE.md" << 'EOF'
# UI Discovery Guide

## Overview
This guide provides instructions for discovering and documenting UI patterns in legacy applications.

## Key Areas to Document
1. UI frameworks and libraries
2. Component patterns
3. Navigation flows
4. Data binding patterns
5. Styling approaches
6. User interaction patterns

## Tools and Techniques
- UI component analysis
- Flow documentation
- Pattern identification
- Accessibility assessment

## Output
Document findings in the appropriate project-artifacts phase directory.
EOF

echo ""
echo -e "${GREEN}✓ Directory structure and guide files created successfully!${NC}"
echo ""
echo "Structure created:"
echo ""
tree -d -L 4 "${DOCS_ROOT}" 2>/dev/null || find "${DOCS_ROOT}" -type d -print | sed 's|[^/]*/| |g'

echo ""
echo -e "${GREEN}Guide files created:${NC}"
echo "- phases/01-discover/GUIDE.md"
echo "- phases/02-design/GUIDE.md" 
echo "- phases/03-transform/GUIDE.md"
echo "- phases/04-validate/GUIDE.md"
echo "- capabilities/environment/GUIDE.md"
echo "- capabilities/ui/GUIDE.md"

echo ""
echo -e "${GREEN}Next steps:${NC}"
echo "1. Navigate to: cd ${DOCS_ROOT}"
echo "2. Copy FRAMEWORK.md to the docs/modernization/ directory"
echo "3. Start using the phase guides to populate project-artifacts/"
echo ""
echo -e "${YELLOW}Tip:${NC} Use 'tree ${DOCS_ROOT}' to view the full structure"
