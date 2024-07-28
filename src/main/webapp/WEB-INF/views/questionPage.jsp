<%--
  Created by IntelliJ IDEA.
  User: laxmi
  Date: 7/28/2024
  Time: 2:38 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Question</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/questionPage.css">
</head>
<body>
<div class="container">
    <h1>${question.question}</h1>
    <form action="${pageContext.request.contextPath}/play/answer" method="post">
        <input type="hidden" name="questionId" value="${question.id}" />
        <div class="options">
            <input type="radio" id="option1" name="answer" value="${question.option1}" />
            <label for="option1">${question.option1}</label>

            <input type="radio" id="option2" name="answer" value="${question.option2}" />
            <label for="option2">${question.option2}</label>

            <input type="radio" id="option3" name="answer" value="${question.option3}" />
            <label for="option3">${question.option3}</label>

            <input type="radio" id="option4" name="answer" value="${question.option4}" />
            <label for="option4">${question.option4}</label>
        </div>
        <button type="submit">Submit</button>
    </form>
</div>
</body>
</html>
