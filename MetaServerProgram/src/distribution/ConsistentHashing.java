package distribution;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class ConsistentHashing {

    private final TreeMap<Long, String> ring;

    public ConsistentHashing(List<String> servers) {
        ring = new TreeMap<>();
        Collections.shuffle(servers);
        for (String server : servers) {
            long position = hash(server);
            ring.put(position, server);
        }
    }

    public String[] getNodeForChunk(String chunkId) {
        int replicationFactor = 2;
        String[] nodes = new String[replicationFactor];
        long chunkHash = hash(chunkId);

        // Find the first key in the ring >= chunkHash
        Arrays.fill(nodes, null);
        int j = 0;
        for (Map.Entry<Long, String> entry : ring.entrySet()) {
            if (chunkHash <= entry.getKey()) {
                nodes[j++] = entry.getValue();
            }
            if (j >= replicationFactor) break;
        }

        for (Map.Entry<Long, String> entry : ring.entrySet()) {
            if (j >= replicationFactor) break;
            nodes[j++] = entry.getValue();
        }

        return nodes;
    }

    private long hash(String key) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = sha1.digest(key.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInt = new BigInteger(1, hashBytes);
            return bigInt.mod(BigInteger.valueOf((1L << 32))).longValue();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing key", e);
        }
    }
}
