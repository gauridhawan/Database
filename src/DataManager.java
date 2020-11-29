import java.util.HashMap;

public class DataManager {
    LockTable lockTable;
    int index;
    HashMap<String, Variable> variableMap = new HashMap<>();

    DataManager(int index){
        this.index = index;
        this.lockTable = new LockTable();
        for(int i = 1; i<=20; i++){
            if(i%2 == 0 || (1 + i%10) == index){
                String name = "x" + i;
                variableMap.put(name, new Variable(name, 10 * i, index));
            }
        }
    }

    public void addVariable(String name, Variable variable){
        variableMap.put(name, variable);
    }

    public Variable getVariable(String name){
        if(variableMap.containsKey(name)) return variableMap.get(name);
        else return null;
    }

    public boolean hasVariable(String name){
        if(variableMap.containsKey(name)) return true;
        return false;
    }

    public void clearLock(Lock lock, Variable variable){
        this.lockTable.clearVariableLock(variable, lock);
    }

    public LockTable getLockTable(){
        return this.lockTable;
    }

    public boolean getLockOnVariable(Transaction transaction, Variable variable, LockType lockType){
        LockTable temp = this.lockTable;
        boolean isLocked = temp.isVariableLockedByTransaction(variable, transaction);
        //System.out.println(isLocked +" "+temp.numberOfLocks(variable));
        if(isLocked){
            if(temp.isVariableWriteLocked(variable)) return true;
            if(temp.numberOfLocks(variable) == 1){
                if(!temp.isVariableWaiting(variable)) {
                    this.lockTable.setLock(transaction, variable, lockType);
                    return true;
                }
                else{
                    if(temp.containsInWaiting(transaction,variable,lockType)){
                        this.lockTable.setLock(transaction, variable, lockType);
                        this.lockTable.removeFromWaiting(transaction, variable, lockType);
                        return true;
                    }
                    else{
                        this.lockTable.addLock(transaction,variable,lockType);
                        return false;
                    }
                }
            }
            else {
                this.lockTable.addLock(transaction,variable,lockType);
                return false;
            }
        }
        else if(lockType == LockType.WRITE && !temp.isVariableLocked(variable)){
            if(!temp.isVariableWaiting(variable)) {
                this.lockTable.setLock(transaction, variable, lockType);
                return true;
            }
            else{
                if(temp.containsInWaiting(transaction,variable,lockType)){
                    this.lockTable.setLock(transaction, variable, lockType);
                    this.lockTable.removeFromWaiting(transaction, variable, lockType);
                    return true;
                }
                else{
                    this.lockTable.addLock(transaction,variable,lockType);
                    return false;
                }
            }
        }
        else if(lockType == LockType.READ && !temp.isVariableWriteLocked(variable)){
            //System.out.println(this.lockTable.locks);
            if(!temp.isVariableWaiting(variable)) {
                //System.out.println("got lock : " + transaction.name);
                this.lockTable.setLock(transaction, variable, lockType);
                return true;
            }
            else{
                if(temp.containsInWaiting(transaction,variable,lockType)){
                    this.lockTable.setLock(transaction, variable, lockType);
                    this.lockTable.removeFromWaiting(transaction, variable, lockType);
                    return true;
                }
                else{
                    this.lockTable.addLock(transaction,variable,lockType);
                    return false;
                }
            }
        }
        // TODO: FIX THIS
        else{
            this.lockTable.addLock(transaction, variable, lockType);

            //System.out.println("Transaction " + transaction.name + " did not get " + lockType +" on Variable " + variable.name + " on Site " + this.index);
            return false;
        }
    }

    public void writeValueToVariable(Transaction transaction, Variable variable, int value){
        //if(this.lockTable.isVariableLockedByTransaction(variable, transaction, LockType.WRITE)){
            //System.out.println("DataManager, writeValueToVariable -> " + value);
            Variable temp = this.variableMap.get(variable.name);
            temp.value = value;
            this.variableMap.replace(variable.name, temp);
            //return true;
        //}
        //return false;
    }

}
