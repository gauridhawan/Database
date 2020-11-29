public class Lock {
    LockType lockType;
    Transaction transaction;
    String transactionId;

    Lock(Transaction transaction, LockType lockType){
        this.lockType = lockType;
        this.transaction = transaction;
        this.transactionId = transaction.name;
    }
    Lock(String transactionId, LockType lockType){
        this.transactionId = transactionId;
        this.lockType = lockType;
    }

    public String toString(){
        return "{ " + this.transactionId +" , " + this.lockType +" }";
    }
}
