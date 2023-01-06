package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class SQLAdapter implements Database {
    private Connection connection;



    public SQLAdapter(String url, String username, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, username, password);
    }

    @Override
    public ArrayList<String[]> readFile(String fileName) {
        return null;
    }

    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {

    }
}

