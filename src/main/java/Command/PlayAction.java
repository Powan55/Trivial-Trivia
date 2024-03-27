package Command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Authentication.User;

@Component
public class PlayAction implements Action {

    private final User user;

    @Autowired
    public PlayAction(User user) {
        this.user = user;
    }

    @Override
    public void execute() {
        user.play();
    }
}
