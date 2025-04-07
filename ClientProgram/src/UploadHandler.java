import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO:
 * This class as name suggests handles the file upload request of the client.
 * Scope of improvement: what to do on receiving acknowledgement, it may take a while to get acknowledgement
 * of task then how to handle the other tasks during that time...
 */

class UploadHandler implements RequestHandler {
    private final String os;
    private final long BUFFER_SIZE = 128 * 1024 * 1024;
    private final DataOutputStream dataOutputStream;
    private final Socket socket;


    UploadHandler(Socket socket, DataOutputStream dataOutputStream) {
        this.os = System.getProperty("os.name").toUpperCase();
        this.dataOutputStream = dataOutputStream;
        this.socket=socket;
    }

    @Override
    public synchronized void sendRequest(String path) {
        FileInputStream fileReader = null;

        try {
            String[] pathSplit;
            pathSplit = ((os.contains("WIN")) ? path.split("\\\\") : path.split("/"));

            String fileName = pathSplit[pathSplit.length - 1];
            File file = new File(path);

            // Verify file exists and is readable before proceeding
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + path);
            }

            if (!file.canRead()) {
                throw new IOException("Cannot read file (check permissions): " + path);
            }

            long fileSize = file.length();
            System.out.println("Preparing to upload: " + fileName + " (" + fileSize + " bytes)");

            // Send write-request and filename
            dataOutputStream.writeUTF("WRITE");
            dataOutputStream.flush();
            dataOutputStream.writeUTF(fileName);
            dataOutputStream.flush();

            // Send the file in byte-stream
            fileReader = new FileInputStream(file);
            byte[] chunk = new byte[(int) Math.min(BUFFER_SIZE, fileSize)];
            int bytesRead;

            // Make list of the chunks to maintain the sequence
            List<byte[]> chunks = new ArrayList<>();
            while ((bytesRead = fileReader.read(chunk)) != -1) {
                chunks.add(Arrays.copyOf(chunk, chunk.length));
            }
            fileReader.close();

            ObjectOutputStream transfer=new ObjectOutputStream(socket.getOutputStream());
            transfer.writeObject(chunks);

            System.out.println("Upload completed: " + fileName + " (" + fileSize + " bytes)");
            fileReader.close();

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found or cannot be accessed.");
        } catch (SocketException se) {
            System.err.println("Error: Connection to server lost during upload.");
            se.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error: Failed to upload file.");
        }
    }

    @Override
    public synchronized void receiveResponse() {
    }
}