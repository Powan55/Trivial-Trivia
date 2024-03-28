<%--
  Created by IntelliJ IDEA.
  User: Uttam
  Date: 3/28/2024
  Time: 3:08 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trivial Trivia Login</title>
    <style>
        body, html {
            height: 100%;
            margin: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(to right, #6dd5ed, #2193b0);
        }

        .login-container {
            height: 100%;
            display: flex;
            justify-content: center;
            align-items: center;
            text-align: center;
        }

        .login-form {
            padding: 20px;
            background: white;
            border-radius: 10px;
            box-shadow: 0px 0px 10px 0px #00000040;
            width: 300px;
        }

        h1 {
            margin-bottom: 2rem;
            color: #333;
        }

        .input-group {
            position: relative;
            margin-bottom: 20px;
        }

        .input-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 5px;
        }

        .input-group label {
            position: absolute;
            top: 0;
            left: 10px;
            padding: 10px 0;
            transition: 0.3s;
            pointer-events: none;
            color: #666;
        }

        .input-group input:focus + label,
        .input-group input:valid + label {
            top: -20px;
            left: 10px;
            font-size: 12px;
            color: #2193b0;
        }

        button {
            width: 100%;
            padding: 10px;
            border: none;
            background: #2193b0;
            color: white;
            border-radius: 5px;
            cursor: pointer;
        }

        button:hover {
            background: #6dd5ed;
        }
    </style>
</head>
<body>

<div class="login-container">
    <form class="login-form" action="loginAction" method="post"> <!-- Instead of login action, replace with url pattern of the Serverlet -->
        <h1>Login</h1>
        <div class="input-group">
            <input type="text" name="username" required>
            <label>Username</label>
        </div>
        <div class="input-group">
            <input type="password" name="password" required>
            <label>Password</label>
        </div>
        <button type="submit">Sign In</button>
    </form>
</div>

</body>
</html>

