package Game;

import java.util.ArrayList;

public class Questions
{
    private ArrayList<String[]> question;
    private String fileName;

    //private Database.Database file;

    public Questions()
    {
        question = new ArrayList<>();
        fileName = "Data/questions.csv";
    }

    public ArrayList<String[]> getQuestion()
    {


        return question;
    }

    public void addQuestion(String data[])
    {
        question.add(data);
    }

}
