import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This program will extract the configuration of non-standard GigabitEthernet interfaces from the 
 * running configuration of a Cisco Catalyst switch and write this configuration to a new file. 
 * Trunk configurations are extracted separately and written to a separate file.
 * 
 * @author      Ben Tang
 * @since       05/22/23
 * @version     1.1.1
 */
public class TrimConfig {
    private static final String VERSION = "1.1.2";
    private static final String BUILD_DATE = "05/23/2023";
    private BufferedReader readIn;
    private boolean currentInterfaceIsTrunk, anyInterfaceIsTrunk;
    private Map<String, List<String>> configuration;
    private Map<String, List<String>> trunkConfiguration;
    private List<String> defaultCommands;

    public TrimConfig(int defaultVLAN) {
        // Using the LinkedHashMap implementation to maintain the order in which the keys are added
        configuration = new LinkedHashMap<>();
        trunkConfiguration = new LinkedHashMap<>();

        defaultCommands = new ArrayList<>();

        defaultCommands.add("switchport mode access");
        defaultCommands.add("switchport nonegotiate");
        defaultCommands.add("snmp trap mac-notification change added");
        defaultCommands.add("snmp trap mac-notification change removed");
        defaultCommands.add("spanning-tree portfast edge");
        defaultCommands.add("spanning-tree bpduguard enable");
        defaultCommands.add("mls qos trust cos");
        defaultCommands.add("switchport access vlan " + defaultVLAN);
    }

    /**
     * Reads in some file from a specified path, and trims all unnecessary lines.
     * This configuration is written to the 'configuration' LinkedHashMap or the trunkConfiguration LinkedHashMap for trunk ports.
     * Approximately O(n), where n is the number of lines in the input file.
     * @param path
     */
    private void loadConfig(String path) {
        System.out.println("[info] Loading and trimming input configuration from " + path + ".");

        try {
            readIn = new BufferedReader(new FileReader(path));

            String currentLine = "";
            String activeInterface = "";

            while (currentLine != null) {
                List<String> currentInterfaceConfig = new ArrayList<>();
                currentInterfaceIsTrunk = false;

                if (exitedInterfaceSection(currentLine)) {
                    return;
                }

                if (currentLineIsInterfaceMarker(currentLine)) {
                    activeInterface = currentLine;
                    currentLine = readIn.readLine();

                    while (!currentLineIsInterfaceMarker(currentLine)) {
                        if (exitedInterfaceSection(currentLine)) {
                            return;
                        }

                        if (currentLine.contains("trunk")) {
                            currentInterfaceIsTrunk = true;
                            anyInterfaceIsTrunk = true;
                        }

                        if (currentLine.equals("!")) {
                            currentInterfaceConfig.add("$");
                        }

                        if (!detectDefault(currentLine.trim()) && !currentLine.equals("!")) {
                            if (currentLine.contains("switchport voice vlan")) {
                                if (currentLine.contains("1316")) {
                                    currentInterfaceConfig.add(currentLine);
                                }
                            } else {
                                currentInterfaceConfig.add(currentLine);
                            }
                        }

                        currentLine = readIn.readLine();
                    }

                    if (!currentInterfaceIsTrunk) {
                        if (currentInterfaceConfig.size() > 0 && !currentInterfaceConfig.get(0).equals("$")) {
                            configuration.put(activeInterface, currentInterfaceConfig);
                        }
                    } else {
                        trunkConfiguration.put(activeInterface, currentInterfaceConfig);
                    }
                }   

                // If and only if currentLine is NOT an interface marker, update currentLine.
                // Otherwise, we end up skipping interfaces.
                if (!currentLine.contains("interface GigabitEthernet")) {
                    currentLine = readIn.readLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines whether the input line is a valid interface marker.
     * @param currentLine
     * @return true if the input is a valid interface marker, or false otherwise. 
     */
    private boolean currentLineIsInterfaceMarker(String currentLine) {
        return currentLine.contains("interface GigabitEthernet");
    }

    /**
     * Determines whether the input command is a command that is present in the default configuration. 
     * @param command
     * @return true if the input command is a default command, or false otherwise.
     */
    private boolean detectDefault(String command) {
        for (String matchAgainst : defaultCommands) {
            if (command.equals(matchAgainst)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the input line marks the end of the port interface section. 
     * Since VLAN interface commands always immediately follow the end of port interface commands, we can return
     * based on whether the input line contains "interface Vlan" or not.
     * @param currentLine
     * @return true if the input line contains "interface Vlan", or false otherwise.
     */
    private boolean exitedInterfaceSection(String currentLine) {
        return currentLine.contains("interface Vlan");
    }

    /**
     * Writes the configuration data contained within the 'configuration' LinkedHashMap to some file located
     * at the provided input path. 
     * 
     * Approximately O(n * m), where n is the number of keys in the map, and m is the number of values in the key's value (list).
     * @param path
     */
    private void saveConfig(String path) {
        System.out.println("[info] Configuration read in and trimmed where applicable.");
        System.out.println("[info] Attempting to write output to target " + path + ".");
        StringBuilder config = new StringBuilder();
        StringBuilder trunkConfig = new StringBuilder();

        if (anyInterfaceIsTrunk) {
            for (String trunkInterfaceName : trunkConfiguration.keySet()) {
                trunkConfig.append(trunkInterfaceName);
                trunkConfig.append("\n");

                for (String command : trunkConfiguration.get(trunkInterfaceName)) {
                    if (command.equals("$")) {
                        trunkConfig.append("\n");
                    } else {
                        trunkConfig.append(command + "\n");
                    }
                }
            }
        }

        if (configuration.size() != 0) {
            for (String interfaceName : configuration.keySet()) {
                config.append(interfaceName);
                config.append("\n");

                for (String command : configuration.get(interfaceName)) {
                    if (command.equals("$")) {
                        config.append("\n");
                    } else {
                        config.append(command + "\n");
                    }
                }
            }
        } else {
            System.out.println("[warn] Not writing output: Input file is blank, unreadable, or does not contain any valid interfaces.");
            System.exit(0);
        }

        writeConfig(path, config, trunkConfig);
    }

    /**
     * Writes the configuration to a file specified by the output path. 
     * @param path to write the file to
     * @param config of all non-trunk ports to write
     * @param trunkConfig of all trunk ports to write to a separate file
     */
    private void writeConfig(String path, StringBuilder config, StringBuilder trunkConfig) {
        try {
            if (identifyFileExtension(path) != -1) {
                Path writeConfigTo = Path.of(path.substring(0, identifyFileExtension(path)) + ".txt");
                Files.writeString(writeConfigTo, config);
            } else {
                Path writeConfigTo = Path.of(path + ".txt");
                Files.writeString(writeConfigTo, config);
            }

            if (anyInterfaceIsTrunk) {
                if (identifyFileExtension(path) != -1) {
                    System.out.println("[info] Detected trunk interface(s), writing all trunk configuration to " + path.substring(0, identifyFileExtension(path)) + "_trunk.txt.");
                    Path writeTrunkConfigTo = Path.of(path.substring(0, identifyFileExtension(path)) + "_trunk.txt");
                    Files.writeString(writeTrunkConfigTo, trunkConfig);
                } else {
                    System.out.println("[info] Detected trunk interface(s), writing all trunk configuration to " + path + "_trunk.txt.");
                    Path writeTrunkConfigTo = Path.of(path + "_trunk.txt");
                    Files.writeString(writeTrunkConfigTo, trunkConfig);
                }
            }
        } catch (Exception e) {
            System.out.println("[error] Could not write to " + path + ". No such file or directory.");
            System.exit(-1);
        }

        System.out.println("[info] Successfully wrote output to " + path + ".");
    }

    /**
     * Finds index of the last 'dot' in the input path, i.e., the beginning of the file extension, if one is provided.
     * @param path
     * @return index location of the beginning of the file extension, or -1 if no such location was found.
     */
    private int identifyFileExtension(String path) {
        char[] pathArray = path.toCharArray();

        for (int i = path.length() - 1; i > 0; i--) {
            if (String.valueOf(pathArray[i]).equals(".")) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Validates arguments. If invalid arguments were passed in, displays an error message and exits with status code -1.
     * If 
     * @param args passed in from main
     */
    private static void validateArgs(String[] args) {
        if (args.length > 0 && args[0].equals("--version")) {
            System.out.println("TrimConfig " + VERSION + " built " + BUILD_DATE);
            System.exit(0);
        }
        
        if (args.length != 3) {
            System.out.println("[error] Invalid input arguments. Expected 3, got: " + args.length + "\n");
            System.exit(-1);
        }
        
        try {
            BufferedReader readInTest = new BufferedReader(new FileReader(args[0]));
            readInTest.close();
        } catch (IOException ioe) {
            System.out.println("[error] Invalid input argument. The specified file does not exist.\n");
            System.exit(-1);
        }

        try {
            Integer.parseInt(args[2]);
        } catch (NumberFormatException nfe) {
            System.out.println("[error] Invalid argument for building VLAN. Given value cannot be parsed as an integer.\n");
            System.exit(-1);
        }
    }

    // Argument format: [path to input file] [path to output file] [building default VLAN]
    public static void main(String[] args) {
        validateArgs(args);

        TrimConfig trim = new TrimConfig(Integer.parseInt(args[2]));

        trim.loadConfig(args[0]);
        trim.saveConfig(args[1]);

        System.out.println("[info] All tasks complete, exiting.");
        System.exit(0);
    }
} 
