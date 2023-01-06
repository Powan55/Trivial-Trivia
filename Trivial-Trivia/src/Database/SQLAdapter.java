package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SQLAdapter implements Database {
    private final Connection connection;

    private String USERNAME = "uttambhattarai";
    private String PASSWORD = "0713";
    private String URL = "postgres://uttambhattarai@localhost:5432/uttambhattarai";


    public SQLAdapter(String url, String username, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, username, password);
    }

    @Override
    public ArrayList<String[]> readFile(String fileName) { //tablename
        ArrayList<String[]> data = new ArrayList<>();
        try {
            String sql = "SELECT * FROM " + fileName;
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int numColumns = rs.getMetaData().getColumnCount();
                String[] row = new String[numColumns];
                for (int i = 1; i <= numColumns; i++) {
                    row[i - 1] = rs.getString(i);
                }
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public void writeFile(String fileName, ArrayList<String[]> data) {

    }
}

