package taskhandle;

public interface RequestHandler {
    void sendRequest(String req);

    void receiveResponse();
}
