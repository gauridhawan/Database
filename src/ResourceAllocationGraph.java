import java.util.*;

public class ResourceAllocationGraph {

    Map<String, List<String>> adjMap = new HashMap<>();
    Set<String> transactionSet = new HashSet<>();


    public void addRequestLockEdge(String start, String end){
        List<String> adjList = adjMap.getOrDefault(start, new ArrayList<>());
        adjList.add(end);
        adjMap.put(start,adjList);
        transactionSet.add(start);
    }

    public void addGetLockEdge(String start, String end){
        // remove the request lock edge first
        List<String> requestedAdjList = adjMap.get(end);
        requestedAdjList.remove(start);
        adjMap.put(end, requestedAdjList);
        List<String> adjList = adjMap.getOrDefault(start, new ArrayList<>());
        adjList.add(end);
        adjMap.put(start,adjList);
    }

    public void detectDeadlock(){

        for(String transaction : transactionSet){
            //dfs();
        }
    }
}
