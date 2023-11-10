public class Runner {
    private boolean editConfig, deleteConfig;

    public Runner() {
        editConfig = false;
        deleteConfig = false;
    }

    private void validateArgs(String[] args) {
        for (String arg : args) {
            switch (arg) {
                case "--edit-config" : editConfig = true;
                case "--delete-config" : deleteConfig = true;
                default : System.out.println("[warn] Unrecognized argument '" + arg + "', ignoring");
            }
        }
    }
    
    public static void main(String[] args) {
        Runner runner = new Runner();
        SelfConfigHelper getConfig = new SelfConfigHelper();
        Trimmer trim = new Trimmer();

        runner.validateArgs(args);

        getConfig.exec(runner.editConfig, runner.deleteConfig);
    }
}
