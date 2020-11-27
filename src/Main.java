import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    public static void main(String args[]) throws FileNotFoundException {
        SiteManager siteManager = new SiteManager(10, 20);
        TransactionManager transactionManager = new TransactionManager();
        DBReader dbReader = new DBReader("../testcases/test2.txt", false);
        int time = 0;
        File file = new File("/Users/kunalkhatri/Desktop/Semester3/ADB/Porject/Database/testcases/test_script_11.txt");

        Scanner fileReader = new Scanner(file);
        while(fileReader.hasNextLine()){
            String line = fileReader.nextLine();
            if(line.startsWith("//")){
                continue;
            }
            time++;
            Instruction instruction = dbReader.getNextInstruction(line);
            instruction.timestamp = time;
            transactionManager.tick(instruction);
        }
    }
}
