package service;
import config.DatabaseConnect;
import dao.MetaDAO;
import models.FileVersionChunks;
import models.FileVersionMeta;
import java.sql.Connection;
import java.sql.SQLException;

public class MetaService {
    private MetaDAO metaDAO;
    private Connection conn;
    public MetaService(Connection connect) {
        this.metaDAO = new MetaDAO();
        this.conn = connect;
    }
    @Override
public String toString() {
    // Assuming that metaDAO has a method like getClassName() or getStatus(), and conn has useful details
    try {
        return "MetaService{" +
               "metaDAO=" + metaDAO.getClass().getSimpleName() + ", " + 
               "conn=" + (conn != null ? conn.getMetaData().getURL() : "No Connection") +
               "}";
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return "Error retrieving connection details";
}

    public boolean addFileVersionMeta(FileVersionMeta meta) {
        return metaDAO.addFileVersionMeta(meta, conn);
    }

    public FileVersionMeta fetchFileMeta(String filename, int version) {
        return metaDAO.fetchFileMeta(filename, version, conn);
    }
    public int fetchFileVersionMeta(String filename, Connection conn) {
        return metaDAO.fetchFileVersionMeta(filename, conn);
    }
    public boolean addFileVersionChunks(FileVersionChunks chunksObj, Connection conn) {
        return metaDAO.addFileVersionChunks(chunksObj, conn);
    }
    public boolean deleteFileVersionChunks(String filename, int version, Connection conn) {
        return metaDAO.deleteFileVersionChunks(filename, version, conn);
    }
    public boolean deleteFileVersionMeta(String filename, int version, Connection conn) {
        return metaDAO.deleteFileVersionMeta(filename, version, conn);
    }
    public Connection getConn() {
        return conn;
    }
    
}
