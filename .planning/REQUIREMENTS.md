# Requirements: Deep Packet Inspection Java

**Defined:** 2026-11-23
**Core Value:** Users can inspect and filter packet captures locally while preserving the application’s existing behavior.

## v1 Requirements

### Java Runtime

- [ ] **JRT-01**: The backend build targets Java 25 LTS.
- [ ] **JRT-02**: The backend packages successfully and all existing backend tests pass on Java 25.
- [ ] **JRT-03**: Project setup and product documentation state the Java 25 runtime requirement.

## v2 Requirements

(None identified.)

## Out of Scope

| Feature | Reason |
|---------|--------|
| Frontend or C++ runtime upgrade | The requested change concerns the Java backend runtime. |
| New packet-inspection behavior | No feature change is needed for the runtime upgrade. |
| Spring Boot major-version migration | The requested runtime change can be delivered while staying on Spring Boot 3. |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| JRT-01 | Phase 1 | Pending |
| JRT-02 | Phase 1 | Pending |
| JRT-03 | Phase 1 | Pending |

**Coverage:**
- v1 requirements: 3 total
- Mapped to phases: 3
- Unmapped: 0

---
*Requirements defined: 2026-11-23*
*Last updated: 2026-11-23 after roadmap initialization*
