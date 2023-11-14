import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Trimmer {
    private boolean anyInterfaceIsTrunk;
    private File configToTrim;
    private Scanner reader;
    private Map<String, List<String>> trimmedContents;
    private Map<String, List<String>> trimmedContentsTrunk;
    
    public Trimmer(){
        anyInterfaceIsTrunk = false;
        trimmedContents = new LinkedHashMap<>();
        trimmedContentsTrunk = new LinkedHashMap<>();

        if (Runner.inputPath == null) {
            System.out.println("[fatal] Failed to read from configuration file.");
            Runner.printExit();
            System.exit(-1);
        }

        configToTrim = new File(Runner.inputPath);

        try {
            reader = new Scanner(configToTrim);
        } catch (FileNotFoundException e) {
            System.out.println("[fatal] Failed to read from configuration file.");
            e.printStackTrace();
            Runner.printExit();
            System.exit(-1);
        }
    }

    private void trimConfig() {
        String currentInterface = "";
        String currentLine = "";
        boolean excludeCommand = false;
        boolean currentInterfaceIsTrunk = false;

        while (reader.hasNextLine()) {
            List<String> commandsToAdd = new ArrayList<>();
            List<String> trunkCommandsToAdd = new ArrayList<>();
            if (currentLine.isEmpty() || !currentLine.contains("interface")) {
                currentLine = reader.nextLine();
            }

            if (currentLine.startsWith("interface Vlan")) {
                return;
            }

            if (currentLine.startsWith("interface")) {
                currentInterface = currentLine;
                currentLine = reader.nextLine();
                currentInterfaceIsTrunk = false;

                while (!currentLine.startsWith("interface") && reader.hasNextLine()) {
                    if (currentLine.contains("trunk")) {
                        anyInterfaceIsTrunk = true;
                        currentInterfaceIsTrunk = true;
                    }

                    if (!currentInterfaceIsTrunk) {
                        for (String excludeThis : SelfConfigHelper.commandsToExclude) {
                            if (currentLine.strip().equals(excludeThis)) {
                                excludeCommand = true;
                            }
                        }
                    }
                    
                    if (!excludeCommand && !currentLine.equals("!") && !currentInterfaceIsTrunk && !currentLine.isBlank()) {
                        commandsToAdd.add(currentLine.strip());
                    } else if (!excludeCommand && !currentLine.equals("!") && currentInterfaceIsTrunk && !currentLine.isBlank()) {
                        trunkCommandsToAdd.add(currentLine.strip());
                    }

                    excludeCommand = false;
                    currentLine = reader.nextLine();
                }
            }

            // Description comes before switchport mode, so trunk interfaces will have their descriptions omitted, unless we do this additional check.
            // If the current interface is trunk, then any commands that were erroneously added into commandsToAdd will be put into the trunkCommandsToAdd ArrayList.
            // To maintain consistency with the Cisco convention if the command contains description, it will be added to the front of the list so it appears on top.
            if (currentInterfaceIsTrunk) {
                for (String command : commandsToAdd) {
                    if (command.contains("description")) {
                        trunkCommandsToAdd.add(0, command);
                    } else {
                        trunkCommandsToAdd.add(command);
                    }
                }
            }

            if (!commandsToAdd.isEmpty() && !currentInterfaceIsTrunk) {
                trimmedContents.put(currentInterface, commandsToAdd);
            }

            if (!trunkCommandsToAdd.isEmpty()) {
                trimmedContentsTrunk.put(currentInterface, trunkCommandsToAdd);
            }
        }
    }

    private void writeTrimmedConfig() {
        try {
            if (trimmedContents.keySet().size() > 0) {
                FileWriter trimmedConfig = new FileWriter(Runner.outputPath);

                trimmedConfig.write("! Trimmed configuration file generated " + ZonedDateTime.now() + ".\n\n");

                for (String interfaceName : trimmedContents.keySet()) {
                    trimmedConfig.write(interfaceName + "\n");
                    for (String commandToAdd : trimmedContents.get(interfaceName)) {
                        trimmedConfig.write(" " + commandToAdd + "\n");
                    }

                    trimmedConfig.write("!\n");
                }
                
                trimmedConfig.close();

                System.out.println("[info] Complete, non-trunk interfaces were written to " + System.getProperty("user.home") + "/" + Runner.fileName + ".");
            } else {
                System.out.println("[warn] Non-trunk interfaces had no configuration after excluding commands, will not write file.");
            }
            
            if (anyInterfaceIsTrunk) {
                FileWriter trimmedConfigTrunk = new FileWriter(Runner.outputPathNoExt + Runner.fileNameNoExt + "_trunk.txt");

                trimmedConfigTrunk.write("! Trimmed configuration file for trunk interfaces generated " + ZonedDateTime.now() + ".\n\n");

                for (String interfaceName : trimmedContentsTrunk.keySet()) {
                    trimmedConfigTrunk.write(interfaceName + "\n");
                    for (String commandToAdd : trimmedContentsTrunk.get(interfaceName)) {
                        trimmedConfigTrunk.write(" " + commandToAdd + "\n");
                    }

                    trimmedConfigTrunk.write("!\n");
                }

                trimmedConfigTrunk.close();

                System.out.println("[info] Complete, trunk interfaces were written to " + Runner.outputPathNoExt + Runner.fileNameNoExt + "_trunk.txt.");
            }
        } catch (IOException e) {
            System.out.println("[fatal] Failed to write trimmed configuration file.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void exec() {
        trimConfig();
        writeTrimmedConfig();
    }
}
