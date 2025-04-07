import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * TODO:
 * Scope of improvement:
 * Either remove threads or keep every function synchronized.
 * If we remove threads, there might be need for task queue....
 * Introduction of error handling in catch blocks and in if statement if needed.
 * Introduction of TCP handoff is still left...
 */

class TaskHandler implements Runnable {
    private final Socket socket;
    private String request;
    private final DataOutputStream dataOutputStream;

    TaskHandler(Socket socket, String request, DataOutputStream dataOutputStream) {
        this.request = request;
        this.socket = socket;
        this.dataOutputStream = dataOutputStream;
    }

    public void run() {
        try {
            String[] parts = request.split(" ", 2);
            if (parts.length != 2) {
                System.err.println("Error: Invalid command format. Use 'COMMAND PATH'");
                return;
            }

            String requestType = parts[0];
            if (requestType.equalsIgnoreCase("STOP")) {
                dataOutputStream.close();
                return;
            }

            String path = parts[1];

            // Check validity of the file if it's an upload request
            if (requestType.equalsIgnoreCase("Upload")) {
                validateUploadFile(path);
            }

            RequestHandler task = createRequestHandler(requestType);
            if (task == null) {
                System.err.println("Error: Unsupported request type '" + requestType + "'");
                System.err.println("Supported commands are: GET, UPLOAD");
                return;
            }

            task.sendRequest(path);
            task.receiveResponse();

        } catch (SocketException se) {
            System.err.println("Error: Connection to server lost.");
            se.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error executing task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateUploadFile(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            throw new Exception("Error: File not found: " + path);
        }
        if (!file.isFile()) {
            throw new Exception("Error: Path is not a file: " + path);
        }
        if (!file.canRead()) {
            throw new Exception("Error: Cannot read file (check permissions): " + path);
        }
        // Check file size
        long fileSize = file.length();
        if (fileSize <= 0) {
            throw new Exception("Error: File is empty: " + path);
        }
    }

    private RequestHandler createRequestHandler(String requestType) {
        if (requestType.equalsIgnoreCase("Upload")) {
            return new UploadHandler(socket, dataOutputStream);
        } else if (requestType.equalsIgnoreCase("Get")) {
//            return new DownloadHandler(socket);
        }
        return null;
    }
}