package Command;

import Authentication.ProxyUser;
import Authentication.Login;
import Authentication.User;

import java.util.Scanner;

public class LoginAction implements Action{
    @Override
    public void execute()
    {
        Scanner scan = new Scanner(System.in);
        User user = new ProxyUser();
        Login login = new Login();

        System.out.print("Enter your username: ");
        String username = scan.nextLine();
        System.out.print("Enter your password: ");
        String password = scan.nextLine();

        user.setAuthenticated(login.authenticate(username, password));
    }
}
