package com.example;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.example.Game", "com.example.Controller", "com.example.Command", "com.example.Authentication"})
public class AppConfig {
    // Define additional configurations if needed
}
