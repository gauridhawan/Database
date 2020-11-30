import java.io.*;
import java.util.Scanner;

/**
 * Driver class for program
 * @author Gauri Dhawan, Kunal Khatri
 * Date : November 29
 * side-effects : none
 */
public class Main {
    static FileWriter fw ;
    public static void main(String args[]) throws IOException {
        SiteManager siteManager = new SiteManager(10, 20);
        TransactionManager transactionManager = new TransactionManager();
        DBReader dbReader = new DBReader("", false);
        int time = 0;
        Scanner scanner = new Scanner(System.in);
        if(args.length != 0) {
            File file = new File(args[0]);
            scanner = new Scanner(file);
        }
        fw = new FileWriter("../outputs/output.txt");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("//")) {
                continue;
            }
            time++;
            Instruction instruction = dbReader.getNextInstruction(line);
            instruction.timestamp = time;
            transactionManager.tick(instruction);
        }
        fw.write("\n");
        fw.close();
    }
}



