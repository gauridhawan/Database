public enum LockStatus {
    ALL_SITES_DOWN(0), NO_LOCK(1), GOT_LOCK(2), GOT_LOCK_RECOVERING(3);
    private int lockStatus;

    LockStatus(int lockStatus) {
        this.lockStatus = lockStatus;
    }

    public int getLockStatus(){
        return lockStatus;
    }
}
