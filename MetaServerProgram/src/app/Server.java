package app;

import config.DatabaseConnect;
import distribution.ConsistentHashing;
import service.MetaService;
import taskhandle.TaskHandler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Scope of improvement:
 * When to quit the connection with the client
 */
public class Server {
    static int port = 8080;
    static String loadBalancerIP;

    public static void registerOrRelease(String req) {

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
        } catch(ConnectException ce){
            System.out.println("Could not connect to load-balancer! Load-balancer might be down.");
            System.exit(-1);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Scanner sc=new Scanner(System.in);
        System.out.print("Enter IP-address of Load-Balancer: ");
        String loadBalancerIP = sc.next();

        registerOrRelease("REGISTER");

        List<String> storageNodes = new ArrayList<>();
        System.out.print("Enter the number of Storage-Nodes possessed: ");
        int nodeCnt= sc.nextInt();

        for(int j=0;j<nodeCnt;j++){
            System.out.print("Enter the IP-address of Storage Node-"+(j+1)+": ");
            storageNodes.add(sc.next()+":8090");
        }
        sc.close();

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
