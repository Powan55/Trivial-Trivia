package Command;

import Authentication.ProxyUser;
import Authentication.User;

public class GuestAction implements Action{
    @Override
    public void execute() {
        User user = ProxyUser.getInstance();
        user.setAuthenticated(false);
        System.out.println("Continuing as a guest. Any progress will not be saved.");
    }
}
