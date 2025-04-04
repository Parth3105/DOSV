import java.io.IOException;
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
        int port=8080;
        ServerSocket serverSocket;
        try {
            serverSocket=new ServerSocket(port,20);
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
        System.out.println("Server is running on port: "+port);

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
}
