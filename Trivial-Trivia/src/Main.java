import Database.*;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, SQLException {
//        Database file = new CSVAdapter();
//        ArrayList<String[]> data = new ArrayList<>();

        Database file = new JSONAdapter();
        ArrayList<String[]> data = new ArrayList<>();
        data.add(new String[] {"John", "Doe", "34"});
        data.add(new String[] {"Jane", "Doe", "29"});
        data.add(new String[] {"Bob", "Smith", "42"});

        file.writeFile("Trivial-Trivia/src/Data/data.json", data);

        ArrayList<String[]> readData = file.readFile("Trivial-Trivia/src/Data/data.json");
        for (String[] row : readData){
            for (int i = 0; i < row.length; i++) {
                System.out.println(row[i]+" ");
            }
            System.out.println();
        }




//        String[] key = {"7","wink","pink"};
//        data.add(key);
//        String[] key2 = {"10","stink","mink"};
//        data.add(key2);
//        String[] key3 = {"11","ghhh","mhhh"};


       //file.writeFile("Trivial-Trivia/src/Data/StatData.csv",data);

     //   Database adapter = new SQLAdapter();
//       data = file.readFile("Trivial-Trivia/src/Data/StatData.csv");
//
//        for (String[] str:data
//             ) {
//            for (int i = 0; i < str.length; i++) {
//                System.out.println(str[i]);
//            }
//        }



   }

}
