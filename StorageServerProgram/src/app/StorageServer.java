package app;
import service.Service;
import taskhandle.TaskHandler;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Scope of improvement:
 * When to quit the connection with the client
 */
public class StorageServer {
    public static void main(String[] args) {
        int port = 8090;
//        int port = Integer.parseInt(args[0]);

        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port, 20);
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
        System.out.println("Server is running on port: " + port);

        String path;
        File dir;
        String DBName = "Objects.db";
//        String DBName = "Objects_"+port+".db";

        if (System.getProperty("os.name").toUpperCase().contains("WIN")) {
            path = ".\\DBFolder\\" + DBName;
            dir = new File(".\\DBFolder");
        } else {
            path = "./DBFolder/" + DBName;
            dir = new File("./DBFolder");
        }

        if (!dir.exists() && !dir.mkdirs()) System.err.println("Failed to create directory");

        Service service = new Service("jdbc:sqlite:" + path);

        List<Socket> clients = new ArrayList<>();
        while (true) {
            try {
                clients.add(serverSocket.accept());
                System.out.println("New Meta-server connected: " + clients.getLast().getInetAddress().getHostAddress() + ":" + clients.getLast().getPort());
                new Thread(new TaskHandler(clients.getLast(), service)).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
