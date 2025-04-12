package config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    public Connection ConnectToDatabase() {
        // Implement database connection logic here
        // This is a placeholder for the actual database connection code
        String url = "jdbc:postgresql://dpg-cvo3m0ffte5s73bajlbg-a.singapore-postgres.render.com:5432/dosv_metaserver";
        String user = "adi_25704";
        String password = "3iBn7AmefwTTZbilb2mOk5PkmJ2caWQy";
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver"); // Optional in modern Java, but safe
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected!");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }
}
