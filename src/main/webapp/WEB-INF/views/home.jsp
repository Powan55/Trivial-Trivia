<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Trivial Trivia"/>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="fragments/head.jspf" %>
</head>
<body>
<%@ include file="fragments/header.jspf" %>

<main id="main" class="page-main">
    <div class="shell">
        <div class="hero">
            <img class="hero-logo" src="${pageContext.request.contextPath}/images/Trivial-Trivia.jpg"
                 alt="" width="96" height="96">
            <h1>Trivial Trivia</h1>
            <p class="lead">Ten multiple-choice questions. Ten points each. A wrong answer costs
                you nothing but the round.</p>
            <div class="btn-row">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/play">Play now</a>
                <a class="btn" href="${pageContext.request.contextPath}/loginMenu">Sign in</a>
                <a class="btn btn-quiet" href="${pageContext.request.contextPath}/createUser">Create an account</a>
            </div>
            <ul class="hero-points">
                <li>Play as a guest, no account needed</li>
                <li>Sign in to keep a score history</li>
                <li>Import and export questions as CSV, JSON or XML</li>
            </ul>
        </div>
    </div>
</main>

<%@ include file="fragments/footer.jspf" %>
</body>
</html>
