package Game;

import Command.*;

import java.util.Scanner;

public class Menu
{
    private Action action;
    private Scanner scan;

    public void menu()
    {
        int userinput = 0;
        scan = new Scanner(System.in);


        System.out.println("Welcome To Trival Trivia\n" +
                "Please select the user type.\n" +
                "1. Login\n2. Contunue as guest\n3. Create a new account");
        userinput = scan.nextInt();

        switch (userinput) {
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
            }


        }
    }
    private void menu2()
    {
        int userinput = 0;

        System.out.println("Please select the user type.\n" +
                "1. Login\n2. Continue as guest\n3. Create a new account");
        userinput = scan.nextInt();

        switch (userinput){
            //add code
        }
    }
}
