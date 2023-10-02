
package Database;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
//import com.opencsv.exceptions.CsvConstraintViolationException;

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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return data;
    }

    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {
        try {
            CSVWriter writer = new CSVWriter(new FileWriter(fileName, false));
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].replace("\"", "");
                }
                writer.writeNext(row);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

