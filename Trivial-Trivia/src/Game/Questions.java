package Game;

import java.util.ArrayList;
import Database.*;

public class Questions
{
    private ArrayList<String[]> question;
    private String fileName;

    private Database file;

    public Questions()
    {
        question = new ArrayList<>();
        fileName = "Data/questions.csv";
    }

    public ArrayList<String[]> getQuestion()
    {
        question = file.readFile(fileName);
        return question;
    }

    public void addQuestion(String data[])
    {
        question.add(data);
        file.writeFile(fileName, question);
    }

}
