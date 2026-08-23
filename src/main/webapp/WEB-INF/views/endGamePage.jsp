<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Game over - Trivial Trivia"/>
<c:set var="answered" value="${right + wrong}"/>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="fragments/head.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/endGamePage.css">
</head>
<body>
<%@ include file="fragments/header.jspf" %>

<main id="main" class="page-main">
    <div class="shell">
        <div class="card result-card">
            <p class="result-kicker">Round complete</p>
            <h1 class="result-title">
                <c:choose>
                    <c:when test="${answered > 0 and right == answered}">A perfect round</c:when>
                    <c:when test="${answered > 0 and right * 2 >= answered}">Well played</c:when>
                    <c:otherwise>Game over</c:otherwise>
                </c:choose>
            </h1>

<%@ include file="fragments/feedback.jspf" %>

            <div class="result-ring" style="--result-share: ${fn:substringBefore(answered > 0 ? (right * 100 / answered) : 0.0, '.')}%">
                <div class="result-ring-inner">
                    <span class="result-score"><c:out value="${score}"/></span>
                    <span class="result-score-label">points</span>
                </div>
            </div>

            <ul class="tally">
                <li class="tally-item tally-right">
                    <span class="tally-count"><c:out value="${right}"/></span>
                    <span class="tally-label">right</span>
                </li>
                <li class="tally-item tally-wrong">
                    <span class="tally-count"><c:out value="${wrong}"/></span>
                    <span class="tally-label">wrong</span>
                </li>
            </ul>

            <c:if test="${not authenticated}">
                <p class="result-note">
                    This round was not saved.
                    <a href="${pageContext.request.contextPath}/loginMenu">Sign in</a>
                    to keep your scores.
                </p>
            </c:if>

            <div class="btn-row result-actions">
                <a class="btn btn-primary" href="${pageContext.request.contextPath}/play">Play again</a>
                <a class="btn btn-quiet" href="${pageContext.request.contextPath}/menu">Back to the menu</a>
                <c:if test="${authenticated}">
                    <a class="btn" href="${pageContext.request.contextPath}/stats">See your scores</a>
                </c:if>
            </div>
        </div>
    </div>
</main>

<%@ include file="fragments/footer.jspf" %>
</body>
</html>
