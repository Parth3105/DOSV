import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LoadBalancer {
    Map<String, Long> loadTracker;
    ReadWriteLock readWriteLock;

    public LoadBalancer() {
        this.loadTracker = new HashMap<>();
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    void registerServer(String ip, int port) {
        readWriteLock.writeLock().lock();
        loadTracker.putIfAbsent(ip + ":" + port, 0L);
        readWriteLock.writeLock().unlock();
    }

    void unregisterServer(String ip, int port) {
        readWriteLock.writeLock().lock();
        loadTracker.remove(ip + ":" + port);
        readWriteLock.writeLock().unlock();
    }

    void incrementLoad(String serverAddress) {
        readWriteLock.writeLock().lock();
        long load = loadTracker.getOrDefault(serverAddress, 0L);
        loadTracker.put(serverAddress, load + 1);
        readWriteLock.writeLock().unlock();
    }

    void decrementLoad(String serverAddress) {
        readWriteLock.writeLock().lock();
        long load = loadTracker.getOrDefault(serverAddress, 0L);
        loadTracker.put(serverAddress, load - 1);
        readWriteLock.writeLock().unlock();
    }

    String leastLoadedServer() {
        long minLoad = Long.MAX_VALUE;
        String reqServer = null;
        readWriteLock.readLock().lock();
        for (Map.Entry<String, Long> entry : loadTracker.entrySet()) {
            if (entry.getValue() < minLoad) {
                minLoad = entry.getValue();
                reqServer = entry.getKey();
            }
        }
        readWriteLock.readLock().unlock();
        return reqServer;
    }

    void handleClientReq(int serverPort, LoadBalancer loadBalancer) {
        ServerSocket serverSocket=null;
        try {
            serverSocket = new ServerSocket(serverPort, 100);
        } catch (Exception e) {
            System.err.println("Error: Server isn't starting.....");
            return;
        }

        System.out.println("Load Balancer is running on port: " + serverPort + " for clients!!!");

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                String serverAddress = loadBalancer.leastLoadedServer();
                dataOutputStream.writeUTF(serverAddress);
                dataOutputStream.flush();
                dataOutputStream.close();
                socket.close();

                System.out.println();
                loadBalancer.incrementLoad(serverAddress);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void handleServerReq(int serverPort, LoadBalancer loadBalancer) {
        ServerSocket serverSocket=null;
        try {
            serverSocket = new ServerSocket(serverPort, 100);
        } catch (Exception e) {
            System.err.println("Error: Server isn't starting.....");
            return;
        }

        System.out.println("Load Balancer is running on port: " + serverPort + " for meta-servers!!!");

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New meta-server connected: " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                String requestType = dataInputStream.readUTF();
                int metaPort = dataInputStream.readInt();

                String metaIP = socket.getInetAddress().getHostAddress();
                String metaAddress = metaIP + ":" + metaPort;

                if (requestType.equalsIgnoreCase("REGISTER")) loadBalancer.registerServer(metaIP, metaPort);
                else if (requestType.equalsIgnoreCase("RELEASE")) loadBalancer.decrementLoad(metaAddress);
                else ;

                dataInputStream.close();
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        int portForClient = 8100;
        int portForMeta = 8110;
        LoadBalancer loadBalancer = new LoadBalancer();

        Thread clientHandler = new Thread(new Runnable() {
            @Override
            public void run() {
                loadBalancer.handleClientReq(portForClient, loadBalancer);
            }
        });
        Thread serverHandler = new Thread(new Runnable() {
            @Override
            public void run() {
                loadBalancer.handleServerReq(portForMeta, loadBalancer);
            }
        });

        serverHandler.start();
        clientHandler.start();
    }
}