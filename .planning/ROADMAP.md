# Roadmap: Deep Packet Inspection Java

## Overview

Phases 1 and 2 record the Java runtime work, with Java 21 LTS as the current backend target. Phase 3 addresses the user-requested website cleanup across the React dashboard and legacy JSP pages while preserving the existing visual identity and application behavior.

## Phases

- [x] **Phase 1: Java 25 LTS Runtime** - Verify the Java 25 toolchain and backend build, align user-facing setup documentation, and confirm existing behavior through tests.
- [x] **Phase 2: Switch to Java 21 LTS** - Set Java 21 as the current backend build/runtime target, align documentation, and verify packaging and tests with a Java 21 JDK.
- [ ] **Phase 3: Responsive UI Polish and Website QA** - Fix verified UX gaps across SPA and legacy pages; validate responsive layouts, navigation, links, actions, feedback, and page metadata.

## Phase Details

### Phase 1: Java 25 LTS Runtime
**Goal**: The backend is verified as a Java 25 LTS application with compatible toolchain settings, accurate setup documentation, and passing builds and tests.
**Depends on**: Nothing (first phase)
**Requirements**: JRT-01, JRT-02, JRT-03
**Success Criteria** (what must be TRUE):
  1. The backend build configuration targets Java 25 LTS.
  2. The backend packages successfully and all existing backend tests pass with Java 25.
  3. Setup and product documentation clearly state Java 25 as the required runtime.
**Plans**: 1 plan

Plans:
- [x] 01-01: Verify Java 25 toolchain compatibility and build configuration, update setup/product documentation, and run the backend package and test suite.

### Phase 2: Switch to Java 21 LTS
**Goal**: The current backend build targets Java 21 LTS, the setup and product documentation consistently state Java 21, and the packaged backend passes its existing test suite under a Java 21 JDK.
**Depends on**: Phase 1
**Requirements**: JRT-04, JRT-05, JRT-06
**Success Criteria** (what must be TRUE):
  1. The backend Maven build targets Java 21.
  2. A Java 21 JDK builds and runs the backend test suite successfully and packages the WAR.
  3. Setup, product, and application-footer runtime references consistently state Java 21, and VS Code selects the requested JDK 21 installation.
**Plans**: 1 plan

Plans:
- [x] 02-01: Switch the backend target and documentation to Java 21, then verify the WAR and existing tests with a JDK 21.

### Phase 3: Responsive UI Polish and Website QA
**Goal**: All user-facing pages work reliably on mobile and desktop, with functional navigation and actions, clear feedback, useful form guidance, and accurate page metadata.
**Depends on**: Phase 2
**Requirements**: WEB-01, WEB-02, WEB-03, WEB-04, WEB-05
**Success Criteria** (what must be TRUE):
  1. React and legacy JSP routes have no unintended horizontal scrolling at mobile widths, and their content remains usable at desktop widths.
  2. Logo, mobile menus, page links, footer links, and visible buttons/actions navigate or work as labeled, with unused navigation removed.
  3. The SPA and legacy pages have accurate titles and descriptions, the favicon resolves, the custom 404 page is usable, and the copyright year is current.
  4. Forms provide visible guidance without placeholder copy, and success/error outcomes are clearly communicated to users.
  5. Existing image assets are optimized where applicable; no contact links are invented without supplied phone/email details.
**Plans**: 2 plans

Plans:
- [x] 03-01-PLAN.md — Repair and QA the React dashboard capture, navigation, forms, metadata, and viewport behavior.
- [ ] 03-02-PLAN.md — Repair legacy JSP forms/navigation/routes and complete live cross-route QA.

## Progress

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Java 25 LTS Runtime | 1/1 | Complete | 2026-11-24 |
| 2. Switch to Java 21 LTS | 1/1 | Complete | 2026-11-24 |
| 3. Responsive UI Polish and Website QA | 1/2 | In Progress | - |
