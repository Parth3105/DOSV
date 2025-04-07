import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Scope of improvement:
 * When to quit the connection with the client
 */
public class Server {
    public static void main(String[] args) {
        int ServerPort=8080;
        int DatabasePort=9000;
        ServerSocket serverSocket;
        String DatabaseIP = "127.0.0.1";
        try {
            serverSocket=new ServerSocket(ServerPort,20);
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
        
        System.out.println("Server is running on port: "+ServerPort);
        //ConnectToDatabase(DatabaseIP, DatabasePort);
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
    public static void ConnectToDatabase(String DatabaseIP, int DatabasePort) {
        // Implement database connection logic here
        // This is a placeholder for the actual database connection code
        int maxRetries = 3; 
        int retryDelay = 2000; 
        Socket socket = null;
        int attempt = 0;

        DataOutputStream dataOutputStream=null;

        while (attempt < maxRetries) {
            try {
                socket = new Socket(DatabaseIP, DatabasePort);
                System.out.println("Connected to server at " + DatabaseIP + ":" + DatabasePort);
                dataOutputStream=new DataOutputStream(socket.getOutputStream());
                break; // Successful connection, exit loop
            } catch (ConnectException ce) {
                // System.err.println("Attempt " + (attempt + 1) + ": Could not connect to server. Retrying...");
            } catch (IOException e) {
                // System.err.println("Attempt " + (attempt + 1) + ": Failed to establish connection. Retrying...");
            }

            attempt++;
            if (attempt < maxRetries) {
                try {
                    // Wait before retrying
                    Thread.sleep(retryDelay); 
                } catch (InterruptedException ie) {
                    System.err.println("Server Connection interrupted. Exiting.");
                    System.exit(1);
                }
            } else {
                System.err.println("Could not connect to Server! Server might be down.");
                System.exit(1);
            }
        }
        System.out.println("Connecting to database at " + DatabaseIP + ":" + DatabasePort);
    }
}
