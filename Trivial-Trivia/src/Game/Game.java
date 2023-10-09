package Game;

import Authentication.User;

import java.util.ArrayList;
import java.util.Scanner;


public class Game
{
    private Questions questions;
    private ArrayList<Question> queList;
    private Scanner scan;
    private ScoreTracker score;

    public Game(User user)
    {
        questions = new Questions();
        queList = new ArrayList<>();
        scan = new Scanner(System.in);
        score = new ScoreTracker(user);
    }
    public void play()
    {
        int userInput;
        queList = questions.getQuestion();

        for (Question question : queList) {
            System.out.println(question.toString());
            userInput = scan.nextInt();
            System.out.println();
            if(userInput == 5) {
                break;
            }
        }
    }

    public void addQuestion(String data[])
    {
        //TODO Need to fix Question class first
     //   question.addQuestion(data);
    }
}


