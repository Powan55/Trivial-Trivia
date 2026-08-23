package com.example.Controller;

import java.util.Optional;

import com.example.Authentication.CreateUser;
import com.example.Authentication.PlayerPrincipal;
import com.example.Authentication.Registration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Registration and the guest path.
 *
 * <p>Signing out is the framework's, at {@code POST /logout}.</p>
 */
@Controller
public class AccountController {

    private final CreateUser createUser;
    private final SecurityContextRepository contexts;

    public AccountController(CreateUser createUser, SecurityContextRepository contexts) {
        this.createUser = createUser;
        this.contexts = contexts;
    }

    @GetMapping("/createUser")
    public String showForm() {
        return "createUser";
    }

    @PostMapping("/createUser")
    public String register(@RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String username,
                           @RequestParam(defaultValue = "") String password,
                           Model model,
                           HttpServletRequest request,
                           HttpServletResponse response) {
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
        signIn(PlayerPrincipal.newlyRegistered(username, name), request, response);
        return "redirect:/menu";
    }

    /** Playing without an account. Anonymous is the state a guest is already in. */
    @GetMapping("/guest")
    public String continueAsGuest() {
        return "redirect:/menu";
    }

    /** The same session fixation and storage the form login path gets, done by hand. */
    private void signIn(PlayerPrincipal player, HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken
                .authenticated(player, null, player.getAuthorities()));
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
    }
}
