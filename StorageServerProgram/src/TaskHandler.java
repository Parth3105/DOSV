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

    TaskHandler(Socket socket, Service service) {
        this.socket = socket;
        try {
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.service=service;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        String requestType = null;
        try {
            requestType = dataInputStream.readUTF();
            if(requestType.equalsIgnoreCase("STORE")) storeFileChunk();
            dataInputStream.close();
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
    }

    public void storeFileChunk(){
        try {
            ObjectInputStream objectInputStream=new ObjectInputStream(socket.getInputStream());
            List<String> chunkNames= (List<String>) objectInputStream.readObject();
            List<byte[]> chunks= (List<byte[]>) objectInputStream.readObject();
            System.out.println("File transfer successful...");

            service.addObjects(chunkNames,chunks);

            /// test purpose
            service.readObjects(chunkNames);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
