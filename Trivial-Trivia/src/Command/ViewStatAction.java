package Command;

import Authentication.ProxyUser;
import Authentication.User;

public class ViewStatAction implements Action{
    @Override
    public void execute() {
        User user = ProxyUser.getInstance();
        System.out.println("Game Summery");
        System.out.println("Score: " + user.getScore());
        System.out.println("Right Answers: "+user.getRight());
        System.out.println("Wrong Answers: "+user.getWrong());

        user.saveScore();
        System.out.println("\n");
    }
}
