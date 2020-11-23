import java.util.HashMap;
import java.util.Map;


public class TransactionManager {

    int numberOfSites = 10;
    int numberOfVariables;
    Map<String,Transaction> transactionMap = new HashMap<>();
    int currentTimeStamp;
    SiteManager siteManager;
    /*
    * If the transaction is valid, commit the uncommitted variables
    * Check if the transaction goes through using the condition - if any of the servers that we've accessed has gone down,
    * then it should abort (Not for readonly)
    * Otherwise the transaction can go ahead
    * */
    public boolean commitTransaction(String transactionId){

        Transaction transaction = transactionMap.get(transactionId);
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
                    Site siteToBeUpdated = siteManager.getSiteMap().get(i);
                    // TODO check if this is correct
                    siteToBeUpdated.writeVariable(uncommittedVariables.get(variable));
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

    }

    public void writeRequest(String transactionId, String variable, int value){
        
    }




}
