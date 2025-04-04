import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String serverIP="localhost";
        int serverPort=8080;
        Socket socket=null;
        try {
            socket=new Socket(serverIP,serverPort);
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }

        List<Thread> threads=new ArrayList<>();
        Scanner sc=new Scanner(System.in);
        System.out.println("Please read this information properly to get familiar with the functionality of the application: ");
        System.out.println("1. To get the file: GET <file-name>");
        System.out.println("2. To upload the file: UPLOAD <path-of-the-file>");

        while(true){
            String cmd=sc.nextLine();
            if(cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase("exit")) break;
            threads.add(new Thread(new TaskHandler(socket,cmd)));
            threads.getLast().start();
        }

        System.out.println("Your data is being processed! Wait, do not close the application abruptly or the data may get lost....");

        for(Thread t: threads) {
            try {
                t.wait();
                new TaskHandler(socket,"STOP");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /*try {
            socket.close();
        } catch (IOException e) {
            // handle error
            throw new RuntimeException(e);
        }*/
    }
}
