public class Lock {
    LockType lockType;
    Transaction transaction;

    Lock(LockType lockType){
        this.lockType = lockType;
    }
}
