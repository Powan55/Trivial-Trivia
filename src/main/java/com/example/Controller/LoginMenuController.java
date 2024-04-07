package com.example.Controller;

import com.example.Game.Menu;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginMenuController {

    private final Menu menu;

    // Constructor injection to get an instance of the Menu class
    public LoginMenuController(Menu menu) {
        this.menu = menu;
    }

    @GetMapping("/loginMenu")
    public String showMenu() {
//        menu.menu(); // Invoke the menu logic
        return "loginmenu"; // Return the name of the view (e.g., loginmenu.jsp)
    }

    // Handle form submissions or other actions
    @PostMapping("/loginMenu/action")
    public String handleMenuAction() {
        // Process the action and return the appropriate view
        return "result"; // Return the name of the result view
    }
}
