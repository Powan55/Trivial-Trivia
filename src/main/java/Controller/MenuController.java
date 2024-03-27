package Controller;

import Game.Menu;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MenuController {

    private final Menu menu;

    // Constructor injection to get an instance of the Menu class
    public MenuController(Menu menu) {
        this.menu = menu;
    }

    @GetMapping("/menu")
    public String showMenu() {
        menu.menu(); // Invoke the menu logic
        return "menu"; // Return the name of the view (e.g., menu.jsp)
    }

    // Handle form submissions or other actions
    @PostMapping("/menu/action")
    public String handleMenuAction() {
        // Process the action and return the appropriate view
        return "result"; // Return the name of the result view
    }
}
