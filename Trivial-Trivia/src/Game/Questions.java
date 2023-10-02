package Game;

import Database.Database;

import java.util.ArrayList;

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
        //TODO need to format the questions into 2D array with question, answer, and option format
        question = file.readFile(fileName);

        return null;
    }

}
