package requests;

public class ClientRequest {
    public static final String UPLOAD = "WRITE";
    public static final String DOWNLOAD = "READ";
    private String requestType;
    private String fileName;
    private int version;

    public ClientRequest(String requestType, String fileName) {
        this.requestType = requestType;
        this.fileName = fileName;
        this.version = -1;
    }

    public ClientRequest(String requestType, String fileName, int version) {
        this.requestType = requestType;
        this.fileName = fileName;
        this.version = version;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getFileName() {
        return fileName;
    }

    public int getVersion() {
        return version;
    }
}
