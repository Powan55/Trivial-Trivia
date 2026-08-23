<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
            <c:when test="${empty scores}">
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
                        <span class="stat-label">Best score</span>
                    </li>
                    <li class="stat">
                        <span class="stat-value"><c:out value="${scores.size()}"/></span>
                        <span class="stat-label">Round<c:if test="${scores.size() != 1}">s</c:if> played</span>
                    </li>
                </ul>

                <div class="card">
                    <h2>Every round</h2>
                    <ol class="score-list">
                        <c:forEach var="score" items="${scores}" varStatus="row">
                            <li class="score-row">
                                <span class="score-round">Round <c:out value="${row.index + 1}"/></span>
                                <span class="score-bar" aria-hidden="true">
                                    <span class="score-bar-fill"
                                          style="width: ${fn:substringBefore(best > 0 ? (score * 100 / best) : 0.0, '.')}%"></span>
                                </span>
                                <span class="score-value"><c:out value="${score}"/>
                                    <c:if test="${score == best}"><span class="badge">Best</span></c:if>
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
