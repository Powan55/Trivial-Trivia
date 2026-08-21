<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trivial Trivia</title>
    <style>
        body {
            font-family: system-ui, -apple-system, "Segoe UI", Arial, sans-serif;
            background-color: #f0f8ff;
            margin: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .container { text-align: center; padding: 2rem; }
        h1 { color: #333; font-size: clamp(2rem, 6vw, 3rem); margin-bottom: 0.5rem; }
        p { color: #555; font-size: 1.1rem; }
        .logo { width: 150px; height: auto; margin-bottom: 2rem; }
        .actions { display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap; margin-top: 2rem; }
        .actions a {
            padding: 0.75rem 1.5rem;
            border-radius: 6px;
            text-decoration: none;
            border: 2px solid #2f6f9f;
            color: #2f6f9f;
        }
        .actions a.primary { background: #2f6f9f; color: #fff; }
        .actions a:focus-visible { outline: 3px solid #14364d; outline-offset: 2px; }
    </style>
</head>
<body>
<div class="container">
    <img src="${pageContext.request.contextPath}/images/Trivial-Trivia.jpg"
         alt="Trivial Trivia logo" class="logo">
    <h1>Trivial Trivia</h1>
    <p>Ten multiple-choice questions. Ten points each.</p>
    <div class="actions">
        <a class="primary" href="${pageContext.request.contextPath}/play">Play now</a>
        <a href="${pageContext.request.contextPath}/loginMenu">Sign in</a>
        <a href="${pageContext.request.contextPath}/createUser">Create an account</a>
    </div>
</div>
</body>
</html>
