import java.util.*;

public class ResourceAllocationGraph {


    Map<String, ArrayList<Pair<String,LockType>>> adjMap = new HashMap<>();
    Set<String> transactionSet = new HashSet<>();


    public void addRequestLockEdge(String start, String end, LockType lockType){
        ArrayList<Pair<String,LockType>> adjList = adjMap.getOrDefault(start, new ArrayList<>());
        adjList.add(new Pair<>(end,lockType));
        adjMap.put(start,adjList);
        transactionSet.add(start);
    }

    public void addGetLockEdge(String start, String end, LockType lockType){
        // remove the request lock edge first
        ArrayList<Pair<String,LockType>> requestedAdjList = adjMap.get(end);
        ArrayList<Pair<String,LockType>> newAdjList = new ArrayList<>();
        for(Pair<String,LockType> edge : requestedAdjList){
            if(lockType == LockType.WRITE){
                if(edge.key.equalsIgnoreCase(start)){
                    continue;
                }
            }else{
                if(edge.key.equalsIgnoreCase(start) && edge.value == LockType.READ){
                    continue;
                }
            }
           newAdjList.add(edge);
        }
        //requestedAdjList.remove(start);
        adjMap.put(end, newAdjList);
        ArrayList<Pair<String,LockType>> adjList = adjMap.getOrDefault(start, new ArrayList<>());
        adjList.add(new Pair<>(end,lockType));
        adjMap.put(start,adjList);
    }

    public void detectDeadlock(Map<String,Transaction> transactionMap){

        List<List<String>> transactionsInCycle = new ArrayList<>();
        Map<String,Integer> visitedTransactions = new HashMap<>();
        boolean isCyclePresent = false;
        for(String transaction : transactionSet){
            if(hasCycle(transaction, visitedTransactions, new ArrayList(), transactionsInCycle)){
                isCyclePresent = true;
                break;
            }
        }
        //System.out.println("Cycles -> " + transactionsInCycle);
        if(isCyclePresent && transactionsInCycle.size() >= 1 && transactionsInCycle.get(0).size() > 0){
            removeCycle(transactionMap, transactionsInCycle);
            detectDeadlock(transactionMap);
        }

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

            System.out.println(youngestTransaction.name+" aborts");
            youngestTransaction.setTransactionStatus(TransactionStatus.ABORTED);
            adjMap.remove(youngestTransaction.name);
            for(Map.Entry<String,ArrayList<Pair<String,LockType>>> entry : adjMap.entrySet()){
                ArrayList<Pair<String,LockType>> vals = entry.getValue();
                vals.remove(new String(youngestTransaction.name));
            }
        }
    }

    public boolean hasCycle(String currentTransaction, Map<String, Integer> visitedTransactions,
                            List<String> transactionPath,
                    List<List<String>> transactionsInCycle){

        if(visitedTransactions.containsKey(currentTransaction)){
            int cycleStartIndex = visitedTransactions.get(currentTransaction);
            transactionsInCycle.add(new ArrayList<>(transactionPath.subList(cycleStartIndex, transactionPath.size())));
            return true;
        }

        if(currentTransaction.startsWith("T")){
            visitedTransactions.put(currentTransaction,transactionPath.size());
            transactionPath.add(currentTransaction);
        }

        boolean result = false;
        for(Pair<String,LockType> neighbour : adjMap.getOrDefault(currentTransaction, new ArrayList<>())){

           result = result || hasCycle(neighbour.key, visitedTransactions, transactionPath, transactionsInCycle);


        }

        if(currentTransaction.startsWith("T")){
            visitedTransactions.remove(currentTransaction);
            transactionPath.remove(transactionPath.size()-1);
        }



        return result;
    }
}
