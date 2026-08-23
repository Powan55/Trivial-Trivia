<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Questions - Trivial Trivia"/>
<c:set var="authKnown" value="true"/>
<c:set var="navCurrent" value="questions"/>
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
            <h1>Questions</h1>
            <p class="lead"><strong><c:out value="${count}"/></strong> questions loaded.</p>
        </div>

        <c:if test="${not empty error}">
            <p id="form-error" class="alert alert-error" role="alert"><c:out value="${error}"/></p>
        </c:if>
        <c:if test="${not empty message}">
            <p class="alert alert-success" role="status"><c:out value="${message}"/></p>
        </c:if>

        <div class="split">
            <section class="card" aria-labelledby="export-heading">
                <h2 id="export-heading">Export</h2>
                <p class="muted">Download the current set in any of the three formats the app reads.</p>
                <ul class="format-list">
                    <c:forEach var="format" items="${formats}">
                        <li>
                            <a class="btn btn-quiet btn-block"
                               href="${pageContext.request.contextPath}/questions/export?format=<c:out value='${format}'/>">
                                Download as <c:out value="${fn:toUpperCase(format)}"/>
                            </a>
                        </li>
                    </c:forEach>
                </ul>
            </section>

            <section class="card" aria-labelledby="import-heading">
                <h2 id="import-heading">Import</h2>
                <c:choose>
                    <c:when test="${authenticated}">
                        <p class="muted">Replaces the whole question set. Keep the header row.</p>
                        <form action="${pageContext.request.contextPath}/questions/import"
                              method="post" enctype="multipart/form-data"
                              <c:if test="${not empty error}">aria-describedby="form-error"</c:if>>
                            <div class="field">
                                <label for="file">Question file</label>
                                <input type="file" id="file" name="file" accept=".csv,.json,.xml"
                                       aria-describedby="file-hint" required>
                                <span class="hint" id="file-hint">A .csv, .json or .xml file, up to 256 KB.
                                    Six columns: the question, four options and the answer.</span>
                            </div>
                            <button type="submit" class="btn btn-primary">Import</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <p class="muted">Replacing the question set changes it for everyone, so it
                            needs an account.</p>
                        <div class="btn-row">
                            <a class="btn btn-primary" href="${pageContext.request.contextPath}/loginMenu">Sign in</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </div>

        <p class="back-link"><a href="${pageContext.request.contextPath}/menu">Back to the menu</a></p>
    </div>
</main>

<%@ include file="fragments/footer.jspf" %>
</body>
</html>
