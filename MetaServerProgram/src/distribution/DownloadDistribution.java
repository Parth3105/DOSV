package distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadDistribution {
    public Map<String, List<String>> handleDistribution(Map<String, List<String>> chunkData) {
        Map<String, List<String>> distribution = new HashMap<>();

        /// debug
        /*System.out.println("CHUNK DATA --> {");
        for(Map.Entry<String,List<String>> entry:chunkData.entrySet()){
            System.out.print("\t"+entry.getKey()+": [ ");
            for(String chunkName: entry.getValue()){
                System.out.print(chunkName+" ");
            }
            System.out.println("]");
        }
        System.out.println("}");*/
        ///

        for (Map.Entry<String, List<String>> entry : chunkData.entrySet()) {
            List<String> nodes = entry.getValue();

            int load = Integer.MAX_VALUE;
            String leastLoadedNode = null;
            for (String node : nodes) {
                List<String> chunksToFetch = distribution.getOrDefault(node, new ArrayList<>());
                if (chunksToFetch.size() < load) {
                    load = chunksToFetch.size();
                    leastLoadedNode = node;
                }
            }

            List<String> chunksToFetch = distribution.getOrDefault(leastLoadedNode, new ArrayList<>());
            chunksToFetch.add(entry.getKey());
            distribution.put(leastLoadedNode, chunksToFetch);
        }

        return distribution;
    }
}
