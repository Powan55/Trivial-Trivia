package Authentication;

import Database.*;

import java.util.ArrayList;

public class Login
{
    private Database database;
    ArrayList<String[]> data;
    public boolean authenticate(String username, String password)
    {
        database = new CSVAdapter();
        data = database.readFile("Trivial-Trivia/src/Data/loginData.txt");

        for (String str[]: data) {
            if(str[0].equals(username) && str[1].equals(password))
                return true;
        }

        return false;
    }
}
