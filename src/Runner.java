import java.io.File;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Runner {
    private final List<String> VALID_ARGS = Arrays.asList(new String[]{"--edit-config", "--delete-config", "--show-config", "--version", "--help"});
    private final String VERSION_BUILD_DAY_CHAR = "Fri";
    private final String VERSION_BUILD_MONTH = "Nov";
    private final int VERSION_BUILD_DAY_NUMERIC = 10;
    private final int VERSION_BUILD_YEAR = 23;
    private final int VERSION_MAJOR = 3;
    private final int VERSION_MINOR = 0;

    private boolean editConfig, deleteConfig, showConfig;

    public String inputPath;
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
            String userFilePath = userInput.nextLine();

            if (new File(userFilePath.replace("'", "")).exists()) {
                this.inputPath = userFilePath;
                break;
            } else {
                System.out.println("[error] File does not exist or is inaccessible.");
            }
        }
    }

    public static void printExit() {
        System.out.println("[info] Exited Configuration Trimmer session at " + ZonedDateTime.now());
    }

    public static void main(String[] args) {
        Runner runner = new Runner();
        SelfConfigHelper getConfig = new SelfConfigHelper();
        // Trimmer trim = new Trimmer();

        runner.validateArgs(args);
        getConfig.exec(runner.editConfig, runner.deleteConfig, runner.showConfig);

        runner.getInfo();

        // trim.trimConfig();

        userInput.close();

        printExit();
    }
}
