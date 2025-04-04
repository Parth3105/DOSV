import java.io.*;
import java.net.Socket;

/**
 * This class as name suggests handles the file download request of the client.
 * Scope of improvement: what to do after sending request, it may take a while until the downloading of the
 * file then how to handle the other tasks during that time...
 */

class DownloadHandler implements RequestHandler{
    private final Socket socket;
    String os;
    String fileName;
    DownloadHandler(Socket socket){
        this.socket=socket;
        this.os=System.getProperty("os.name").toUpperCase();
    }

    @Override
    public void sendRequest(String fileName){
        try {
            this.fileName=fileName;
            PrintWriter printWriter=new PrintWriter(socket.getOutputStream());
            printWriter.println("READ");
            printWriter.flush();
            printWriter.println(fileName);
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }
    }

    @Override
    public void receiveResponse(){
        try {
            String downloadPath;
            if(os.contains("WIN")) downloadPath="D:\\DOSV\\Downloads\\"+fileName;
            else downloadPath="";

            // read the file size.
            DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
            long fileSize=dataInputStream.readLong();


			BufferedInputStream reader=new BufferedInputStream(socket.getInputStream()); //read from socket.
            FileOutputStream fileWriter=new FileOutputStream(downloadPath); // write the file.
            byte[] chunk=new byte[4*1024];
            int bytesRead;
            int totalBytes=0;

            // Read/Download the file from the socket.
            while((bytesRead=reader.read(chunk))!=-1){
                fileWriter.write(chunk,0,bytesRead);
                fileWriter.flush();
                totalBytes+=bytesRead;
            }

            // If any chunk lost or any other problems while reading.
            if(totalBytes!=bytesRead){
                // handle error
                System.out.println("file not received properly");
            }

            dataInputStream.close();

            fileWriter.close();
            reader.close();

            // Display acknowledgement to client
            System.out.println("Downloaded "+fileName+" successfully");

        } catch (IOException e) {
            //handle error
            throw new RuntimeException(e);
        }
    }
}
