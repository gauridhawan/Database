import java.util.*;

public class LockTable {
    HashMap<String, Queue<Lock>> locks = new HashMap<>();
    HashMap<String, Queue<Lock>> waitingLocks = new HashMap<>();

    public int numberOfLocks(Variable variable){
        if(locks.containsKey(variable.name)){
            return locks.get(variable.name).size();
        }
        else return 0;
    }

    public boolean containsInWaiting(Transaction transaction, Variable variable, LockType lockType){
        Queue<Lock> existingLocks = waitingLocks.getOrDefault(variable.name, new LinkedList<>());
        if(existingLocks.size() == 0) return false;
        Lock lock = existingLocks.peek();
        if(lock.transactionId.equals(transaction.name) && lock.lockType.equals(lockType)){
            return true;
        }
        return false;
    }

    public void removeFromWaiting(Transaction transaction, Variable variable, LockType lockType){
        if(containsInWaiting(transaction, variable, lockType)) {

            Queue<Lock> existingLocks = waitingLocks.getOrDefault(variable.name, new LinkedList<>());
            //System.out.println("BEFORE:    " + existingLocks);
            existingLocks.poll();
            waitingLocks.put(variable.name, existingLocks);
            //System.out.println("AFTER:    " + existingLocks);
        }
    }

    public void setLock(Transaction transaction, Variable variable, LockType lockType){
        Lock lock = new Lock(transaction.name, lockType);
        Queue<Lock> existingLocks = locks.getOrDefault(variable.name, new LinkedList<>());
        for(Lock locks : existingLocks){
            if(locks.transactionId.equals(transaction.name) && lock.lockType.equals(lockType)) return;
        }
        existingLocks.add(lock);
        locks.put(variable.name,existingLocks);
    }

    public void addLock(Transaction transaction, Variable variable, LockType lockType){
        Lock lock = new Lock(transaction.name, lockType);
        Queue<Lock> waiting = waitingLocks.getOrDefault(variable.name, new LinkedList<>());
        //System.out.println("BEFORE:    " + waiting);
        for(Lock locks : waiting){
            if(locks.transactionId.equals(transaction.name) && lock.lockType.equals(lockType)) return;
        }
        waiting.add(lock);
        //System.out.println("AFTER:    " + waiting);
        waitingLocks.put(variable.name,waiting);
    }

    public boolean isVariableLocked(Variable variable){
        if(locks.containsKey(variable.name)){
            if(locks.get(variable.name).size() == 0) return false;
            return true;
        }
        return false;
    }

    public boolean isVariableWaiting(Variable variable){
        if(waitingLocks.containsKey(variable.name)){
            if(waitingLocks.get(variable.name).size() == 0) return false;
            return true;
        }
        return false;
    }

    public boolean isVariableWriteLocked(Variable variable){
        if(isVariableLocked(variable)){
            Queue<Lock> queue = locks.get(variable.name);
            for(Lock lock : queue) {
                if (lock.lockType.equals(LockType.WRITE)) return true;
            }
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
                if(lock.transactionId.equals(transaction.name)) return true;
            }
            return false;
        }
        return false;
    }

    public boolean isVariableLockedByTransaction(Variable variable, Transaction transaction){
        if(locks.containsKey(variable.name)){
            Queue<Lock> tempQueue = locks.get(variable.name);
            for(Lock lock : tempQueue){
                if(lock.transactionId.equals(transaction.name)) return true;
            }
            return false;
        }
        return false;
    }
}
