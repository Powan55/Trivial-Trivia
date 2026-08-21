<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Game over - Trivial Trivia</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/endGamePage.css">
</head>
<body>
<div class="container">
    <h1 class="title">Game over</h1>
    <p class="score">Your final score is: <span><c:out value="${score}"/></span></p>
    <p class="tally"><c:out value="${right}"/> right, <c:out value="${wrong}"/> wrong</p>
    <c:if test="${not authenticated}">
        <p class="note"><a href="${pageContext.request.contextPath}/loginMenu">Sign in</a> to keep your scores.</p>
    </c:if>
    <a class="play-again-btn" href="${pageContext.request.contextPath}/play">Play again</a>
    <a href="${pageContext.request.contextPath}/menu">Back to the menu</a>
</div>
</body>
</html>
