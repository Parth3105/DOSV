import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UploadHandler implements Handler {
    private final String os;
    private final DataInputStream dataInputStream;
    private final Socket socket;

    UploadHandler(Socket socket, DataInputStream dataInputStream) {
        this.os = System.getProperty("os.name").toUpperCase();
        this.dataInputStream = dataInputStream;
        this.socket=socket;
    }

    /**
     * There are two paths to this function either write the file first and delete after transferring.
     * or directly transfer chunk coming from the client to the storage node.
     * Most suitable is first because we want to divide chunks too among storage nodes.
     *
     * @param fileName
     */
    @Override
    public void receiveRequest(String fileName) {
        try {
            String path = null;
            if (os.contains("WIN")) {
                path = "..\\testFolder\\" + fileName;

                // Create directory if it doesn't exist
                File dir = new File("..\\testFolder");
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IOException("Failed to create download directory");
                }
            } else {
                // String homeDir = System.getProperty("user.home");
                // downloadPath = homeDir + "/Downloads/" + fileName;

                // // Create directory if it doesn't exist
                // File downloadDir = new File(homeDir + "/Downloads");
                // if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                //     throw new IOException("Failed to create download directory");
                // }
            }

            // Retrieve file.
            ObjectInputStream clientObj=new ObjectInputStream(socket.getInputStream());
            List<byte[]> chunks= (List<byte[]>) clientObj.readObject();
            System.out.println("File Received Successfully!!!!"); //debug
            SendToSQLiteDB(chunks, fileName); // send to DB.

        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
    public void SendToSQLiteDB(List<byte[]> chunks, String fileName) {
        String serverIP = "192.168.110.94";
        int serverPort = 8090;
        int maxRetries = 3; 
        int retryDelay = 2000; 
        
        Socket socket = null;
        int attempt = 0;

        DataOutputStream dataOutputStream=null;
        while (attempt < maxRetries) {
            try {
                socket = new Socket(serverIP, serverPort);
                System.out.println("Connected to Database at " + serverIP + ":" + serverPort);
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
                    System.err.println("Database Connection interrupted. Exiting.");
                    System.exit(1);
                }
            } else {
                System.err.println("Could not connect to Database! Database might be down.");
                System.exit(1);
            }
        }
         try {
            // Send write-request and filename
            dataOutputStream.writeUTF("STORE");
            dataOutputStream.flush();
            //Saving of object in a file
            String fileType = fileName.substring(fileName.lastIndexOf('.') + 1); // Extract file type
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            List<String> chunk_names = new ArrayList();
            for (int i = 0; i < chunks.size(); i++) {
                chunk_names.add(fileName+"_chunk_"+ i+"." + fileType); // create chunk names.
            }
            System.out.println("Sending chunk names to DB"); //debug
            out.writeObject(chunk_names); // write the names of the chunks to the file.
            out.flush();
            // Method for serialization of object
            System.out.println("Sending chunks to DB"); //debug
            out.writeObject(chunks);
            out.flush();
            System.out.println("Chunks sent to DB"); //debug
            out.close();
            
        } catch (IOException e) {
            System.err.println("Error while sending write request or filename: " + e.getMessage());
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("DataOutputStream or fileName is null: " + e.getMessage());
            e.printStackTrace();
        }
        
    }
    @Override
    public void sendResponse() {

    }

}
