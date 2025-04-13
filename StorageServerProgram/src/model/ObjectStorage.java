package model;

import java.io.InputStream;
import java.sql.Blob;

public class ObjectStorage {
    private String name;
    private int version;
    private byte[] objChunk;

    public ObjectStorage(String name, int version, byte[] objChunk) {
        this.name = name;
        this.version=version;
        this.objChunk = objChunk;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getObjChunk() {
        return objChunk;
    }
}
