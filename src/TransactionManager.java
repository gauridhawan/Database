
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

/**
 * Transaction Manager Class
 * Authors: Kunal Khatri, Gauri Dhawan
 * Date: November 29
 */
public class TransactionManager {

    int numberOfSites = 10;
    int numberOfVariables = 20;
    Map<String,Transaction> transactionMap = new HashMap<>();
    Map<String, List<Pair<String,Integer>>> transactionWritePermission = new HashMap<>();
    List<Pair<Transaction, String>> waitingReadOnly = new ArrayList<>();
    Map<String, Queue<Pair<Lock,Integer>>> waitingTransactionMap = new HashMap<>();
    int currentTimeStamp;
    SiteManager siteManager = new SiteManager(numberOfSites, numberOfVariables);
    ResourceAllocationGraph resourceAllocationGraph = new ResourceAllocationGraph();
    /*
    * If the transaction is valid, commit the uncommitted variables
    * Check if the transaction goes through using the condition - if any of the servers that we've accessed has gone down,
    * then it should abort (Not for readonly)
    * Otherwise the transaction can go ahead
    * If transaction has been aborted, return false
    * */

    /**
     * Method used to commit a transaction.
     * @param transactionId of the transaction to be committed
     * @return true if the transaction was committed successfully, false if it was aborted
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public boolean commitTransaction(String transactionId){

        Transaction transaction = transactionMap.get(transactionId);
        if(transaction.getTransactionStatus() == TransactionStatus.ABORTED){
            return false;
        }
        if(transaction.isReadOnly()){
            List<Pair<Transaction,String>> updated = new ArrayList<>();
            for(Pair<Transaction, String> ROTransaction : waitingReadOnly){
                if(!ROTransaction.key.name.equals(transactionId)){
                    updated.add(ROTransaction);
                }
            }
            waitingReadOnly.clear();
            waitingReadOnly.addAll(updated);
        }

        if(!transaction.isReadOnly()){
            for(Pair<Site, Integer> siteAccessed : transaction.getSitesAccessed()){
                //System.out.println(transactionId +" "+ siteAccessed.key.index + " " + siteAccessed.key.lastFailedTime +" "+siteAccessed.value);
                if(siteAccessed.key.getLastFailedTime() >= siteAccessed.value){
                    // transaction cannot go forward since it failed since the transaction started
                    //System.out.println(transactionId +" "+siteAccessed.getLastFailedTime()+" "+transaction.getStartTime());
                    transaction.setTransactionStatus(TransactionStatus.ABORTED);
                    return false;
                }
            }
        }

        Map<String,Pair<Integer,List<Site>>> uncommittedVariables = transaction.getUncommittedVariables();
        //System.out.println(uncommittedVariables);
        for(String variable : uncommittedVariables.keySet()){
            // Write the variable at their resp sites
            List<Site> sitesToBeUpdated = uncommittedVariables.get(variable).value;
            for(Site site : sitesToBeUpdated){
                Site siteToBeUpdated = site;
                Variable var = siteToBeUpdated.getDataManager().getVariable(variable);
                siteToBeUpdated.writeVariable(transaction,var,uncommittedVariables.get(variable).key);
            }
        }

        transaction.setTransactionStatus(TransactionStatus.COMMITTED);
        return true;
    }

    /**
     * Method used to start a transaction by creating a transaction object.
     * @param transactionId of the transaction to be started
     * @param timestamp at which the transaction was started
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void beginTransaction(String transactionId, int timestamp){

        Transaction transaction = createTransaction(transactionId, timestamp);
        transactionMap.put(transactionId,transaction);
    }

    /**
     * Create a transaction object with the fields provided.
     * @param transactionId of the transaction whose object is to be created
     * @param timestamp at which function is called
     * @return Transaction object created
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    private Transaction createTransaction(String transactionId, int timestamp) {
        Transaction transaction = new Transaction();
        transaction.setName(transactionId);
        transaction.setStartTime(timestamp);
        transaction.setTransactionStatus(TransactionStatus.RUNNING);
        return transaction;
    }

    /*
    * TODO : Store current variable values
    * */

    /**
     * Method used to start a read-only transaction. This method will record the values of the variables when the
     * transaction starts.
     * @param transactionId of the transaction to be started
     * @param timestamp at which the function is called
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void beginROTransaction(String transactionId, int timestamp){
        Transaction transaction = createTransaction(transactionId, timestamp);
        transaction.setReadOnly(true);
        HashMap<String, Pair<Site,Integer>> variableValueMap = this.siteManager.getVariableValues();
        HashMap<String, Pair<Site,Integer>> variableValueMap2 = this.siteManager.getOddVariableValues();
        //System.out.println(variableValueMap);
        HashMap<String, Integer> variableValueMap1 = new HashMap<>();
        for(String str : variableValueMap.keySet()){
            variableValueMap1.put(str, variableValueMap.get(str).value);
        }
        for(String str : variableValueMap2.keySet()){
            if(!variableValueMap.containsKey(str))
                variableValueMap1.put(str, variableValueMap2.get(str).value);
        }
        transaction.setCommittedValues(variableValueMap1);
        transactionMap.put(transactionId,transaction);
    }

    /*
    * RO will just have to check if the site is up or not, if yes, then put in blocked, otherwise simply read committed value
    * normal read, try to get read lock, check if it doesn't have a write lock from any other transaction
    * read can read from any site
    * whichever site is up and is first, take read lock on that
    *
    * TODO : update read only part
    * */

    /**
     * Method to process a read request. Based on the locks available, the method assesses whether the transaction
     * is able to get a lock to read the variable, otherwise it moves to the waiting queue.
     * @param transactionId of the transaction that tries to perform a read
     * @param timestamp at which the request comes in
     * @param variable that the transaction wants to read
     * @return true if the transaction is able to get a read lock on the variable, false if not
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public boolean readRequest(String transactionId, int timestamp, String variable){
        Transaction transaction = transactionMap.get(transactionId);
        int variableIndex = Integer.parseInt(variable.substring(1));
        if(transaction.getTransactionStatus() == TransactionStatus.ABORTED){
            return true;
        }
        if(transaction.isReadOnly()){
            Map<String, Integer> variableValueAtTransactionStart = transaction.getCommittedValues();
            //System.out.println(variableValueAtTransactionStart);
            if(variableValueAtTransactionStart.containsKey(variable)){
                if(variableIndex % 2 == 1){
                    if(this.siteManager.getSite(variableIndex%10 + 1).siteStatus != SiteStatus.DOWN){
                        printVariableValue(variable, variableValueAtTransactionStart.get(variable));
                        return true;
                    }
                    else{
                        if(transaction.transactionStatus != TransactionStatus.WAITING) {
                            transaction.setTransactionStatus(TransactionStatus.WAITING);
                            waitingReadOnly.add(new Pair(transaction, variable));
                        }
                        return false;
                    }
                }
                else {
                    printVariableValue(variable, variableValueAtTransactionStart.get(variable));
                    return true;
                }
            }else{
                if(transaction.transactionStatus != TransactionStatus.WAITING) {
                    transaction.setTransactionStatus(TransactionStatus.WAITING);
                    waitingReadOnly.add(new Pair(transaction, variable));
                }
                return false;
            }
        }else{
            if(transaction.uncommittedVariables.containsKey(variable)){
                resourceAllocationGraph.addGetLockEdge(variable,transactionId, LockType.WRITE);
                Pair<Integer, List<Site>> variableValue = transaction.uncommittedVariables.get(variable);
                printVariableValue("x"+variableIndex, variableValue.key);
                transaction.variablesAccessed.add(variable);
                return true;
            }
            Pair<Site, Integer> siteLock = siteManager.getLock(transaction, variableIndex, LockType.READ);
            int lockAcquired = siteLock.value;
            //System.out.println("######### " + lockAcquired);
            if(lockAcquired == LockStatus.GOT_LOCK.getLockStatus() || lockAcquired == LockStatus.GOT_LOCK_RECOVERING.getLockStatus()){
                resourceAllocationGraph.addGetLockEdge(variable,transactionId, LockType.READ);
                List<Variable> variables = siteLock.key.getAllVariables();
                int variableValue = -1;
                for(Variable var : variables){
                    if(var.name.equals(variable)){
                        variableValue = var.value;
                    }
                }
                //Pair<Site, Integer> variableSitePair = siteManager.getVariableValues().get("x"+variableIndex);
                Site site = siteLock.key;
                transaction.sitesAccessed.add(new Pair<>(site, currentTimeStamp));
                printVariableValue("x"+variableIndex, variableValue);
                transaction.variablesAccessed.add(variable);
                return true;
            }
            else{
                addToWaitingQueue(variable, transaction, LockType.READ, -1);
                return false;
            }
        }
    }

    /**
     * Method to add a transaction to a waiting queue when it is unable to attain a lock on a variable.
     * @param variable the transaction wants to get a lock on
     * @param transaction object that is trying to get a lock
     * @param lockType type of lock the transaction is trying to attain
     * @param value the transaction wants to write in case the transaction is a write transaction
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    private void addToWaitingQueue(String variable, Transaction transaction, LockType lockType, int value) {
        //if(transaction.transactionStatus == TransactionStatus.WAITING) {
            if (waitingTransactionMap.containsKey(variable)) {
                boolean isAlreadyPresent = false;
                for(Pair<Lock,Integer> waitingTrasanction : waitingTransactionMap.get(variable)){
                    if(waitingTrasanction.key.transaction.name.equals(transaction.name)
                            && waitingTrasanction.key.lockType == lockType){
                        isAlreadyPresent = true;
                    }
                }

                if(!isAlreadyPresent){
                    waitingTransactionMap.get(variable).add(new Pair(new Lock(transaction, lockType), value));
                }

            } else {
                Queue<Pair<Lock, Integer>> queue = new LinkedList<>();
                queue.add(new Pair(new Lock(transaction, lockType), value));
                waitingTransactionMap.put(variable, queue);
            }
            transaction.setTransactionStatus(TransactionStatus.WAITING);
        //}
    }

    /**
     * Method to end a transaction. The method proceeds to commit the transaction and print a result based on whether
     * the transaction was committed or aborted.
     * @param transactionId of the transaction to be ended
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void endTransaction(String transactionId){
        //System.out.println(transactionId);
        boolean isCommitted = commitTransaction(transactionId);
        this.clearLocks(transactionId);
        this.clearWaitingTransactions(transactionId);
        if(isCommitted){
            System.out.println(transactionId+" commits");
        }else{
            System.out.println(transactionId+" aborts");
        }
    }

    /**
     * Method to clear the locks being held by a transaction.
     * @param transactionId of the transaction holding the locks
     */
    public void clearLocks(String transactionId){
        Transaction transaction = this.transactionMap.get(transactionId);
        for(Pair<Site, Integer> siteAccessed : transaction.sitesAccessed){
            for(String variable : transaction.variablesAccessed){
                Queue<Lock> locks = this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.locks.getOrDefault(variable, new LinkedList<>());
                Queue<Lock> ans = new LinkedList<>();
                for(Lock lock : locks){
                    //System.out.println("Locks : " + transactionId + " " + lock.transactionId +" "+ lock.lockType);
                    if(!lock.transactionId.equals(transaction.name)) {
                        ans.add(lock);
                    }
                }
                this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.locks.put(variable, ans);
                Queue<Lock> locks1 = this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.waitingLocks.getOrDefault(variable, new LinkedList<>());
                Queue<Lock> ans1 = new LinkedList<>();
                for(Lock lock : locks1){
                    //System.out.println("Locks : " + transactionId + " " + lock.transactionId +" "+ lock.lockType);
                    if(!lock.transactionId.equals(transaction.name)) {
                        ans1.add(lock);
                    }
                }
                this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.waitingLocks.put(variable, ans1);
            }
        }
    }


    /**
     * Method to process a write request. The transaction tries to acquire locks, if the lock is not acquired, the
     * transaction is added to the waiting queue.
     * @param transactionId of the transaction trying to write
     * @param variable the transaction wants to update
     * @param value the transaction wants to set
     * @return true if the transaction is able to acquire locks, false if not
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public boolean writeRequest(String transactionId, String variable, int value){
        int variableIndex = Integer.parseInt(variable.substring(1));
        Transaction transaction = transactionMap.get(transactionId);
        if(transaction.getTransactionStatus() == TransactionStatus.ABORTED){
            // So that it exits from the waiting transactions queue
            return true;
        }
        Pair<Site, Integer> siteLock = siteManager.getLock(transaction, variableIndex, LockType.WRITE);
        int lockAcquired = siteLock.value;
        if (lockAcquired == LockStatus.GOT_LOCK.getLockStatus()){
            resourceAllocationGraph.addGetLockEdge(variable,transactionId, LockType.WRITE);
            Map<String, Pair<Integer,List<Site>>> uncommittedVars =  transaction.getUncommittedVariables();
            List<Site> sitesToBeUpdated = new ArrayList<>();
            if(variableIndex%2==0) {
                for (Site site : siteManager.sites) {
                    if (site.siteStatus != SiteStatus.DOWN) {
                        transaction.sitesAccessed.add(new Pair<>(site,currentTimeStamp));
                        sitesToBeUpdated.add(site);
                    }
                }
            }
            else{
                Site site = siteManager.getSite(variableIndex%10 + 1);
                sitesToBeUpdated.add(site);
                transaction.sitesAccessed.add(new Pair<>(site,currentTimeStamp));
            }
            uncommittedVars.put(variable,new Pair<>(value,sitesToBeUpdated));
            transaction.setUncommittedVariables(uncommittedVars);
            transaction.variablesAccessed.add(variable);
            //System.out.println("Got write lock" + transactionId);
            return true;
        }
        else{
            addToWaitingQueue(variable,transaction,LockType.WRITE, value);
            return false;
        }
    }

    /*
    * Check deadlock
    * Instruction as argument
    * check deadlock before each tick
    * if deadlock, abort youngest transaction
    * */

    /**
     * Method to process all transactions and clear locks for all aborted transactions
     * @param transactionMap containing mapping of transaction id and transaction
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void clearAbortedTransactions(Map<String, Transaction> transactionMap){
        for(String transactionId : transactionMap.keySet()){
            Transaction transaction = transactionMap.get(transactionId);
            if(transaction.transactionStatus == TransactionStatus.ABORTED) {
                clearLocks(transactionId);
            }
        }

    }

    // TODO : TO BE UPDATED
    /**
     * Remove waiting locks
     * @param transactionId
     * @param lockType
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void clearWaitingLocks(String transactionId, LockType lockType){
        Transaction transaction = this.transactionMap.get(transactionId);
        for(Site site : this.siteManager.sites){
            for(String variable : transaction.variablesAccessed){
                Queue<Lock> locks1 = site.dataManager.lockTable.waitingLocks.getOrDefault(variable, new LinkedList<>());
                Queue<Lock> ans1 = new LinkedList<>();
                for(Lock lock : locks1){
                    //System.out.println("Locks : " + transactionId + " " + lock.transactionId +" "+ lock.lockType);
                    if(!(lock.transactionId.equals(transaction.name) && lock.lockType.equals(lockType))) {
                        ans1.add(lock);
                    }
                }
                site.dataManager.lockTable.waitingLocks.put(variable, ans1);
            }
        }
    }

    /**
     *
     * @param transactionId
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void clearWaitingTransactions(String transactionId){
        for(String str : waitingTransactionMap.keySet()){
            Queue<Pair<Lock,Integer>> currentTransactions = new LinkedList<>();
            for(Pair<Lock, Integer> transaction : waitingTransactionMap.get(str)){
                if(!transaction.key.transactionId.equals(transactionId)){
                    currentTransactions.add(transaction);
                }
            }
            waitingTransactionMap.replace(str,currentTransactions);
        }
    }

    /**
     * Process read only transactions that are currently waiting and see if they can be completed.
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void tryWaitingReadOnly(){
        List<Pair<Transaction, String>> notCompleted = new ArrayList<>();
        for(Pair<Transaction, String> pair : this.waitingReadOnly){
            Transaction transaction = pair.key;
            String variable = pair.value;
            boolean isCompleted = readRequest(transaction.name, currentTimeStamp, variable);
            if(!isCompleted) {
                notCompleted.add(pair);
            }
        }
        this.waitingReadOnly.clear();
        this.waitingReadOnly.addAll(notCompleted);
    }

    /**
     * Process read write transactions that are currently waiting and see if they can be completed.
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void tryWaitingTransactions(){
        //System.out.println(waitingTransactionMap.keySet());
        //System.out.println(waitingTransactionMap.entrySet());
        boolean b = false;
        for(String variable : waitingTransactionMap.keySet()){
            Queue<Pair<Lock, Integer>> queue = waitingTransactionMap.get(variable);
            if(queue.isEmpty()){
               continue;
            }
            Lock lock = queue.peek().key;
            boolean flag = true;
            if(lock.lockType == LockType.READ){
                flag &= this.readRequest(lock.transaction.name, currentTimeStamp, variable);
            }
            else{
                //System.out.println("Transaction " + lock.transactionId + " trying " + lock.lockType);
                flag &= this.writeRequest(lock.transaction.name, variable, queue.peek().value);
            }
            //System.out.println(flag +" "+ lock.transaction.uncommittedVariables);
            if(flag){
                b = true;
                //System.out.println("Transaction " + lock.transactionId + " got " + lock.lockType);
                queue.poll();
                if(!queue.isEmpty()){
                    waitingTransactionMap.replace(variable, queue);
                }
                clearWaitingLocks(lock.transactionId, lock.lockType);
                //else waitingTransactionMap.remove(variable);
            }
        }
        if(b){
            tryWaitingTransactions();
        }
    }

    /**
     * Driver method that is executed every second. This method checks for deadlocks, tries to complete waiting transactions
     * and clears aborted transactions. Then, based on the type of incoming instruction, calls appropriate methods to
     * process the instruction.
     * @param currentInstr incoming instruction
     * @author Gauri Dhawan, Kunal Khatri
     * @side-effects increments timestamp
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void tick(Instruction currentInstr){
        resourceAllocationGraph.detectAndRemoveDeadlock(transactionMap);
        clearAbortedTransactions(transactionMap);
        tryWaitingReadOnly();
        tryWaitingTransactions();
        //System.out.println(currentInstr.transactionType);
        if(currentInstr.transactionType == TransactionType.begin){
            this.beginTransaction(currentInstr.transactionId, currentInstr.timestamp);
        }else if(currentInstr.transactionType == TransactionType.beginRO){
            this.beginROTransaction(currentInstr.transactionId, currentInstr.timestamp);
        }else if(currentInstr.transactionType == TransactionType.R){
            resourceAllocationGraph.addRequestLockEdge(currentInstr.transactionId,currentInstr.variableName, LockType.READ);
            this.readRequest(currentInstr.transactionId, currentInstr.timestamp, currentInstr.variableName);
        }else if(currentInstr.transactionType == TransactionType.W){
            resourceAllocationGraph.addRequestLockEdge(currentInstr.transactionId,currentInstr.variableName, LockType.WRITE);
            this.writeRequest(currentInstr.transactionId, currentInstr.variableName, currentInstr.value);
        }else if(currentInstr.transactionType == TransactionType.end){
            this.endTransaction(currentInstr.transactionId);
        }else {
            this.siteManager.tick(currentInstr, currentTimeStamp);
        }
        currentTimeStamp++;
    }

    /**
     * Method to print out the value of a variable
     * @param variable
     * @param value
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public void printVariableValue(String variable, int value){
        System.out.println(variable+": "+value);
    }

}
