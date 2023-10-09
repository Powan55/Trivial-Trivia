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
        user.setAuthenticated(login.authenticate());
    }
}
