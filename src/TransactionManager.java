
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;


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
    public boolean commitTransaction(String transactionId){

        Transaction transaction = transactionMap.get(transactionId);
        if(transaction.getTransactionStatus() == TransactionStatus.ABORTED){
            return false;
        }
        if(!transaction.isReadOnly()){
            for(Pair<Site, Integer> siteAccessed : transaction.getSitesAccessed()){
                //System.out.println(transactionId +" "+ siteAccessed.key.lastFailedTime +" "+siteAccessed.value);
                if(siteAccessed.key.getLastFailedTime() > siteAccessed.value){
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

    public void beginTransaction(String transactionId, int timestamp){

        Transaction transaction = createTransaction(transactionId, timestamp);
        transactionMap.put(transactionId,transaction);
    }

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
    public void beginROTransaction(String transactionId, int timestamp){
        Transaction transaction = createTransaction(transactionId, timestamp);
        transaction.setReadOnly(true);
        HashMap<String, Pair<Site,Integer>> variableValueMap = this.siteManager.getVariableValues();
        //System.out.println(variableValueMap);
        HashMap<String, Integer> variableValueMap1 = new HashMap<>();
        for(String str : variableValueMap.keySet()){
            variableValueMap1.put(str, variableValueMap.get(str).value);
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
                printVariableValue(variable, variableValueAtTransactionStart.get(variable));
                return true;
            }else{
                if(transaction.transactionStatus != TransactionStatus.WAITING) {
                    transaction.setTransactionStatus(TransactionStatus.WAITING);
                    waitingReadOnly.add(new Pair(transaction, variable));
                }
                return false;
            }
        }else{
            Pair<Site, Integer> siteLock = siteManager.getLock(transaction, variableIndex, LockType.READ);
            int lockAcquired = siteLock.value;
            System.out.println("######### " + lockAcquired);
            if(lockAcquired == LockStatus.GOT_LOCK.getLockStatus() || lockAcquired == LockStatus.GOT_LOCK_RECOVERING.getLockStatus()){
                resourceAllocationGraph.addGetLockEdge(variable,transactionId);
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

    private void addToWaitingQueue(String variable, Transaction transaction, LockType lockType, int value) {
        if(transaction.transactionStatus != TransactionStatus.WAITING) {
            if (waitingTransactionMap.containsKey(variable)) {
                waitingTransactionMap.get(variable).add(new Pair(new Lock(transaction, lockType), value));
            } else {
                Queue<Pair<Lock, Integer>> queue = new LinkedList<>();
                queue.add(new Pair(new Lock(transaction, lockType), value));
                waitingTransactionMap.put(variable, queue);
            }
            transaction.setTransactionStatus(TransactionStatus.WAITING);
        }
    }

    public void endTransaction(String transactionId){
        //System.out.println(transactionId);
        boolean isCommitted = commitTransaction(transactionId);
        this.clearLocks(transactionId);
        if(isCommitted){
            System.out.println(transactionId+" commits");
        }else{
            System.out.println(transactionId+" aborts");
        }
    }

    public void clearLocks(String transactionId){
        Transaction transaction = this.transactionMap.get(transactionId);
        for(Pair<Site, Integer> siteAccessed : transaction.sitesAccessed){
            for(String variable : transaction.variablesAccessed){
                Queue<Lock> locks = this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.locks.getOrDefault(variable, new LinkedList<>());
                Queue<Lock> ans = new LinkedList<>();
                for(Lock lock : locks){
                 //   System.out.println("Locks : " + transactionId + " " + lock.transactionId +" "+ lock.lockType);
                    if(!lock.transactionId.equals(transaction.name)) {
                        ans.add(lock);
                    }
                }
                this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.locks.put(variable, ans);
            }
        }
    }

    /*
    public void clearLocks(String transactionId){
        Transaction transaction = this.transactionMap.get(transactionId);
        for(Pair<Site, Integer> siteAccessed : transaction.sitesAccessed){
            for(String variable : transaction.variablesAccessed){
                Queue<Lock> locks = this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.locks.getOrDefault(variable, new LinkedList<>());
                Queue<Lock> ans = new LinkedList<>();
                for(Lock lock : locks){
                    if(!lock.transactionId.equals(transaction.name)) ans.add(lock);
                }
                this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.locks.put(variable, ans);
            }
        }
    }
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
            resourceAllocationGraph.addGetLockEdge(variable,transactionId);
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
            transaction.variablesAccessed.add(variable);
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
    public void clearAbortedTransactions(Map<String, Transaction> transactionMap){
        for(String str : transactionMap.keySet()){
            Transaction transaction = transactionMap.get(str);
            if(transaction.transactionStatus == TransactionStatus.ABORTED) {
                clearLocks(str);
            }
        }
    }

    public void tryWaitingReadOnly(){
        for(Pair<Transaction, String> pair : this.waitingReadOnly){
            Transaction transaction = pair.key;
            String variable = pair.value;
            readRequest(transaction.name, currentTimeStamp, variable);
        }
    }

    public void tryWaitingTransactions(){
        //System.out.println(waitingTransactionMap.keySet());
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
                flag &= this.writeRequest(lock.transaction.name, variable, queue.peek().value);
            }
            //System.out.println(flag +" "+ lock.transaction.uncommittedVariables);
            if(flag){
                queue.poll();
                if(!queue.isEmpty()){
                    waitingTransactionMap.replace(variable, queue);
                }
                //else waitingTransactionMap.remove(variable);
            }
        }
    }

    public void tick(Instruction currentInstr){
        resourceAllocationGraph.detectDeadlock(transactionMap);
        clearAbortedTransactions(transactionMap);
        tryWaitingReadOnly();
        tryWaitingTransactions();
        //System.out.println(currentInstr.transactionType);
        if(currentInstr.transactionType == TransactionType.begin){
            this.beginTransaction(currentInstr.transactionId, currentInstr.timestamp);
        }else if(currentInstr.transactionType == TransactionType.beginRO){
            this.beginROTransaction(currentInstr.transactionId, currentInstr.timestamp);
        }else if(currentInstr.transactionType == TransactionType.R){
            resourceAllocationGraph.addRequestLockEdge(currentInstr.transactionId,currentInstr.variableName);
            this.readRequest(currentInstr.transactionId, currentInstr.timestamp, currentInstr.variableName);
        }else if(currentInstr.transactionType == TransactionType.W){
            resourceAllocationGraph.addRequestLockEdge(currentInstr.transactionId,currentInstr.variableName);
            this.writeRequest(currentInstr.transactionId, currentInstr.variableName, currentInstr.value);
        }else if(currentInstr.transactionType == TransactionType.end){
            this.endTransaction(currentInstr.transactionId);
        }else {
            this.siteManager.tick(currentInstr, currentTimeStamp);
        }
        currentTimeStamp++;
    }


    public void checkDeadlock(){




        
    }

    public void printVariableValue(String variable, int value){
        System.out.println(variable+": "+value);
    }

}
