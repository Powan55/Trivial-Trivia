package Authentication;

import Game.Game;

public class RealUser implements User
{
    private String name;
    private String userName;
    private String password;
    private int score;
    private Game game;

    public RealUser()
    {
        score = 0;
        game = new Game();
    }

    public RealUser(String name, String userName, String password)
    {
        this.name = name;
        this.userName = userName;
        this.password = password;
        score = 0;
        game = new Game();
    }

    @Override
    public void play()
    {
        game.play();
    }


    @Override
    public String getUserInfo()
    {
        StringBuilder info = new StringBuilder();
        info.append("Player Info \n Name: " + name);
        info.append("Username: " + userName);
        info.append("Score: " + score);
        return info.toString();
    }

    @Override
    public void saveScore(int score) {

    }

    @Override
    public void importData(String fileName) {
    }

    @Override
    public void exportData(String fileName) {


    }

    @Override
    public void addQuestions(String data[]) {
        //game.addQuestion(data);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public void setAuthenticated(boolean authentication) {
        //code
    }

}
