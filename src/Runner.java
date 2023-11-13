import java.io.File;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Runner {
    private final List<String> VALID_ARGS = Arrays.asList(new String[]{"--edit-config", "--delete-config", "--show-config", "--version", "--help"});
    private final Pattern QUOTATION_MARKS = Pattern.compile("[\"'\u2018\u2019\u201c\u201d]");   // https://stackoverflow.com/a/35534669
    private final String VERSION_BUILD_DAY_CHAR = "Fri";
    private final String VERSION_BUILD_MONTH = "Nov";
    private final int VERSION_BUILD_DAY_NUMERIC = 10;
    private final int VERSION_BUILD_YEAR = 23;
    private final int VERSION_MAJOR = 3;
    private final int VERSION_MINOR = 0;
    
    private String userFilePath;

    private boolean editConfig, deleteConfig, showConfig;

    public static int userVLAN;
    public static String inputPath, outputPath, fileName;
    public static Scanner userInput;

    public Runner() {
        editConfig = false;
        deleteConfig = false;
        showConfig = false;
        userInput = new Scanner(System.in);
    }

    private void validateArgs(String[] args) {
        if (args.length < 1) {
            return;
        }

        if (args.length > 1) {
            System.out.println("[warn] Invalid arguments. Expected: 1, got: " + args.length + ". Ignoring extra arguments");
        }

        String arg = args[0];

        if (!VALID_ARGS.contains(arg)) {
            System.out.println("[warn] Invalid argument '" + arg + "'. Ignoring.");
            return;
        }

        if (arg.equals("--edit-config")) {
            System.out.println("\n--edit-config passed, prompting to edit the list of commands to exclude when trimming.\n");
            editConfig = true;
            return;
        }

        if (arg.equals("--delete-config")) {
            deleteConfig = true;
            return;
        }

        if (arg.equals("--show-config")) {
            System.out.println("\n--show-config passed, displaying current list of commands to exclude when trimming.\n");
            showConfig = true;
            return;
        }

        if (arg.equals("--version")) {
            System.out.println("Configuration Trimmer Software, Version " + VERSION_MAJOR + "." + VERSION_MINOR + 
                               "\nCompiled " + VERSION_BUILD_DAY_CHAR + " " + VERSION_BUILD_DAY_NUMERIC + "-" + VERSION_BUILD_MONTH + "-" + VERSION_BUILD_YEAR + " by btang");
            
            printExit();
            System.exit(0);
        }

        if (arg.equals("--help")) {
            System.out.println("usage: java -jar Trim.jar [--edit-config] [--delete-config] [--show-config] [--version] [--help]");
            printExit();
            System.exit(0);
        }
    }
    
    private void getInfo() {
        while (true) {
            System.out.print("Enter the path to the input configuration file to trim: ");
            userFilePath = userInput.nextLine();

            if (new File(QUOTATION_MARKS.matcher(userFilePath).replaceAll("")).exists()) {
                inputPath = QUOTATION_MARKS.matcher(userFilePath).replaceAll("");
                break;
            } else {
                System.out.println("[error] File does not exist or is inaccessible.");
            }
        }

        setFileName();

        while (true) {
            System.out.print("Enter the building default VLAN, or enter '0' to skip: ");
            String userInputVLAN = userInput.nextLine();

            try {
                userVLAN = Integer.parseInt(userInputVLAN);
                break;
            } catch (NumberFormatException e) {
                System.out.println("[error] Input is not a valid integer.");
            }
        }

        while(true) {
            System.out.print("Enter the path that the trimmed configuration file should be outputted to, or leave blank to use the default: ");
            String userOutputPath = userInput.nextLine();

            if (!(userOutputPath.isEmpty() && userOutputPath.isBlank())) {
                outputPath = userOutputPath;
                return;
            } else {
                System.out.println("[info] No path given, will write to " + System.getProperty("user.home") + "/Downloads/" + fileName);
                outputPath = System.getProperty("user.home") + "/Downloads/" + fileName;
                return;
            }
        }
    }

    private void setFileName() {
        String cleanPath = QUOTATION_MARKS.matcher(userFilePath).replaceAll("");

        String fileExtension = cleanPath.substring(cleanPath.lastIndexOf("."), cleanPath.length());

        if (cleanPath.contains("/")) {
            cleanPath = cleanPath.substring(cleanPath.lastIndexOf("/") + 1, cleanPath.length());
        }

        fileName = cleanPath.substring(0, cleanPath.lastIndexOf(".")) + "_trimmed" + fileExtension;
    }

    public static void printExit() {
        System.out.println("[info] Exited Configuration Trimmer session at " + ZonedDateTime.now());
    }

    public static void main(String[] args) {
        Runner runner = new Runner();
        runner.validateArgs(args);
        runner.getInfo();

        SelfConfigHelper getConfig = new SelfConfigHelper();
        getConfig.exec(runner.editConfig, runner.deleteConfig, runner.showConfig);
        
        Trimmer trim = new Trimmer();
        trim.exec();

        userInput.close();

        printExit();
    }
}
