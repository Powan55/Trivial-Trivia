<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign in - Trivial Trivia</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/loginMenu.css">
</head>
<body>
<div class="login-container">
    <h1>Welcome to Trivial Trivia</h1>
    <div class="game-theme-description">
        <p>Test your wits and join the ranks of trivia masters.</p>
    </div>

    <c:if test="${not empty error}">
        <p class="error" role="alert"><c:out value="${error}"/></p>
    </c:if>

    <form id="loginForm" action="${pageContext.request.contextPath}/loginServlet" method="post" class="login-form">
        <div class="input-group">
            <label for="username">Username</label>
            <input type="text" id="username" name="username" value="<c:out value='${username}'/>"
                   autocomplete="username" required>
        </div>
        <div class="input-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" autocomplete="current-password" required>
        </div>
        <button type="submit" class="btn">Sign in</button>
    </form>

    <div class="options">
        <a href="${pageContext.request.contextPath}/createUser">Create an account</a>
        or
        <a href="${pageContext.request.contextPath}/guest">play as a guest</a>
    </div>
</div>
</body>
</html>
