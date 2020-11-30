import java.util.*;

/*
    Author : Kunal Khatri, Gauri Dhawan
    This is the locktable class which is local to each site
    Date : December 29
    Side Effects: None
 */
public class LockTable {
    HashMap<String, Queue<Lock>> locks = new HashMap<>();
    HashMap<String, Queue<Lock>> waitingLocks = new HashMap<>();

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This returns the number of locks on a variable
        Inputs: variable
        Output: the number of locks present on that variable
        Date : December 29
        Side Effects: None
     */
    public int numberOfLocks(Variable variable){
        if(locks.containsKey(variable.name)){
            return locks.get(variable.name).size();
        }
        else return 0;
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This returns if the transaction is waiting for a lock on a variable or not
        Inputs: transaction, variable and locktype
        Output: yes/no
        Date : December 29
        Side Effects: None
     */
    public boolean containsInWaiting(Transaction transaction, Variable variable, LockType lockType){
        Queue<Lock> existingLocks = waitingLocks.getOrDefault(variable.name, new LinkedList<>());
        if(existingLocks.size() == 0) return false;
        Lock lock = existingLocks.peek();
        if(lock.transactionId.equals(transaction.name) && lock.lockType.equals(lockType)){
            return true;
        }
        return false;
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This removes the transaction from the waiting queue of the variable
        Inputs: transaction, variable, locktype
        Output: void
        Date : December 29
        Side Effects: removes from the waiting queue
     */
    public void removeFromWaiting(Transaction transaction, Variable variable, LockType lockType){
        if(containsInWaiting(transaction, variable, lockType)) {

            Queue<Lock> existingLocks = waitingLocks.getOrDefault(variable.name, new LinkedList<>());
            //System.out.println("BEFORE:    " + existingLocks);
            existingLocks.poll();
            waitingLocks.put(variable.name, existingLocks);
            //System.out.println("AFTER:    " + existingLocks);
        }
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This sets a lock on a variable for a transaction
        Inputs: transaction, variable, locktype
        Output: void
        Date : December 29
        Side Effects: adds lock to the variable
     */
    public void setLock(Transaction transaction, Variable variable, LockType lockType){
        Lock lock = new Lock(transaction.name, lockType);
        Queue<Lock> existingLocks = locks.getOrDefault(variable.name, new LinkedList<>());
        for(Lock locks : existingLocks){
            if(locks.transactionId.equals(transaction.name) && lock.lockType.equals(lockType)) return;
        }
        existingLocks.add(lock);
        locks.put(variable.name,existingLocks);
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This adds lock to the waiting queue of the variable
        Inputs: transaction, variable, locktype
        Output: void
        Date : December 29
        Side Effects: adds lock to the waiting queue
     */
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

    /*
        Author : Kunal Khatri, Gauri Dhawan
        this checks if a variable is locked
        Inputs: variable
        Output: yes/no
        Date : December 29
        Side Effects: none
     */
    public boolean isVariableLocked(Variable variable){
        if(locks.containsKey(variable.name)){
            if(locks.get(variable.name).size() == 0) return false;
            return true;
        }
        return false;
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        this checks if there is a transaction waiting for a lock on a variable is locked
        Inputs: variable
        Output: yes/no
        Date : December 29
        Side Effects: none
     */
    public boolean isVariableWaiting(Variable variable){
        if(waitingLocks.containsKey(variable.name)){
            if(waitingLocks.get(variable.name).size() == 0) return false;
            return true;
        }
        return false;
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        this checks if a variable is write locked
        Inputs: variable
        Output: yes/no
        Date : December 29
        Side Effects: none
     */
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

    /*
        Author : Kunal Khatri, Gauri Dhawan
        this checks if a variable is read locked
        Inputs: variable
        Output: yes/no
        Date : December 29
        Side Effects: none
     */
    public boolean isVariableReadLocked(Variable variable){
        if(isVariableLocked(variable)){
            Lock lock = locks.get(variable.name).peek();
            if(lock.lockType == LockType.READ) return true;
            return false;
        }
        return false;
    }


    /*
        Author : Kunal Khatri, Gauri Dhawan
        this frees all the locks on a variable
        Inputs: variable
        Output: yes/no
        Date : December 29
        Side Effects: none
     */
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

    /*
        Author : Kunal Khatri, Gauri Dhawan
        this checks if the variable is locked by a transaction with a particular locktype
        Inputs: variable, transaction, locktype
        Output: boolean
        Date : December 29
        Side Effects: none
     */
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

    /*
        Author : Kunal Khatri, Gauri Dhawan
        this checks if the variable is locked by a transaction
        Inputs: variable, transaction
        Output: boolean
        Date : December 29
        Side Effects: none
     */
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
