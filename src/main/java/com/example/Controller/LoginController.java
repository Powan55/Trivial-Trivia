package com.example.Controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.Authentication.Login;
import com.example.Authentication.User;
import org.springframework.stereotype.Controller;
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
    private final User user;

    public LoginController(Login login, User user) {
        this.login = login;
        this.user = user;
    }

    @GetMapping("/loginMenu")
    public String showMenu() {
        return "loginmenu";
    }

    @PostMapping("/loginServlet")
    public String signIn(@RequestParam("username") String username,
                         @RequestParam("password") String password) {
        if (login.authenticate(username, password)) {
            user.setAuthenticated(true);
            return "redirect:/menu";
        }
        logger.log(Level.INFO, "Sign-in rejected");
        return "redirect:/loginMenu";
    }
}
