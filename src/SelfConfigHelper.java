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
    private boolean editConfig, showConfig;
    public static List<String> commandsToExclude;

    public SelfConfigHelper() {
        editConfig = false;
        commandsToExclude = new ArrayList<>();
    }

    private boolean checkForConfig() {
        File config = new File(CONFIG_PATH);

        if (config.exists() && !config.isDirectory() && config.length() > 0) {
            return true;
        }

        return false;
    }

    private void createConfigFile() {
        System.out.println("[info] No config file found. Will create one at " + CONFIG_PATH);
        System.out.println("\nEnter configuration commands to exclude, one per line.  End by passing EXIT.");

        while (true) {
            System.out.print("TrimHelper(config)#");

            String command = Runner.userInput.nextLine();

            if (command.length() >= 1) {
                if (command.toLowerCase().equals("exit")) {
                    System.out.println();
                    break;
                } else {
                    if (command.equals("spanning-tree portfast") && !commandsToExclude.contains("spanning-tree portfast edge")) {
                        commandsToExclude.add("spanning-tree portfast edge");
                        commandsToExclude.add("spanning-tree portfast");
                    } else if (command.equals("spanning-tree portfast edge") && !commandsToExclude.contains("spanning-tree portfast")) {
                        commandsToExclude.add("spanning-tree portfast");
                        commandsToExclude.add("spanning-tree portfast edge");
                    } else {
                        commandsToExclude.add(command);
                    }
                }
            } else {
                for (int i = 0; i < "TrimHelper(config)#".length(); i++) {
                    System.out.print(" ");
                }
                System.out.println("^");
                System.out.println("% Invalid input detected at '^' marker. Command must not be of zero length.\n");
            }
        }

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
            System.out.println("[info] --edit-config passed, prompting to edit the list of commands to exclude when trimming.\n");
            editConfig();
        }

        if (showConfig) {
            System.out.println("[info] --show-config passed, displaying current list of commands to exclude when trimming.\n");
            showConfig();
        }
    }

    private void editConfig() {
        List<String> commandsToRemove = new ArrayList<>();

        showConfig();

        System.out.println("Enter configuration commands to exclude, one per line.  " +
        "Prefix with 'no' to remove the specified command from the exclusion list.  End by passing EXIT.");

        while (true) {
            System.out.print("TrimHelper(config)#");

            String command = Runner.userInput.nextLine();

            if (command.length() >= 1) {
                if (command.toLowerCase().equals("exit")) {
                    System.out.println();
                    break;
                } else {
                    if (command.toLowerCase().startsWith("no")) {
                        if (command.contains("spanning-tree portfast")) {
                            commandsToRemove.add("spanning-tree portfast");
                            commandsToRemove.add("spanning-tree portfast edge");
                        } else {
                            commandsToRemove.add(command.substring(3));
                        }
                    } else {
                        if (command.equals("spanning-tree portfast") && !commandsToExclude.contains("spanning-tree portfast edge")) {
                            commandsToExclude.add("spanning-tree portfast edge");
                            commandsToExclude.add("spanning-tree portfast");
                        } else if (command.equals("spanning-tree portfast edge") && !commandsToExclude.contains("spanning-tree portfast")) {
                            commandsToExclude.add("spanning-tree portfast");
                            commandsToExclude.add("spanning-tree portfast edge");
                        } else {
                            commandsToExclude.add(command);
                        }
                    }
                }
            } else {
                for (int i = 0; i < "TrimHelper(config)#".length(); i++) {
                    System.out.print(" ");
                }
                System.out.println("^");
                System.out.println("% Invalid input detected at '^' marker. Command must not be of zero length.\n");
            }
        }

        if (commandsToRemove.size() > 0) {
            for (int i = 0; i < commandsToExclude.size(); i++) {
                for (String command : commandsToRemove) {
                    if (commandsToExclude.get(i).equals(command)) {
                        commandsToExclude.remove(i);
                    }
                }
            }
        }

        new File(CONFIG_PATH).delete();
        writeConfigFile();
    }

    private void showConfig() {
        System.out.println("Building configuration...\n");

        File config = new File(CONFIG_PATH);

        System.out.println("Current configuration : " + config.length() + " bytes\n!");
        for (String command : commandsToExclude) {
            if (!command.startsWith("//")) {
                System.out.println(" " + command);
            }
        }

        System.out.println("end\n");
    }

    public void excludeDefaultVLAN() {
        if (Runner.userVLAN != 0) {
            commandsToExclude.add("switchport access vlan " + Runner.userVLAN);
        }
    }

    public void exec(boolean editConfig, boolean deleteConfig, boolean showConfig) {
        this.editConfig = editConfig;
        this.showConfig = showConfig;

        if (deleteConfig) {
            new File(CONFIG_PATH).delete();
            System.out.println("[info] Config file deleted, on next run will prompt to recreate.");
            Runner.printExit();
            System.exit(0);
        }
        
        if (!checkForConfig()) {
            if (editConfig || showConfig) {
                System.out.println("[warn] --edit-config or --show-config was passed, but no config file previously existed. Ignoring argument.");
                editConfig = false;
                showConfig = false;
            }
            
            createConfigFile();
        } else {
            readConfig();
        }
    }
}