<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Main menu - Trivial Trivia</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/menu.css">
</head>
<body>
<div class="navbar">
    <div class="nav-logo">
        <a href="${pageContext.request.contextPath}/">Trivial Trivia</a>
    </div>
    <div class="nav-items">
        <ul>
            <li><a href="${pageContext.request.contextPath}/play">Play</a></li>
            <li><a href="${pageContext.request.contextPath}/stats">Scores</a></li>
            <li><a href="${pageContext.request.contextPath}/questions">Questions</a></li>
        </ul>
    </div>
    <div class="nav-button">
        <div class="anim-layer"></div>
        <c:choose>
            <c:when test="${authenticated}">
                <a href="${pageContext.request.contextPath}/logout">Sign out</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/loginMenu">Sign in</a>
            </c:otherwise>
        </c:choose>
    </div>
    <div id="hamburger-menu">&#9776;</div>
</div>

<div id="mobile-menu">
    <div class="mobile-nav-items">
        <ul>
            <li><a href="${pageContext.request.contextPath}/play">Play</a></li>
            <li><a href="${pageContext.request.contextPath}/stats">Scores</a></li>
        </ul>
    </div>
    <div id="hamburger-cross">&#10006;</div>
</div>

<div class="menu-container">
    <h1>Main menu</h1>
    <p>Playing as <c:out value="${player}"/></p>
    <ul class="menu-list">
        <li><a href="${pageContext.request.contextPath}/play">Play</a></li>
        <li><a href="${pageContext.request.contextPath}/stats">View scores</a></li>
        <li><a href="${pageContext.request.contextPath}/questions">Import or export questions</a></li>
    </ul>
</div>

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
</body>
</html>
