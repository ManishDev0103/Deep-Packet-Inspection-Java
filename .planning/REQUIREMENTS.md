# Requirements: Deep Packet Inspection Java

**Defined:** 2026-11-23
**Core Value:** Users can inspect and filter packet captures locally while preserving the application’s existing behavior.

## v1 Requirements

### Java Runtime

Historical Java 25 requirements were satisfied in Phase 1 and are superseded by the current Java 21 requirements below.

- [x] **JRT-01**: The backend build targets Java 25 LTS.
- [x] **JRT-02**: The backend packages successfully and all existing backend tests pass on Java 25.
- [x] **JRT-03**: Project setup and product documentation state the Java 25 runtime requirement.
- [x] **JRT-04**: The current backend Maven build target is Java 21 LTS.
- [x] **JRT-05**: The backend packages and all existing tests pass when built and run with a Java 21 JDK.
- [x] **JRT-06**: Current setup and product documentation state the Java 21 runtime requirement, and the workspace and Windows user environment select the configured JDK 21 runtime.

## v2 Requirements

### Website UX

- [ ] **WEB-01**: The React dashboard and legacy pages remain usable across mobile and desktop viewport sizes without unintended horizontal overflow.
- [ ] **WEB-02**: The logo, responsive navigation, page/footer links, and visible buttons/actions work as labeled, and unused navigation is removed.
- [ ] **WEB-03**: User-facing routes have accurate page titles and meta descriptions, a working favicon, a useful custom 404 page, and the current copyright year.
- [ ] **WEB-04**: Forms use visible guidance rather than placeholder-only prompts, and user actions show clear success or error feedback.
- [ ] **WEB-05**: Existing image assets are checked and optimized where applicable; pages do not ship unnecessarily large images.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Phone and email links | No contact details are present; the user approved omitting these rather than inventing contact information. |
| New packet-inspection behavior | The website cleanup should preserve the existing application behavior. |
| Spring Boot major-version migration | Not part of the website UX request. |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| JRT-01 | Phase 1 | Complete |
| JRT-02 | Phase 1 | Complete |
| JRT-03 | Phase 1 | Complete |
| JRT-04 | Phase 2 | Complete |
| JRT-05 | Phase 2 | Complete |
| JRT-06 | Phase 2 | Complete |
| WEB-01 | Phase 3 | Pending |
| WEB-02 | Phase 3 | Pending |
| WEB-03 | Phase 3 | Pending |
| WEB-04 | Phase 3 | Pending |
| WEB-05 | Phase 3 | Pending |

**Coverage:**
- v1 requirements: 6 total
- v2 requirements: 5 total
- Mapped to phases: 11
- Unmapped: 0

---
*Requirements defined: 2026-11-23*
*Last updated: 2026-11-25 for Phase 3 website UX*
