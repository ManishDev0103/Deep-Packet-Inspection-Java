# Deep Packet Inspection Java

## What This Is

Deep Packet Inspection is a local packet-inspection application with a Java Spring Boot backend and a React/Vite dashboard, alongside the original C++ engine. Users upload classic Ethernet PCAP files, inspect packet and classification statistics, apply traffic-blocking rules, and download filtered captures.

## Core Value

Users can inspect and filter packet captures locally while preserving the application’s existing behavior.

## Requirements

### Validated

(None yet — complete and verify the current milestone.)

### Active

- [ ] The backend supports Java 25 LTS as its runtime and build target.
- [ ] The existing backend builds and its tests pass on Java 25.
- [ ] Setup and application documentation identify the Java 25 requirement.

### Out of Scope

- Frontend or C++ runtime upgrades — this milestone targets the Java backend only.
- New packet-inspection features — not needed to change the backend runtime.
- A Spring Boot major-version migration — retain the Spring Boot 3 line while establishing Java 25 compatibility.

## Context

- The Java application is in `backend/`, built with Maven as a WAR, and uses Spring Boot.
- The React/Vite dashboard is in `frontend/`; original C++ sources are separate.
- The backend processes captures locally and does not persist uploaded PCAP files.
- The Java 25 upgrade has been applied. Runtime verification uses Java 25.0.4 LTS; the build/test results are recorded as the phase is planned and verified.

## Constraints

- **Compatibility**: Keep the Spring Boot 3 framework line and preserve current backend behavior.
- **Scope**: Do not alter unrelated frontend or C++ components.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Target Java 25 LTS | The request is to upgrade to the latest LTS runtime. | — Pending phase verification |
| Retain Spring Boot 3 | Use a compatible Boot 3 release without expanding scope to a major framework migration. | — Pending phase verification |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition:**
1. Move validated requirements to Validated and update their phase reference.
2. Move invalidated requirements to Out of Scope with a reason.
3. Add any new requirements and decisions.
4. Confirm this project description remains accurate.

**After each milestone:** Reassess the core value, scope boundaries, and project context.

---
*Last updated: 2026-11-23 after GSD workflow initialization*
