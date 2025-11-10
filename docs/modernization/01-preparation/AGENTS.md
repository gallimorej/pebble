# AI Agent Instructions - Phase 1: Preparation

## Mission Statement

Your role in Phase 1 is to systematically analyze the Pebble application codebase and create comprehensive documentation that will enable intelligent, accurate conversion in Phase 2 and thorough validation in Phase 3. Focus on gathering all critical context needed for AI agents to successfully understand, build, run, and test the application.

## Primary Objectives

1. **Enable Continuous Validation**: Document everything needed for AI to build and run the application
2. **Preserve Business Logic**: Capture all business rules, workflows, and domain knowledge
3. **Map Technical Architecture**: Document system structure, dependencies, and integrations
4. **Establish Baselines**: Create metrics and analysis reports for comparison

## Environment Setup Documentation Instructions

### File: `technical/environment-setup.md`

**Priority**: 🔴 Critical - This must be completed first as it enables all other tasks

**Your Task**: Analyze the Pebble application structure and create step-by-step environment setup documentation.

#### Analysis Steps

1. **Examine Project Structure**:
   - Look for `pom.xml` (Maven), `build.xml` (Ant), `build.gradle` (Gradle)
   - Check for `package.json` files (Node.js/npm)
   - Identify any `Dockerfile` or `docker-compose.yml` files
   - Look for `.env.example` or similar configuration templates
   - **Note**: The target deployment strategy is containerized application in Docker

2. **Identify Build System**:
   - Based on your examination, this appears to be a Java project with `pom.xml` and `build.xml`
   - Document the primary build system (likely Maven based on `pom.xml` presence)
   - Note any secondary build systems or tools

3. **Document Prerequisites**:
   - Java version requirements (check `pom.xml` for compiler source/target)
   - Maven version requirements
   - Any database requirements (check for database configs)
   - Any other external dependencies or services

4. **Test Build Commands**:
   - Try `mvn clean install` (if Maven)
   - Try `ant` commands (if Ant is used)
   - Document any failures and required fixes
   - Note the output artifacts (JAR files, WAR files, etc.)

5. **Identify Startup Procedures**:
   - Look for main classes or startup scripts
   - Check for web application deployment requirements
   - Document how to run the application locally
   - Note any required configuration files or environment variables

#### Required Documentation Format

Create `technical/environment-setup.md` with these sections:

```markdown
# Environment Setup Guide

## Prerequisites

- Java version: [X.X] (specify minimum version)
- Maven version: [X.X] (if applicable)
- Database: [type and version] (if required)
- [Any other prerequisites]

## Build Commands

### Backend Build
```bash
# Primary build command
[exact command here]

# Alternative build commands (if any)
[commands here]
```

### Frontend Build (if applicable)
```bash
# Frontend build commands (if separate frontend exists)
[commands here]
```

## Startup Commands

### Backend Application
```bash
# Command to start the backend application
[exact command here]

# Alternative startup methods
[commands here]
```

### Required Services

List any services that must be running before starting the application:
- Database: [connection details, startup command]
- [Other services]

## Configuration

### Environment Variables
- `VARIABLE_NAME`: Description and example value
- [List all required environment variables]

### Configuration Files
- `file-name.properties`: Purpose and location
- [List all critical configuration files]

## Verification

### Health Check Commands
```bash
# Commands to verify the application is running correctly
[commands here]
```

### Test Endpoints (if web application)
- http://localhost:[port]/[endpoint] - Expected response
- [List key endpoints to test]

## Docker Containerization

### Legacy Application Container (Java 6)
**Purpose**: Containerize the current Java 6 version for preservation and baseline comparison

```dockerfile
# Document requirements for current version container
# - Java 6 base image (e.g., openjdk:6-jdk or custom Java 6 image)
# - Current application deployment method
# - Legacy configuration requirements
# - Port exposure for current application
```

### Legacy Container Verification
```bash
# Commands to build and test current version container
docker build -t pebble-app-legacy .
docker run -p [current-port]:[current-port] pebble-app-legacy
# Test current endpoints to establish baseline behavior
```

### Modernized Application Container Requirements
- Base image recommendations for target Java version (e.g., openjdk:11-jre-slim)
- Required system packages
- Application port(s) to expose
- Volume mount points for data/logs

### Docker Build Process
```dockerfile
# Document key Dockerfile requirements for modernized version
# - Modern base image selection
# - Application artifact copying
# - Port exposure
# - Startup command
```

### Docker Compose Setup (if applicable)
```yaml
# Document multi-service setup requirements
# - Application service configuration
# - Database service (if required)
# - Network configuration
# - Environment variable mapping
```

### Container Verification
```bash
# Commands to verify containerized application works
docker build -t pebble-app .
docker run -p [port]:[port] pebble-app
# Test endpoints in containerized environment
```

## Troubleshooting

### Common Issues
- Issue: [description]
  - Solution: [steps to resolve]

### Log Locations
- Application logs: [file path]
- Error logs: [file path]
```

#### Validation Requirements

Before marking this task complete:

1. **Test All Commands**: Every command in your documentation must work
2. **Verify Startup**: Confirm the application actually starts and runs
3. **Document Failures**: If something doesn't work, document the issue and any workarounds
4. **Include Output Examples**: Show what successful command output looks like

#### Critical Success Criteria

- [ ] All build commands are tested and working
- [ ] Application can be started successfully
- [ ] All prerequisites are clearly documented
- [ ] All configuration requirements are identified
- [ ] Health check or verification method is provided
- [ ] Common troubleshooting scenarios are covered
- [ ] Docker containerization requirements documented for current Java 6 version
- [ ] Docker containerization requirements documented for modernized version
- [ ] Container build and run process verified for both versions

## Next Steps After Environment Setup

Once `environment-setup.md` is complete and validated:

1. Move to `technical/dependencies.md` - Document all dependencies and versions
2. Continue with `technical/database-schema.md` - If database is required
3. Proceed through other critical technical documentation

## Documentation Standards

- Use exact commands, not pseudo-code
- Include version numbers where applicable
- Test everything you document
- Use clear, step-by-step instructions
- Include troubleshooting for common issues
- Make it usable by both humans and AI agents

## Notes Specific to Pebble Application

Based on the project structure visible:
- This appears to be a Java-based application
- Both Maven (`pom.xml`) and Ant (`build.xml`) are present
- There's a webapp directory suggesting a web application
- Target directory suggests Maven is the primary build system
- Look for any specific Pebble application requirements or conventions

**Containerization Target**: The Pebble application should be documented for containerization in TWO scenarios:
1. **Current State**: Container for existing Java 6 application (baseline preservation)
2. **Modernized State**: Container for modernized application with current Java version

Document both traditional deployment methods AND container-ready deployment approaches for comparison and migration planning.

Remember: This documentation will be critical for all subsequent AI agent work. Take time to ensure it's complete and accurate.