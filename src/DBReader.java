public class DBReader {

    String filePath;
    boolean isStdin;

    public DBReader(String filePath, boolean isStdin) {
        this.filePath = filePath;
        this.isStdin = isStdin;
    }

    public Instruction getNextInstruction(String line){
        String[] str = line.split("[\\(\\)]");
        Instruction instr = new Instruction();
        String transactionType = str[0];
        if(transactionType.equalsIgnoreCase(TransactionType.begin.getTransactionType()) ||
                transactionType.equalsIgnoreCase(TransactionType.beginRO.getTransactionType())){
            instr.transactionType = TransactionType.begin;
            if(transactionType.equalsIgnoreCase(TransactionType.beginRO.getTransactionType())){
                instr.transactionType = TransactionType.beginRO;
            }
            String[] values = str[1].split(",");
            instr.transactionId = values[0];
        }else if(transactionType.equalsIgnoreCase(TransactionType.W.getTransactionType())){
            instr.transactionType = TransactionType.W;
            String[] values = str[1].split(",");
            instr.transactionId = values[0];
            instr.variableName = values[1];
            instr.value = Integer.parseInt(values[2]);
        }else if(transactionType.equalsIgnoreCase(TransactionType.R.getTransactionType())){
            instr.transactionType = TransactionType.R;
            String[] values = str[1].split(",");
            instr.transactionId = values[0];
            instr.variableName = values[1];
        }
    }
}
