import Authentication.CreateUser;
import Game.Menu;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, SQLException {

        Menu menu = new Menu();

//        CreateUser createUser1 = new CreateUser();
//        String[] user1 = {"Uttam","uttam7","abc"};
//        createUser1.makeUser(user1);

//        CreateUser createUser2 = new CreateUser();
//        String[] user2 = {"Uttam7","hi","aga"};
//        createUser2.makeUser(user2);


       menu.menu();

   }

}

