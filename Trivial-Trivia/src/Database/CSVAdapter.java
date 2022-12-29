package Database;

import java.io.*;
import java.util.ArrayList;

public class CSVAdapter implements Database{
    @Override
    public ArrayList<String[]> readFile(String fileName) {

        try{
            File NewFile = new File("src/testdata.txt");
            System.out.println(NewFile.getCanonicalPath());
            FileInputStream File_Input_Stream = new FileInputStream(NewFile);

            DataInputStream Data_Input_Stream = new DataInputStream(File_Input_Stream);
            BufferedReader Buffered_Reader = new BufferedReader(new InputStreamReader(Data_Input_Stream));
            String line;

            while((line = Buffered_Reader.readLine()) != null){
                System.out.println(line);
            }
            Data_Input_Stream.close();
        }catch(Exception e){
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {

    }
}
