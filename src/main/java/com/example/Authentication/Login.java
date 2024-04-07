package com.example.Authentication;

import com.example.Database.CSVAdapter;
import com.example.Database.Database;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Scanner;


@Component
public class Login {
    public boolean authenticate() {
        Database database = new CSVAdapter();
        ArrayList<String[]> data = database.readFile("src/main/java/Data/userData.csv");

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
}
