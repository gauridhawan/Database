
import com.sun.tools.javac.util.Pair;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TransactionManager {

    int numberOfSites = 10;
    int numberOfVariables = 20;
    Map<String,Transaction> transactionMap = new HashMap<>();
    Map<String, List<Pair<String,Integer>>> transactionWritePermission = new HashMap<>();
    int currentTimeStamp;
    SiteManager siteManager = new SiteManager(numberOfSites, numberOfVariables);
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
            for(Site siteAccessed : transaction.getSitesAccessed()){
                if(siteAccessed.getLastFailedTime() > transaction.getStartTime()){
                    // transaction cannot go forward since it failed since the transaction started
                    //System.out.println(transactionId +" "+siteAccessed.getLastFailedTime()+" "+transaction.getStartTime());
                    transaction.setTransactionStatus(TransactionStatus.ABORTED);
                    return false;
                }
            }
        }

        Map<String,Integer> uncommittedVariables = transaction.getUncommittedVariables();
        System.out.println(uncommittedVariables);
        for(String variable : uncommittedVariables.keySet()){
            // Write the variable at their resp sites
            int variableIndex = Integer.parseInt(variable.substring(1));
            for(int i=1;i<=numberOfSites;i++){
                if(variableIndex%2 == 0 || (variableIndex%10 + 1 == i)){
                    //System.out.println("updating variables on commit");
                    Site siteToBeUpdated = siteManager.getSite(i);
                    Variable var = siteToBeUpdated.getDataManager().getVariable(variable);
                    // TODO check if this is correct
                    siteToBeUpdated.writeVariable(transaction,var,uncommittedVariables.get(variable));
                }
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
        HashMap<String, Integer> variableValueMap = this.siteManager.getVariableValues();
        transaction.setCommittedValues(variableValueMap);
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
            if(variableValueAtTransactionStart.containsKey(variable)){
                printVariableValue(variable, variableValueAtTransactionStart.get(variable));
            }else{
                transaction.setTransactionStatus(TransactionStatus.WAITING);
            }

        }else{
            int lockAcquired = siteManager.getLock(transaction, variableIndex, LockType.READ);
            System.out.println("######### " + lockAcquired);
            if(lockAcquired == LockStatus.GOT_LOCK.getLockStatus()){
                int variableValue = siteManager.getVariableValues().get("x"+variableIndex);
                System.out.println(variableValue);
            }
        }


    }

    public void endTransaction(String transactionId){
        //System.out.println(transactionId);
        boolean isCommitted = commitTransaction(transactionId);
        if(isCommitted){
            System.out.println(transactionId+" commits");
        }else{
            System.out.println(transactionId+" aborts");
        }

    }

    public void writeRequest(String transactionId, String variable, int value){

        int variableIndex = Integer.parseInt(variable.substring(1));
        Transaction transaction = transactionMap.get(transactionId);

        if (siteManager.getLock(transaction,variableIndex,LockType.WRITE) == LockStatus.GOT_LOCK.getLockStatus()){
            Map<String, Integer> uncommittedVars =  transaction.getUncommittedVariables();
            uncommittedVars.put(variable,value);
        }
        else{
            transaction.setTransactionStatus(TransactionStatus.WAITING);
        }


    }

    /*
    * Check deadlock
    * Instruction as argument
    * check deadlock before each tick
    * if deadlock, abort youngest transaction
    * */
    public void tick(Instruction currentInstr){
        checkDeadlock();
        //System.out.println(currentInstr.transactionType);
        if(currentInstr.transactionType == TransactionType.begin){
            this.beginTransaction(currentInstr.transactionId, currentInstr.timestamp);
        }else if(currentInstr.transactionType == TransactionType.beginRO){
            this.beginROTransaction(currentInstr.transactionId, currentInstr.timestamp);
        }else if(currentInstr.transactionType == TransactionType.R){
            this.readRequest(currentInstr.transactionId, currentInstr.timestamp, currentInstr.variableName);
        }else if(currentInstr.transactionType == TransactionType.W){
            this.writeRequest(currentInstr.transactionId, currentInstr.variableName, currentInstr.value);
        }else if(currentInstr.transactionType == TransactionType.end){
            this.endTransaction(currentInstr.transactionId);
        }else {
            this.siteManager.tick(currentInstr);
        }

    }


    public void checkDeadlock(){

        
    }

    public void printVariableValue(String variable, int value){
        System.out.println(variable+": "+value);
    }

}
