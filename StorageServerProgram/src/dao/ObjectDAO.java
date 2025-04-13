package dao;

import model.ObjectStorage;

import java.sql.*;

public class ObjectDAO {

    public void addObject(Connection conn, ObjectStorage obj) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet result = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            boolean tableExists = false;
            while (result.next()) {
                if (result.getString("TABLE_NAME").equalsIgnoreCase("ObjectStorage")) tableExists = true;
            }

            if (!tableExists) createTable(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            String INSERT_QUERY = "INSERT INTO ObjectStorage(name,version,objChunk) VALUES (?,?,?)";
            PreparedStatement ps = conn.prepareStatement(INSERT_QUERY);
            ps.setString(1, obj.getName());
            ps.setInt(2, obj.getVersion());
            ps.setBytes(3, obj.getObjChunk());

            ps.executeUpdate();
        } catch (SQLException e) {
            // handle error
            throw new RuntimeException(e);
        }
    }

    public ObjectStorage fetchObjectByName(Connection conn, String objName) {
        ObjectStorage obj = null;
        PreparedStatement ps = null;
        try {
            String FETCH_QUERY = "SELECT name, version, objChunk FROM ObjectStorage WHERE name=?";
            ps = conn.prepareStatement(FETCH_QUERY);
            ps.setString(1, objName);

            ResultSet result = ps.executeQuery();
            while (result.next()) {
                obj = new ObjectStorage(result.getString("name"), result.getInt("version"), result.getBytes("objChunk"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return obj;
    }

    public ObjectStorage fetchObjectByNameVersion(Connection conn, String objName, int objVersion) {
        ObjectStorage obj = null;
        PreparedStatement ps = null;
        try {
            String FETCH_QUERY = "SELECT name, version, objChunk FROM ObjectStorage WHERE name=? and version=?";
            ps = conn.prepareStatement(FETCH_QUERY);
            ps.setString(1, objName);
            ps.setInt(2, objVersion);

            ResultSet result = ps.executeQuery();
            while (result.next()) {
                obj = new ObjectStorage(result.getString("name"), result.getInt("version"), result.getBytes("objChunk"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return obj;
    }

    private void createTable(Connection conn) {
        try {
            Statement statement = conn.createStatement();
            String CREATE_TABLE = """
                    CREATE TABLE ObjectStorage(
                        name VARCHAR(256),
                        version INTEGER,
                        objChunk Blob,
                        uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY(name,version)
                    )
                    """;
            statement.executeUpdate(CREATE_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
