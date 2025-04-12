import config.DatabaseConnect;
import java.io.DataOutputStream;
import distribution.ConsistentHashing;
import taskhandle.TaskHandler;
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
        int ServerPort=8080;

        List<String> storageNodes=new ArrayList<>();
        storageNodes.add("192.168.110.94:8090");
        storageNodes.add("192.168.110.51:8090");
        storageNodes.add("192.168.110.182:8090");
        ConsistentHashing chunkDistributor=new ConsistentHashing(storageNodes);

        ServerSocket serverSocket;
        try {
            serverSocket=new ServerSocket(ServerPort,20);
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
        
        System.out.println("Server is running on port: "+ServerPort);
        DatabaseConnect.ConnectToDatabase();
        List<Socket> clients=new ArrayList<>();
        while(true){
            try {
                clients.add(serverSocket.accept());
                new Thread(new TaskHandler(clients.getLast(), chunkDistributor)).start();
                System.out.println("New client connected: "+clients.getLast().getInetAddress().getHostAddress()+":"+clients.getLast().getPort());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
