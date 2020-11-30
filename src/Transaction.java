import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model class for transaction
 * Author : Gauri Dhawan, Kunal Khatri
 */
public class Transaction {

    String name;
    TransactionStatus transactionStatus;
    List<Pair<Site,Integer>> sitesAccessed = new ArrayList<>();
    List<String> variablesAccessed = new ArrayList<>();
    Map<String, Pair<Integer, List<Site>>> uncommittedVariables = new HashMap<>();
    int startTime;
    boolean isReadOnly;
    Map<String, Integer> committedValues = new HashMap<>();


    public Map<String, Pair<Integer, List<Site>>> getUncommittedVariables() {
        return uncommittedVariables;
    }

    public void setUncommittedVariables(Map<String, Pair<Integer, List<Site>>> uncommittedVariables) {
        this.uncommittedVariables = uncommittedVariables;
    }



    /*
    Getters and Setters for transaction class
    Authors: Kunal Khatri, Gauri Dhawan
    Date: November 29
     */
    public Map<String, Integer> getCommittedValues() {
        return committedValues;
    }

    public void setCommittedValues(Map<String, Integer> committedValues) {
        this.committedValues = committedValues;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public List<Pair<Site,Integer>> getSitesAccessed() {
        return sitesAccessed;
    }

    public void setSitesAccessed(List<Pair<Site,Integer>> sitesAccessed) {
        this.sitesAccessed = sitesAccessed;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }
}
