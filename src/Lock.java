/*
    Author : Kunal Khatri, Gauri Dhawan
    This class corresponds to a Lock that is used by the transactions
    Date : December 29
 */

/**
 * Model class for lock
 */
public class Lock {
    LockType lockType;
    Transaction transaction;
    String transactionId;

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This is the constructor of the lock class
        Inputs: Transaction, LockType
        Output: Void
        Date : December 29
        Side Effects: None
     */
    Lock(Transaction transaction, LockType lockType){
        this.lockType = lockType;
        this.transaction = transaction;
        this.transactionId = transaction.name;
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This is the constructor of the lock class
        Inputs: Transaction Name, LockType
        Output: Void
        Date : December 29
        Side Effects: None
     */
    Lock(String transactionId, LockType lockType){
        this.transactionId = transactionId;
        this.lockType = lockType;
    }

    /*
        Author : Kunal Khatri, Gauri Dhawan
        This is the toString of the lock class
        Inputs: None
        Output: String
        Date : December 29
        Side Effects: None
     */
    public String toString(){
        return "{ " + this.transactionId +" , " + this.lockType +" }";
    }
}
