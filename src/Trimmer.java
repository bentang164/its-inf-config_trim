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
    private File configToTrim;
    private Scanner reader;
    private Map<String, List<String>> trimmedContents;
    
    public Trimmer(){
        trimmedContents = new LinkedHashMap<>();

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

    /**
     * Current issue: Commands that should be added are added to the commandsToAdd ArrayList, but when it gets to write, somehow it is blank.
     */
    private void trimConfig() {
        String currentInterface = "";
        String currentLine = "";
        boolean excludeCommand = false;
        List<String> commandsToAdd = new ArrayList<>();

        while (reader.hasNextLine()) {

            if (currentLine.isEmpty()) {
                currentLine = reader.nextLine();
            } else {
                currentLine = reader.nextLine();
            }

            if (currentLine.startsWith("interface Vlan")) {
                // System.out.println("[debug] At return, trimmedContents is: " + trimmedContents.toString());
                return;
            }

            if (currentLine.startsWith("interface")) {
                currentInterface = currentLine;
                currentLine = reader.nextLine();

                while (!currentLine.startsWith("interface")) {
                    for (String excludeThis : SelfConfigHelper.commandsToExclude) {
                        if (currentLine.strip().equals(excludeThis)) {
                            excludeCommand = true;
                        }
                    }
                    
                    if (!excludeCommand && !currentLine.equals("!")) {
                        commandsToAdd.add(currentLine.strip());
                    }

                    excludeCommand = false;
                    currentLine = reader.nextLine();
                }
            }

            
            if (!commandsToAdd.isEmpty()) {
                trimmedContents.put(currentInterface, commandsToAdd);
                commandsToAdd.clear();      // behaves exactly as trimmedContents.get(currentInterface).clear() for some stupid reason
            }
        }
    }

    private void writeTrimmedConfig() {
        try {
            FileWriter trimmedConfig = new FileWriter(Runner.outputPath);

            trimmedConfig.write("// Trimmed configuration file generated " + ZonedDateTime.now() + ".\n");

            for (String interfaceName : trimmedContents.keySet()) {
                trimmedConfig.write(interfaceName + "\n");
                for (String commandToAdd : trimmedContents.get(interfaceName)) {
                    trimmedConfig.write(" " + commandToAdd + "\n");
                }
            }

            trimmedConfig.close();
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
