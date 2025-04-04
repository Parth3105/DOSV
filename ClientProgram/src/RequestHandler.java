import java.net.Socket;

public interface RequestHandler {
    void sendRequest(String req);
    void receiveResponse();
}
