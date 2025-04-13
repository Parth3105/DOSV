package requests;

public class ClientRequest {
    public static final String UPLOAD="WRITE";
    public static final String DOWNLOAD="READ";
    private String requestType;
    private String fileName;

    public ClientRequest(String requestType, String fileName) {
        this.requestType = requestType;
        this.fileName = fileName;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getFileName() {
        return fileName;
    }
}
