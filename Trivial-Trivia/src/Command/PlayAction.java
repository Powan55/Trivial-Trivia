package Command;

import Authentication.ProxyUser;
import Authentication.User;

public class PlayAction implements Action{
    @Override
    public void execute() {
        User user = new ProxyUser();
        user.play();
    }
}
