import config.DatabaseConnect;
import java.io.DataOutputStream;
import distribution.ConsistentHashing;
import service.MetaService;
import taskhandle.TaskHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement;

/**
 * Scope of improvement:
 * When to quit the connection with the client
 */
public class Server {
    public static void main(String[] args) {
        int ServerPort=8080;

        List<String> storageNodes=new ArrayList<>();
        storageNodes.add("127.0.0.1:8090");
        storageNodes.add("127.0.0.1:8090");
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
        Connection conn = DatabaseConnect.ConnectToDatabase();
        if (conn == null) {
            System.err.println("Failed to connect to the database.");
            return;
        }
        MetaService metaService = new MetaService(conn);
        List<Socket> clients=new ArrayList<>();
        while(true){
            try {
                clients.add(serverSocket.accept());
                new Thread(new TaskHandler(clients.getLast(), chunkDistributor,metaService)).start();
                System.out.println("New client connected: "+clients.getLast().getInetAddress().getHostAddress()+":"+clients.getLast().getPort());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
