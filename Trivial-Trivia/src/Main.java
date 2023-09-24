import Database.*;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws FileNotFoundException, SQLException {

        //  TEST for csv read/write
        Database file = new CSVAdapter();
        ArrayList<String[]> data = new ArrayList<>();
        data.add(new String[] {"John", "Doe", "34"});
        data.add(new String[] {"Jane", "Doe", "29"});
        data.add(new String[] {"Bob", "Smith", "42"});

        file.writeFile("Trivial-Trivia/src/Data/StatData.csv", data);

        ArrayList<String[]> readData = file.readFile("Trivial-Trivia/src/Data/StatData.csv");

        for (String[] row : readData){
            for (int i = 0; i < row.length; i++) {
                System.out.println(row[i]+" ");
            }
            System.out.println();
        }

   }

}

