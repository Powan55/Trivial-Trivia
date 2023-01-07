package Database;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.io.FileWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONAdapter implements Database {
    @Override
    public ArrayList<String[]> readFile(String fileName) {
        ArrayList<String[]> data = new ArrayList<>();

        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(fileName)) {
            JSONArray jsonArray = (JSONArray) parser.parse(reader);
            for (Object object : jsonArray) {
                JSONObject jsonObject = (JSONObject) object;
                int numColumns = jsonObject.keySet().size();
                String[] row = new String[numColumns];
                int i = 0;
                for (Object key : jsonObject.keySet()) {
                    row[i] = (String) jsonObject.get(key);
                    i++;
                }
                data.add(row);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {
        JSONArray jsonArray = new JSONArray();
        for (String[] row : data) {
            JSONObject jsonObject = new JSONObject();
            for (int i = 0; i < row.length; i++) {
                jsonObject.put("column" + i, row[i]);
            }
            jsonArray.add(jsonObject);
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            jsonArray.writeJSONString(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
