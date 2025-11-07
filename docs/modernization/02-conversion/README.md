### Phase 2: Conversion

The conversion phase focuses on executing the modernization using AI agents guided by preparation artifacts.

#### 02-conversion/

**AGENTS.md** 🔴 *Critical*
- **Purpose**: Provide AI agent with instructions, context, and guidance for conversion phase
- **File**: `AGENTS.md`
- **Content**:
  - Agent mission statement
  - Core principles for conversion
  - Technology stack decisions
  - Conversion patterns and best practices
  - Common pitfalls to avoid
  - Reference to all preparation documents
  - Testing expectations
  - Code style and conventions for target platform
- **AI Use**: Primary instruction manual for conversion work
- **Human Use**: Review and update as learnings emerge

**TODO.md** 🔴 *Critical*
- **Purpose**: Define specific conversion tasks for AI agent
- **File**: `TODO.md`
- **Content**:
  - Ordered list of conversion tasks
  - Task breakdown by module or feature
  - Dependencies between tasks
  - Completion criteria for each task
  - Status tracking (Not Started, In Progress, Complete)
- **AI Use**: Task list to work through sequentially
- **Human Use**: Track progress, adjust priorities

**Conversion Log** 🟠 *High*
- **Purpose**: Real-time record of changes made
- **File**: `conversion-log.md`
- **Content**:
  - Timestamp-ordered log of changes
  - Files created, modified, deleted
  - Major decisions made
  - Issues encountered and resolutions
  - Deviations from original plan
- **AI Use**: Automatically append entries as work progresses
- **Human Use**: Audit trail, troubleshooting reference

**Architecture Decision Records** 🟠 *High*
- **Purpose**: Document major technical decisions
- **Directory**: `decisions/`
- **Files**: `[number]-[title].md` (e.g., `001-target-stack.md`)
- **Content** (per ADR):
  - Status (Proposed, Accepted, Deprecated, Superseded)
  - Context (what necessitated the decision)
  - Decision (what was chosen)
  - Consequences (positive and negative impacts)
  - Alternatives considered
- **Template**: `template.md` in the decisions directory
- **AI Use**: Reference when making related decisions
- **Human Use**: Understand rationale, governance, and review

**Issues Log** 🟠 *High*
- **Purpose**: Track blocking issues encountered during conversion
- **Directory**: `issues/`
- **Files**: `[number]-[title].md` (e.g., `001-csrf-token-problem.md`)
- **Content** (per issue):
  - Problem description
  - Impact on conversion
  - Investigation steps taken
  - Resolution or workaround
  - Date opened and date resolved
- **AI Use**: Document when stuck or uncertain
- **Human Use**: Review and provide guidance on blocking issues

**README.md in 02-conversion/**
- **Purpose**: Phase overview and navigation
- **Content**:
  - Current conversion status
  - How to use AGENTS.md and TODO.md
  - Links to key preparation documents
  - Process for updating ADRs and issues
  - Conversion workflow explanation
