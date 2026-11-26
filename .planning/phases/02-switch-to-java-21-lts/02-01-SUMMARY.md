---
phase: "02"
plan: "01"
subsystem: backend-runtime
tags: [java21, maven, spring-boot, war, vscode]
dependency_graph:
  requires: [JRT-01, JRT-02, JRT-03]
  provides: [JRT-04, JRT-05, JRT-06]
  affects: [backend/pom.xml, README.md, WINDOWS_SETUP.md, .vscode/settings.json]
tech_stack:
  added: []
  patterns: [Java 21 Maven verification, workspace Java runtime selection]
key_files:
  created:
    - .planning/phases/02-switch-to-java-21-lts/02-01-SUMMARY.md
    - .planning/phases/02-switch-to-java-21-lts/02-VERIFICATION.md
  modified:
    - backend/pom.xml
    - README.md
    - WINDOWS_SETUP.md
    - backend/src/main/webapp/WEB-INF/views/layout/footer.jsp
    - .vscode/settings.json
decisions:
  - "Java 21 replaces Java 25 as the current runtime target; Phase 1 remains historical."
  - "Pinned VS Code workspace runtime to the local JDK 21 path at the user's request."
metrics:
  duration: "~7 minutes"
  completed_date: "2026-11-24"
  tasks: 4
  commits: 0
  files: 5
status: complete
---

# Phase 2 Plan 1: Switch to Java 21 LTS Summary

**The backend now targets Java 21, VS Code selects the requested local JDK 21 installation, and Maven clean verify passes under JDK 21.0.10.**

## Changes

- Changed `backend/pom.xml` compiler target from Java 25 to Java 21; retained Spring Boot 3.5.16 and Maven WAR plugin 3.5.1.
- Updated current Java runtime references in `README.md`, `WINDOWS_SETUP.md`, and the JSP footer to Java 21.
- Configured `.vscode/settings.json` to select `C:\Tools\jdk-21\jdk-21.0.10` as the default `JavaSE-21` runtime.
- Set Windows user-level `JAVA_HOME` to `C:\Tools\jdk-21\jdk-21.0.10` and put its `bin` first on the user `PATH`; verified `java -version` resolves to 21.0.10.
- Preserved Phase 1 Java 25 verification artifacts as historical.

## Validation

- JDK: Microsoft OpenJDK `21.0.10+7-LTS`.
- Maven: bundled Apache Maven `3.9.9`, running on Java 21.0.10.
- `mvn -f backend\pom.xml clean verify`: **BUILD SUCCESS**.
- Compiler compiled main and test sources with `--release 21`.
- Packaged WAR: `backend/target/dpi-backend-1.0.0.war`.
- Surefire: 12 tests, 0 failures, 0 errors, 0 skipped.
- Confirmed Java 21 references in current setup/product/footer docs, a valid JavaSE-21 workspace runtime setting, and the persisted Windows user Java environment.

## Requirements

- JRT-04: satisfied — backend Maven target is Java 21.
- JRT-05: satisfied — clean Maven verification passed under JDK 21.0.10 with all 12 tests.
- JRT-06: satisfied — current documentation, VS Code workspace, and Windows user environment select Java 21.

## Deviations

No application behavior changes were needed. JDK installation was performed after the first download attempt failed; the installed JDK 21 was used for the successful clean verification.
