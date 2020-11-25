import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TransactionManager {

    int numberOfSites = 10;
    int numberOfVariables;
    Map<String,Transaction> transactionMap = new HashMap<>();
    Map<String, List<Pair<String,Integer>>> transactionWritePermission = new HashMap<>();
    int currentTimeStamp;
    SiteManager siteManager;
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
                    transaction.setTransactionStatus(TransactionStatus.ABORTED);
                    return false;
                }
            }
        }

        Map<String,Integer> uncommittedVariables = transaction.getUncommittedVariables();
        for(String variable : uncommittedVariables.keySet()){
            // Write the variable at their resp sites
            int variableIndex = Integer.parseInt(variable.substring(1));
            for(int i=0;i<numberOfSites;i++){
                if(variableIndex%2 == 0 || (variableIndex%10 + 1 == i)){
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

    public void beginROTransaction(String transactionId, int timestamp){
        Transaction transaction = createTransaction(transactionId, timestamp);
        transaction.setReadOnly(true);
        transactionMap.put(transactionId,transaction);
    }

    public void endTransaction(String transactionId){

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


    }

    /*
    * Check deadlock
    * Instruction as argument
    * check deadlock before each tick
    * if deadlock, abort youngest transaction
    * */
    public void tick(Instruction currentInstr){
        checkDeadlock();
        if(currentInstr.transactionType == TransactionType.begin){
            this.beginTransaction(currentInstr.transactionId, );
        }


    }

    public void checkDeadlock(){}

}
