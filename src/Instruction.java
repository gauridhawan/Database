public class Instruction {

    TransactionType transactionType;
    String variableName;
    String transactionId;
    int value;
    int site;
    int timestamp;

    Instruction(TransactionType transactionType){
        this.transactionType = transactionType;
    }

    Instruction(TransactionType transactionType, String variableName, int value){
        this.transactionType = transactionType;
        this.variableName = variableName;
        this.value = value;
    }

    Instruction(int site){
        this.site = site;
    }

    Instruction(TransactionType type, String id){
        this.transactionType = type;
        this.transactionId = id;
    }

    public Instruction() {
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getSite() {
        return site;
    }

    public void setSite(int site) {
        this.site = site;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }
}
