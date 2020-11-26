import java.util.*;

public class ResourceAllocationGraph {

    Map<String, ArrayList<String>> adjMap = new HashMap<>();
    Set<String> transactionSet = new HashSet<>();


    public void addRequestLockEdge(String start, String end){
        ArrayList<String> adjList = adjMap.getOrDefault(start, new ArrayList<>());
        adjList.add(end);
        adjMap.put(start,adjList);
        transactionSet.add(start);
    }

    public void addGetLockEdge(String start, String end){
        // remove the request lock edge first
        ArrayList<String> requestedAdjList = adjMap.get(end);
        requestedAdjList.remove(start);
        adjMap.put(end, requestedAdjList);
        ArrayList<String> adjList = adjMap.getOrDefault(start, new ArrayList<>());
        adjList.add(end);
        adjMap.put(start,adjList);
    }

    public void detectDeadlock(Map<String,Transaction> transactionMap){

        List<List<String>> transactionsInCycle = new ArrayList<>();
        Map<String,Integer> visitedTransactions = new HashMap<>();
        for(String transaction : transactionSet){
            dfs(transaction, visitedTransactions, new ArrayList(), transactionsInCycle);
        }

        removeCycle(transactionMap, transactionsInCycle);
    }

    private void removeCycle(Map<String, Transaction> transactionMap, List<List<String>> transactionsInCycle) {
        for(List<String> cycle : transactionsInCycle){
            Transaction youngestTransaction = null;
            for(String trans : cycle){
                if(youngestTransaction == null){
                    youngestTransaction = transactionMap.get(trans);
                }else{
                    if(youngestTransaction.startTime < transactionMap.get(trans).startTime){
                        youngestTransaction = transactionMap.get(trans);
                    }
                }
            }

            youngestTransaction.setTransactionStatus(TransactionStatus.ABORTED);
            adjMap.remove(youngestTransaction.name);
            for(Map.Entry<String,ArrayList<String>> entry : adjMap.entrySet()){
                ArrayList<String> vals = entry.getValue();
                vals.remove(new String(youngestTransaction.name));
            }
        }
    }

    public void dfs(String currentTransaction, Map<String, Integer> visitedTransactions, List<String> transactionPath,
                    List<List<String>> transactionsInCycle){

        if(visitedTransactions.containsKey(currentTransaction)){
            int cycleStartIndex = visitedTransactions.get(currentTransaction);
            transactionsInCycle.add(new ArrayList<>(transactionPath.subList(cycleStartIndex, transactionPath.size())));
            return;
        }
        if(currentTransaction.startsWith("T")){
            visitedTransactions.put(currentTransaction,transactionPath.size());
            transactionPath.add(currentTransaction);
        }

        for(String neighbour : adjMap.get(currentTransaction)){
            dfs(neighbour, visitedTransactions, transactionPath, transactionsInCycle);
        }

        if(currentTransaction.startsWith("T")){
            visitedTransactions.remove(currentTransaction);
            transactionPath.remove(transactionPath.size()-1);
        }

    }
}
