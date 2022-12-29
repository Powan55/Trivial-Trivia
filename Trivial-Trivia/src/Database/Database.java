package Database;

import java.util.ArrayList;

public interface Database
{
    public ArrayList<String[]> readFile(String fileName);
    public void writeFile(String fileName, ArrayList<String[]> data);
}
