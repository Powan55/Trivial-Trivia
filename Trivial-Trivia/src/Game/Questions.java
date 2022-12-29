package Game;

import java.util.ArrayList;

public class Questions
{
    private ArrayList<String[]> question;
    private String fileName;

    //private Database file;

    public Questions()
    {
        question = new ArrayList<>();
        fileName = "Data/questions.csv";
    }

    public ArrayList<String[]> getQuestion()
    {
        //add code
        return question;
    }

    public void addQuestion(String data[])
    {
        question.add(data);
        //call write function from database
    }

}
