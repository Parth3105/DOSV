package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public Connection getConnection(String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connected to the Database");
        } catch (SQLException e) {
            // handle error
            throw new RuntimeException(e);
        }

        return conn;
    }

    public Connection getConnection(String url, String username, String password) {
        Connection conn = null;
        try {
            
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to the Database");
        } catch (SQLException  e) {
            // handle error
            throw new RuntimeException(e);
        }

        return conn;
    }
}
