<%--
  Created by IntelliJ IDEA.
  User: laxmi
  Date: 4/9/2024
  Time: 2:22 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Game Menu</title>
    <link rel="stylesheet" type="text/css" href="css/menu.css">
</head>
<body>
<div class="container">
    <h1>Game Menu</h1>
    <form action="/menu" method="post">
        <label><input type="radio" name="userInput" value="1"> Play</label><br>
        <label><input type="radio" name="userInput" value="2"> View Stat</label><br>
        <label><input type="radio" name="userInput" value="5"> Exit</label><br><br>
        <input type="submit" value="Submit">
    </form>
</div>
</body>
</html>
