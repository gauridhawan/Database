import java.util.*;

/**
 * Graph to represent what resource is being held by what transaction.
 */
public class ResourceAllocationGraph {


    Map<String, ArrayList<Pair<String,LockType>>> adjMap = new HashMap<>();
    Set<String> transactionSet = new HashSet<>();


    /**
     * Method to add and edge in the graph when a transaction requests a lock on a variable. Updates the adjacency
     * map with an edge
     * @param start of the edge, transaction asking the lock
     * @param end of the edge, the variable on which lock is being requested
     * @param lockType type of lock being requested
     * @author Gauri Dhawan, Kunal Khatri
     */
    public void addRequestLockEdge(String start, String end, LockType lockType){
        ArrayList<Pair<String,LockType>> adjList = adjMap.getOrDefault(start, new ArrayList<>());
        adjList.add(new Pair<>(end,lockType));
        adjMap.put(start,adjList);
        transactionSet.add(start);
    }

    /**
     * Method to add an edge when a transaction receives a lock on a variable. It removes the edge added when the
     * lock is requested and then adds a reversed edge.
     * @param start of the edge, the variable on which lock is acquired
     * @param end of the edge, the transaction that gets the lock
     * @param lockType type of lock
     * @author Gauri Dhawan, Kunal Khatri
     */
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

    /**
     * Method to detect and remove a deadlock in transactions.
     * @param transactionMap containing mapping of transaction id and transaction
     * @author Gauri Dhawan, Kunal Khatri
     */
    public void detectAndRemoveDeadlock(Map<String,Transaction> transactionMap){

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
        if(isCyclePresent && transactionsInCycle.size() >= 1 ){
            boolean isCycleToBeRemoved = true;
            if(transactionsInCycle.get(0).size() == 1){
                isCycleToBeRemoved = false;
                // self loop, check if someone is also asking for the lock on the variable in the cycle
                ArrayList<Pair<String,LockType>> adjList = adjMap.get(transactionsInCycle.get(0).get(0));
                for(Pair<String,LockType> edge : adjList){
                    ArrayList<Pair<String,LockType>> adjList2 = adjMap.get(edge.key);
                    boolean isVariableInCycle = false;
                    for(Pair<String,LockType> reverseEdge : adjList2){
                        if(reverseEdge.key.equalsIgnoreCase(transactionsInCycle.get(0).get(0))){
                            isVariableInCycle = true;
                        }
                    }

                    if(isVariableInCycle){
                        for(String trans : transactionMap.keySet()){
                            if(!trans.equalsIgnoreCase(transactionsInCycle.get(0).get(0))){
                                ArrayList<Pair<String,LockType>> otherTransAdjList = adjMap.get(trans);
                                for(Pair<String,LockType> edge2 :  otherTransAdjList){
                                    if(edge2.key.equalsIgnoreCase(edge.key)){
                                        isCycleToBeRemoved = true;
                                    }
                                }
                            }
                        }
                    }


                }

            }

            if(isCycleToBeRemoved){
                removeCycle(transactionMap, transactionsInCycle);
                detectAndRemoveDeadlock(transactionMap);
            }
        }


    }

    /**
     * Method to abort the youngest transaction in a cycle.
     * @param transactionMap containing mapping of transaction id and transaction
     * @param transactionsInCycle list of transactions in involved in the cycle
     * @author Gauri Dhawan, Kunal Khatri
     */
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

    /**
     * Method to detect whether a graph has a cycle or not.
     * @param currentTransaction, transactionId of the transaction whos edges will be explored
     * @param visitedTransactions set of transactions visited till now
     * @param transactionPath path of transactions followed till  now
     * @param transactionsInCycle if a cycle is present, this list will contain a list containing transactions in a cycle
     * @return true if a cycle is present, else false
     * @author Gauri Dhawan, Kunal Khatri
     */
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
