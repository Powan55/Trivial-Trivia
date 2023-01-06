import Database.*;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, SQLException {
//        Database file = new CSVAdapter();
//        ArrayList<String[]> data = new ArrayList<>();
//
//
//
//        String[] key = {"7","wink","pink"};
//        data.add(key);
//        String[] key2 = {"10","stink","mink"};
//        data.add(key2);
//        String[] key3 = {"11","ghhh","mhhh"};
//
//
//        file.writeFile("Trivial-Trivia/src/Data/TestcsvData.csv",data);

        Database adapter = new SQLAdapter();
        adapter.HOST = "localhost";
        adapter.DATABASE = "mydatabase";
        adapter.PORT = "5432";

   }

}
