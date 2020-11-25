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
        boolean isLocked = temp.isVariableLockedByTransaction(variable, transaction, lockType);
        if(isLocked){
            if(temp.numberOfLocks(variable) == 1){
                this.lockTable.setLock(transaction, variable, lockType);
                return true;
            }
            else return false;
        }
        else if(lockType == LockType.WRITE && !temp.isVariableLocked(variable)){
            this.lockTable.setLock(transaction, variable, lockType);
            return true;
        }
        else if(lockType == LockType.READ && !temp.isVariableWriteLocked(variable)){
            this.lockTable.setLock(transaction, variable, lockType);
            return true;
        }
        else{
            System.out.println("Transaction " + transaction.toString() + "did not get" + lockType.toString() +" on Variable " + variable.toString());
            return false;
        }
    }

    public void writeValueToVariable(Transaction transaction, Variable variable, int value){
        //if(this.lockTable.isVariableLockedByTransaction(variable, transaction, LockType.WRITE)){
            System.out.println("DataManager, writeValueToVariable -> " + value);
            Variable temp = this.variableMap.get(variable.name);
            temp.value = value;
            this.variableMap.replace(variable.name, temp);
            //return true;
        //}
        //return false;
    }

}
