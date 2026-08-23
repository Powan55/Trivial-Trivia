<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Main menu - Trivial Trivia"/>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="fragments/head.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/menu.css">
</head>
<body>
<%@ include file="fragments/header.jspf" %>

<main id="main" class="page-main">
    <div class="shell">
        <div class="page-head">
            <h1>Main menu</h1>
            <p class="lead">Playing as <strong><c:out value="${player}"/></strong>.</p>
        </div>

        <ul class="menu-grid">
            <li>
                <a class="menu-card" href="${pageContext.request.contextPath}/play">
                    <span class="menu-card-title">Play a round</span>
                    <span class="menu-card-note">Ten questions, ten points each.</span>
                </a>
            </li>
            <li>
                <a class="menu-card" href="${pageContext.request.contextPath}/stats">
                    <span class="menu-card-title">Your scores</span>
                    <span class="menu-card-note">Every round you have finished while signed in.</span>
                </a>
            </li>
            <li>
                <a class="menu-card" href="${pageContext.request.contextPath}/questions">
                    <span class="menu-card-title">Questions</span>
                    <span class="menu-card-note">Import or export the set as CSV, JSON or XML.</span>
                </a>
            </li>
        </ul>

        <c:if test="${not authenticated}">
            <p class="menu-note muted">You are playing as a guest, so nothing is saved.
                <a href="${pageContext.request.contextPath}/loginMenu">Sign in</a> or
                <a href="${pageContext.request.contextPath}/createUser">create an account</a>
                to keep a score history.</p>
        </c:if>
    </div>
</main>

<%@ include file="fragments/footer.jspf" %>
</body>
</html>
