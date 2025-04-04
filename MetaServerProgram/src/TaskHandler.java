import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Scope of Improvement:
 * Introduction to interrupt signals...(urgent messages)
 */

public class TaskHandler implements Runnable{
    private final Socket socket;
    private BufferedReader requestReader;
    TaskHandler(Socket socket){
        this.socket=socket;
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
            handler = new UploadHandler(socket);
        }
        else ;

        handler.receiveRequest(request.getFileName());

        // after handling all tasks.
        try {
            requestReader.close();
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
