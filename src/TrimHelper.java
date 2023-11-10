import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TrimHelper {
    private final String CONFIG_DIR = System.getProperty("user.home");
    private final String CONFIG_PATH = System.getProperty("user.home") + "/.trimHelper_config"; 
    private List<String> commandsToExclude;

    public TrimHelper() {
        commandsToExclude = new ArrayList<>();
    }

    private boolean checkForConfig() {
        new File(CONFIG_PATH).delete();     // debug purposes, delete before pushing to prod...

        File config = new File(CONFIG_PATH);

        if (config.exists() && !config.isDirectory()) {
            return true;
        }

        return false;
    }

    private void createConfigFile() {
        System.out.println("[warn] No self-config file found. Will create file .trimHelper_config at " + CONFIG_DIR);
        System.out.println("[info] This config file will contain all commands that will be removed.");
        System.out.println("\n[info] Enter configuration commands, one per line.  End by passing EXIT.");

        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.print("TrimHelper(config)#");

            String command = input.nextLine();

            if (command.length() < 1) {
                System.out.println("[warn] Command must not be of zero length.");
            }

            if (command.toLowerCase().equals("exit")) {
                break;
            }

            commandsToExclude.add(command);
        }
    
        input.close();

        try {
            new File(CONFIG_PATH).createNewFile();
        } catch (IOException e) {
            System.out.println("[fatal] Failed to create configuration file.");
            e.printStackTrace();
            System.exit(-1);
        }

        writeConfigFile();
    }

    private void writeConfigFile() {
        System.out.println("[debug] In writeConfigFile");
        try {
            FileWriter configFile = new FileWriter(CONFIG_PATH);

            configFile.write("// TrimConfig configuration file generated " + ZonedDateTime.now() + ".\n");

            for (String command : commandsToExclude) {
                configFile.write(command + "\n");
            }

            configFile.close();
        } catch (IOException e) {
            System.out.println("[fatal] Failed to write to configuration file.");
            e.printStackTrace();
            System.exit(-1);
        }

        readConfig();
    }

    private void readConfig() {
        File config = new File(CONFIG_PATH);
        Scanner readConfig;
        try {
            readConfig = new Scanner(config);

            while (readConfig.hasNextLine()) {
                String command = readConfig.nextLine();

                if (!command.startsWith("//")) {
                    commandsToExclude.add(command);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("[fatal] Failed to read from configuration file.");
            e.printStackTrace();
            System.exit(-1);
        }   
    }

    private void run() {
        if (!checkForConfig()) {
            createConfigFile();
        } else {
            readConfig();
        }

        System.out.println("[debug] Command list read from file: " + commandsToExclude.toString());
    }

    public static void main(String[] args) {
        TrimHelper trim = new TrimHelper();

        trim.run();
    }
}