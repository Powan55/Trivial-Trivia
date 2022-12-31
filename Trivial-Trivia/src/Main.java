import Database.*;

import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Database file = new CSVAdapter();
        ArrayList<String[]> data = new ArrayList<>();
//        data = file.readFile("Trivial-Trivia/src/Data/TestcsvData.csv");
//        for (String[] str : data) {
//            for (String s : str) {
//                System.out.println(s);
//            }
//        }
        String[] key = {"7","Uttam","POPSO"};
        data.add(key);
        String[] key2 = {"MONKEY","DONKEY","TEA"};
        data.add(key2);

        data.add(key);

        file.writeFile("Trivial-Trivia/src/Data/TestcsvData.csv",data);

   }

}
