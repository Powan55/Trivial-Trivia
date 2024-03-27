package Authentication;

import Database.*;
import Game.Game;

import java.util.ArrayList;

public class RealUser implements User
{
    private String name;
    private String userName;
    private String password;

    private String salt;
    private int score;

    private int wrong;
    private int right;
    private Game game;

    public RealUser()
    {
        wrong = 0;
        right = 0;
        score = 0;
        game = new Game();
    }

    public RealUser(String name, String userName, String password)
    {
        this.name = name;
        this.userName = userName;
        this.password = password;
        score = 0;
        wrong = 0;
        right = 0;
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
    public void saveScore() {
        Database database = new CSVAdapter();
        ArrayList<String[]> data = database.readFile("Trivial-Trivia/src/Data/StatData.csv");
        String[] info = {userName, (String.valueOf(score))};
        data.add(info);
        database.writeFile("Trivial-Trivia/src/Data/StatData.csv", data);
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

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public void setAuthenticated(boolean authentication) {
        //code
    }

    @Override
    public int getRight() {
        return right;
    }

    @Override
    public void setRight(int right) {
        this.right = right;
    }

    @Override
    public int getWrong() {
        return wrong;
    }

    @Override
    public void setWrong(int wrong) {
        this.wrong = wrong;
    }

}
