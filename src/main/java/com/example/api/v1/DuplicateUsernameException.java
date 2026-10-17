package com.example.api.v1;

public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException() {
        super("That username is taken.");
    }
}
