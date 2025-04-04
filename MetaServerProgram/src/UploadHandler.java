import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UploadHandler implements Handler{
    private final Socket socket;
    private String os;

    UploadHandler(Socket socket){
        this.socket=socket;
        this.os=System.getProperty("os.name").toUpperCase();
    }


    /**
     * There are two paths to this function either write the file first and delete after transferring.
     * or directly transfer chunk coming from the client to the storage node.
     * Most suitable is first because we want to divide chunks too among storage nodes.
     * @param fileName
     */
    @Override
    public void receiveRequest(String fileName) {    
        try {
            String path = null;
            if (os.contains("WIN")) {
                path = "..\\testFolder\\" + fileName;
                
                // Create directory if it doesn't exist
                File dir = new File("..\\testFolder");
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IOException("Failed to create download directory");
                }
            } else {
                // String homeDir = System.getProperty("user.home");
                // downloadPath = homeDir + "/Downloads/" + fileName;
                
                // // Create directory if it doesn't exist
                // File downloadDir = new File(homeDir + "/Downloads");
                // if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                //     throw new IOException("Failed to create download directory");
                // }
            }

            // Retrieve file size for error detection.
            DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
            long fileSize=dataInputStream.readLong();

            // Retrieve file.
            BufferedInputStream reader=new BufferedInputStream(socket.getInputStream()); // read from socket
            FileOutputStream fileWriter=new FileOutputStream(path); // write into file.
            byte[] chunk=new byte[128*1024*1024];
            int bytesRead,totalBytes=0;
            while((bytesRead=reader.read(chunk))!=-1){
                fileWriter.write(chunk,0,bytesRead);
                fileWriter.flush();
                totalBytes+=bytesRead;
            }

            // If any chunk lost or any other problems while reading.
            if(totalBytes!=fileSize){
                // handle error
                System.out.println("File not received properly because totalBytes="+totalBytes+" and fileSize="+fileSize);
            }

            dataInputStream.close();
            fileWriter.close();
            reader.close();

            System.out.println("File Received Successfully!!!!"); //debug
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }

    }

    @Override
    public void sendResponse() {

    }

}
