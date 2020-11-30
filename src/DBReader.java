public class DBReader {

    String filePath;
    boolean isStdin;

    public DBReader(String filePath, boolean isStdin) {
        this.filePath = filePath;
        this.isStdin = isStdin;
    }

    /**
     * Method to read the line in input file and extract values from it to create an Instruction object
     * @param line provided in the input
     * @return an Instruction object
     * @author Gauri Dhawan, Kunal Khatri
     * @side-effects None
     */
    public Instruction getNextInstruction(String line){
        String[] str = line.split("[\\(\\)]");
        Instruction instr = new Instruction();
        String transactionType = str[0];
        //System.out.println("DBReader class, getNextInstruction method -> " + transactionType);
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
            instr.transactionId = values[0].trim();
            instr.variableName = values[1].trim();
            instr.value = Integer.parseInt(values[2].trim());
        }else if(transactionType.equalsIgnoreCase(TransactionType.R.getTransactionType())){
            instr.transactionType = TransactionType.R;
            String[] values = str[1].split(",");
            instr.transactionId = values[0].trim();
            instr.variableName = values[1].trim();
        }else if(transactionType.equalsIgnoreCase(TransactionType.fail.getTransactionType()) ||
                transactionType.equalsIgnoreCase(TransactionType.recover.getTransactionType())){
            instr.transactionType = TransactionType.fail;
            if(transactionType.equalsIgnoreCase(TransactionType.recover.getTransactionType())){
                instr.transactionType = TransactionType.recover;
            }
            String[] values = str[1].split(",");
            instr.site = Integer.parseInt(values[0].trim());
        }else if(transactionType.equalsIgnoreCase(TransactionType.dump.getTransactionType())){
            instr.transactionType = TransactionType.dump;
        }
        else if(transactionType.equalsIgnoreCase(TransactionType.end.getTransactionType())){
            instr.transactionType = TransactionType.end;
            instr.transactionId = str[1].trim();
        }
        return instr;
    }

}
