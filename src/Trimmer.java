import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Trimmer {
    private File configToTrim;
    private Scanner readConfig;
    private StringBuilder fileContents;
    
    public Trimmer(){
        if (Runner.inputPath == null) {
            System.out.println("[fatal] Failed to read from configuration file.");
            Runner.printExit();
            System.exit(-1);
        }

        configToTrim = new File(Runner.inputPath);
        fileContents = new StringBuilder();

        try {
            readConfig = new Scanner(configToTrim);
        } catch (FileNotFoundException e) {
            System.out.println("[fatal] Failed to read from configuration file.");
            e.printStackTrace();
            Runner.printExit();
            System.exit(-1);
        }
    }

    private void trimConfig() {
        
    }

    public void exec() {
        trimConfig();
    }
}
