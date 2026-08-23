package com.example.Controller;

import com.example.Authentication.CurrentPlayer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {

    private final CurrentPlayer user;

    public MenuController(CurrentPlayer user) {
        this.user = user;
    }

    @GetMapping("/menu")
    public String showMenu(Model model) {
        model.addAttribute("player", user.getUserInfo());
        model.addAttribute("authenticated", user.isAuthenticated());
        return "menu";
    }
}
