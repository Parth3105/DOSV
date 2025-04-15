package app;

import config.DatabaseConnect;
import distribution.ConsistentHashing;
import service.MetaService;
import taskhandle.TaskHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Scope of improvement:
 * When to quit the connection with the client
 */
public class Server {
    static int port = 8080;

    public static void registerOrRelease(String req) {
        String loadBalancerIP = "192.168.17.94";
        int loadBalancerPort = 8110;

        try {
            Socket socket = new Socket(loadBalancerIP, loadBalancerPort);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(req);
            dataOutputStream.flush();
            dataOutputStream.writeInt(port);
            dataOutputStream.flush();
            dataOutputStream.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        registerOrRelease("REGISTER");

        List<String> storageNodes = new ArrayList<>();
        storageNodes.add("192.168.17,94:8090");
        storageNodes.add("192.168.17.182:8090");
//        storageNodes.add("192.168.17.51:8090");
        ConsistentHashing chunkDistributor = new ConsistentHashing(storageNodes);

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port, 20);
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }

        System.out.println("Server is running on port: " + port);
        Connection conn = DatabaseConnect.ConnectToDatabase();
        if (conn == null) {
            System.err.println("Failed to connect to the database.");
            return;
        }
        MetaService metaService = new MetaService(conn);
        List<Socket> clients = new ArrayList<>();
        while (true) {
            try {
                Thread.sleep(500);
                Socket socket = serverSocket.accept();
                clients.add(socket);
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                new Thread(new TaskHandler(socket, chunkDistributor, metaService, clients)).start();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
