# Phase 2: Switch to Java 21 LTS - Context

**Gathered:** 2026-11-24
**Status:** Ready for execution

<domain>
## Phase Boundary

Replace Java 25 as the current backend build/runtime target with Java 21 LTS. Align backend setup and user-facing references, and verify packaging and the existing tests using a Java 21 JDK. Keep the existing Spring Boot 3 line and do not change frontend or C++ runtimes.

</domain>

<decisions>
## Implementation Decisions

### Runtime and framework
- **D-01:** Use Java 21 LTS as the current backend build target.
- **D-02:** Retain the existing Spring Boot 3.5.16 parent and WAR plugin unless Java 21 verification proves a compatibility issue.
- **D-03:** Keep Phase 1's successful Java 25 verification as historical evidence; Phase 2 establishes the new current target.

### Documentation and validation
- **D-04:** Update root README, Windows setup instructions, and the legacy JSP footer to Java 21.
- **D-05:** Run the backend Maven verify lifecycle under a JDK 21 runtime and confirm the WAR and test results.
- **D-06:** Pin `.vscode/settings.json` to this machine's installed JDK 21 because the user explicitly selected the local path despite its reduced portability.
- **D-07:** Set the Windows user-level `JAVA_HOME` to JDK 21 and put its `bin` first on the user `PATH`, as explicitly confirmed by the user.

### the agent's Discretion
- Use an available JDK 21 distribution and a process-local JAVA_HOME for verification.
- Preserve unrelated worktree changes.

</decisions>

<specifics>
## Specific Ideas

No application behavior change is intended; this is a runtime/build target change only.

</specifics>

<canonical_refs>
## Canonical References

- `.planning/REQUIREMENTS.md` — JRT-04 through JRT-06 define Phase 2.
- `.planning/ROADMAP.md` — Phase 2 goal and success criteria.
- `backend/pom.xml` — Maven Java target.
- `README.md` and `WINDOWS_SETUP.md` — user-facing runtime/setup guidance.

</canonical_refs>

<code_context>
## Existing Code Insights

- The backend is a Maven WAR using Spring Boot 3.5.16.
- Existing Maven tests provide behavior checks; run them with Java 21.
- `.vscode/settings.json` pins the workspace to `C:\Tools\jdk-21\jdk-21.0.10` as requested.
- Windows user environment now sets `JAVA_HOME=C:\Tools\jdk-21\jdk-21.0.10` and prioritizes that JDK's `bin` directory.

</code_context>

<deferred>
## Deferred Ideas

No frontend, C++ runtime, framework-major, or packet-processing changes.

</deferred>

---

*Phase: 02-switch-to-java-21-lts*
*Context gathered: 2026-11-24*
