import java.io.*;
import java.net.Socket;

/**
 * Scope of Improvement:
 * Introduction to interrupt signals...(urgent messages)
 */

public class TaskHandler implements Runnable{
    private final Socket socket;
    private BufferedReader requestReader;
    private BufferedInputStream reader;
    private DataInputStream dataInputStream;

    TaskHandler(Socket socket){
        this.socket=socket;
        try {
            reader=new BufferedInputStream(socket.getInputStream());
            dataInputStream=new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        ClientRequest request=receiveRequest();
        if(request.getRequestType()==null || request.getFileName()==null){
            // handle error
            return;
        }
        Handler handler=null;
        if(request.getRequestType().equalsIgnoreCase(ClientRequest.UPLOAD)) {
            System.out.println("File is being transferred"); //debug.
            handler = new UploadHandler(dataInputStream,reader);
        }
        else ;

        while(socket.isConnected()) handler.receiveRequest(request.getFileName());
        // after handling all tasks.
        try {
            requestReader.close();
            dataInputStream.close();
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ClientRequest receiveRequest(){
        String requestType=null,fileName=null;
        try {
            requestReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestType=requestReader.readLine();
            fileName=requestReader.readLine();
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
        return new ClientRequest(requestType,fileName);
    }
}
