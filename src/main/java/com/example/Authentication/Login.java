package com.example.Authentication;

import com.example.Database.CSVAdapter;
import com.example.Database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class Login {

    private static final Logger logger = Logger.getLogger(Login.class.getName());

    @Autowired
    private Database database;
    @Autowired
    private ResourceLoader resourceLoader;

    public boolean authenticate() {
        database = new CSVAdapter();
        ArrayList<String[]> data = database.readFile("userData.csv");

        for (int i = 0; i < 4 ; i++) {
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter your username: ");
            String username = scan.nextLine();
            System.out.print("Enter your password: ");
            String password = scan.nextLine();


            for (String[] str : data) {
                if (str[1].equals(username)) {
                    String hashedPassword = PasswordHashing.hashPassword(password, str[3]);
                    if (str[2].equals(hashedPassword)) {
                        System.out.println("You have been logged in successfully.");
                        return true;
                    }
                }
            }
            System.out.println("ERROR: Incorrect username or password");
            System.out.println(3 - i + " attempt remaining!!");
        }
        return false;
    }

    public boolean authenticate(String username, String password) {
        database = new CSVAdapter();
        ArrayList<String[]> data = database.readFile("userData.csv");


        for (String[] str : data) {
            if (str[1].equals(username)) {
                String hashedPassword = PasswordHashing.hashPassword(password, str[3]);
                if (str[2].equals(hashedPassword)) {
                    logger.log(Level.INFO, "You have been logged in successfully.");
                    return true;
                }
            }
        }
        logger.log(Level.INFO, "ERROR: Incorrect username or password");

        return false;
    }
}
