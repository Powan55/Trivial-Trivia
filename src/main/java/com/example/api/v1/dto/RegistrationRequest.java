package com.example.api.v1.dto;

import com.example.Authentication.Registration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank @Size(max = Registration.NAME_MAX) String name,
        @NotBlank @Size(min = Registration.USERNAME_MIN, max = Registration.USERNAME_MAX)
        @Pattern(regexp = "[A-Za-z0-9_]+") String username,
        @NotBlank @Size(min = Registration.PASSWORD_MIN) String password) {
}
