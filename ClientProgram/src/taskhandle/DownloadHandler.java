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
            dataOutputStream.writeUTF("DOWNLOAD");
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
            } else {
                // String homeDir = System.getProperty("user.home");
                // downloadPath = homeDir + "/Downloads/" + fileName;

                // // Create directory if it doesn't exist
                // File downloadDir = new File(homeDir + "/Downloads");
                // if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                //     throw new IOException("Failed to create download directory");
                // }
            }

            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            List<String> chunkNames = (List<String>) in.readObject();
            List<byte[]> chunks = (List<byte[]>) in.readObject();
            in.close();

            Map<String, byte[]> nameChunkMap = new TreeMap<>();
            for (int j = 0; j < chunkNames.size(); j++) {
                nameChunkMap.put(chunkNames.get(j), chunks.get(j));
            }

            FileOutputStream writer = new FileOutputStream(downloadPath, true);
            for (byte[] chunk : nameChunkMap.values()) {
                writer.write(chunk);
                writer.flush();
            }
            writer.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}