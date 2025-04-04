package model;

import java.io.InputStream;
import java.sql.Blob;

public class ObjectStorage {
    private String name;
    private byte[] objChunk;

    public ObjectStorage(String name, byte[] objChunk) {
        this.name = name;
        this.objChunk = objChunk;
    }

    public String getName() {
        return name;
    }

    public byte[] getObjChunk() {
        return objChunk;
    }
}
