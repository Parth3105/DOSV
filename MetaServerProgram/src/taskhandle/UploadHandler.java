package taskhandle;

import distribution.ConsistentHashing;
import models.FileVersionChunks;
import models.FileVersionMeta;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import dao.MetaDAO;
import service.MetaService;
import java.sql.Timestamp;
public class UploadHandler implements Handler {
    private final String os;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final Socket socket;
    private final ConsistentHashing chunkDistributor;
    private final MetaService metaService;

    UploadHandler(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream, ConsistentHashing chunkDistributor, MetaService metaService) {
        this.metaService = metaService;
        this.os = System.getProperty("os.name").toUpperCase();
        this.dataInputStream = dataInputStream;
        this.dataOutputStream=dataOutputStream;
        this.socket=socket;
        this.chunkDistributor=chunkDistributor;
    }

    /**
     * There are two paths to this function either write the file first and delete after transferring.
     * or directly transfer chunk coming from the client to the storage node.
     * Most suitable is first because we want to divide chunks too among storage nodes.
     *
     * @param fileName
     */
    @Override
    public void receive(String fileName) {
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
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            int version = metaService.fetchFileVersionMeta(fileName, metaService.getConn());
            version = Math.max(version,0);
            version++;
            System.out.println("Version: "+version); //debug
            FileVersionMeta meta = new FileVersionMeta(
                fileName,
                version,
                timestamp
            );
            boolean result = metaService.addFileVersionMeta(meta);
            System.out.println(result ? "Inserted successfully" : "Insert failed");
            distributeChunks(chunks, fileName, version);
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public void distributeChunks(List<byte[]> chunks, String fileName, int version) {
        //Saving of object in a file
        String fileType = fileName.substring(fileName.lastIndexOf('.') + 1); // Extract file type
        List<String> chunkNames = new ArrayList();
        for (int i = 0; i < chunks.size(); i++) {
            chunkNames.add(fileName+"_chunk_"+ i+"." + fileType); // create chunk names.
        }

        Map<String, List<String>> chunkNameDistribution=new HashMap<>();
        Map<String, List<byte[]>> chunkDistribution=new HashMap<>();

        for(int j=0;j<chunkNames.size();j++){
            String chunkName=chunkNames.get(j);
            byte[] chunk=chunks.get(j);

            String[] nodes=chunkDistributor.getNodeForChunk(chunkName);
            for(String node: nodes){
                List<String> chunkNameList=chunkNameDistribution.getOrDefault(node, new ArrayList<>());
                List<byte[]> chunkList=chunkDistribution.getOrDefault(node, new ArrayList<>());

                chunkNameList.add(chunkName);
                chunkList.add(chunk);

                chunkNameDistribution.put(node,chunkNameList);
                chunkDistribution.put(node, chunkList);
            }
        }

        for(Map.Entry<String,List<String>> entry: chunkNameDistribution.entrySet()){
            String node = entry.getKey();
            List<String> chunksForNode = entry.getValue();

            /// debug
            /* System.out.print(node+": [ ");
            for(String name: entry.getValue()){
                System.out.print(name+", ");
            }
            System.out.println("]"); */

            FileVersionChunks fvc = new FileVersionChunks(
                fileName,
                version,
                node,
                chunksForNode.toArray(new String[0])
            );

            metaService.addFileVersionChunks(fvc, metaService.getConn());
        }

        ConcurrentHashMap<String, Integer> results = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(chunkNameDistribution.keySet().size());

        for (String node : chunkNameDistribution.keySet()) {
            String serverIP = node.split(":")[0];
            int serverPort = Integer.parseInt(node.split(":")[1]);

            new Thread(() -> {
                try {
                    int result = sendToSQLiteDB(serverIP, serverPort,
                            chunkDistribution.get(node), chunkNameDistribution.get(node), version);
                    results.put(node, result);
                } catch (Exception e) {
                    e.printStackTrace();
                    results.put(node, -1); // failure
                } finally {
                    latch.countDown(); // signal thread completion
                }
            }).start();
        }

        try {
            latch.await(); // wait for all threads to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean rollbackNeeded = results.values().contains(-1);

        if (rollbackNeeded) {
            System.out.println("Failure detected. Rolling back MetaServer DB inserts...");
            // You’ll need to track what you inserted and now delete them
            metaService.deleteFileVersionChunks(fileName, version,metaService.getConn());
            metaService.deleteFileVersionMeta(fileName, version, metaService.getConn());
            System.exit(1);
        } else {
            System.out.println("All chunks sent successfully.");
        }
}
    public int sendToSQLiteDB(String serverIP, int serverPort, List<byte[]> chunks, List<String> chunkNames, int version) {
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
                    return -1;
                }
            } else {
                System.err.println("Could not connect to Database! Database might be down.");
                return -1;
            }
        }
         try {
            // Send write-request and filename
            dataOutputStream.writeUTF("STORE:"+version);
            dataOutputStream.flush();
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            System.out.println("Sending chunk names to DB"); //debug
            out.writeObject(chunkNames); // write the names of the chunks to the file.
            out.flush();
            // Method for serialization of object
            System.out.println("Sending chunks to DB"); //debug
            out.writeObject(chunks);
            out.flush();
            System.out.println("Chunks sent to DB"); //debug
            out.close();
            return 1; // Success

        } catch (IOException e) {
            System.err.println("Error while sending write request or filename: " + e.getMessage());
            e.printStackTrace();
            return -1; // Failure
        } catch (NullPointerException e) {
            System.err.println("DataOutputStream or fileName is null: " + e.getMessage());
            e.printStackTrace();
            return -1; // Failure
        }
    }

    @Override
    public void send() {

    }

}