<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Game Over</title>
    <link rel="stylesheet" href="/css/endGamePage.css">
</head>
<body>
<div class="container">
    <h1 class="title">Game Over</h1>
    <p class="score">Your final score is: <span>${score}</span></p>
    <p class="tally">${right} right, ${wrong} wrong</p>
    <button class="play-again-btn" onclick="window.location.href='${pageContext.request.contextPath}/play'">Play Again</button>
</div>
</body>
</html>
