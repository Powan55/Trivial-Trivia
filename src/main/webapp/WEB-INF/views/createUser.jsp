<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create an account - Trivial Trivia</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/loginMenu.css">
</head>
<body>
<div class="login-container">
    <h1>Create an account</h1>

    <c:if test="${not empty error}">
        <p class="error" role="alert"><c:out value="${error}"/></p>
    </c:if>

    <form action="${pageContext.request.contextPath}/createUser" method="post" class="login-form">
        <div class="input-group">
            <label for="name">Name</label>
            <input type="text" id="name" name="name" maxlength="60"
                   value="<c:out value='${name}'/>" autocomplete="name" required>
        </div>
        <div class="input-group">
            <label for="username">Username</label>
            <input type="text" id="username" name="username" minlength="3" maxlength="20"
                   pattern="[A-Za-z0-9_]+" value="<c:out value='${username}'/>"
                   autocomplete="username" required>
            <small>3 to 20 characters: letters, numbers and underscores.</small>
        </div>
        <div class="input-group">
            <label for="password">Password</label>
            <input type="password" id="password" name="password" minlength="8"
                   autocomplete="new-password" required>
            <small>At least 8 characters.</small>
        </div>
        <button type="submit" class="btn">Create account</button>
    </form>

    <div class="options">
        <a href="${pageContext.request.contextPath}/loginMenu">Already have an account?</a>
    </div>
</div>
</body>
</html>
