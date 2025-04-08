import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Scope of improvement:
 * When to quit the connection with the client
 */
public class Server {
    public static void main(String[] args) {
        int ServerPort=8080;
        ServerSocket serverSocket;
        try {
            serverSocket=new ServerSocket(ServerPort,20);
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
        
        System.out.println("Server is running on port: "+ServerPort);
        ConnectToDatabase();
        List<Socket> clients=new ArrayList<>();
        while(true){
            try {
                clients.add(serverSocket.accept());
                new Thread(new TaskHandler(clients.getLast())).start();
                System.out.println("New client connected: "+clients.getLast().getInetAddress().getHostAddress()+":"+clients.getLast().getPort());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void ConnectToDatabase() {
        // Implement database connection logic here
        // This is a placeholder for the actual database connection code
        String url = "jdbc:postgresql://dpg-cvo3m0ffte5s73bajlbg-a.singapore-postgres.render.com:5432/dosv_metaserver";
        String user = "adi_25704";
        String password = "3iBn7AmefwTTZbilb2mOk5PkmJ2caWQy";
        try {
            Class.forName("org.postgresql.Driver"); // Optional in modern Java, but safe
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected!");

        }catch(Exception e) {
            e.printStackTrace();
        }
    }
}
