package com.example.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The sign-in page. The form itself is posted to {@code /loginServlet}, which the security filter
 * chain takes.
 */
@Controller
public class LoginController {

    /** One message for both cases: naming which half was wrong tells an attacker whether the
     *  username exists. */
    public static final String REJECTED = "That username and password did not match.";
    public static final String LOCKED_OUT = "Too many sign-in attempts. Try again in a few minutes.";

    @GetMapping("/loginMenu")
    public String showMenu(@RequestParam(required = false) String error, Model model,
                           HttpServletRequest request) {
        if (error != null) {
            model.addAttribute("error", lastFailureWasALockout(request) ? LOCKED_OUT : REJECTED);
        }
        return "loginmenu";
    }

    private static boolean lastFailureWasALockout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object failure = session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        // Read once, so a later visit to the page does not repeat a message about an old attempt.
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        return failure instanceof LockedException;
    }
}
