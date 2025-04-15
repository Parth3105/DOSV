package taskhandle;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class as name suggests handles the file download request of the client.
 * Scope of improvement: what to do after sending request, it may take a while until the downloading of the
 * file then how to handle the other tasks during that time...
 */

class DownloadHandler implements RequestHandler {
    private final String os;
    private final DataOutputStream dataOutputStream;
    private final Socket socket;
    private String fileName = null;


    public DownloadHandler(Socket socket, DataOutputStream dataOutputStream) {
        this.os = System.getProperty("os.name").toUpperCase();
        this.dataOutputStream = dataOutputStream;
        this.socket = socket;
    }

    @Override
    public synchronized void sendRequest(String fileName) {
        try {
            this.fileName = fileName;
            dataOutputStream.writeUTF("READ");
            dataOutputStream.flush();
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.flush();
            System.out.println("Download request sent for: " + fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receiveResponse() {
        FileOutputStream fileWriter = null;

        try {
            String downloadPath = null;
            if (os.contains("WIN")) {
                downloadPath = "D:\\DOSV\\Downloads\\" + fileName;

                // Create directory if it doesn't exist
                File downloadDir = new File("D:\\DOSV\\Downloads");
                if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                    throw new IOException("Failed to create download directory");
                }
                System.out.println("Directory Created....");
            } else {
                // String homeDir = System.getProperty("user.home");
                // downloadPath = homeDir + "/Downloads/" + fileName;

                // // Create directory if it doesn't exist
                // File downloadDir = new File(homeDir + "/Downloads");
                // if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                //     throw new IOException("Failed to create download directory");
                // }
            }

            System.out.println("Downloading file..."); //debug

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            List<String> chunkNames = (List<String>) in.readObject();
            List<byte[]> chunks = (List<byte[]>) in.readObject();

            System.out.println("received chunks successfully"); // debug

            Map<String, byte[]> nameChunkMap = new TreeMap<>();
            for (int j = 0; j < chunkNames.size(); j++) {
                nameChunkMap.put(chunkNames.get(j), chunks.get(j));
            }

            fileWriter=new FileOutputStream(downloadPath);
            for (byte[] chunk : nameChunkMap.values()) {
                fileWriter.write(chunk);
                fileWriter.flush();
            }
            fileWriter.close();

            System.out.println("File Downloaded successfully...");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}