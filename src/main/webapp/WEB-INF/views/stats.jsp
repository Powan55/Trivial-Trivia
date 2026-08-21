<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your scores - Trivial Trivia</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/menu.css">
</head>
<body>
<div class="menu-container">
    <h1>Your scores</h1>
    <p>Playing as <c:out value="${player}"/></p>

    <c:choose>
        <c:when test="${not authenticated}">
            <p>Guests' scores are not kept.
                <a href="${pageContext.request.contextPath}/loginMenu">Sign in</a> to start a record.</p>
        </c:when>
        <c:when test="${empty scores}">
            <p>No finished rounds yet.</p>
        </c:when>
        <c:otherwise>
            <p>Best: <strong><c:out value="${best}"/></strong> over <c:out value="${scores.size()}"/>
                round<c:if test="${scores.size() != 1}">s</c:if>.</p>
            <ol class="menu-list">
                <c:forEach var="score" items="${scores}">
                    <li><c:out value="${score}"/></li>
                </c:forEach>
            </ol>
        </c:otherwise>
    </c:choose>

    <p><a href="${pageContext.request.contextPath}/menu">Back to the menu</a></p>
</div>
</body>
</html>
