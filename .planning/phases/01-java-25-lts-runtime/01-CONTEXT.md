# Phase 1: Java 25 LTS Runtime - Context

**Gathered:** 2026-11-23
**Status:** Ready for planning

<domain>
## Phase Boundary

Verify and document the Java 25 LTS upgrade for the existing Spring Boot backend. This covers Java and Maven compatibility, backend packaging and tests, and directly related setup and product documentation. Frontend and C++ runtime changes are excluded.

</domain>

<decisions>
## Implementation Decisions

### Runtime and framework compatibility
- **D-01:** Target Java 25 LTS.
- **D-02:** Retain the Spring Boot 3 line; use the compatible Spring Boot release required for Java 25.
- **D-03:** Update the Maven WAR plugin only as needed for the upgraded build.

### Validation and documentation
- **D-04:** Verify the backend package and full existing test suite with a Java 25 JDK.
- **D-05:** Keep setup and user-facing runtime references consistent with Java 25.

### the agent's Discretion
- Select the minimal compatible Maven/plugin versions and make narrowly scoped documentation updates.
- Preserve unrelated pre-existing worktree changes.

</decisions>

<specifics>
## Specific Ideas

No additional behavior changes are required; the runtime upgrade must preserve existing packet-processing behavior.

</specifics>

<canonical_refs>
## Canonical References

### Scope and acceptance
- `.planning/REQUIREMENTS.md` — JRT-01 through JRT-03 define this phase's completion conditions.
- `.planning/ROADMAP.md` — Phase 1 goal and success criteria.

### Build and setup
- `backend/pom.xml` — Maven and Java target configuration.
- `WINDOWS_SETUP.md` — Windows runtime setup instructions.
- `README.md` — Project runtime description and usage.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- Existing Maven backend build and test suite provide compatibility and behavior verification.

### Established Patterns
- The backend is a Maven WAR using Spring Boot 3; retain that structure.
- Setup guidance is maintained in the root README and Windows setup guide.

### Integration Points
- `backend/pom.xml` controls compiler/runtime compatibility and WAR packaging.
- The packaged backend is consumed alongside the unchanged React/Vite dashboard.

</code_context>

<deferred>
## Deferred Ideas

No frontend, C++ runtime, or new packet-inspection behavior is included in this phase.

</deferred>

---

*Phase: 01-java-25-lts-runtime*
*Context gathered: 2026-11-23*
