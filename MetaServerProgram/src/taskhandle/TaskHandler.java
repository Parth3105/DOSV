package taskhandle;

import app.Server;
import distribution.ConsistentHashing;
import requests.ClientRequest;
import service.MetaService;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * TODO:
 * Scope of Improvement:
 * Introduction to interrupt signals...(urgent messages)
 */

public class TaskHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final ConsistentHashing chunkDistributor;
    private final MetaService metaService;
    private final List<Socket> clients;
  
    public TaskHandler(Socket socket, ConsistentHashing chunkDistributor, MetaService metaService, List<Socket> clients) {
        this.metaService = metaService;
        this.socket = socket;
        this.chunkDistributor = chunkDistributor;
        try {
            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
        this.clients = clients;
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            ClientRequest request = receiveRequest();
            if (request == null || request.getRequestType() == null || request.getFileName() == null) {
                // handle error
                try {
                    /// TODO: test this logic to close connection with idle client
                    socket.close();
                    clients.remove(socket);
                    System.out.println("Remaining Client Connections: " + clients.size());
                    Server.registerOrRelease("RELEASE");
                    ///
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Handler handler = null;
            if (request.getRequestType().equalsIgnoreCase(ClientRequest.UPLOAD)) {
                System.out.println("File is being transferred"); //debug.
                handler = new UploadHandler(socket, dataInputStream, dataOutputStream, chunkDistributor, metaService);
            } else if (request.getRequestType().equalsIgnoreCase(ClientRequest.DOWNLOAD)) {
                System.out.println("File is being received");//debug.
                handler = new DownloadHandler(socket, dataOutputStream, dataInputStream, metaService);
            } else ;
            handler.receive(request.getFileName(), request.getVersion());
            
        }
        // after handling all tasks.
        try {
            dataInputStream.close();
            dataOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClientRequest receiveRequest() {
        String requestType = null, fileName = null;
        try {
            /// TODO: test this logic to close connection with idle client
            int minute = 60 * 1000;
            socket.setSoTimeout(10* minute);
            requestType = dataInputStream.readUTF();
            if (requestType.equalsIgnoreCase("BYE")) {
                return null;
            }
            fileName = dataInputStream.readUTF();
//            System.out.println("Request Type: " + requestType); //debug
            System.out.println("File Name: " + fileName); //debug
//            socket.setSoTimeout(0);
            ///
        } catch (SocketTimeoutException e) {
            System.out.println("Closing connection with client: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
            return null;
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
        return new ClientRequest(requestType, fileName);
    }
}
