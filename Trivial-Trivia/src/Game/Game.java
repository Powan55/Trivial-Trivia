package Game;

import Authentication.*;

import java.util.ArrayList;
import java.util.Scanner;

public class Game
{
    private Questions question;
    private ArrayList<String[]> queList;
    private Scanner scan;
    private ScoreTracker score;

    public Game(User user)
    {
        question = new Questions();
        queList = new ArrayList<>();
        scan = new Scanner(System.in);
        score = new ScoreTracker(user);
    }
    public void play()
    {
        //add code
        int count = 0;
        int userInput;
        queList = question.getQuestion();
        for (String data[]: queList.subList(1, queList.size())) {
            count++;
            for (int i = 0; i < data.length-1; i++) {
                if(i == 0)
                    System.out.print(count + ". ");
                else
                    System.out.print(i + ". ");
                System.out.println(data);
            }
            userInput = scan.nextInt();
            score.ansChecker(data[userInput], data[data.length-1]);
        }

    }

    public void addQuestion()
    {
        //String data[4] = new String[];

       // question.addQuestion(data);
    }
}


