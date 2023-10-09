package Game;

import Command.*;

import java.util.Scanner;

public class Menu
{
    private Action action;
    private Scanner scan;


    /**
     * Displays the main menu for the Trivial Trivia game, allowing the user to choose from various options
     * such as logging in, continuing as a guest, creating a new account, or exiting the game.
     * This method reads user input and triggers corresponding actions based on the user's choice.
     * After executing the selected action, it calls the `menu2()` method to display the next menu or
     * terminates the game if the user chooses to exit.
     */
    public void menu()
    {
        int userInput = 0;
        scan = new Scanner(System.in);


        System.out.println("Welcome To Trivial Trivia\n" +
                "Please select the user type.\n" +
                "1. Login\n2. Continue as guest\n3. Create a new account");
        userInput = scan.nextInt();

        switch (userInput) {
            case 1:{
                action = new LoginAction();
                action.execute();
                menu2();
                break;
            }
            case 2: {
                action = new GuestAction();
                action.execute();
                menu2();
                break;
            }
            case 3:{
                action = new CreateAccountAction();
                action.execute();
                menu2();
                break;
            }
            case 4:{
                System.out.println("Thanks for playing");
                break;
            }
            default:{
                System.out.println("Please select a valid option");
                break;
            }


        }
    }
    private void menu2()
    {
        scan = new Scanner(System.in);
        int userInput = 0;
        while (userInput != 5){
            System.out.println("Please one of the following options:\n" +
                    "1. Play\n2. View Stat\n5. Exit");
            userInput = scan.nextInt();

            switch (userInput) {
                case 1: {
                    action = new PlayAction();
                    action.execute();
                    break;
                }
                case 2:{
                    System.out.println();
                    action = new ViewStatAction();
                    action.execute();
                    System.out.println("\n");
                    break;
                }
                case 5: {
                    System.out.println("Exiting the Game.");
                    System.out.println("Game Summery");
                    //TODO Add the necessary code to display a game summery for user once they chose to exit.
                    break;
                }
                default:{
                    System.out.println("Invalid option!!\nPlease select a valid option.");
                    break;
                }

            }
        }
    }
}
