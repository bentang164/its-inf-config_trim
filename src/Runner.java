import java.util.Arrays;
import java.util.List;

public class Runner {
    private final List<String> VALID_ARGS = Arrays.asList(new String[]{"--edit-config", "--delete-config", "--version", "--help"});
    private final String VERSION_BUILD_DAY_CHAR = "Fri";
    private final String VERSION_BUILD_MONTH = "Nov";
    private final int VERSION_BUILD_DAY_NUMERIC = 10;
    private final int VERSION_BUILD_YEAR = 23;
    private final int VERSION_MAJOR = 3;
    private final int VERSION_MINOR = 0;

    private boolean editConfig, deleteConfig;

    public Runner() {
        editConfig = false;
        deleteConfig = false;
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
            editConfig = true;
            return;
        }

        if (arg.equals("--delete-config")) {
            deleteConfig = true;
            return;
        }

        if (arg.equals("--version")) {
            System.out.println("Configuration Trimmer Software, Version " + VERSION_MAJOR + "." + VERSION_MINOR + 
                               "\nCompiled " + VERSION_BUILD_DAY_CHAR + " " + VERSION_BUILD_DAY_NUMERIC + "-" + VERSION_BUILD_MONTH + "-" + VERSION_BUILD_YEAR + " by btang");
            
            System.exit(0);
        }

        if (arg.equals("--help")) {
            System.out.println("usage: java -jar Trim.jar [--edit-config] [--delete-config] [--version] [--help]");
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        Runner runner = new Runner();
        SelfConfigHelper getConfig = new SelfConfigHelper();
        // Trimmer trim = new Trimmer();

        runner.validateArgs(args);

        getConfig.exec(runner.editConfig, runner.deleteConfig);
    }
}
