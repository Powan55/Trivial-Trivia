package Game;

import Database.Database;
import Database.CSVAdapter;

import java.util.ArrayList;

public class Questions
{
    private ArrayList<String[]> question;
    private String fileName;

    private final Database file;

    public Questions()
    {
        file = new CSVAdapter();
        question = new ArrayList<>();
        fileName = "Trivial-Trivia/src/Data/QuestionData.csv";
    }

    public ArrayList<Question> getQuestion()
    {
        ArrayList<Question> questions = new ArrayList<>();
        question = file.readFile(fileName);
        for (String[] data: question.subList(1, question.size())) {
            questions.add(new Question(data[0], data[1], data[2], data[3], data[4], data[5]));
        }
        return questions;
    }
}
