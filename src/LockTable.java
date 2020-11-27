import java.util.*;

public class LockTable {
    HashMap<String, Queue<Lock>> locks = new HashMap<>();

    public int numberOfLocks(Variable variable){
        if(locks.containsKey(variable.name)){
            return locks.get(variable.name).size();
        }
        else return 0;
    }

    public boolean setLock(Transaction transaction, Variable variable, LockType lockType){
        Lock lock = new Lock(transaction.name, lockType);
        if(locks.containsKey(variable.name)){
            locks.get(variable.name).add(lock);
            return false;
        }
        else{
            Queue<Lock> tempQueue = new LinkedList<>();
            tempQueue.add(lock);
            locks.put(variable.name, tempQueue);
            return true;
        }
    }

    public boolean isVariableLocked(Variable variable){
        if(locks.containsKey(variable.name)){
            if(locks.get(variable.name).size() == 0) return false;
            return true;
        }
        return false;
    }

    public boolean isVariableWriteLocked(Variable variable){
        if(isVariableLocked(variable)){
            Lock lock = locks.get(variable.name).peek();
            if(lock.lockType == LockType.WRITE) return true;
            return false;
        }
        return false;
    }

    public boolean isVariableReadLocked(Variable variable){
        if(isVariableLocked(variable)){
            Lock lock = locks.get(variable.name).peek();
            if(lock.lockType == LockType.READ) return true;
            return false;
        }
        return false;
    }

    public void freeLocks(Variable variable){
        locks.remove(variable.name);
    }

    public boolean clearVariableLock(Variable variable, Lock lock){
        if(locks.containsKey(variable.name)){
            Queue<Lock> tempQueue = locks.get(variable.name);
            tempQueue.remove(lock);
            if(tempQueue.size() == 0) locks.remove(variable.name);
            else locks.replace(variable.name, tempQueue);
            return true;
        }
        return false;
    }

    public boolean isVariableLockedByTransaction(Variable variable, Transaction transaction, LockType lockType){
        if(locks.containsKey(variable.name)){
            Queue<Lock> tempQueue = locks.get(variable.name);
            for(Lock lock : tempQueue){
                if(lock.transactionId == transaction.name && lock.lockType == lockType) return true;
            }
            return false;
        }
        return false;
    }
}
