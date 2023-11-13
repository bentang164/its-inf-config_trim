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

                    for (String excludeThis : SelfConfigHelper.commandsToExclude) {
                        if (currentLine.strip().equals(excludeThis) && !currentInterfaceIsTrunk) {
                            excludeCommand = true;
                        }
                    }
                    
                    if (!excludeCommand && !currentLine.equals("!") && !currentInterfaceIsTrunk) {
                        commandsToAdd.add(currentLine.strip());
                    } else if (!excludeCommand && !currentLine.equals("!") && currentInterfaceIsTrunk) {
                        trunkCommandsToAdd.add(currentLine.strip());
                    }

                    excludeCommand = false;
                    currentLine = reader.nextLine();
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
            if (!anyInterfaceIsTrunk) {
                FileWriter trimmedConfig = new FileWriter(Runner.outputPath);

                trimmedConfig.write("// Trimmed configuration file generated " + ZonedDateTime.now() + ".\n");

                for (String interfaceName : trimmedContents.keySet()) {
                    trimmedConfig.write(interfaceName + "\n");
                    for (String commandToAdd : trimmedContents.get(interfaceName)) {
                        trimmedConfig.write(" " + commandToAdd + "\n");
                    }
                }
                
                trimmedConfig.close();
            } else {
                FileWriter trimmedConfig = new FileWriter(Runner.outputPathNoExt + Runner.fileNameNoExt + "_trunk.txt");

                trimmedConfig.write("// Trimmed configuration file for trunk interfaces generated " + ZonedDateTime.now() + ".\n");

                for (String interfaceName : trimmedContentsTrunk.keySet()) {
                    trimmedConfig.write(interfaceName + "\n");
                    for (String commandToAdd : trimmedContentsTrunk.get(interfaceName)) {
                        trimmedConfig.write(" " + commandToAdd + "\n");
                    }
                }

                trimmedConfig.close();
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

        System.out.print("\n[info] Complete, file(s) written to " + System.getProperty("user.home") + "/" + Runner.fileName + ".");
        System.out.println(" If there were any trunk interfaces, their configuration was written separately to " + Runner.outputPathNoExt + 
        Runner.fileNameNoExt + "_trunk.txt");
    }
}
