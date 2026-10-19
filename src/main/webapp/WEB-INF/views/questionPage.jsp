<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Question - Trivial Trivia"/>
<c:set var="navCurrent" value="play"/>
<!DOCTYPE html>
<html lang="en">
<head>
<%@ include file="fragments/head.jspf" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/questionPage.css">
</head>
<body>
<%@ include file="fragments/header.jspf" %>

<main id="main" class="page-main">
    <div class="shell quiz-shell">
<%@ include file="fragments/feedback.jspf" %>

        <div class="progress">
            <p class="progress-text">Question <c:out value="${questionNumber}"/>
                of <c:out value="${totalQuestions}"/></p>
            <div class="progress-track" role="progressbar" aria-label="Round progress"
                 aria-valuemin="0" aria-valuemax="<c:out value='${totalQuestions}'/>"
                 aria-valuenow="<c:out value='${questionNumber}'/>">
                <div class="progress-fill"
                     style="width: ${fn:substringBefore(totalQuestions > 0 ? (questionNumber * 100 / totalQuestions) : 0.0, '.')}%"></div>
            </div>
        </div>

        <div class="card quiz-card">
            <h1 id="question-heading" class="quiz-question"><c:out value="${question.question}"/></h1>

            <form action="${pageContext.request.contextPath}/play/answer" method="post">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                <input type="hidden" name="roundId" value="<c:out value='${roundId}'/>">
                <input type="hidden" name="position" value="<c:out value='${position}'/>">
                <fieldset class="options" aria-labelledby="question-heading">
                    <c:forEach var="index" begin="1" end="4">
                        <div class="option">
                            <input type="radio" id="option${index}" name="answer"
                                   value="<c:out value='${question.getOption(index)}'/>" required>
                            <label for="option${index}"><c:out value="${question.getOption(index)}"/></label>
                        </div>
                    </c:forEach>
                </fieldset>
                <button type="submit" class="btn btn-primary quiz-submit">Submit answer</button>
            </form>
        </div>
    </div>
</main>

<%@ include file="fragments/footer.jspf" %>
</body>
</html>
