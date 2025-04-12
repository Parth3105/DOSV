package models;

import java.sql.Timestamp;

public class FileVersionMeta {
    private String filename;
    private int version;
    private Timestamp validFrom;
    private boolean isCurrent;

    public FileVersionMeta() {}

    public FileVersionMeta(String filename, int version, Timestamp validFrom, boolean isCurrent) {
        this.filename = filename;
        this.version = version;
        this.validFrom = validFrom;
        this.isCurrent = isCurrent;
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

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    @Override
    public String toString() {
        return "FileVersionMeta{" +
                "filename='" + filename + '\'' +
                ", version=" + version +
                ", validFrom=" + validFrom +
                ", isCurrent=" + isCurrent +
                '}';
    }
}
