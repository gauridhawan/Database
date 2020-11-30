import java.util.*;

/*
    Author : Kunal Khatri, Gauri Dhawan
    This is the class which represents each site
    Date : December 29
    Side Effects: None
 */
public class Site {
    int index;
    int lastFailedTime;
    SiteStatus siteStatus;
    DataManager dataManager;
    HashSet<String> recoveredVariables = new HashSet<>();

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This is the constructor for a site
        Inputs: index of the site and the sitestatus
        Output: void
        Date : December 29
        Side Effects: None
     */
    Site(int index, SiteStatus siteStatus){
        this.index = index;
        this.siteStatus = siteStatus;
        this.dataManager = new DataManager(index);
        HashMap<String,Variable> temp = this.dataManager.variableMap;
        for(String str : temp.keySet()){
            recoveredVariables.add(str);
        }
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This is the constructor for a site
        Inputs: index of the site
        Output: void
        Date : December 29
        Side Effects: sets the status of site to UP
     */
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
    /*
        Author : Kunal Khatri, Gauri Dhawan
        This returns the last time at which a site failed
        Inputs: Void
        Output: the time at which site failed
        Date : December 29
        Side Effects: None
     */
    public int getLastFailedTime() {
        return lastFailedTime;
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This method tells whether a transaction got a lock on a variable of not
        Inputs: Transaction, Variable and locktype
        Output: yes/no for lock
        Date : December 29
        Side Effects: Gives the lock or puts lock in waiting queue
     */
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


    /*
        Author : Kunal Khatri, Gauri Dhawan
        This writes the variable to that site
        Inputs: transaction, variable and value to be written
        Output: true if write was successful, no otherwise
        Date : December 29
        Side Effects: None
     */
    public boolean writeVariable(Transaction transaction, Variable variable, int value){
        //System.out.println(siteStatus +" "+ variable.name);
        // removing check on recovered variables since it is only required in the case of read request
        // recoveredVariables.contains(variable.name)
        if(this.siteStatus != SiteStatus.DOWN){
            //System.out.println("inside");
            this.dataManager.writeValueToVariable(transaction, variable, value);
            this.recoveredVariables.add(variable.name);
            //System.out.println(this.dataManager.getVariable(variable.name).value);
            return true;
        }
        return false;
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This sets the site status to DOWN
        Inputs: time at which site failed
        Output: void
        Date : December 29
        Side Effects: None
     */
    public void failSite(int time){
        this.siteStatus = SiteStatus.DOWN;
        this.recoveredVariables = new HashSet<>();
        this.lastFailedTime = time;
        LockTable temp = this.dataManager.getLockTable();
        HashMap<String, Queue<Lock>> locks= temp.locks;
        temp.locks.clear();
        //for(String variable : locks.keySet()){
        //    Queue<Lock> queue = locks.get(variable);
        //    for(Lock lock: queue){
                //lock.transaction.setTransactionStatus(TransactionStatus.ABORTED);
        //        System.out.println("aborting " + lock.transaction.name);
        //    }
        //}
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This recovers the failed site, i.e, sets site status to RECOVERING
        Inputs: Void
        Output: void
        Date : December 29
        Side Effects: None
     */
    public void recover(){
        for(String string : this.dataManager.variableMap.keySet()){
            int index = Integer.parseInt(string.substring(1));
            if(index % 2 == 1){
                this.recoveredVariables.add(string);
            }
        }
        this.siteStatus = SiteStatus.RECOVERING;
        //System.out.println("Recovered Vairables : " + this.recoveredVariables);
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This returns all the variables present at that site
        Inputs: Void
        Output: list of variables
        Date : December 29
        Side Effects: None
     */
    public List<Variable> getAllVariables(){
        List<Variable> lst = new ArrayList<>();
        for(String str : this.dataManager.variableMap.keySet()){
            lst.add(this.dataManager.variableMap.get(str));
        }
        return lst;
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This dumps the site
        Inputs: Void
        Output: void
        Date : December 29
        Side Effects: Prints each variable and its value at the site
     */
    public void dumpSite(){
        System.out.print("site " + this.index + "- ");
        TreeMap<Integer, String> variableValuesMap = new TreeMap<>();
        for(String varName : this.dataManager.variableMap.keySet()){
            Variable variable = this.dataManager.variableMap.get(varName);
            variableValuesMap.put(variable.index, varName+":"+variable.value);
        }
        for(Integer index : variableValuesMap.keySet()){
            System.out.print(variableValuesMap.get(index) +" ");
        }
        System.out.print("\n");
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This returns the datamanager of the site
        Inputs: Void
        Output: datamanager
        Date : December 29
        Side Effects: None
     */
    public DataManager getDataManager() {
        return dataManager;
    }

}
