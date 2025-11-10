# Pebble AI Agent Instructions

For comprehensive documentation about working with the Pebble codebase, see:
**[docs/modernization/01-preparation/AGENTS.md](../docs/modernization/01-preparation/AGENTS.md)**

## Quick Reference
- **Build**: `mvn clean install` 
- **Deploy**: Copy `target/pebble-*.war` to Tomcat webapps
- **Access**: `http://localhost:8080/pebble/` (username/password)
- **Architecture**: Action-based MVC with file-based persistence (no database)