<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="The requested page could not be found. Return to the Deep Packet Inspector dashboard.">
    <link rel="icon" type="image/svg+xml" href="/favicon.svg">
    <title>404 — Page Not Found | DPI Legacy Portal</title>
    <style>
        *, *::before, *::after { box-sizing: border-box; }
        body {
            font-family: Segoe UI, -apple-system, sans-serif; background: #0f172a; color: #f8fafc;
            margin: 0; display: flex; flex-direction: column; align-items: center;
            justify-content: center; min-height: 100vh; text-align: center; padding: 2rem;
        }
        .code {
            font-size: clamp(5rem, 20vw, 9rem); font-weight: 700; letter-spacing: -4px;
            color: transparent; -webkit-text-stroke: 2px #38bdf8; line-height: 1; margin-bottom: 1rem;
        }
        h1 { font-size: 1.5rem; font-weight: 600; color: #cbd5e1; margin: 0 0 0.75rem; }
        p { color: #64748b; font-size: 0.95rem; max-width: 400px; margin: 0 0 2rem; }
        .btn {
            display: inline-block; padding: 0.65rem 1.5rem; background: #0284c7; color: #fff;
            border-radius: 6px; text-decoration: none; font-weight: 600; font-size: 0.875rem;
        }
        .btn:hover { background: #0369a1; }
    </style>
</head>
<body>
    <div class="code">404</div>
    <h1>Page not found</h1>
    <p>The page you're looking for doesn't exist or has been moved.</p>
    <a class="btn" href="/">← Back to Dashboard</a>
</body>
</html>
