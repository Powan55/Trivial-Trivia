package Database;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.*;
import java.util.ArrayList;

public class CSVAdapter implements Database{
    @Override
    public ArrayList<String[]> readFile(String fileName) {
        ArrayList<String[]> data = new ArrayList<>();
        try{
            CSVReader reader = new CSVReader(new FileReader(fileName));
            String[] line;
            while((line = reader.readNext()) != null){
                data.add(line);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return data;
    }
    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(fileName, true));
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    String str = row[i].replaceAll("^\"|\"$", "");
                    row[i] = str;
                }
                writer.writeNext(row);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
