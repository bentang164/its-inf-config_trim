import java.io.File;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Runner {
    private final List<String> VALID_ARGS = Arrays.asList(new String[]{"--edit-config", "--delete-config", "--show-config", "--version", "--help"});
    private final Pattern QUOTATION_MARKS = Pattern.compile("[\"'\u2018\u2019\u201c\u201d]");   // https://stackoverflow.com/a/35534669
    private final String VERSION_BUILD_DAY_CHAR = "Mon";
    private final String VERSION_BUILD_MONTH = "Nov";
    private final String VERSION = "3.0.0prd1";
    private final int VERSION_BUILD_DAY_NUMERIC = 13;
    private final int VERSION_BUILD_YEAR = 23;
    
    private String userFilePath;

    private boolean editConfig, deleteConfig, showConfig;

    public static int userVLAN;
    public static String inputPath, outputPath, outputPathNoExt, fileName, fileNameNoExt;
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
            editConfig = true;
            return;
        }

        if (arg.equals("--delete-config")) {
            deleteConfig = true;
            return;
        }

        if (arg.equals("--show-config")) {
            showConfig = true;
            return;
        }

        if (arg.equals("--version")) {
            printStart();
            printExit();
            System.exit(0);
        }

        if (arg.equals("--help")) {
            System.out.println("usage: java -jar TrimConfig.jar [--edit-config] [--delete-config] [--show-config] [--version] [--help]");
            printExit();
            System.exit(0);
        }
    }
    
    private void getInfo() {
        while (true) {
            System.out.print("Enter the path to the input configuration file to trim: ");
            userFilePath = userInput.nextLine().strip();

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
    }

    private void setFileName() {
        String cleanPath = QUOTATION_MARKS.matcher(userFilePath).replaceAll("");

        String fileExtension = cleanPath.substring(cleanPath.lastIndexOf("."), cleanPath.length());

        if (cleanPath.contains("/")) {
            cleanPath = cleanPath.substring(cleanPath.lastIndexOf("/") + 1, cleanPath.length());
        }

        fileNameNoExt = cleanPath.substring(0, cleanPath.lastIndexOf(".")) + "_trimmed";
        fileName = cleanPath.substring(0, cleanPath.lastIndexOf(".")) + "_trimmed" + fileExtension;

        outputPath = System.getProperty("user.home") + "/" + fileName;
        outputPathNoExt = System.getProperty("user.home") + "/";
    }

    private void printStart() {
        System.out.println("\nConfiguration Trimmer Software, Version " + VERSION + ", RELEASE SOFTWARE (fc1)");
        System.out.println("Technical Support: https://github.com/bentang164/its-inf-config_trim#readme");
        System.out.println("Compiled " + VERSION_BUILD_DAY_CHAR + " " + VERSION_BUILD_DAY_NUMERIC + "-" + VERSION_BUILD_MONTH + "-" +
        VERSION_BUILD_YEAR + " by btang");
        System.out.println("\n\n" +
        "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS " +
        "OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY " + 
        "AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR " + 
        "CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL " +
        "DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, " +
        "DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN " +
        "CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF " +
        "THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n\n");
    }

    public static void printExit() {
        System.out.println("[info] Exited Configuration Trimmer session at " + ZonedDateTime.now());
    }

    public static void main(String[] args) {
        Runner runner = new Runner();
        runner.printStart();
        runner.validateArgs(args);
        
        SelfConfigHelper getConfig = new SelfConfigHelper();
        getConfig.exec(runner.editConfig, runner.deleteConfig, runner.showConfig);
        
        runner.getInfo();

        Trimmer trim = new Trimmer();
        trim.exec();

        userInput.close();

        printExit();
    }
}
