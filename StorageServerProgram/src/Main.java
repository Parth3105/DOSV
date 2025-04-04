import service.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args){
        String path;
        File dir;
        String DBName ="Objects.db";
        if (System.getProperty("os.name").toUpperCase().contains("WIN")) {
            path = ".\\DBFolder\\" + DBName;
            dir = new File(".\\DBFolder");
        } else {
            path = "./DBFolder/" + DBName;
            dir = new File("./DBFolder");
        }

        if (!dir.exists() && !dir.mkdirs()) System.err.println("Failed to create directory");

        Service service=new Service("jdbc:sqlite:"+path);
        String fileName="IT559 LAB 8 Message Passing and Pub-Sub Model.pdf";
        String filePath="C:\\Users\\parth\\Downloads\\"+fileName;
        try {
            FileInputStream fileInputStream=new FileInputStream(filePath);
            byte[] buffer=fileInputStream.readAllBytes();

            service.addObject(fileName,buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        service.readObject(fileName);
    }
}
