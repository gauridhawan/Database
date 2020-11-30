/**
 * Class for instruction containing all details of an instruction
 * @author Gauri Dhawan, Kunal Khatri
 */
public class Instruction {

    TransactionType transactionType;
    String variableName;
    String transactionId;
    int value;
    int site;
    int timestamp;

    /**
     * Constructor for instruction class
     * @author Gauri Dhawan, Kunal Khatri
     * @param transactionType
     */
    Instruction(TransactionType transactionType){
        this.transactionType = transactionType;
    }

    /**
     * Constructor for instruction class
     * @param transactionType
     * @param variableName
     * @param value
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    Instruction(TransactionType transactionType, String variableName, int value){
        this.transactionType = transactionType;
        this.variableName = variableName;
        this.value = value;
    }

    /**
     * Constructor for instruction class
     * @param site
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    Instruction(int site){
        this.site = site;
    }

    /**
     * Constructor for instruction class
     * @author Gauri Dhawan, Kunal Khatri
     * @param type
     * @param id
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    Instruction(TransactionType type, String id){
        this.transactionType = type;
        this.transactionId = id;
    }

    /**
     * Constructor for instruction class
     * @author Gauri Dhawan, Kunal Khatri
     * @author Gauri Dhawan, Kunal Khatri
     * Date : November 29
     * side-effects : none
     */
    public Instruction() {
    }

    /*
    Getter and Setters for Instruction class attributes
    Authors: Kunal Khatri, Gauri Dhawan
    Date: November 29
     */
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
