package taskhandle;

import distribution.DownloadDistribution;
import service.MetaService;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO:
 * if we don't get the chunks from requested server i.e. requestSQLiteDB returns null then try requesting
 * another server where these chunks are replicated
 */
public class DownloadHandler implements Handler {

    private final Socket socket;
    private final DataOutputStream dataOutputStream;
    private final DataInputStream dataInputStream;
    private final MetaService metaService;

    public DownloadHandler(Socket socket, DataOutputStream dataOutputStream, DataInputStream dataInputStream, MetaService metaService) {
        this.socket = socket;
        this.dataOutputStream = dataOutputStream;
        this.dataInputStream = dataInputStream;
        this.metaService = metaService;
    }

    @Override
    public void receive(String fileName, int version) {
        if (version == -1) {
            version = metaService.fetchFileVersionMeta(fileName, metaService.getConn());
            version = Math.max(version, 0);
        }


        Map<String, List<String>> chunkData = metaService.fetchAllChunkData(fileName, version);
        DownloadDistribution downloadDistribution = new DownloadDistribution();
        Map<String, List<String>> distribution = downloadDistribution.handleDistribution(chunkData);

        /*

        /// debug
        System.out.println("{");
        for (Map.Entry<String, List<String>> entry : distribution.entrySet()) {
            System.out.print("\t" + entry.getKey() + ": [ ");
            for (String chunkName : entry.getValue()) {
                System.out.print(chunkName + " ");
            }
            System.out.println("]");
        }
        System.out.println("}");
        ///
    */

        List<byte[]> chunksToSend = new ArrayList<>();
        List<String> chunkNamesToSend = new ArrayList<>();
        Lock lock = new ReentrantLock();
        List<Thread> threads = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : distribution.entrySet()) {
            String serverAddress = entry.getKey();
            String serverIP = serverAddress.split(":", 2)[0];
            int serverPort = Integer.parseInt(serverAddress.split(":", 2)[1]);

            int finalVersion = version;
            Thread thread=new Thread(new Runnable() {
                @Override
                public void run() {
                    lock.lock();
//                    System.out.println("Adding chunk names"); // debug
                    chunkNamesToSend.addAll(entry.getValue());
//                    System.out.println("Adding chunks"); // debug
                    chunksToSend.addAll(sendToSQLiteDB(serverIP, serverPort, entry.getValue(), finalVersion));
//                    System.out.println("Added chunks"); // debug
                    lock.unlock();
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
                e.printStackTrace();
            }
        }
//        System.out.println("All threads completed"); // debug
//        System.out.println(chunkNamesToSend.size() +':'+ chunksToSend.size()); // debug
        send(chunkNamesToSend, chunksToSend);
    }

    public List<byte[]> sendToSQLiteDB(String serverIP, int serverPort, List<String> chunkNames, int version) {
        int maxRetries = 3;
        int retryDelay = 2000;

        Socket socket = null;
        int attempt = 0;

        DataOutputStream dataOutputStream = null;
        while (attempt < maxRetries) {
            try {
                socket = new Socket(serverIP, serverPort);
                System.out.println("Connected to Database at " + serverIP + ":" + serverPort);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
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
                    return null;
                }
            } else {
                System.err.println("Could not connect to Database! Database might be down.");
                return null;
            }
        }
        try {
            // Send write-request and filename
            dataOutputStream.writeUTF("FETCH:" + version);
            dataOutputStream.flush();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

//            System.out.println("Sending chunk names to DB"); //debug
            out.writeObject(chunkNames); // write the names of the chunks to the file.
            out.flush();
//            System.out.println("chunk name sent");// debug
            
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
//            System.out.println("Receiving chunks from DB"); //debug
            List<byte[]> chunks = (List<byte[]>) in.readObject();
            System.out.println("Chunks received from Storage Node"); //debug
            return chunks;

        } catch (IOException e) {
            System.err.println("Error while sending write request or filename: " + e.getMessage());
            e.printStackTrace();
            return null; // Failure
        } catch (NullPointerException e) {
            System.err.println("DataOutputStream or fileName is null: " + e.getMessage());
            e.printStackTrace();
            return null; // Failure
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void send(List<String> chunkNames, List<byte[]> chunks) {
        try {
            /// debug
            if (chunkNames.size() == chunks.size()) System.out.println("Sending Chunks to client...");
            ///

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(chunkNames);
            out.flush();
            out.writeObject(chunks);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
