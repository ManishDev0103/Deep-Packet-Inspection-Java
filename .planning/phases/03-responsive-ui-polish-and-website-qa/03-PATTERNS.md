# Phase 3: Responsive UI Polish and Website QA - Pattern Map

**Mapped:** 2026-11-25
**Files analyzed:** 13
**Analogs found:** 11 / 13

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|-------------------|------|-----------|----------------|---------------|
| `frontend/src/pages/InspectorPage.jsx` | component/page | request-response | itself / `frontend/src/pages/InspectorPage.jsx` | exact |
| `frontend/src/pages/InspectorPage.css` | utility/styles | transform/layout | itself / `frontend/src/pages/InspectorPage.css` | exact |
| `frontend/src/index.css` | config/styles | transform/layout | itself / `frontend/src/index.css` | exact |
| `frontend/src/components/RuleInput.jsx` | component/form | transform/input | itself / `frontend/src/components/RuleInput.jsx` | exact |
| `frontend/index.html` | config/metadata | file-I/O | itself / `frontend/index.html` | exact |
| `backend/src/main/webapp/WEB-INF/views/layout/header.jsp` | layout/template | request-response | itself / `header.jsp` | exact |
| `backend/src/main/webapp/WEB-INF/views/layout/footer.jsp` | layout/template | request-response | itself / `footer.jsp` | exact |
| `backend/src/main/webapp/WEB-INF/views/report.jsp` | view/template | request-response | itself / `report.jsp` | exact |
| `backend/src/main/webapp/WEB-INF/views/rules.jsp` | component/form/view | request-response | itself / `rules.jsp` | exact |
| `backend/src/main/webapp/WEB-INF/views/error/404.jsp` | error view | request-response | itself / `404.jsp` | exact |
| `backend/src/main/java/com/packetinspector/dpi/controller/legacy/DpiLegacyController.java` | controller | request-response | itself / `DpiLegacyController.java` | exact |
| `frontend/package.json` / QA scripts | config/tooling | batch | `backend/pom.xml` test setup | role-match |
| Backend or frontend responsive QA tests | test | batch/request-response | `DpiLegacyControllerTest.java` and `DpiApiControllerTest.java` | role-match |

## Pattern Assignments

### `frontend/src/pages/InspectorPage.jsx` (component/page, request-response)

**Analog:** `frontend/src/pages/InspectorPage.jsx`

**Preserve-first imports and state pattern** (lines 1-43):
```jsx
import { useState } from 'react'
import { Metric } from '../components/Metric.jsx'
import { RuleInput } from '../components/RuleInput.jsx'
import { useCaptureProcessing } from '../hooks/useCaptureProcessing.js'
import './InspectorPage.css'
```

**Brand/mobile navigation pattern** (lines 75-101):
```jsx
<a className="brand" href="/" aria-label="Packet Inspector home">
  {/* existing brand mark */}
</a>

<div className="topbar-nav" aria-label="Primary navigation">
  <a href="/legacy/report" className="nav-link">Legacy Report</a>
  <a href="/legacy/rules" className="nav-link">Legacy Rules</a>
</div>

<button
  className={`hamburger${menuOpen ? ' is-open' : ''}`}
  type="button"
  aria-label={menuOpen ? 'Close navigation menu' : 'Open navigation menu'}
  aria-expanded={menuOpen}
  onClick={() => setMenuOpen((o) => !o)}
>
```

Use `NAV_LINKS` for only the audited destinations: `/`, `/legacy/report`, and `/legacy/rules`. Preserve close-on-navigation behavior and `aria-expanded`; do not add a fourth entry or invent a contact link.

**Feedback pattern** (lines 171-183):
```jsx
{error && (
  <div className="error-banner" role="alert" aria-live="assertive">
    <span aria-hidden="true">!</span>{error}
  </div>
)}

{report && (
  <div class="success-banner" role="status" aria-live="polite">
    <span aria-hidden="true">✓</span>
    Analysis complete — {formatNumber(report.totalPackets)} packets inspected.
  </div>
)}
```

The JSX should keep these roles and nearby placement. The success/error patterns already work; only fix verified copy or layout gaps.

**Footer pattern** (lines 265-268):
```jsx
<footer className="footer">
  <span>PACKET INSPECTOR <b>·</b> LOCAL PCAP ANALYSIS</span>
  <span>© {new Date().getFullYear()} · Processing stays on this machine</span>
</footer>
```

This is the existing dynamic copyright pattern to preserve. It is not the source for footer links because the React footer currently has no link list.

### `frontend/src/pages/InspectorPage.css` (utility/styles, responsive layout)

**Analog:** `frontend/src/pages/InspectorPage.css`

**Global overflow guard** (see `frontend/src/index.css`, lines 7-8):
```css
html, body { margin: 0; padding: 0; overflow-x: hidden; width: 100%; min-width: 320px; min-height: 100vh; }
*, *::before, *::after { box-sizing: border-box; }
img { max-width: 100%; height: auto; }
```

This guard is present; do not compensate by masking an actual overflow bug globally. Inspect widths and fix the offending grid/table/text first.

**Responsive layout pattern** (lines 122-145):
```css
@media (max-width: 760px) {
  main { padding: 0 16px 48px; }
  .workspace { grid-template-columns: 1fr; }
  .action-row { grid-column: auto; align-items: flex-start; flex-direction: column; gap: 14px; }
  .process-button { width: 100%; }
  .metric-grid { grid-template-columns: 1fr 1fr; }
  .report-details, .report-extras { grid-template-columns: 1fr; }
  .footer { margin: 0 16px; padding-inline: 0; }
}

@media (max-width: 420px) {
  .panel { padding: 18px 15px; }
  .rule-grid { grid-template-columns: 1fr; }
  .report-header { align-items: flex-start; flex-direction: column; }
  .download-button { width: 100%; }
  .footer { align-items: flex-start; flex-direction: column; }
}
```

**Mobile menu pattern** (lines 149-195):
```css
.topbar-nav { display: flex; gap: 1rem; align-items: center; }
.hamburger { display: none; ... }
.mobile-nav {
  position: fixed; inset: 74px 0 0; z-index: 100;
  background: rgba(12,21,27,.97); padding: 1.5rem;
  display: flex; flex-direction: column; gap: 0.5rem;
  border-top: 1px solid #26373e; overflow-y: auto;
}
@media (max-width: 760px) {
  .topbar-nav { display: none; }
  .hamburger { display: flex; }
  .mobile-nav { top: 64px; }
}
```

Keep current breakpoints unless an actual overflow test proves otherwise. At mobile widths the fixed menu must leave room for close/scroll and must not alter desktop layout.

### `frontend/src/components/RuleInput.jsx` (component/form, transform/input)

**Analog:** `frontend/src/components/RuleInput.jsx`

**Current label/placeholder implementation** (lines 1-7):
```jsx
export function RuleInput({ label, hint, value, onChange }) {
  return (
    <label className="rule-field">
      <span>{label}</span>
      <textarea rows="2" placeholder={hint} value={value}
        onChange={(event) => onChange(event.target.value)} />
    </label>
  )
}
```

The label is visible, but Phase 3 copy guidance prohibits placeholder-only/hint-placeholder examples. Modify this analog to retain the wrapped label and add adjacent persistent helper text (probably a `<small>` or sibling span in the existing `.rule-field`) while removing/retiring `placeholder={hint}`. Keep the existing `hint` API only if the value is rendered as helper copy. Do not change the source IP/application/domain/port state names or `onChange` contract.

### `frontend/src/hooks/useCaptureProcessing.js` (hook/service, request-response)

**Analog:** `frontend/src/hooks/useCaptureProcessing.js`

**Busy/reset/error/report pattern** (lines 1-27):
```jsx
async function run(file, rules) {
  setBusy(true)
  setError('')
  setReport(null)
  try {
    const result = await processCapture(file, rules)
    setReport(result.report)
  } catch (requestError) {
    setError(requestError.message || 'PCAP processing failed.')
  } finally {
    setBusy(false)
  }
}
```

Preserve reset-on-new-selection, busy disablement, no stale report on retry, and API problem text. Add no toast library or second feedback system.

### `frontend/src/api/client.js` (service, request-response)

**Analog:** `frontend/src/api/client.js`

**Fetch/error pattern** (lines 18-27):
```js
const response = await fetch('/api/dpi/process', {
  method: 'POST',
  body: form,
})

if (!response.ok) {
  const body = await response.json().catch(() => ({}))
  throw new Error(body.message || `Inspection failed (${response.status}).`)
}
```

Preserve JSON/problem fallback behavior and the existing FormData field names (`blockedIps`, `blockedApps`, `blockedDomains`, `blockedPorts`). No API contract changes are needed for UI copy fixes.

### `frontend/index.html` (config/metadata, file-I/O)

**Analog:** `frontend/index.html`

**Metadata pattern** (lines 1-12):
```html
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<meta name="theme-color" content="#0c151b" />
<meta name="description" content="Local Deep Packet Inspection tool. Analyze PCAP traffic, inspect TLS SNI, HTTP hosts, DNS queries, and enforce IP, domain, and port blocking rules." />
<link rel="icon" type="image/svg+xml" href="/favicon.svg" />
<title>Packet Inspector — Deep Packet Inspection &amp; Network Analyzer</title>
```

Verify the SPA’s built live output and favicon route, not only this source. The Vite build copies output into `backend/src/main/resources/static` per `frontend/vite.config.js`; preserve that build relationship. No React route library exists: `App.jsx` is one inspector page, so SPA 404 behavior must be verified at Spring/static-server routing rather than a React route tree.

### `backend/src/main/webapp/WEB-INF/views/layout/header.jsp` (layout/template, request-response)

**Analog:** `backend/src/main/webapp/WEB-INF/views/layout/header.jsp`

**Metadata/nav pattern** (lines 6-22 and 65-78):
```jsp
<meta name="description" content="Deep Packet Inspector legacy portal — manage blocking rules and view inspection reports.">
<link rel="icon" type="image/svg+xml" href="/favicon.svg">
<title><c:if test="${not empty pageTitle}">${pageTitle} | </c:if>DPI Legacy Portal</title>

<a class="logo-link" href="/">DPI Legacy Portal</a>
<nav class="desktop-nav" aria-label="Primary navigation">
  <a href="/legacy/report">Inspection Report</a>
  <a href="/legacy/rules">Rule Manager</a>
  <a href="/">React Dashboard</a>
</nav>
```

**Mobile menu pattern** (lines 21-22, 53-56, 71-91):
```jsp
.hamburger-btn { display: none; ... }
#mobile-nav { display: none; ... }
#mobile-nav.open { display: flex; }
@media (max-width: 640px) {
  nav.desktop-nav { display: none; }
  .hamburger-btn { display: flex; }
}

<nav id="mobile-nav" aria-label="Mobile navigation" aria-hidden="true">
  <a href="/">Dashboard</a>
  <a href="/legacy/report">Inspection Report</a>
  <a href="/legacy/rules">Rule Manager</a>
</nav>
```

Preserve this 640px breakpoint and close behavior only if live tests pass. The `toggleMobileNav()` function updates `aria-expanded` and `aria-hidden`, which is the analog to preserve. Audit unused entries; there are only three resolved destinations, all valid.

### `backend/src/main/webapp/WEB-INF/views/layout/footer.jsp` (layout/template, request-response)

**Analog:** `backend/src/main/webapp/WEB-INF/views/layout/footer.jsp`

**Dynamic copyright/link pattern** (lines 1-13):
```jsp
<%@ page import="java.time.Year" %>
<%
    request.setAttribute("currentYear", Year.now().getValue());
%>
<footer>
  <p>Deep Packet Inspection System &copy; ${currentYear} &mdash;
     <a href="/">React Dashboard</a> &nbsp;|&nbsp;
     <a href="/legacy/report">Inspection Report</a> &nbsp;|&nbsp;
     <a href="/legacy/rules">Rule Manager</a>
  </p>
</footer>
```

Preserve `java.time.Year` as the current-year source and the three known routes. Do not add phone/email links. Fix no working footer link merely for cosmetic reasons.

### `backend/src/main/webapp/WEB-INF/views/report.jsp` (view/template, request-response)

**Analog:** `backend/src/main/webapp/WEB-INF/views/report.jsp`

**Metadata title and status feedback** (lines 1-11):
```jsp
<c:set var="pageTitle" value="Inspection Report" scope="request"/>
<jsp:include page="layout/header.jsp"/>

<c:if test="${not empty successMsg}">
    <div class="alert alert-success" role="status"><c:out value="${successMsg}"/></div>
</c:if>
<c:if test="${not empty errorMsg}">
    <div class="alert alert-error" role="alert"><c:out value="${errorMsg}"/></div>
</c:if>
```

**Internal table overflow pattern** (lines 29-57):
```jsp
<div class="table-responsive">
  <table>
    <!-- 580px minimum table; internal wrapper scrolls -->
  </table>
</div>
```

The header already provides `.table-responsive { overflow-x: auto }`; preserve internal table scrolling and verify at 320/375/768px that it does not widen the page. Preserve `<c:out>` escaping and the empty state copy.

### `backend/src/main/webapp/WEB-INF/views/rules.jsp` (view/form, request-response)

**Analog:** `backend/src/main/webapp/WEB-INF/views/rules.jsp`

**Existing flash feedback pattern** (lines 5-12):
```jsp
<c:if test="${not empty successMsg}">
    <div class="alert alert-success" role="status"><c:out value="${successMsg}"/></div>
</c:if>
<c:if test="${not empty errorMsg}">
    <div class="alert alert-error" role="alert"><c:out value="${errorMsg}"/></div>
</c:if>
```

**Form pattern requiring visible guidance** (lines 24-54):
```jsp
<form action="/legacy/rules/add" method="post">
  <select name="ruleType" required aria-label="Rule type">...</select>
  <input type="text" name="ruleValue" placeholder="e.g. 192.168.1.50, YouTube, *.facebook.com, 443"
         required aria-label="Rule value">
  <button type="submit">Add Rule</button>
</form>
```

Do not retain placeholder-only guidance. Add visible `<label>` and adjacent helper text for rule type and value. For delete, explain exact stored value required, then add an explicit in-flow confirmation naming the exact value:
`Delete rule \`...\`?` with `Keep Blocking Rule` and `Delete Rule`. Preserve POST endpoints and flash feedback; a modal is not required.

The copy contract also requires add feedback:
`Rule added: {type} = {value}`, delete feedback:
`Rule deleted: {type} = {value}`, and controller failures remain
`Failed to add rule: <concrete problem>` / `Failed to delete rule: <concrete problem>`.

### `backend/src/main/webapp/WEB-INF/views/error/404.jsp` (error view, request-response)

**Analog:** `backend/src/main/webapp/WEB-INF/views/error/404.jsp`

**Existing usable 404 pattern** (lines 1-32):
```jsp
<title>404 — Page Not Found | DPI Legacy Portal</title>
<link rel="icon" type="image/svg+xml" href="/favicon.svg">
<div class="code">404</div>
<h1>Page not found</h1>
<p>The page you're looking for doesn't exist or has been moved.</p>
<a class="btn" href="/">← Back to Dashboard</a>
```

Preserve the usable back link and identity. Verify actual routing first: there is a `404.jsp` file, but no error mapping is visible in `WebMvcConfig.java`; confirm live unknown paths resolve to this view instead of assuming file presence is sufficient. Add metadata description/route handling only after live verification if needed.

### `backend/src/main/java/com/packetinspector/dpi/controller/legacy/DpiLegacyController.java` (controller, request-response)

**Analog:** `backend/src/main/java/com/packetinspector/dpi/controller/legacy/DpiLegacyController.java`

**Route/flash pattern** (lines 26-75):
```java
@GetMapping("/report")
public String showReport(Model model) {
    model.addAttribute("ruleStats", ruleManager.getStats());
    model.addAttribute("connections", connectionRepository.findTop100ByOrderByIdDesc());
    return "report";
}

@PostMapping("/rules/add")
public String addRule(..., RedirectAttributes ra) {
  try {
    // existing ruleManager call
    ra.addFlashAttribute("successMsg", "Rule added: " + ruleType + " = " + ruleValue);
  } catch (Exception e) {
    ra.addFlashAttribute("errorMsg", "Failed to add rule: " + e.getMessage());
  }
  return "redirect:/legacy/rules";
}
```

Preserve this controller contract. UI confirmation must not bypass or replace the server POST. No new rule behavior or validation model is needed unless live testing exposes a bug.

## Shared Patterns

### Navigation and Brand
**Source:** `frontend/src/pages/InspectorPage.jsx` lines 75-101; `backend/src/main/webapp/WEB-INF/views/layout/header.jsp` lines 65-91
**Apply to:** React and legacy route chrome
```jsx
<a className="brand" href="/" aria-label="Packet Inspector home">...</a>
```
```jsp
<a class="logo-link" href="/">DPI Legacy Portal</a>
```
Keep both brands clickable to `/`. Preserve only verified destinations: `/`, `/legacy/report`, `/legacy/rules`.

### Responsive Overflow
**Source:** `frontend/src/index.css` lines 7-9; `frontend/src/pages/InspectorPage.css` lines 122-145; `header.jsp` lines 34-39
**Apply to:** React and legacy pages
```css
html, body { overflow-x: hidden; }
.workspace { grid-template-columns: 1fr; }
.table-responsive { overflow-x: auto; -webkit-overflow-scrolling: touch; }
```
The audit must verify 320px, 375px, 768px, and desktop widths. The legacy table alone may scroll internally; unrelated surfaces must not widen or mask overflow.

### Form Labels and Helper Text
**Source:** `frontend/src/components/RuleInput.jsx` lines 1-7; `backend/src/main/webapp/WEB-INF/views/rules.jsp` lines 24-54
**Apply to:** React blocking-rule fields and legacy add/delete fields
```jsx
<label className="rule-field">
  <span>{label}</span>
  <textarea ... />
</label>
```
Use visible labels plus adjacent helpers. Remove placeholder-only instructions. React file capture should also retain/explicitly render a label/helper for accepted PCAP/CAP format; the drop zone already renders text but is not a conventional separate label.

### Action Feedback
**Source:** `frontend/src/pages/InspectorPage.jsx` lines 171-183; `DpiLegacyController.java` lines 52-75; `header.jsp` alert CSS lines 47-48
**Apply to:** capture and legacy rule actions
```jsx
<div className="error-banner" role="alert" aria-live="assertive">...</div>
<div className="success-banner" role="status" aria-live="polite">...</div>
```
```jsp
<div class="alert alert-success" role="status"><c:out value="${successMsg}"/></div>
<div class="alert alert-error" role="alert"><c:out value="${errorMsg}"/></div>
```
Do not replace these with generic browser alerts/toasts.

### Metadata and 404
**Source:** `frontend/index.html` lines 1-12; `backend/src/main/webapp/WEB-INF/views/layout/header.jsp` lines 6-11; `backend/src/main/webapp/WEB-INF/views/error/404.jsp` lines 5-32
**Apply to:** user-facing routes
- SPA: accurate title and description, `/favicon.svg`, working live SPA route.
- Legacy: dynamic route title using `pageTitle`, shared description, `/favicon.svg`.
- 404: title, favicon, clear copy, and `/` back action.

### Copyright and Asset Inventory
**Source:** `frontend/src/pages/InspectorPage.jsx` lines 265-268; `footer.jsp` lines 1-13; `frontend/public/favicon.svg`; `backend/src/main/resources/static/favicon.svg`
**Apply to:** all visible pages
- React: dynamic `new Date().getFullYear()`.
- Legacy: `Year.now().getValue()`.
- SVG favicon is the only repository raster-adjacent asset discovery. No compression step for raster formats is applicable unless a later full audit finds one; do not convert or rename the already-working SVG.

## Tests and Tooling

### Existing backend analog
**Source:** `backend/src/test/java/com/packetinspector/dpi/controller/legacy/DpiLegacyControllerTest.java`
```java
mockMvc.perform(get("/legacy/report"))
    .andExpect(status().isOk())
    .andExpect(view().name("report"))
    .andExpect(model().attributeExists("ruleStats"));
```
Extend this test class for route/status, flash feedback, and form POST behavior. Preserve Spring MockMvc and JUnit style.

**Source:** `backend/src/test/java/com/packetinspector/dpi/controller/api/DpiApiControllerTest.java`
```java
mockMvc.perform(multipart("/api/dpi/process")
        .file(capture())
        .param("blockedPorts", "53"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.totalPackets").value(1));
```
Reuse for API/error feedback only if UI bug reproduction requires it; do not duplicate existing packet behavior tests.

**Frontend tooling:** `frontend/package.json` currently has only `dev`, `build`, and `preview`. There is no Playwright, Cypress, Vitest, Jest, or DOM test configuration in the repository. Add only if the Phase 3 plan requires repeatable browser viewport/404/link checks. Preferred analog for build verification remains `frontend/vite.config.js` and the backend Maven test suite:
```js
build: {
  outDir: '../backend/src/main/resources/static',
  emptyOutDir: true
}
```
Live route QA may use the existing Vite dev server plus Spring Boot, but no test framework should be invented into the app without a plan decision.

## No Analog Found

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| Repeatable browser/visual QA suite (exact filename TBD) | test harness | batch/request-response | No frontend test framework or Playwright/Cypress/Vitest config exists |
| SPA route-specific 404 component | component/route | request-response | React app has no router and `App.jsx` renders only `InspectorPage` |
| Runtime image compressor/config (exact filename TBD) | utility/build config | file-I/O | Repository inventory found only SVG assets; no raster build pipeline exists |

## Cautions Against Duplicating Already-Working Features

- Do not redesign or restyle the React or JSP identities. Preserve `#0c151b`/mint and slate/blue, respectively.
- Do not rebuild the React brand, desktop nav, mobile menu, success/error banners, busy state, or dynamic React year; audit and patch only verified gaps.
- Do not rebuild the legacy brand, mobile nav, table wrapper, flash alerts, current-year footer, favicon, or 404 file; verify live behavior first.
- Do not add phone or email links.
- Do not alter packet processing or rule semantics. Legacy delete confirmation is a UI guard around the existing POST, not a new API.
- Do not hide overflow with a broad `overflow-x: hidden` when the actual offender can be fixed. The global guard exists, so QA should validate real child widths.
- Do not invent metadata for route data or dynamic report titles that do not exist; use the shared description and existing `pageTitle` convention.
- Do not convert or optimize the existing SVG favicons; compression is not applicable to the SVG inventory, and no raster assets were found.
- Do not add a React router or test library solely for QA if the existing static dashboard and Spring routes can be checked with a minimal plan.
- `NAV_LINKS` and header/footer lists are not evidence of broken links without live HTTP checks. Preserve all destinations that resolve.

## Metadata

**Analog search scope:** `frontend/src`, `frontend/index.html`, `frontend/public`, `frontend/vite.config.js`, `backend/src/main/java/com/packetinspector/dpi/controller/legacy`, `backend/src/main/webapp/WEB-INF/views`, `backend/src/test/java/com/packetinspector/dpi/controller`, `backend/pom.xml`, package scripts
**Files scanned:** 22 primary source/config/test files
**Pattern extraction date:** 2026-11-25
