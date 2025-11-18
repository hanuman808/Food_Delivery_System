package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/fooddb?useSSL=false&serverTimezone=UTC";
    private static final String USER = "UserName";
    private static final String PASSWORD = "my_Password"; // replace

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // MySQL 8.x
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
