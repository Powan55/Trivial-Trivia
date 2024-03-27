package Command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Authentication.Login;
import Authentication.User;

@Component
public class LoginAction implements Action {

    private final User user;

    @Autowired
    public LoginAction(User user) {
        this.user = user;
    }

    @Override
    public void execute() {
        Login login = new Login();
        if (login.authenticate()) {
            user.setAuthenticated(true);
        } else {
            System.out.println("Due to multiple failed login attempts,\nthe game will continue as a guest user.");
        }
    }
}
