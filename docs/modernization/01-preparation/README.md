### Phase 1: Preparation

The preparation phase focuses on gathering all context needed for intelligent, accurate conversion. Items are organized by the directory structure, with priority levels indicated for each item.

#### 01-preparation/technical/

**Environment Setup** 🔴 *Critical*
- **Purpose**: Enable AI to build and run the application
- **File**: `environment-setup.md`
- **Artifacts**:
  - Exact build commands for backend (e.g., `mvn clean install`)
  - Exact build commands for frontend (e.g., `npm run build`)
  - Backend startup commands (e.g., `java -jar app.jar`)
  - Frontend dev server commands (e.g., `npm start`)
  - Required services (database, message queues, etc.)
- **AI Use**: Continuous compilation and runtime verification
- **Human Use**: Onboarding, troubleshooting, build process validation

**Dependencies** 🔴 *Critical*
- **Purpose**: Document all external dependencies and versions
- **File**: `dependencies.md`
- **Artifacts**:
  - Complete dependency manifests (pom.xml, build.gradle, package.json, etc.)
  - Exact versions: JDK, Node.js, database engine
  - Complete `.env` example files with all required variables
  - Dependency tree analysis
- **AI Use**: Setup build environment, install dependencies, identify upgrade paths
- **Human Use**: Verify prerequisites are documented, plan dependency updates

**Database Schema** 🔴 *Critical*
- **Purpose**: Document all data structures and relationships
- **File**: `database-schema.md`
- **Artifacts**:
  - ORM entity classes or DDL scripts
  - Migration scripts (Flyway, Liquibase, etc.)
  - Data access layer interfaces
  - **Entity relationship diagrams (ERD)**
  - Index and constraint documentation
- **AI Use**: Generate equivalent data access code, validate queries
- **Human Use**: Verify data model completeness and accuracy

**API Inventory** 🔴 *Critical*
- **Purpose**: Document all API endpoints and their contracts
- **File**: `api-inventory.md`
- **Artifacts**:
  - REST controller/endpoint catalog
  - Request/response formats
  - Authentication/authorization requirements per endpoint
  - OpenAPI/Swagger specifications
  - **API endpoint hierarchy maps**
  - Rate limiting and caching rules
- **AI Use**: Generate equivalent API implementations, create integration tests
- **Human Use**: Verify API completeness and behavior

**Test Infrastructure** 🔴 *Critical*
- **Purpose**: Enable AI to validate correctness continuously
- **File**: `test-infrastructure.md`
- **Artifacts**:
  - Test execution commands (backend and frontend)
  - Test directory locations (unit, integration, e2e)
  - Test configuration files
  - CI/CD pipeline configuration
  - Test data setup/teardown procedures
- **AI Use**: Continuous validation signal ("did my changes break anything?")
- **Human Use**: Test execution reference, CI/CD maintenance

**Architecture Overview** 🟠 *High*
- **Purpose**: Understand system structure and relationships
- **File**: `architecture-overview.md`
- **Artifacts**:
  - Architectural pattern description (monolith, microservices, SPA, etc.)
  - **Component diagrams** showing apps, services, database, externals
  - **Technology stack diagrams** showing layers and dependencies
  - **Deployment architecture diagrams** showing infrastructure
  - Scalability and performance characteristics
- **AI Use**: Architecture constraints for conversion decisions
- **Human Use**: Big-picture understanding, architecture review

**Code Map** 🟠 *High*
- **Purpose**: Navigate the codebase efficiently
- **File**: `code-map.md`
- **Artifacts**:
  - Package/module structure guide
  - **Module dependency graphs**
  - **Package structure trees** showing organization
  - Key directories and their purposes
  - Module dependency relationships
  - Entry points and main classes
  - Known "hacky" areas (🟢 *Low* - Technical Debt)
- **AI Use**: Navigation map for code exploration, conversion order planning
- **Human Use**: Codebase orientation, onboarding

**External Integrations** 🟠 *High*
- **Purpose**: Document external system interactions
- **Directory**: `integrations/`
- **Files**: `[system].md` (one per integration)
- **Artifacts**:
  - Integration catalog (all external systems)
  - Integration types (REST, SOAP, MQ, file transfer)
  - Data exchanged (request/response formats)
  - Error handling requirements
  - Authentication/authorization methods
  - Retry/timeout configurations
  - **Sequence diagrams** for complex integrations
  - **Data flow diagrams** showing integration patterns
- **AI Use**: Generate integration test stubs and mocks
- **Human Use**: Plan integration testing strategy

#### 01-preparation/business/

**User Roles** 🔴 *Critical*
- **Purpose**: Define who uses the system and their permissions
- **File**: `user-roles.md`
- **Artifacts**:
  - User role matrix with permissions
  - Authentication methods per role
  - **User journey maps** for critical workflows
  - **Role hierarchy diagrams** showing inheritance and permissions
  - Authorization rules and access control
- **AI Use**: Generate role-based access control tests
- **Human Use**: Validate authorization requirements

**Feature Inventory** 🔴 *Critical*
- **Purpose**: Ensure no functionality is lost in conversion
- **File**: `feature-inventory.md`
- **Artifacts**:
  - Feature hierarchy (modules → features → sub-features)
  - Top 10-15 core "killer" features
  - Business criticality ratings (Critical/High/Medium/Low)
  - Feature specifications (user story format for critical features)
  - Usage metrics if available (most-used features)
- **AI Use**: Generate feature parity test checklist
- **Human Use**: Validate completeness, prioritize conversion order

**Business Rules** 🔴 *Critical*
- **Purpose**: Preserve complex calculations and decision logic
- **File**: `business-rules.md`
- **Artifacts**:
  - Calculation formulas (tax, discounts, scoring, pricing, etc.)
  - Validation rules (required fields, formats, cross-field validation)
  - Decision trees and conditional logic (if-then-else rules)
  - State machine/workflow rules (status transitions)
  - Business constraints and invariants
- **AI Use**: Generate unit tests for business logic
- **Human Use**: Verify logic correctness after conversion

**Data Dictionary** 🟠 *High*
- **Purpose**: Document what data means in business terms
- **File**: `data-dictionary.md`
- **Artifacts**:
  - For each entity/table: business name, technical name, purpose
  - For each field: business meaning, data type, required/optional, defaults
  - Enumeration meanings and business context
  - Cascade rules in business terms
  - **Business entity relationship diagrams**
  - Data lifecycle (creation, updates, archival, deletion)
- **AI Use**: Generate comprehensive data validation tests
- **Human Use**: Understand data semantics and relationships

**Workflows** 🟠 *High*
- **Purpose**: Document multi-step business processes
- **Directory**: `workflows/`
- **Files**: `[workflow-name].md` (one per critical workflow)
- **Artifacts**:
  - **Process flow diagrams** for 5-10 critical processes
  - **State machine diagrams** for entities with status transitions
  - Decision points and branching logic
  - Who does what (swimlanes by role/system)
  - SLA/timing requirements
  - Happy path and alternative flows
- **AI Use**: Generate integration tests for workflows
- **Human Use**: Validate end-to-end process preservation

**Edge Cases** 🟡 *Moderate*
- **Purpose**: Document unusual scenarios and exception handling
- **File**: `edge-cases.md`
- **Artifacts**:
  - Known edge cases that have caused issues
  - Business rules for handling unusual scenarios
  - Error message inventory (user-facing messages)
  - Boundary conditions (max values, min values)
  - Null/empty data scenarios
  - Concurrency scenarios (two users editing same record)
- **AI Use**: Generate negative test cases
- **Human Use**: Ensure robust error handling

**Reports** 🟡 *Moderate*
- **Purpose**: Document reporting and analytics requirements
- **File**: `reports.md`
- **Artifacts**:
  - Report catalog with business purpose
  - Data sources and calculations
  - Filters, groupings, aggregations
  - Export formats (PDF, Excel, CSV)
  - Scheduling requirements (daily, weekly, on-demand)
  - Dashboard specifications and KPIs
  - Data refresh frequency
- **AI Use**: Generate report output validation tests
- **Human Use**: Ensure critical reports are recreated

#### 01-preparation/analysis-reports/

**Security Analysis** 🟡 *Moderate*
- **Purpose**: Identify security issues requiring remediation
- **Directory**: `security/`
- **Artifacts**:
  - Tool outputs (Snyk, OWASP ZAP, npm audit) - raw format
  - `security-summary.md` - Human-readable prioritized list
  - CVE details and remediation guidance
  - Vulnerability severity ratings
- **AI Use**: Task list for dependency upgrades and security fixes
- **Human Use**: Risk assessment and prioritization

**Code Quality Analysis** 🟡 *Moderate*
- **Purpose**: Identify code quality issues and refactoring opportunities
- **Directory**: `code-quality/`
- **Artifacts**:
  - Tool outputs (SonarQube, PMD, ESLint) - raw format
  - `quality-summary.md` - Human-readable summary
  - Code smell locations and descriptions
  - Refactoring task list with priorities
  - Style configuration files (🟢 *Low* - often replaced with new standards)
- **AI Use**: Specific refactoring tasks with locations
- **Human Use**: Code quality metrics and trends

**Test Coverage Analysis** 🟡 *Moderate*
- **Purpose**: Understand testing gaps and refactoring risks
- **Directory**: `test-coverage/`
- **Artifacts**:
  - Coverage tool outputs (JaCoCo, Jest, Cypress) - raw format
  - `coverage-summary.md` - Human-readable metrics
  - Coverage metrics by module/package
  - Risk analysis (high coverage = safe to refactor, low = add tests first)
  - Untested critical path identification
- **AI Use**: Risk assessment for refactoring decisions
- **Human Use**: Identify untested areas requiring attention

**README.md in analysis-reports/**
- **Purpose**: Instructions for generating and updating reports
- **Content**:
  - Commands to run each analysis tool
  - Report generation frequency
  - How to interpret results
  - Baseline vs. post-conversion comparison approach

#### 01-preparation/

**AGENTS.md** 🔴 *Critical*
- **Purpose**: Provide AI agent with instructions, context, and guidance for preparation phase
- **File**: `AGENTS.md`
- **Content**:
  - Agent mission statement for preparation tasks
  - Instructions for analyzing the codebase
  - Documentation standards and templates
  - How to run analysis tools
  - Reference to all preparation document templates
- **AI Use**: Primary instruction manual for preparation work
- **Human Use**: Review and validate AI-generated preparation documents

**TODO.md** 🔴 *Critical*
- **Purpose**: Define specific preparation and analysis tasks for AI agent
- **File**: `TODO.md`
- **Content**:
  - Ordered list of documentation tasks
  - Analysis tool execution tasks
  - Completion criteria for each task
  - Status tracking (Not Started, In Progress, Complete)
- **AI Use**: Task list to work through sequentially during preparation
- **Human Use**: Track preparation progress, adjust priorities

#### 01-preparation/PREPARATION-CHECKLIST.md

**Purpose**: Phase gate document to verify readiness for conversion

**Content**:
```markdown
# Preparation Phase Checklist

## 🔴 Critical Technical Items
- [ ] Environment setup documented and verified
- [ ] Dependencies analyzed and documented
- [ ] Database schema documented with ERD
- [ ] API inventory complete with all endpoints
- [ ] Test infrastructure documented and verified
- [ ] Build commands tested and working
- [ ] Startup commands tested and working

## 🔴 Critical Business Items
- [ ] User roles and permissions documented
- [ ] Top 10-15 features identified and documented
- [ ] Business rules catalog created with examples
- [ ] Feature specifications created for critical features

## 🟠 High Priority Items
- [ ] Architecture overview documented
- [ ] Code map created
- [ ] External integrations documented
- [ ] Data dictionary created
- [ ] 5-10 critical workflows documented

## 🟡 Moderate Priority Items
- [ ] Security vulnerability scan completed
- [ ] Code quality analysis completed
- [ ] Test coverage baseline established
- [ ] Edge cases documented
- [ ] Report requirements documented

## Conversion Planning
- [ ] Target technology stack decided
- [ ] Conversion approach documented
- [ ] Success criteria defined
- [ ] Timeline estimated

## Sign-off
- [ ] Technical Lead Approval: _______________ Date: ___________
- [ ] Business Owner Approval: _______________ Date: ___________
```