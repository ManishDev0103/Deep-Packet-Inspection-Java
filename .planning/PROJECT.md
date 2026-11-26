# Deep Packet Inspection Java

## What This Is

Deep Packet Inspection is a local packet-inspection application with a Java Spring Boot backend and a React/Vite dashboard, alongside the original C++ engine. Users upload classic Ethernet PCAP files, inspect packet and classification statistics, apply traffic-blocking rules, and download filtered captures.

## Core Value

Users can inspect and filter packet captures locally while preserving the application’s existing behavior.

## Requirements

### Validated

- ✓ The backend built and passed its existing test suite on Java 25 — Phase 1 (historical; superseded by the current Java 21 target).
- ✓ The current backend target, workspace runtime, setup guidance, and user-facing references use Java 21; Java 21.0.10 `mvn clean verify` passed — Phase 2.

### Active

- Phase 3 is improving and validating responsive UX, navigation, form feedback, and page metadata across the React dashboard and legacy JSP pages.

### Out of Scope

- Frontend or C++ runtime upgrades — this milestone targets the Java backend only.
- New packet-inspection features — not needed to change the backend runtime.
- A Spring Boot major-version migration — retain the Spring Boot 3 line while establishing Java 21 compatibility.

## Context

- The Java application is in `backend/`, built with Maven as a WAR, and uses Spring Boot.
- The React/Vite dashboard is in `frontend/`; original C++ sources are separate.
- The requested website cleanup spans the React SPA and legacy Spring MVC/JSP routes.
- The backend processes captures locally and does not persist uploaded PCAP files.
- Phase 1 verified a Java 25 build in November 2026. Phase 2 superseded the current target to Java 21 LTS and verified it on JDK 21.0.10.
- VS Code workspace settings are pinned to this machine's JDK 21 installation at `C:\Tools\jdk-21\jdk-21.0.10`.

## Constraints

- **Compatibility**: Keep the Spring Boot 3 framework line and preserve current backend behavior.
- **Workspace**: The VS Code Java runtime path is machine-specific, as explicitly requested; update it if the local JDK installation moves.
- **Scope**: Preserve packet-inspection behavior and avoid unrelated C++ or backend runtime changes.
- **Contact links**: Do not add `tel:` or `mailto:` links without user-provided contact details.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Target Java 25 LTS | Initial request was for the latest LTS runtime. | Superseded by the Java 21 target below |
| Retain Spring Boot 3 | Avoid expanding scope to a major framework migration. | ✓ Verified in Phase 1 |
| Target Java 21 LTS | User requested switching the current backend target to Java 21. | ✓ Maven verify passed on JDK 21.0.10 |
| Pin VS Code to the local JDK 21 installation | User explicitly selected a workspace-local runtime path. | ✓ Set in `.vscode/settings.json` |
| Preserve the current UI identity while fixing verified UX gaps | The UI request targets reliability and responsiveness, not a redesign. | Phase 3 |
| Omit phone/email links without contact details | Avoid inventing contact information; user approved omission. | Phase 3 |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition:**
1. Move validated requirements to Validated and update their phase reference.
2. Move invalidated requirements to Out of Scope with a reason.
3. Add any new requirements and decisions.
4. Confirm this project description remains accurate.

**After each milestone:** Reassess the core value, scope boundaries, and project context.

---
*Last updated: 2026-11-24 for Phase 3 website UX*
