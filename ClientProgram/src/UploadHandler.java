import java.io.*;
import java.net.Socket;

/**
 * This class as name suggests handles the file upload request of the client.
 * Scope of improvement: what to do on receiving acknowledgement, it may take a while to get acknowledgement
 * of task then how to handle the other tasks during that time...
 */

class UploadHandler implements RequestHandler{
    private final Socket socket;
    String os;
    UploadHandler(Socket socket){
        this.os=System.getProperty("os.name").toUpperCase();
        this.socket=socket;
    }

    @Override
    public synchronized void sendRequest(String path) {
        String[] pathSplit;
        if(os.contains("WIN")){
            pathSplit=path.split("\\\\");
        }
        else pathSplit=path.split("/");

        String fileName=pathSplit[pathSplit.length-1];
        try {
            File file=new File(path);

            // send write-request and filename.
            PrintWriter printWriter=new PrintWriter(socket.getOutputStream());
            printWriter.println("WRITE");
            printWriter.flush();
            printWriter.println(fileName);
            printWriter.flush();

            // send file size.
            DataOutputStream dataOutputStream=new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeLong(file.length());
            dataOutputStream.flush();

            // send the file in byte-stream.
            FileInputStream fileReader =new FileInputStream(file);
            byte[] chunk=new byte[4*1024];
            BufferedOutputStream transfer=new BufferedOutputStream(socket.getOutputStream());
            int bytesRead;

            // Write/upload the file to the socket.
            while((bytesRead= fileReader.read(chunk))!=-1){
                transfer.write(chunk,0,bytesRead);
                transfer.flush();
            }

            printWriter.close();
            dataOutputStream.close();
            transfer.close();
            fileReader.close();

            // Display acknowledgement to client.
            System.out.println("Uploaded "+fileName+" successfully");

        } catch (FileNotFoundException e) {
            // handle error
            throw new RuntimeException(e);
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receiveResponse() {
        // handle scenario according to server's response;
    }
}
