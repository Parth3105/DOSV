package taskhandle;

import service.MetaService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class DownloadHandler implements Handler {

    private final Socket socket;
    private final DataOutputStream dataOutputStream;
    private final DataInputStream dataInputStream;
    private final MetaService metaService;

    public DownloadHandler(Socket socket, DataOutputStream dataOutputStream, DataInputStream dataInputStream, MetaService metaService) {
        this.socket = socket;
        this.dataOutputStream = dataOutputStream;
        this.dataInputStream = dataInputStream;
        this.metaService = metaService;
    }

    @Override
    public void receive(String fileName) {

    }

    @Override
    public void send() {

    }
}
