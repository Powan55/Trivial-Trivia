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

        //create a salt and hash
        String salt = PasswordHashing.generateSalt();
        String hash = PasswordHashing.hashPassword(info[2], salt);


        user = new RealUser(info[0], info[1], hash);
        user.setSalt(salt);
        //add salt and hash to user
        //write it to user
        info[2] = hash;
        info[3] = salt;
        userInfo.add(info);

        data.writeFile("Trivial-Trivia/src/Data/userData.csv", userInfo);
       return true;
    }
}