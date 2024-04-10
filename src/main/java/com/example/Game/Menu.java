package com.example.Game;

import com.example.Command.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class Menu {
    private final LoginAction loginAction;
    private final GuestAction guestAction;
    private final CreateAccountAction createAccountAction;
    private final ViewStatAction viewStatAction;
    private final PlayAction playAction;
//    private Action action;
    private boolean isTrue;

    private final Scanner scan;

    @Autowired
    public Menu(LoginAction loginAction, GuestAction guestAction, CreateAccountAction createAccountAction, PlayAction playAction, ViewStatAction viewStatAction) {
        this.loginAction = loginAction;
        this.guestAction = guestAction;
        this.createAccountAction = createAccountAction;
        this.playAction = playAction;
        this.viewStatAction = viewStatAction;
        this.scan = new Scanner(System.in);
        this.isTrue = true;
    }

    public void menu() {
        int userInput;
        boolean isTrue = true;
        System.out.println("Welcome To Trivial Trivia");

        while (isTrue) {
            System.out.println("Please select the user type.\n" +
                    "1. Login\n2. Continue as guest\n3. Create a new account\n4. Exit");
            userInput = scan.nextInt();

            switch (userInput) {
                case 1:
                    loginAction.execute();
                    menu2();
                    break;
                case 2:
                    guestAction.execute();
                    menu2();
                    break;
                case 3:
                    createAccountAction.execute();
                    menu2();
                    break;
                case 4:
                    System.out.println("Thanks for playing");
                    isTrue = false;
                    break;
                default:
                    System.out.println("Please select a valid option");
                    break;
            }
        }
    }

    private void menu2() {
        int userInput = 0;
        while (userInput != 5 && isTrue){
            System.out.println("Please one of the following options:\n" +
                    "1. Play\n2. View Stat\n5. Exit");
            userInput = scan.nextInt();

            switch (userInput) {
                case 1: {
                    playAction.execute();
                    break;
                }
                case 2: {
                    System.out.println();
                    viewStatAction.execute();
                    break;
                }
                case 5: {
                    System.out.println("\nExiting the Game.");
                    playAction.execute();
                    break;
                }
                default: {
                    System.out.println("Invalid option!!\nPlease select a valid option.");
                    break;
                }
            }
        }
    }
}
