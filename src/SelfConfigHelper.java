import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SelfConfigHelper {
    private final String CONFIG_PATH = System.getProperty("user.home") + "/.trimHelper_config"; 
    public List<String> commandsToExclude;

    public SelfConfigHelper() {
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
        System.out.println("[warn] No config file found. Will create one at " + CONFIG_PATH);
        System.out.println("[info] This config file will contain all commands that will be removed.");
        System.out.println("[info] Enter configuration commands, one per line.  End by passing EXIT.\n");

        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.print("TrimHelper(config)#");

            String command = input.nextLine();

            if (command.length() >= 1) {
                if (command.toLowerCase().equals("exit")) {
                    break;
                } else {
                    commandsToExclude.add(command);
                }
            } else {
                System.out.println("[warn] Command must not be of zero length.");
            }

            if (command.toLowerCase().equals("exit")) {
                break;
            }
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

    public void exec(boolean editConfig, boolean deleteConfig) {
        if (!checkForConfig()) {
            createConfigFile();
        } else {
            readConfig();
        }
    }
}