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

# Phase 1: Preparation
echo "Creating 01-preparation structure..."
mkdir -p "${DOCS_ROOT}/01-preparation/technical/integrations"
mkdir -p "${DOCS_ROOT}/01-preparation/business/workflows"
mkdir -p "${DOCS_ROOT}/01-preparation/analysis-reports/security"
mkdir -p "${DOCS_ROOT}/01-preparation/analysis-reports/code-quality"
mkdir -p "${DOCS_ROOT}/01-preparation/analysis-reports/test-coverage"

# Phase 2: Conversion
echo "Creating 02-conversion structure..."
mkdir -p "${DOCS_ROOT}/02-conversion/decisions"
mkdir -p "${DOCS_ROOT}/02-conversion/issues"

# Phase 3: Validation
echo "Creating 03-validation structure..."
mkdir -p "${DOCS_ROOT}/03-validation/test-results"
mkdir -p "${DOCS_ROOT}/03-validation/acceptance-testing"
mkdir -p "${DOCS_ROOT}/03-validation/code-review"
mkdir -p "${DOCS_ROOT}/03-validation/deployment-readiness"

echo ""
echo -e "${GREEN}✓ Directory structure created successfully!${NC}"
echo ""
echo "Structure created:"
echo ""
tree -d -L 4 "${DOCS_ROOT}" 2>/dev/null || find "${DOCS_ROOT}" -type d -print | sed 's|[^/]*/| |g'

echo ""
echo -e "${GREEN}Next steps:${NC}"
echo "1. Navigate to: cd ${DOCS_ROOT}"
echo "2. Create README.md files in each phase directory"
echo "3. Start populating with documentation"
echo ""
echo -e "${YELLOW}Tip:${NC} Use 'tree ${DOCS_ROOT}' to view the full structure"
