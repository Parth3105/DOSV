package taskhandle;

import service.Service;

import java.io.*;
import java.net.Socket;
import java.util.List;

/**
 * Scope of Improvement:
 * Introduction to interrupt signals...(urgent messages)
 */

public class TaskHandler implements Runnable {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final Service service;

    public TaskHandler(Socket socket, Service service) {
        this.socket = socket;
        try {
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.service = service;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        String request = null;
        try {
            request = dataInputStream.readUTF();
            String requestType = request.split(":", 2)[0];
            int version = Integer.parseInt(request.split(":", 2)[1]);
            if (requestType.equalsIgnoreCase("STORE")) storeObjChunk(version);
            else if (requestType.equalsIgnoreCase("FETCH")) sendObjChunk(version);
            else ;
            dataInputStream.close();
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
    }

    public void storeObjChunk(int version) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            List<String> chunkNames = (List<String>) objectInputStream.readObject();
            List<byte[]> chunks = (List<byte[]>) objectInputStream.readObject();
            System.out.println("File received successfully!");

            service.addObjects(chunkNames, chunks, version);

            /// test purpose
//            service.readObjects(chunkNames); //debug
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendObjChunk(int version) {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            List<String> chunkNames = (List<String>) in.readObject();

            List<byte[]> chunks = service.fetchChunksInList(chunkNames, version);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(chunks);
            out.flush();

            out.close();
            in.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
