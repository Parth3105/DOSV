import java.io.File;
import java.net.Socket;

/**
 * Scope of improvement:
 * Either remove threads or keep every function synchronized.
 * If we remove threads, there might be need for task queue....
 * Introduction of error handling in catch blocks and in if statement if needed.
 * Introduction of TCP handoff is still left...
 */

class TaskHandler implements Runnable{
    private final Socket socket;
    String request;
    TaskHandler(Socket socket, String request){
        this.request=request;
        this.socket=socket;
    }
    public void run(){
        String requestType=request.split(" ")[0];
        String path=request.split(" ",2)[1];

        // Check validity of the file
        File file=new File(path);
        if(!file.isFile()){
            // handle error

            try {
                throw new Exception(path+" is NOT a FILE!!");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        RequestHandler task=null;
        if(requestType.equalsIgnoreCase("Upload")) task=new UploadHandler(socket);
        else if (requestType.equalsIgnoreCase("Get")) task=new DownloadHandler(socket);
        if(task==null){
            // handle error
            return;
        }
        task.sendRequest(path);
        task.receiveResponse();
    }
}