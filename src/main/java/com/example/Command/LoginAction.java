package com.example.Command;

import com.example.Authentication.Login;
import com.example.Authentication.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The LoginAction class represents the controller responsible for handling login-related requests.
 * It implements the Action interface to define the execution of login actions.
 */
@Component
@Controller
public class LoginAction implements Action {

    private final User user;


    private static final Logger logger = Logger.getLogger(LoginAction.class.getName());

    /**
     * Constructs a new LoginAction with the specified User.
     * @param user the User object for authentication
     */
    @Autowired
    public LoginAction(User user) {
        this.user = user;
    }

    /**
     * Executes the login action, authenticating the user based on provided credentials.
     */
    @Override
    public void execute() {
        Login login = new Login();
        if (login.authenticate()) {
            user.setAuthenticated(true);
        } else {
            System.out.println("Due to multiple failed login attempts,\nthe game will continue as a guest user.");
        }
    }

    /**
     * Displays the login menu page.
     * @return the name of the view (loginmenu.jsp)
     */
    @GetMapping("/loginMenu")
    public String showMenu() {
        return "loginmenu";
    }

    /**
     * Handles the login form submission.
     * @param username the username submitted in the login form
     * @param password the password submitted in the login form
     * @return the view name for the login success page
     */
    @PostMapping("/loginServlet")
    public String loginServlet(@RequestParam("username") String username,
                               @RequestParam("password") String password) {

        Login login = new Login();
        if (login.authenticate(username, password)) {
            logger.log(Level.INFO, "Logged in successfully");
            user.setAuthenticated(true);
            return "redirect:/menu";
        }

        logger.log(Level.INFO, "Log in failed!!");

        return "redirect:/loginMenu";
    }
}
