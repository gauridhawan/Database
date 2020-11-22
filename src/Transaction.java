import java.util.List;
import java.util.Map;

public class Transaction {

    String name;
    TransactionStatus transactionStatus;
    List<Site> siteList;
    Map<String, Integer> uncommittedVariables;
    boolean isReadOnly;


    public Map<String, Integer> getUncommittedVariables() {
        return uncommittedVariables;
    }

    public void setUncommittedVariables(Map<String, Integer> uncommittedVariables) {
        this.uncommittedVariables = uncommittedVariables;
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

    public List<Site> getSiteList() {
        return siteList;
    }

    public void setSiteList(List<Site> siteList) {
        this.siteList = siteList;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }
}
