<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="theme-color" content="#0f172a">
    <meta name="description" content="Deep Packet Inspector legacy portal — manage blocking rules and view inspection reports.">
    <link rel="icon" type="image/svg+xml" href="/favicon.svg">
    <title><c:if test="${not empty pageTitle}">${pageTitle} | </c:if>DPI Legacy Portal</title>
    <style>
        *, *::before, *::after { box-sizing: border-box; }
        html, body { margin: 0; padding: 0; overflow-x: hidden; }
        body { font-family: Segoe UI, -apple-system, BlinkMacSystemFont, Roboto, sans-serif; background: #0f172a; color: #f8fafc; min-width: 320px; }
        header { background: #1e293b; padding: 0 2rem; height: 60px; border-bottom: 1px solid #334155; display: flex; justify-content: space-between; align-items: center; position: sticky; top: 0; z-index: 50; }
        .logo-link { color: #38bdf8; text-decoration: none; font-size: 1.1rem; font-weight: 700; }
        .logo-link:hover, .logo-link:focus { color: #7dd3fc; }
        nav.desktop-nav a { color: #94a3b8; text-decoration: none; margin-left: 1.5rem; font-weight: 500; font-size: 0.9rem; }
        nav.desktop-nav a:hover, nav.desktop-nav a:focus { color: #f8fafc; }
        .hamburger-btn { display: none; flex-direction: column; justify-content: center; gap: 5px; width: 36px; height: 36px; padding: 6px; background: none; border: 1px solid #334155; border-radius: 6px; cursor: pointer; }
        .hamburger-btn span { display: block; height: 2px; background: #94a3b8; border-radius: 2px; }
        #mobile-nav { display: none; position: fixed; inset: 60px 0 0; z-index: 100; background: rgba(15,23,42,.97); padding: 1.5rem; flex-direction: column; gap: 0.5rem; border-top: 1px solid #334155; overflow-y: auto; }
        #mobile-nav.open { display: flex; }
        #mobile-nav a { display: block; padding: 0.875rem 1rem; color: #cbd5e1; text-decoration: none; font-size: 1rem; font-weight: 500; border-radius: 8px; border: 1px solid transparent; }
        #mobile-nav a:hover, #mobile-nav a:focus { background: rgba(56,189,248,.07); border-color: rgba(56,189,248,.2); color: #38bdf8; }
        .container { max-width: 1200px; margin: 2rem auto; padding: 0 1rem; }
        .card { background: #1e293b; border-radius: 8px; border: 1px solid #334155; padding: 1.5rem; margin-bottom: 1.5rem; }
        h2 { color: #e2e8f0; margin: 0 0 1rem; font-size: 1.1rem; }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }
        .stat-box { background: #0f172a; border: 1px solid #334155; padding: 1rem; border-radius: 6px; }
        .stat-label { color: #94a3b8; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; }
        .stat-val { font-size: 1.5rem; font-weight: 700; color: #38bdf8; margin-top: 0.25rem; }
        .table-responsive { overflow-x: auto; -webkit-overflow-scrolling: touch; border-radius: 6px; }
        table { width: 100%; border-collapse: collapse; min-width: 580px; font-size: 0.875rem; }
        th, td { padding: 0.75rem; text-align: left; border-bottom: 1px solid #334155; white-space: nowrap; }
        th { color: #94a3b8; font-weight: 600; background: #0f172a; }
        td { color: #e2e8f0; }
        .form-row { display: flex; gap: 1rem; align-items: center; margin-top: 1rem; flex-wrap: wrap; }
        input, select, button { padding: 0.5rem 0.75rem; border-radius: 4px; border: 1px solid #475569; background: #0f172a; color: #f8fafc; font: inherit; }
        input:focus, select:focus { outline: 2px solid #38bdf8; outline-offset: 1px; }
        button { background: #0284c7; border-color: #0284c7; cursor: pointer; font-weight: 600; }
        button:hover { background: #0369a1; }
        button.btn-danger { background: #dc2626; border-color: #dc2626; }
        button.btn-danger:hover { background: #b91c1c; }
        .alert { padding: 0.875rem 1rem; border-radius: 6px; margin-bottom: 1rem; font-size: 0.9rem; }
        .alert-success { background: rgba(21,128,61,.2); border: 1px solid rgba(21,128,61,.4); color: #86efac; }
        .alert-error { background: rgba(185,28,28,.2); border: 1px solid rgba(185,28,28,.4); color: #fca5a5; }
        footer { text-align: center; padding: 2rem 1rem; color: #64748b; font-size: 0.85rem; border-top: 1px solid #1e293b; }
        footer a { color: #38bdf8; text-decoration: none; }
        footer a:hover { text-decoration: underline; }
        @media (max-width: 640px) {
            header { padding: 0 1rem; }
            nav.desktop-nav { display: none; }
            .hamburger-btn { display: flex; }
            .form-row { flex-direction: column; align-items: stretch; }
            input, select, button { width: 100%; }
            .card { padding: 1rem; }
            .container { margin: 1rem auto; }
        }
    </style>
</head>
<body>
<header>
    <a class="logo-link" href="/">DPI Legacy Portal</a>
    <nav class="desktop-nav" aria-label="Primary navigation">
        <a href="/legacy/report">Inspection Report</a>
        <a href="/legacy/rules">Rule Manager</a>
        <a href="/">React Dashboard</a>
    </nav>
    <button class="hamburger-btn" type="button" aria-label="Open navigation menu" aria-expanded="false"
            id="hamburger-btn" onclick="toggleMobileNav()">
        <span></span><span></span><span></span>
    </button>
</header>
<nav id="mobile-nav" aria-label="Mobile navigation" aria-hidden="true">
    <a href="/">Dashboard</a>
    <a href="/legacy/report">Inspection Report</a>
    <a href="/legacy/rules">Rule Manager</a>
</nav>
<script>
function toggleMobileNav() {
    var nav = document.getElementById('mobile-nav');
    var btn = document.getElementById('hamburger-btn');
    var open = nav.classList.toggle('open');
    btn.setAttribute('aria-expanded', open ? 'true' : 'false');
    nav.setAttribute('aria-hidden', open ? 'false' : 'true');
}
</script>
<div class="container">
