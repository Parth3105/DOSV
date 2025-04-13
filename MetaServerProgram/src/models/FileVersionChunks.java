package models;

public class FileVersionChunks {
    private String filename;
    private int version;
    private String node;
    private String[] chunks;

    public FileVersionChunks() {}

    public FileVersionChunks(String filename, int version, String node, String[] chunks) {
        this.filename = filename;
        this.version = version;
        this.node = node;
        this.chunks = chunks;
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

    public String getNode() {
        return node;
    }

    public void setNodeNo(String node) {
        this.node = node;
    }

    public String[] getChunks() {
        return chunks;
    }

    public void setChunks(String[] chunks) {
        this.chunks = chunks;
    }

    @Override
    public String toString() {
        return "FileVersionChunks{" +
                "filename='" + filename + '\'' +
                ", version=" + version +
                ", nodeNo=" + node +
                ", chunks=" + String.join(", ", chunks) +
                '}';
    }
}
