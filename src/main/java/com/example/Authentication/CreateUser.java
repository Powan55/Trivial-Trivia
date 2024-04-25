package com.example.Authentication;

import com.example.Database.CSVAdapter;
import com.example.Database.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * The CreateUser class is responsible for creating a user and storing their
 * information in a CSV file.
 * @author Laxmi Poudel
 */
@Component
public class CreateUser {
    private User user;

    @Autowired
    private Database database;


    public CreateUser(){
        user = null;
        database = new CSVAdapter();
    }

    public boolean makeUser(String[] info){
        ArrayList<String[]> userInfo = database.readFile("Trivial-Trivia/src/Data/userData.csv"); // Load existing user data


   if(isUniqueUser(info[1], userInfo)){
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

       database.writeFile("Trivial-Trivia/src/Data/userData.csv", userInfo);
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