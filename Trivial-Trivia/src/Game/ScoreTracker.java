package Game;
import Authentication.*;

public class ScoreTracker
{
    private User user;
    //Main.Database.Database file;

    public ScoreTracker(User user)
    {
        this.user = user;
        //file = new Database.CSVAdapter();  // Database.Database needs to be implimented
    }

    public void ansChecker(String userInput, String ans)
    {
        if(userInput.equals(ans)){
            addScore();
        }
        else {
            removeScore();
        }
    }

    private void addScore()
    {
        int score = user.getScore();
        user.setScore(score + 10);
    }

    private void removeScore()
    {
        int score = user.getScore();
        if(score > 0)
            user.setScore(score - 10);
        else
            user.setScore(0);
    }

    

}
