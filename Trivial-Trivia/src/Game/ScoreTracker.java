package Game;
import Authentication.User;

public class ScoreTracker {

    private User user;
    private int wrongAns;
    private int rightAns;

    public ScoreTracker(User user) {
        this.user = user;
    }

    public void ansChecker(String userInput, String ans) {

        if (userInput.equals(ans)) {
            addScore();
            System.out.println("Correct!\n");
        } else {
            removeScore();
            System.out.println("Incorrect ;(\n");
        }

    }

    private void addScore() {
        int right = user.getRight();
        int score = user.getScore();
        user.setScore(score + 10);
        user.setRight(right+1);
    }

    private void removeScore() {

        int wrong = user.getWrong();
        int score = user.getScore();
        if (score > 0) {
            user.setWrong(wrong+1);
            user.setScore(score - 10);
        } else {
            user.setScore(0);
        }
    }
}
