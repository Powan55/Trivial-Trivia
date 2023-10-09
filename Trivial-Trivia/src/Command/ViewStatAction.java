package Command;

import Authentication.ProxyUser;
import Authentication.User;

public class ViewStatAction implements Action{
    @Override
    public void execute() {
        User user = ProxyUser.getInstance();

        System.out.println("Score: " + user.getScore());
    }
}
