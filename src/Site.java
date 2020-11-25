import java.util.*;

public class Site {
    int index;
    int lastFailedTime;
    SiteStatus siteStatus;
    DataManager dataManager;
    HashSet<Variable> recoveredVariables = new HashSet<>();

    Site(int index, SiteStatus siteStatus){
        this.index = index;
        this.siteStatus = siteStatus;
        this.dataManager = new DataManager(index);
    }

    Site(int index){
        this.index = index;
        this.siteStatus = SiteStatus.UP;
        this.dataManager = new DataManager(index);
    }
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getLastFailedTime() {
        return lastFailedTime;
    }

    public void setLastFailedTime(int lastFailedTime) {
        this.lastFailedTime = lastFailedTime;
    }

    public SiteStatus getSiteStatus() {
        return siteStatus;
    }

    public void setSiteStatus(SiteStatus siteStatus) {
        this.siteStatus = siteStatus;
    }

    public boolean getLock(Transaction transaction, Variable variable, LockType lockType){
        if(this.dataManager.getLockOnVariable(transaction, variable, lockType)){
            this.recoveredVariables.add(variable);

            if(recoveredVariables.size() == this.dataManager.variableMap.size()){
                this.siteStatus = SiteStatus.UP;
            }
            return true;
        }
        return false;
    }

    public void clearLock(Variable variable, Lock lock){
        this.dataManager.clearLock(lock, variable);
    }

    public boolean writeVariable(Transaction transaction, Variable variable, int value){
        if(this.siteStatus != SiteStatus.DOWN && recoveredVariables.contains(variable)){
            this.dataManager.writeValueToVariable(transaction, variable, value);
            return true;
        }
        return false;
    }

    public void failSite(){
        this.siteStatus = SiteStatus.DOWN;
        this.recoveredVariables = new HashSet<>();
        LockTable temp = this.dataManager.getLockTable();
        HashMap<Variable, Queue<Lock>> locks= temp.locks;
        for(Variable variable : locks.keySet()){
            Queue<Lock> queue = locks.get(variable);
            for(Lock lock: queue){
                lock.transaction.setTransactionStatus(TransactionStatus.ABORTED);
            }
        }
    }

    public void recover(){
        for(String string : this.dataManager.variableMap.keySet()){
            int index = Integer.parseInt(string.substring(1));
            if(index % 2 == 1){
                this.recoveredVariables.add(this.dataManager.variableMap.get(string));
            }
        }
        this.siteStatus = SiteStatus.RECOVERING;
    }

    public List<Variable> getAllVariables(){
        List<Variable> lst = new ArrayList<>();
        for(String str : this.dataManager.variableMap.keySet()){
            lst.add(this.dataManager.variableMap.get(str));
        }
        return lst;
    }

    //TODO : Complete this!
    public void dumpSite(){

    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }
}
