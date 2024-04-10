package com.example.Command;

import com.example.Authentication.CreateUser;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class CreateAccountAction implements Action{

    @Override
    public void execute() {
        Scanner scan = new Scanner(System.in);
        CreateUser createUser = new CreateUser();

        String[] info = new String[4];

        System.out.println("Enter your name: ");
        info[0] = scan.nextLine();

        System.out.println("Enter your username: ");
        info[1] = scan.nextLine();

        System.out.println("Enter your password");
        info[2] = scan.nextLine();

        boolean isSuccessful = createUser.makeUser(info);

        if(isSuccessful){
            System.out.println("Account has been created successfully.\n");
        }
        else {
            System.out.println("Creating account was unsuccessful!!\nPlease try again.\n");
        }

    }
}
