package com.example.Controller;

import java.util.Optional;

import com.example.Authentication.CreateUser;
import com.example.Authentication.ProxyUser;
import com.example.Authentication.RealUser;
import com.example.Authentication.Registration;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Registration, the guest path, and signing out.
 */
@Controller
public class AccountController {

    private final CreateUser createUser;
    private final ProxyUser user;

    public AccountController(CreateUser createUser, ProxyUser user) {
        this.createUser = createUser;
        this.user = user;
    }

    @GetMapping("/createUser")
    public String showForm() {
        return "createUser";
    }

    @PostMapping("/createUser")
    public String register(@RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String username,
                           @RequestParam(defaultValue = "") String password,
                           Model model) {
        Optional<String> problem = Registration.validate(name, username, password);
        if (problem.isEmpty() && !createUser.makeUser(new String[] {name, username, password})) {
            problem = Optional.of("That username is taken.");
        }
        if (problem.isPresent()) {
            // Back to the form with what they typed, minus the password.
            model.addAttribute("error", problem.get());
            model.addAttribute("name", name);
            model.addAttribute("username", username);
            return "createUser";
        }
        user.signIn(new RealUser(name, username));
        return "redirect:/menu";
    }

    /** Playing without an account. Clears any signed-in user rather than inheriting one. */
    @GetMapping("/guest")
    public String continueAsGuest() {
        user.signOut();
        return "redirect:/menu";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        user.signOut();
        session.invalidate();
        return "redirect:/loginMenu";
    }
}
