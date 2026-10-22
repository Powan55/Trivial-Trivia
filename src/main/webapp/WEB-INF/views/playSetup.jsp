<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Choose a round - Trivial Trivia"/>
<c:set var="navCurrent" value="play"/>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="fragments/head.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/playSetup.css">
</head>
<body>
<%@ include file="fragments/header.jspf" %>
<main id="main" class="page-main">
    <div class="shell">
        <div class="card setup-card">
            <h1>Choose a round</h1>
            <p>Up to five questions, ten points each. Questions never repeat within a round.</p>
            <c:if test="${not empty error}">
                <p class="alert alert-error" role="alert"><c:out value="${error}"/></p>
            </c:if>
            <form action="${pageContext.request.contextPath}/play" method="get" aria-describedby="selection-help">
                <div class="field">
                    <label for="category">Category</label>
                    <select id="category" name="category">
                        <option value="">All categories</option>
                        <c:forEach var="category" items="${categories}">
                            <option value="<c:out value='${category}'/>" ${category eq selectedCategory ? 'selected' : ''}><c:out value="${category}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <div class="field">
                    <label for="difficulty">Difficulty</label>
                    <select id="difficulty" name="difficulty">
                        <option value="">All difficulties (mixed)</option>
                        <c:forEach var="difficulty" items="${difficulties}">
                            <option value="<c:out value='${difficulty}'/>" ${difficulty eq selectedDifficulty ? 'selected' : ''}><c:out value="${difficulty}"/></option>
                        </c:forEach>
                    </select>
                </div>
                <p id="selection-help" class="muted">Update selection to check the count before starting.</p>
                <button type="submit" class="btn btn-quiet">Update selection</button>
            </form>
            <div class="setup-summary">
                <p><strong><c:out value="${matchingCount}"/></strong> matching ${matchingCount eq 1 ? 'question' : 'questions'}.
                    This round: <strong><c:out value="${roundSize}"/></strong> ${roundSize eq 1 ? 'question' : 'questions'}.</p>
                <c:if test="${matchingCount gt 0 and roundSize lt 5}">
                    <p class="muted">Fewer than five match, so this will be a shorter round.</p>
                </c:if>
                <c:if test="${matchingCount eq 0}">
                    <p role="status">No questions match. Change your selection and update it to start.</p>
                </c:if>
                <form action="${pageContext.request.contextPath}/play" method="post">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                    <input type="hidden" name="category" value="<c:out value='${selectedCategory}'/>">
                    <input type="hidden" name="difficulty" value="<c:out value='${selectedDifficulty}'/>">
                    <p class="muted">Starting with: <c:out value="${empty selectedCategory ? 'All categories' : selectedCategory}"/>,
                        <c:out value="${empty selectedDifficulty ? 'all difficulties' : selectedDifficulty}"/>.</p>
                    <button type="submit" class="btn btn-primary" ${matchingCount eq 0 ? 'disabled' : ''}>Start round</button>
                </form>
            </div>
        </div>
    </div>
</main>
<%@ include file="fragments/footer.jspf" %>
</body>
</html>
