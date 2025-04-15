package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Array;

import models.FileVersionChunks;
import models.FileVersionMeta;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MetaDAO {
    public boolean addFileVersionMeta(FileVersionMeta meta, Connection conn) {
        try (Statement setPathStmt = conn.createStatement()) {
            // Set the search path
            setPathStmt.execute("SET search_path TO dataversioned");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql = "INSERT INTO FileVersionMeta (filename, version, valid_from) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, meta.getFilename());
            stmt.setInt(2, meta.getVersion());
            stmt.setTimestamp(3, meta.getValidFrom());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public FileVersionMeta fetchFileMeta(String filename, int version, Connection conn) {
        try (Statement setPathStmt = conn.createStatement()) {
            // Set the search path
            setPathStmt.execute("SET search_path TO dataversioned");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql = "SELECT * FROM FileVersionMeta WHERE filename = ? AND version = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filename);
            stmt.setInt(2, version);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp validFrom = rs.getTimestamp("valid_from");

                return new FileVersionMeta(filename, version, validFrom);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int fetchFileVersionMeta(String filename, Connection conn) {
        try (Statement setPathStmt = conn.createStatement()) {
            // Set the search path
            setPathStmt.execute("SET search_path TO dataversioned");

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        String sql = "SELECT max(version) FROM FileVersionMeta WHERE filename = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filename);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                
                return rs.getInt(1); // getInt(1) for the first column
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean addFileVersionChunks(FileVersionChunks chunksObj, Connection conn) {
        try (Statement setPathStmt = conn.createStatement()) {
            // Set the search path
            setPathStmt.execute("SET search_path TO dataversioned");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql = "INSERT INTO FileVersionChunks (filename, version, node, chunks) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chunksObj.getFilename());
            stmt.setInt(2, chunksObj.getVersion());
            stmt.setString(3, chunksObj.getNode());
            Array chunkArray = conn.createArrayOf("TEXT", chunksObj.getChunks());
            stmt.setArray(4, chunkArray);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean UpdateValidFrom(String filename, int version, Timestamp validFrom, Connection conn) {
        try (Statement setPathStmt = conn.createStatement()) {
            // Set the search path
            setPathStmt.execute("SET search_path TO dataversioned");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql = "UPDATE FileVersionMeta SET valid_from = ? WHERE filename = ? AND version = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, validFrom);
            stmt.setString(2, filename);
            stmt.setInt(3, version);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<FileVersionChunks> fetchAllChunkData(String filename, int version, Connection conn) {
        List<FileVersionChunks> chunkData = new ArrayList<>();
        String sql = "SELECT * FROM FileVersionChunks WHERE filename = ? AND version = ?";
        try {
            Statement setPathStmt = conn.createStatement();
            setPathStmt.execute("SET search_path TO dataversioned");
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, filename);
            stmt.setInt(2, version);

            System.out.println("SQL: " + stmt.toString()); // debug
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Array chunksArray = rs.getArray("chunks");
                String[] chunks = (String[]) chunksArray.getArray();
                String node = rs.getString("node");
                chunkData.add(new FileVersionChunks(filename, version, node, chunks));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chunkData;
    }

    public boolean deleteFileVersionChunks(String filename, int version, Connection conn) {
        try (Statement setPathStmt = conn.createStatement()) {
            // Set the search path
            setPathStmt.execute("SET search_path TO dataversioned");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql = "DELETE FROM FileVersionChunks WHERE filename = ? AND version = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filename);
            stmt.setInt(2, version);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFileVersionMeta(String filename, int version, Connection conn) {
        try (Statement setPathStmt = conn.createStatement()) {
            // Set the search path
            setPathStmt.execute("SET search_path TO dataversioned");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String sql = "DELETE FROM FileVersionMeta WHERE filename = ? AND version = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, filename);
            stmt.setInt(2, version);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }


    }
}
