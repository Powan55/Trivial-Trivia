package com.example.Command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.Authentication.Login;
import com.example.Authentication.User;

@Component
public class LoginAction implements Action {

    @Qualifier("ProxyUser")
    private final User user;

    @Autowired
    public LoginAction(User user) {
        this.user = user;
    }

    @Override
    public void execute() {
        Login login = new Login();
        if (login.authenticate()) {
            user.setAuthenticated(true);
        } else {
            System.out.println("Due to multiple failed login attempts,\nthe game will continue as a guest user.");
        }
    }
}
