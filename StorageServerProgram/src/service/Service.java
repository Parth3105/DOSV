package service;

import config.DatabaseConnection;
import dao.ObjectDAO;
import model.ObjectStorage;

import java.io.*;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Service {
    private final ObjectDAO dao = new ObjectDAO();
    private final Connection conn;

    public Service(String url) {
        DatabaseConnection dbHandle = new DatabaseConnection();
        this.conn = dbHandle.getConnection(url);
    }

    Service(String url, String username, String password) {
        DatabaseConnection dbHandle = new DatabaseConnection();
        this.conn = dbHandle.getConnection(url, username, password);
    }

    public void addObject(String fileName, byte[] obj, int version) {
        DatabaseConnection dbConfig = new DatabaseConnection();

        dao.addObject(conn, new ObjectStorage(fileName, version, obj));
    }

    public void addObjects(List<String> fileName, List<byte[]> obj, int version) {
        for (int j = 0; j < fileName.size(); j++) addObject(fileName.get(j), obj.get(j), version);
        System.out.println("File stored successfully!");
    }

    public void readObject(String fileName) {
        ObjectStorage obj = dao.fetchObjectByName(conn, fileName);
        String path;
        File dir;
        if (System.getProperty("os.name").toUpperCase().contains("WIN")) {
            path = ".\\testFolder\\" + fileName;
            dir = new File(".\\testFolder");
        } else {
            path = "./testFolder/" + fileName;
            dir = new File("./testFolder");
        }

        if (!dir.exists() && !dir.mkdirs()) System.err.println("Failed to create directory");
        try {
            System.out.println("Reading....");
            FileOutputStream writer = new FileOutputStream(path);
            byte[] buffer = obj.getObjChunk();
            writer.write(buffer);
            writer.flush();
            writer.close();
            System.out.println("Read...");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<byte[]> fetchChunksInList(List<String> chunkNames, int version) {
        List<byte[]> chunks = new ArrayList<>();

        for (String chunkName : chunkNames) {
            ObjectStorage obj = dao.fetchObjectByNameVersion(conn, chunkName, version);
            chunks.add(obj.getObjChunk());
        }

        return chunks;
    }

    /// only for test purpose
    public void readObjects(List<String> chunkNames) {
        if (chunkNames.isEmpty()) return;
        String fileName = chunkNames.getFirst().split("_chunk", 2)[0];
        String path;
        File dir;
        if (System.getProperty("os.name").toUpperCase().contains("WIN")) {
            path = "..\\testFolder\\" + fileName;
            dir = new File("..\\testFolder");
        } else {
            path = "../testFolder/" + fileName;
            dir = new File("../testFolder");
        }

        if (!dir.exists() && !dir.mkdirs()) System.err.println("Failed to create directory");
        try {
            System.out.println("Reading....");
            FileOutputStream writer = new FileOutputStream(path, true);
            for (int j = 0; j < chunkNames.size(); j++) {
                ObjectStorage obj = dao.fetchObjectByName(conn, chunkNames.get(j));
                byte[] buffer = obj.getObjChunk();
                writer.write(buffer);
                writer.flush();
            }
            writer.close();
            System.out.println("Read...");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
