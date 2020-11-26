
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;


public class TransactionManager {

    int numberOfSites = 10;
    int numberOfVariables = 20;
    Map<String,Transaction> transactionMap = new HashMap<>();
    Map<String, List<Pair<String,Integer>>> transactionWritePermission = new HashMap<>();
    Map<String, Queue<Lock>> waitingTransactionMap = new HashMap<>();
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
                System.out.println(transactionId +" "+ siteAccessed.key.lastFailedTime +" "+siteAccessed.value);
                if(siteAccessed.key.getLastFailedTime() > siteAccessed.value){
                    // transaction cannot go forward since it failed since the transaction started
                    //System.out.println(transactionId +" "+siteAccessed.getLastFailedTime()+" "+transaction.getStartTime());
                    transaction.setTransactionStatus(TransactionStatus.ABORTED);
                    return false;
                }
            }
        }

        Map<String,Pair<Integer,List<Site>>> uncommittedVariables = transaction.getUncommittedVariables();
        System.out.println(uncommittedVariables);
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
    public void readRequest(String transactionId, int timestamp, String variable){

        Transaction transaction = transactionMap.get(transactionId);
        int variableIndex = Integer.parseInt(variable.substring(1));

        if(transaction.isReadOnly()){
            Map<String, Integer> variableValueAtTransactionStart = transaction.getCommittedValues();
            System.out.println(variableValueAtTransactionStart);
            if(variableValueAtTransactionStart.containsKey(variable)){
                printVariableValue(variable, variableValueAtTransactionStart.get(variable));
            }else{
                transaction.setTransactionStatus(TransactionStatus.WAITING);
            }

        }else{
            int lockAcquired = siteManager.getLock(transaction, variableIndex, LockType.READ);
            System.out.println("######### " + lockAcquired);
            if(lockAcquired == LockStatus.GOT_LOCK.getLockStatus()){
                resourceAllocationGraph.addGetLockEdge(variable,transactionId);
                Pair<Site, Integer> variableSitePair = siteManager.getVariableValues().get("x"+variableIndex);
                int variableValue = variableSitePair.value;
                Site site = variableSitePair.key;
                transaction.sitesAccessed.add(new Pair<>(site, currentTimeStamp));
                printVariableValue("x"+variableIndex, variableValue);

            }
            else{
                addToWaitingQueue(variable, transaction, LockType.READ);
            }
        }

    }

    private void addToWaitingQueue(String variable, Transaction transaction, LockType lockType) {
        if(transaction.transactionStatus != TransactionStatus.WAITING) {
            if (waitingTransactionMap.containsKey(variable)) {
                waitingTransactionMap.get(variable).add(new Lock(transaction, lockType));
            } else {
                Queue<Lock> queue = new LinkedList<>();
                queue.add(new Lock(transaction, LockType.READ));
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
                Queue<Lock> locks = this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.locks.get(variable);
                Queue<Lock> ans = new LinkedList<>();
                for(Lock lock : locks){
                    if(lock.transaction.name != transaction.name) ans.add(lock);
                }
                this.siteManager.getSite(siteAccessed.key.index).dataManager.lockTable.locks.put(variable, ans);
            }
        }
    }

    public void writeRequest(String transactionId, String variable, int value){
        int variableIndex = Integer.parseInt(variable.substring(1));
        Transaction transaction = transactionMap.get(transactionId);

        if (siteManager.getLock(transaction,variableIndex,LockType.WRITE) == LockStatus.GOT_LOCK.getLockStatus()){
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
        }
        else{
            addToWaitingQueue(variable,transaction,LockType.WRITE);
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

    public void tick(Instruction currentInstr){
        resourceAllocationGraph.detectDeadlock(transactionMap);
        clearAbortedTransactions(transactionMap);
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
