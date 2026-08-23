<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Create an account - Trivial Trivia"/>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="fragments/head.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/loginMenu.css">
</head>
<body>
<%@ include file="fragments/header.jspf" %>

<main id="main" class="page-main">
    <div class="shell">
        <div class="card card-narrow auth-card">
            <h1>Create an account</h1>
            <p class="lead auth-lead">An account keeps every finished round on your scores page.</p>

            <c:if test="${not empty error}">
                <p id="form-error" class="alert alert-error" role="alert"><c:out value="${error}"/></p>
            </c:if>

            <form action="${pageContext.request.contextPath}/createUser" method="post"
                  <c:if test="${not empty error}">aria-describedby="form-error"</c:if>>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <div class="field">
                    <label for="name">Name</label>
                    <input type="text" id="name" name="name" maxlength="60"
                           value="<c:out value='${name}'/>" autocomplete="name" required>
                </div>
                <div class="field">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" minlength="3" maxlength="20"
                           pattern="[A-Za-z0-9_]+" value="<c:out value='${username}'/>"
                           autocomplete="username" aria-describedby="username-hint" required>
                    <span class="hint" id="username-hint">3 to 20 characters: letters, numbers and underscores.</span>
                </div>
                <div class="field">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" minlength="8"
                           autocomplete="new-password" aria-describedby="password-hint" required>
                    <span class="hint" id="password-hint">At least 8 characters.</span>
                </div>
                <button type="submit" class="btn btn-primary btn-block">Create account</button>
            </form>

            <p class="auth-alt">Already have an account?
                <a href="${pageContext.request.contextPath}/loginMenu">Sign in</a>.</p>
        </div>
    </div>
</main>

<%@ include file="fragments/footer.jspf" %>
</body>
</html>
