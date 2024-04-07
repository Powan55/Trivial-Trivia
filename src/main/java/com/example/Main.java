package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Main extends SpringBootServletInitializer { // Extend SpringBootServletInitializer for WAR deployment

    @Override // Override the configure method
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application){
        return application.sources(Main.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}

// Use a separate class or the same class for web controllers
@RestController // This annotation is necessary to handle HTTP requests
class WebController {

    @GetMapping(value = "/hello") // Use GetMapping for mapping HTTP GET requests
    public String helloWorld(){
        return "Hello World!";
    }
}
