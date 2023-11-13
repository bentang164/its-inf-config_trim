import java.io.File;
import java.io.FileNotFoundException;
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

    private void trimConfig() {
        String line = "";
        String currentInterface = "";
        boolean updateLine = true;
        List<String> commandsToAdd = new ArrayList<>();

        while (reader.hasNextLine()) {
            if (updateLine) {
                line = reader.nextLine();
            }

            if (line.toLowerCase().contains("interface vlan")) {
                break;
            }

            if (line.contains("interface")) {
                currentInterface = line;

                while (!line.contains("interface")) {
                    line = reader.nextLine();

                    for (String commandToExclude : SelfConfigHelper.commandsToExclude) {
                        if (!line.equals(commandToExclude)) {
                            commandsToAdd.add(line);
                        }
                    }
                }
            }

            trimmedContents.put(currentInterface, commandsToAdd);

            updateLine = false;
        }
    }

    public void exec() {
        trimConfig();
    }
}
