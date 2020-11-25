import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    public static void main(String args[]) throws FileNotFoundException {
        SiteManager siteManager = new SiteManager(10, 20);
        TransactionManager transactionManager = new TransactionManager();
        DBReader dbReader = new DBReader("input.txt", false);
        int time = 0;
        File file = new File("input.txt");
        Scanner fileReader = new Scanner(file);
        while(fileReader.hasNextLine()){
            time++;
            Instruction instruction = dbReader.getNextInstruction(fileReader.nextLine());
            instruction.timestamp = time;
            if(instruction.transactionType == TransactionType.fail || instruction.transactionType == TransactionType.recover ||
            instruction.transactionType == TransactionType.dump){
                siteManager.tick(instruction);
            }
            else{
                transactionManager.tick(instruction);
            }
        }
    }
}
