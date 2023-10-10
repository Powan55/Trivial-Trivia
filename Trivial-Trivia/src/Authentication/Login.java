package Authentication;

import Database.*;

import java.util.ArrayList;
import java.util.Scanner;

public class Login
{
    public boolean authenticate()
    {
        Database database = new CSVAdapter();
        ArrayList<String[]> data = database.readFile("Trivial-Trivia/src/Data/userData.csv");

        for (int i = 0; i < 4 ; i++) {
            Scanner scan = new Scanner(System.in);
            System.out.print("Enter your username: ");
            String username = scan.nextLine();
            System.out.print("Enter your password: ");
            String password = scan.nextLine();

            for (String[] str : data) {

                if (str[1].equals(username) && str[2].equals(password)) {
                    System.out.println("You have been logged in successfully.");
                    return true;
                }
            }
            System.out.println("ERROR: Incorrect username or password");
            System.out.println(3-i + " attempt remaining!!");
        }
        return false;
    }
}
