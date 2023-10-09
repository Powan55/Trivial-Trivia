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
        ArrayList<String[]> userInfo = data.readFile("Trivial-Trivia/src/Data/userData.csv"); // Load existing user data

        if(isUniqueUser(info[1], userInfo)){ // Check username uniqueness based on index 1
            userInfo.add(info);
            user = new RealUser(info[0], info[1], info[2]);
            data.writeFile("Trivial-Trivia/src/Data/userData.csv", userInfo); // Save updated user data
            return true;
        }else{
            return false;
        }
    }

    public boolean isUniqueUser(String username, ArrayList<String[]> userInfo){
        for(String[] userData: userInfo){
            if(userData[1].equals(username)){ // Check username based on index 1
                return false; // Username is not unique
            }
        }
        return true; // Username is unique
    }
}