import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * TODO:
 * This class as name suggests handles the file upload request of the client.
 * Scope of improvement: what to do on receiving acknowledgement, it may take a while to get acknowledgement
 * of task then how to handle the other tasks during that time...
 */

class UploadHandler implements RequestHandler {
    private final Socket socket;
    private String os;
    private final int BUFFER_SIZE = 128 * 1024 * 1024;

    UploadHandler(Socket socket) {
        this.os = System.getProperty("os.name").toUpperCase();
        this.socket = socket;
    }

    @Override
    public synchronized void sendRequest(String path) {
        PrintWriter printWriter = null;
        DataOutputStream dataOutputStream = null;
        BufferedOutputStream transfer = null;
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
            printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println("WRITE");
            printWriter.flush();
            printWriter.println(fileName);
            printWriter.flush();

            // Send file size
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeLong(fileSize);
            dataOutputStream.flush();

            // Send the file in byte-stream
            fileReader = new FileInputStream(file);
            byte[] chunk = new byte[BUFFER_SIZE];
            transfer = new BufferedOutputStream(socket.getOutputStream());
            int bytesRead;
            long totalBytesSent = 0;
            int progressMarker = 0;

            // Write/upload the file to the socket
            while ((bytesRead = fileReader.read(chunk)) != -1) {
                transfer.write(chunk, 0, bytesRead);
                transfer.flush();
                totalBytesSent += bytesRead;
                
                // Show progress
                int progress = (int)((totalBytesSent * 100) / fileSize);
                if (progress >= progressMarker + 10) {
                    progressMarker = progress;
                    System.out.println("Upload progress: " + progress + "%");
                }
            }

            System.out.println("Upload completed: " + fileName + " (" + totalBytesSent + "/" + fileSize + " bytes)");

        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found or cannot be accessed.");
        } catch (SocketException se) {
            System.err.println("Error: Connection to server lost during upload.");
        } catch (IOException e) {
            System.err.println("Error: Failed to upload file.");
        } finally {
            try {
                // Close resources in reverse order of creation
                if (fileReader != null) fileReader.close();
                if (transfer != null) transfer.close();
                if (dataOutputStream != null) dataOutputStream.close();
                if (printWriter != null) printWriter.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    @Override
    public synchronized void receiveResponse() {}
}