import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class as name suggests handles the file download request of the client.
 * Scope of improvement: what to do after sending request, it may take a while until the downloading of the
 * file then how to handle the other tasks during that time...
 */

class DownloadHandler implements RequestHandler {
    private String os;
    private String fileName;
    private final int BUFFER_SIZE = 128 * 1024 * 1024;
    private final PrintWriter printWriter;
    private final DataInputStream dataInputStream;
    private final BufferedInputStream reader;

    DownloadHandler(PrintWriter printWriter, DataInputStream dataInputStream, BufferedInputStream reader) {
        this.os = System.getProperty("os.name").toUpperCase();
        this.printWriter = printWriter;
        this.dataInputStream = dataInputStream;
        this.reader = reader;
    }

    @Override
    public synchronized void sendRequest(String fileName) {
        this.fileName = fileName;
        printWriter.println("READ");
        printWriter.flush();
        printWriter.println(fileName);
        printWriter.flush();
        System.out.println("Download request sent for: " + fileName);
    }

    @Override
    public void receiveResponse() {
        DataInputStream dataInputStream = null;
        BufferedInputStream reader = null;
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

            // Read the file size
            long fileSize = dataInputStream.readLong();
            System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");

            fileWriter = new FileOutputStream(downloadPath); // Write the file

            byte[] chunk = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytes = 0;
            int progressMarker = 0;

            // Read/Download the file from the socket
            while ((bytesRead = reader.read(chunk)) != -1) {
                fileWriter.write(chunk, 0, bytesRead);
                fileWriter.flush();
                totalBytes += bytesRead;

                // Show progress
                int progress = (int) ((totalBytes * 100) / fileSize);
                if (progress >= progressMarker + 10) {
                    progressMarker = progress;
                    System.out.println("Download progress: " + progress + "%");
                }

                if (totalBytes >= fileSize) {
                    break;
                }
            }

            // Verify file integrity
            if (totalBytes != fileSize) {
                System.err.println("Warning: File not received completely.");
            }

            // Display acknowledgement to client
            System.out.println("Downloaded " + fileName + " successfully to " + downloadPath);

        } catch (FileNotFoundException e) {
            System.err.println("Error: Could not create file for download.");
        } catch (SocketException se) {
            System.err.println("Error: Connection to server lost during download.");
        } catch (IOException e) {
            System.err.println("Error: Failed to download file.");
        } finally {
            try {
//                if (dataInputStream != null) dataInputStream.close();
                if (fileWriter != null) fileWriter.close();
//                if (reader != null) reader.close();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}