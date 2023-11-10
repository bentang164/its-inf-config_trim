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
    private boolean editConfig;
    public List<String> commandsToExclude;

    public SelfConfigHelper() {
        editConfig = false;
        commandsToExclude = new ArrayList<>();
    }

    private boolean checkForConfig() {
        File config = new File(CONFIG_PATH);

        if (config.exists() && !config.isDirectory()) {
            return true;
        }

        return false;
    }

    private void createConfigFile() {
        System.out.println("[info] No config file found. Will create one at " + CONFIG_PATH);
        System.out.println("[info] This config file will contain all commands that will be removed.");
        System.out.println("\nEnter configuration commands, one per line.  End by passing EXIT.");

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
                for (int i = 0; i < "TrimHelper(config)#".length(); i++) {
                    System.out.print(" ");
                }
                System.out.println("^");
                System.out.println("% Invalid input detected at '^' marker. Command must not be of zero length.\n");
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
            configFile.write("// Commands listed under this line will be removed from the input running configuration.\n");

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

        if (editConfig) {
            editConfig();
        }
    }

    private void editConfig() {
        List<String> commandsToRemove = new ArrayList<>();

        System.out.println("Building configuration...\n");

        File config = new File(CONFIG_PATH);

        System.out.println("Current configuration : " + config.length() + " bytes\n!");
        for (String command : commandsToExclude) {
            if (!command.startsWith("//")) {
                System.out.println(" " + command);
            }
        }

        System.out.println("end\n");

        System.out.println("Enter configuration commands to remove, one per line. Prefix with 'no'. End by passing EXIT.");

        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.print("TrimHelper(config-rem)#");

            String command = input.nextLine();

            if (command.length() >= 1) {
                if (command.toLowerCase().equals("exit")) {
                    break;
                } else {
                    if (!command.toLowerCase().startsWith("no")) {
                        for (int i = 0; i < "TrimHelper(config-rem)#".length(); i++) {
                            System.out.print(" ");
                        }
                        System.out.println("^");
                        System.out.println("% Invalid input detected at '^' marker. Command must be prefixed with 'no'.\n");
                    } else {
                        commandsToRemove.add(command.substring(3));
                    }
                }
            } else {
                for (int i = 0; i < "TrimHelper(config-rem)#".length(); i++) {
                    System.out.print(" ");
                }
                System.out.println("^");
                System.out.println("% Invalid input detected at '^' marker. Command must not be of zero length.\n");
            }

            if (command.toLowerCase().equals("exit")) {
                break;
            }
        }

        input.close();

        if (commandsToRemove.size() < 1) {
            System.out.println("[warn] No commands specified to remove, continuing without modifying config.");
        } else {
            for (int i = 0; i < commandsToExclude.size(); i++) {
                for (String command : commandsToRemove) {
                    if (commandsToExclude.get(i).equals(command)) {
                        commandsToExclude.remove(i);
                    }
                }
            }

            new File(CONFIG_PATH).delete();
            writeConfigFile();
        }
    }

    public void exec(boolean editConfig, boolean deleteConfig) {
        this.editConfig = editConfig;

        if (deleteConfig) {
            new File(CONFIG_PATH).delete();
            System.out.println("[info] Config file deleted, on next run will prompt to recreate.");
            System.exit(0);
        }
        
        if (!checkForConfig()) {
            if (editConfig) {
                System.out.println("[warn] --edit-config was passed, but no config file previously existed. Ignoring argument.");
                editConfig = false;
            }
            createConfigFile();
        } else {
            readConfig();
        }
    }
}