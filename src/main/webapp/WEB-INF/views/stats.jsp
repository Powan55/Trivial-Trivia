<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your scores - Trivial Trivia"/>
<c:set var="navCurrent" value="stats"/>
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
            <h1>Your scores</h1>
            <p class="lead">Playing as <strong><c:out value="${player}"/></strong>.</p>
        </div>

        <c:choose>
            <c:when test="${empty history}">
                <div class="card empty-state">
                    <h2>No finished rounds yet</h2>
                    <p class="muted">Play a round through to the end and it will show up here.</p>
                    <div class="btn-row">
                        <a class="btn btn-primary" href="${pageContext.request.contextPath}/play">Play a round</a>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <ul class="stat-strip">
                    <li class="stat">
                        <span class="stat-value"><c:out value="${best}"/></span>
                        <span class="stat-label">Best score in points</span>
                    </li>
                    <li class="stat">
                        <span class="stat-value"><c:out value="${history.size()}"/></span>
                        <span class="stat-label">Round<c:if test="${history.size() != 1}">s</c:if> played</span>
                    </li>
                </ul>

                <div class="card">
                    <h2>Every round</h2>
                    <p class="muted">Newest first. Dates shown in server time. Older scores may not include dates or answer counts.</p>
                    <ol class="score-list" role="list" aria-label="Rounds, newest first">
                        <c:forEach var="round" items="${history}">
                            <li class="score-row">
                                <span class="score-round">
                                    <c:choose>
                                        <c:when test="${not empty round.finishedAt}">
                                            <time datetime="<c:out value='${round.finishedAt}'/>"><c:out value="${round.finishedAtLabel}"/></time>
                                        </c:when>
                                        <c:otherwise>Date unavailable</c:otherwise>
                                    </c:choose>
                                </span>
                                <span class="score-answers">
                                    <c:choose>
                                        <c:when test="${round.correctCount != null and round.totalQuestions != null}">
                                            <c:out value="${round.correctCount}"/> / <c:out value="${round.totalQuestions}"/> correct
                                        </c:when>
                                        <c:otherwise>Answer counts unavailable</c:otherwise>
                                    </c:choose>
                                </span>
                                <span class="score-value"><span><c:out value="${round.score}"/> points</span>
                                    <c:if test="${round.score == best}"><span class="badge">Best</span></c:if>
                                </span>
                            </li>
                        </c:forEach>
                    </ol>
                </div>
            </c:otherwise>
        </c:choose>

        <p class="back-link"><a href="${pageContext.request.contextPath}/menu">Back to the menu</a></p>
    </div>
</main>

<%@ include file="fragments/footer.jspf" %>
</body>
</html>
