package com.example.Command;

import com.example.Authentication.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class GuestAction implements Action{

    @Qualifier("ProxyUser")
    private final User user;
    @Autowired
    public GuestAction(User user){
        this.user = user;
    }
    @Override
    public void execute() {

    }
}
