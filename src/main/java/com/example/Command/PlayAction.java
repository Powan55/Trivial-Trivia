package com.example.Command;

import com.example.Authentication.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class PlayAction implements Action {

    private final User user;

    @Autowired
    public PlayAction(User user) {
        this.user = user;
    }

    @Override
    public void execute() {
        user.play();
    }

    @GetMapping("/play") // Mapping the HTTP GET request to the execute method
    public String playGame() {
        return "play";
    }

}
