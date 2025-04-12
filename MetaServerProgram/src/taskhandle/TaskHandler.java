package taskhandle;

import distribution.ConsistentHashing;
import request.ClientRequest;

import java.io.*;
import java.net.Socket;

/**
 * Scope of Improvement:
 * Introduction to interrupt signals...(urgent messages)
 */

public class TaskHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final ConsistentHashing chunkDistributor;

    public TaskHandler(Socket socket, ConsistentHashing chunkDistributor) {
        this.socket = socket;
        this.chunkDistributor=chunkDistributor;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            ClientRequest request = receiveRequest();
            if (request.getRequestType() == null || request.getFileName() == null) {
                // handle error
                return;
            }
            Handler handler = null;
            if (request.getRequestType().equalsIgnoreCase(ClientRequest.UPLOAD)) {
                System.out.println("File is being transferred"); //debug.
                handler = new UploadHandler(socket, dataInputStream, chunkDistributor);
            } else ;

            handler.receiveRequest(request.getFileName());
        }
        // after handling all tasks.
        try {
            dataInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClientRequest receiveRequest() {
        String requestType = null, fileName = null;
        try {
            requestType = dataInputStream.readUTF();
            fileName = dataInputStream.readUTF();
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
        return new ClientRequest(requestType, fileName);
    }
}
