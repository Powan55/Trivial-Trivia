package com.example.Game;

import com.example.Database.CSVAdapter;
import com.example.Database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class Questions
{
    private ArrayList<String[]> question;
    private String fileName;


    @Autowired
    private Database database;


    public Questions()
    {
        database = new CSVAdapter();
        question = new ArrayList<>();
        fileName = "src/main/java/Data/QuestionData.csv";
    }

    public ArrayList<Question> getQuestion()
    {
        ArrayList<Question> questions = new ArrayList<>();
        question = database.readFile(fileName);
        for (String[] data: question.subList(1, question.size())) {
            questions.add(new Question(data[0], data[1], data[2], data[3], data[4], data[5]));
        }
        return questions;
    }
}
