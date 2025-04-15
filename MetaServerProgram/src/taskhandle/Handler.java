package taskhandle;

import java.util.List;

public interface Handler {
    public void receive(String fileName, int version);

    public void send(List<String> chunkNames, List<byte[]> chunks);
}
