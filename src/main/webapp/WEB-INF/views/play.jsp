<%--
  Created by IntelliJ IDEA.
  User: laxmi
  Date: 5/2/2024
  Time: 3:35 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="https://jakarta.ee/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Play Trivial Trivia</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/resources/css/play.css">
</head>
<body>
<div class="container">
    <h1>Trivial Trivia</h1>
    <div id="question-container">
        <div id="question">${question.text}</div>
        <div id="options">
            <c:forEach var="option" items="${question.options}">
                <div class="option">
                    <form action="${pageContext.request.contextPath}/submitAnswer" method="post">
                        <input type="hidden" name="questionId" value="${question.id}">
                        <input type="hidden" name="selectedOption" value="${option.index}">
                        <button type="submit">${option.text}</button>
                    </form>
                </div>
            </c:forEach>
        </div>
    </div>
    <div id="feedback">${feedback}</div>
    <div id="score-container">
        <p id="score">Score: ${score}</p>
    </div>
    <c:if test="${!gameOver}">
        <form action="${pageContext.request.contextPath}/nextQuestion" method="get">
            <button type="submit" id="next-question">Next Question</button>
        </form>
    </c:if>
    <c:if test="${gameOver}">
        <div id="game-over">Game Over! Your final score: ${score}</div>
    </c:if>
</div>
</body>
</html>
