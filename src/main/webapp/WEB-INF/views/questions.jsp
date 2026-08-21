<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Questions - Trivial Trivia</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/menu.css">
</head>
<body>
<div class="menu-container">
    <h1>Questions</h1>
    <p><c:out value="${count}"/> questions loaded.</p>

    <c:if test="${not empty error}">
        <p class="error" role="alert"><c:out value="${error}"/></p>
    </c:if>
    <c:if test="${not empty message}">
        <p class="message" role="status"><c:out value="${message}"/></p>
    </c:if>

    <h2>Export</h2>
    <ul class="menu-list">
        <c:forEach var="format" items="${formats}">
            <li>
                <a href="${pageContext.request.contextPath}/questions/export?format=${format}">
                    Download as <c:out value="${format}"/>
                </a>
            </li>
        </c:forEach>
    </ul>

    <h2>Import</h2>
    <c:choose>
        <c:when test="${authenticated}">
            <p>Replaces the whole question set. Keep the header row.</p>
            <form action="${pageContext.request.contextPath}/questions/import"
                  method="post" enctype="multipart/form-data">
                <label for="file">Question file (.csv, .json or .xml)</label>
                <input type="file" id="file" name="file" accept=".csv,.json,.xml" required>
                <button type="submit">Import</button>
            </form>
        </c:when>
        <c:otherwise>
            <p><a href="${pageContext.request.contextPath}/loginMenu">Sign in</a> to replace the question set.</p>
        </c:otherwise>
    </c:choose>

    <p><a href="${pageContext.request.contextPath}/menu">Back to the menu</a></p>
</div>
</body>
</html>
