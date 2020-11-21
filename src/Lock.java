public class Lock {
    LockType lockType;
    Transaction transaction;

    Lock(Transaction transaction, LockType lockType){
        this.lockType = lockType;
        this.transaction = transaction;
    }
}
