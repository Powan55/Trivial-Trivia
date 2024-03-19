package Command;

import Authentication.Login;
import Authentication.ProxyUser;
import Authentication.User;

public class LoginAction implements Action{
    @Override
    public void execute()
    {
        User user = ProxyUser.getInstance();
        Login login = new Login();
        if(login.authenticate()) {
            user.setAuthenticated(true);
            System.out.print("Login successful!");
        }
        else {
            System.out.println("Due to multiple failed login attempts,\nthe game will continue as a guest user.");
            user.setAuthenticated(false);
        }
    }
}
