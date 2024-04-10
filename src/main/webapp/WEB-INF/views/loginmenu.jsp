<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TrivialTrivia Login</title>
    <link rel="stylesheet" type="text/css" href="css/loginMenu.css">
</head>
<body>
<div class="login-container">
    <h2>Welcome to TrivialTrivia!</h2>
    <div class="game-theme-description">
        <p>Test your wits and join the ranks of trivia masters.</p>
    </div>
    <form id="loginForm" action="loginServlet" method="post" class="login-form">
        <div class="input-group">
            <label for="username">Username:</label>
            <input type="text" id="username" name="username" placeholder="TriviaMaster123" required>
        </div>
        <div class="input-group">
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" placeholder="Enter your secret key" required>
        </div>
        <button type="submit" class="btn">Dive In!</button>
    </form>
    <div class="options">
        <a href="createUser.jsp">Join the Quest</a> or <a href="continueAsGuest.jsp">Explore as a Guest</a>
    </div>
</div>
</body>
</html>
