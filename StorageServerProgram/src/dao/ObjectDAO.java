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
            String INSERT_QUERY = "INSERT INTO ObjectStorage(name,objChunk) VALUES (?,?)";
            PreparedStatement ps = conn.prepareStatement(INSERT_QUERY);
            ps.setString(1, obj.getName());
            ps.setBytes(2, obj.getObjChunk());

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
            String FETCH_QUERY = "SELECT name, objChunk FROM ObjectStorage WHERE name=?";
            ps = conn.prepareStatement(FETCH_QUERY);
            ps.setString(1, objName);

            ResultSet result = ps.executeQuery();
            while (result.next()) {
                obj = new ObjectStorage(result.getString("name"), result.getBytes("objChunk"));
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
                        objChunk Blob,
                        uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY(name)
                    )
                    """;
            statement.executeUpdate(CREATE_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
