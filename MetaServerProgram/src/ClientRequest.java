class ClientRequest {
    static final String UPLOAD="WRITE", DOWNLOAD="READ";
    private String requestType;
    private String fileName;

    ClientRequest(String requestType, String fileName) {
        this.requestType = requestType;
        this.fileName = fileName;
    }

    String getRequestType() {
        return requestType;
    }

    String getFileName() {
        return fileName;
    }
}
