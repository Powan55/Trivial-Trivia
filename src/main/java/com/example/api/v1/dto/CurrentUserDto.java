package com.example.api.v1.dto;

import com.example.Authentication.CurrentPlayer;

public record CurrentUserDto(boolean authenticated, String username, String info, boolean admin) {

    public static CurrentUserDto from(CurrentPlayer player) {
        return new CurrentUserDto(player.isAuthenticated(), player.getUsername(),
                player.getUserInfo(), player.isAdmin());
    }
}
