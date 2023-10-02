package Authentication;

import Database.CSVAdapter;
import Database.Database;

import java.util.ArrayList;

/**
 * The CreateUser class is responsible for creating a user and storing their
 * information in a CSV file.
 * @author Laxmi Poudel
 */
public class CreateUser {
    private User user;
    private Database data;

    public CreateUser(){
        user = null;
        data = new CSVAdapter();
    }

    public boolean makeUser(String[] info){
        ArrayList<String[]> userInfo = new ArrayList<>();

        userInfo.add(info);
        user = new RealUser(info[0], info[1], info[2]);
        data.writeFile("Trivial-Trivia/src/Data/userData.csv", userInfo);
       return true;
    }
}