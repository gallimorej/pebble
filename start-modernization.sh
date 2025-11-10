#!/bin/bash

# Quick start script for modernization documentation
# This script helps you get started with the modernization process

echo "🚀 Starting Modernization Process"
echo "================================"
echo ""
echo "Project: pebble"
echo "Location: /Users/jgallimore/Projects/pebble"
echo ""
echo "📖 Documentation Location: docs/modernization/"
echo ""
echo "Next Steps:"
echo "1. Read docs/modernization/FRAMEWORK.md for overview"
echo "2. Review docs/modernization/PROJECT-CUSTOMIZATION.md"
echo "3. Start with docs/modernization/phases/01-discover/GUIDE.md"
echo "4. Begin documenting in docs/modernization/project-artifacts/01-discover/"
echo ""
echo "💡 Tip: Use 'tree docs/modernization' to see the full structure"
echo ""

# Check if tree command is available
if command -v tree >/dev/null 2>&1; then
    echo "📁 Directory Structure:"
    tree docs/modernization -I '*.md' || ls -la docs/modernization/
else
    echo "📁 Directory Structure (install 'tree' for better view):"
    find docs/modernization -type d | sort
fi
