import Database.*;

import java.io.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Database data = new CSVAdapter();
        ArrayList<String[]> testData  = data.readFile("");
//
//        for (String []str: testData
//             ) {
//            System.out.println(str.toString());
//        }

        System.out.println("Hello World!");
    }
}