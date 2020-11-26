import java.util.*;

public class Site {
    int index;
    int lastFailedTime;
    SiteStatus siteStatus;
    DataManager dataManager;
    HashSet<String> recoveredVariables = new HashSet<>();

    Site(int index, SiteStatus siteStatus){
        this.index = index;
        this.siteStatus = siteStatus;
        this.dataManager = new DataManager(index);
        HashMap<String,Variable> temp = this.dataManager.variableMap;
        for(String str : temp.keySet()){
            recoveredVariables.add(str);
        }
    }

    Site(int index){
        this.index = index;
        this.siteStatus = SiteStatus.UP;
        this.dataManager = new DataManager(index);
        HashMap<String,Variable> temp = this.dataManager.variableMap;
        for(String str : temp.keySet()){
            recoveredVariables.add(str);
        }
        //System.out.println(recoveredVariables);
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
            this.recoveredVariables.add(variable.name);

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
        //System.out.println(siteStatus +" "+ variable.name);
        if(this.siteStatus != SiteStatus.DOWN && recoveredVariables.contains(variable.name)){
            //System.out.println("inside");
            this.dataManager.writeValueToVariable(transaction, variable, value);
            this.recoveredVariables.add(variable.name);
            System.out.println(this.dataManager.getVariable(variable.name).value);
            return true;
        }
        return false;
    }

    public void failSite(){
        this.siteStatus = SiteStatus.DOWN;
        this.recoveredVariables = new HashSet<>();
        LockTable temp = this.dataManager.getLockTable();
        HashMap<String, Queue<Lock>> locks= temp.locks;
        for(String variable : locks.keySet()){
            Queue<Lock> queue = locks.get(variable);
            for(Lock lock: queue){
                lock.transaction.setTransactionStatus(TransactionStatus.ABORTED);
                System.out.println("aborting " + lock.transaction.name);
            }
        }
    }

    public void recover(){
        for(String string : this.dataManager.variableMap.keySet()){
            int index = Integer.parseInt(string.substring(1));
            if(index % 2 == 1){
                this.recoveredVariables.add(string);
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
        System.out.print("site " + this.index + "- ");
        for(String varName : this.dataManager.variableMap.keySet()){
            System.out.print(varName+":"+this.dataManager.variableMap.get(varName).value+" ");
        }
        System.out.print("\n");
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }
}
