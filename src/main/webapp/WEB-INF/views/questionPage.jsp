<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Question - Trivial Trivia</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/questionPage.css">
</head>
<body>
<main>
    <h1><c:out value="${question.question}"/></h1>
    <form action="${pageContext.request.contextPath}/play/answer" method="post">
        <fieldset>
            <legend class="visually-hidden">Choose an answer</legend>
            <c:forEach var="index" begin="1" end="4">
                <div class="option">
                    <input type="radio" id="option${index}" name="answer"
                           value="<c:out value='${question.getOption(index)}'/>" required>
                    <label for="option${index}"><c:out value="${question.getOption(index)}"/></label>
                </div>
            </c:forEach>
        </fieldset>
        <button type="submit">Submit</button>
    </form>
</main>
</body>
</html>
