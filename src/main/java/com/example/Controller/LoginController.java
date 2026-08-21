package com.example.Controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.Authentication.Login;
import com.example.Authentication.ProxyUser;
import com.example.Authentication.RealUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Serves the sign-in page and handles the sign-in form.
 */
@Controller
public class LoginController {

    private static final Logger logger = Logger.getLogger(LoginController.class.getName());

    private final Login login;
    private final ProxyUser user;

    public LoginController(Login login, ProxyUser user) {
        this.login = login;
        this.user = user;
    }

    @GetMapping("/loginMenu")
    public String showMenu() {
        return "loginmenu";
    }

    @PostMapping("/loginServlet")
    public String signIn(@RequestParam(defaultValue = "") String username,
                         @RequestParam(defaultValue = "") String password,
                         Model model) {
        RealUser authenticated = login.authenticate(username, password);
        if (authenticated == null) {
            logger.log(Level.INFO, "Sign-in rejected");
            // One message for both cases: naming which half was wrong tells an attacker
            // whether the username exists.
            model.addAttribute("error", "That username and password did not match.");
            model.addAttribute("username", username);
            return "loginmenu";
        }
        user.signIn(authenticated);
        return "redirect:/menu";
    }
}
