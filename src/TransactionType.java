public enum TransactionType {
    begin("begin"),
    beginRO("beginRO"),
    W("W"),
    R("R"),
    fail("fail"),
    recover("recover"),
    dump("dump"),
    end("end");

    private String transactionType;

    TransactionType(String transactionType) {

        this.transactionType = transactionType;
    }

    public String getTransactionType(){
        return transactionType;
    }
}
