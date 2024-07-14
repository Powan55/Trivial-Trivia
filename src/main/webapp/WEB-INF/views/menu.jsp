<%--
  Created by IntelliJ IDEA.
  User: laxmi
  Date: 4/9/2024
  Time: 2:22 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Main Menu</title>
    <link rel="stylesheet" type="text/css" href="css/menu.css">
    <!-- Include Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
</head>
<body>
<div class="navbar">
    <div class="nav-logo">
        <a href="#">Trivial-Trivia</a>
    </div>
    <div class="nav-items">
        <ul>
            <li><a href="#"> Home </a></li>
            <li><a href="#"> Game </a></li>
            <li><a href="#"> About </a></li>
            <li><a href="#"> Contact </a></li>
        </ul>
    </div>
    <div class="nav-button">
        <div class="anim-layer"></div>
        <!-- User button with icon -->
        <a href="#"><i class="fas fa-user"></i></a>
    </div>
    <div id="hamburger-menu">&#9776;</div>
</div>

<div id="mobile-menu">
    <div class="mobile-nav-items">
        <ul>
            <li><a href="#"> Home </a></li>
            <li><a href="#"> Game </a></li>
            <li><a href="#"> About </a></li>
            <li><a href="#"> Contact </a></li>
        </ul>
    </div>
    <div class="mobile-nav-button">
        <div class="anim-layer"></div>
        <!-- User button with icon -->
        <a href="#"><i class="fas fa-user"></i></a>
    </div>
    <div id="hamburger-cross">&#10006;</div>
</div>

<div class="menu-container">
    <h1>Main Menu</h1>
    <ul class="menu-list">
        <li><a href="/play">Play</a></li>
        <li><a href="viewStat.jsp">View Stat</a></li>
        <li><a href="#">Exit</a></li>
    </ul>
</div>
</body>
<script>
    var hamburgerMenu = document.getElementById("hamburger-menu");
    var hamburgerCross = document.getElementById("hamburger-cross");
    var mobileMenu = document.getElementById("mobile-menu");

    hamburgerMenu.addEventListener("click", function () {
        mobileMenu.style.display = "flex";
        setTimeout(function () {
            mobileMenu.style.transform = "translateX(0%)";
        }, 50);
    });

    hamburgerCross.addEventListener("click", function () {
        mobileMenu.style.transform = "translateX(-100%)";
        setTimeout(function () {
            mobileMenu.style.display = "none";
        }, 300);
    });

    window.addEventListener("resize", function () {
        if (window.innerWidth > 770) {
            mobileMenu.style.display = "none";
        }
    });
</script>
</html>
