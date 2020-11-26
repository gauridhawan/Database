import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transaction {

    String name;
    TransactionStatus transactionStatus;
    List<Site> sitesAccessed = new ArrayList<>();
    Map<String, Integer> uncommittedVariables = new HashMap<>();
    int startTime;
    boolean isReadOnly;
    Map<String, Integer> committedValues = new HashMap<>();


    public Map<String, Integer> getUncommittedVariables() {
        return uncommittedVariables;
    }

    public void setUncommittedVariables(Map<String, Integer> uncommittedVariables) {
        this.uncommittedVariables = uncommittedVariables;
    }

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

    public List<Site> getSitesAccessed() {
        return sitesAccessed;
    }

    public void setSitesAccessed(List<Site> sitesAccessed) {
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
