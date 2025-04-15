package models;

import java.sql.Timestamp;

public class FileVersionMeta {
    private String filename;
    private int version;
    private Timestamp validFrom;

    public FileVersionMeta() {
    }

    public FileVersionMeta(String filename, int version, Timestamp validFrom) {
        this.filename = filename;
        this.version = version;
        this.validFrom = validFrom;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Timestamp getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Timestamp validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public String toString() {
        return "FileVersionMeta{" +
                "filename='" + filename + '\'' +
                ", version=" + version +
                ", validFrom=" + validFrom +
                '}';
    }
}
