import java.io.*;
import java.net.Socket;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String serverIP = "localhost";
        int serverPort = 8080;
        int maxRetries = 3; 
        int retryDelay = 2000; 
        
        Socket socket = null;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                socket = new Socket(serverIP, serverPort);
                System.out.println("Connected to server at " + serverIP + ":" + serverPort);
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

        List<Thread> threads = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        System.out.println("Please read this information properly to get familiar with the functionality of the application: ");
        System.out.println("1. To get the file: GET <file-name>");
        System.out.println("2. To upload the file: UPLOAD <path-of-the-file>");
        System.out.println("3. Type 'exit' or 'quit' to close the application");

        while (true) {
            System.out.print("> ");
            String cmd = sc.nextLine();
            if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")) break;
            
            // Validate command format
            if (!isValidCommand(cmd)) {
                System.err.println("Invalid command format. Please use 'GET <file-name>' or 'UPLOAD <path-of-the-file>'");
                continue;
            }
            
            Thread taskThread = new Thread(new TaskHandler(socket, cmd));
            threads.add(taskThread);
            taskThread.start();
        }

        System.out.println("Your data is being processed! Wait, do not close the application abruptly or the data may get lost....");

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                System.err.println("Error: Thread interrupted while waiting.");
                System.err.println("Details: " + e.getMessage());
            }
        }

        // Send stop command to server
        try {
            new TaskHandler(socket, "STOP").run();
        } catch (Exception e) {
            System.err.println("Error sending stop command to server: " + e.getMessage());
        }

        // Close socket and scanner
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Connection closed.");
            }
            sc.close();
        } catch (IOException e) {
            System.err.println("Error while closing connection: " + e.getMessage());
        }
    }
    
    private static boolean isValidCommand(String cmd) {
        String[] parts = cmd.split(" ", 2);
        if (parts.length != 2) {
            return false;
        }
        
        String command = parts[0].toUpperCase();
        return command.equals("GET") || command.equals("UPLOAD");
    }
}