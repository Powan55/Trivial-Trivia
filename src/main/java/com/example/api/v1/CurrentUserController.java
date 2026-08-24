package com.example.api.v1;

import com.example.Authentication.CurrentPlayer;
import com.example.api.v1.dto.CurrentUserDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CurrentUserController {

    private final CurrentPlayer user;

    public CurrentUserController(CurrentPlayer user) {
        this.user = user;
    }

    @GetMapping("/api/v1/users/me")
    public CurrentUserDto currentUser() {
        return CurrentUserDto.from(user);
    }
}
