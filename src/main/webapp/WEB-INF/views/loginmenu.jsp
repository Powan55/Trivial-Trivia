<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Sign in - Trivial Trivia"/>
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
            <h1>Sign in</h1>
            <p class="lead auth-lead">Pick up your score history where you left it.</p>

            <c:if test="${not empty error}">
                <p id="form-error" class="alert alert-error" role="alert"><c:out value="${error}"/></p>
            </c:if>

            <form id="loginForm" action="${pageContext.request.contextPath}/loginServlet" method="post"
                  <c:if test="${not empty error}">aria-describedby="form-error"</c:if>>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <div class="field">
                    <label for="username">Username</label>
                    <input type="text" id="username" name="username" value="<c:out value='${username}'/>"
                           autocomplete="username" required>
                </div>
                <div class="field">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password"
                           autocomplete="current-password" required>
                </div>
                <button type="submit" class="btn btn-primary btn-block">Sign in</button>
            </form>

            <p class="auth-alt">No account yet?
                <a href="${pageContext.request.contextPath}/createUser">Create one</a>, or
                <a href="${pageContext.request.contextPath}/guest">play as a guest</a>.</p>
        </div>
    </div>
</main>

<%@ include file="fragments/footer.jspf" %>
</body>
</html>
