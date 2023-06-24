import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class
 */
public class Qbot {

    private static final String CONFIG_FILE = "./conf/config.yml";

    private static Logger log = LogManager.getLogger("common-log");

    /* This verbosity should always be displayed (displays title) */
    private static final Level ALWAYS = Level.forName("ALWAYS", 010);

    /**
     * Main method
     * @param args no arguments
     */
    public static void main(String[] args) {
        String configFileName = "";
        String configFile;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-h") == true || arg.equals("--help")) {
                printUsage();
                return;
            } else if (arg.equals("-c") == true || arg.equals("--config")) {
                configFileName = args[++i];
            } else {
                // invalid argument
                printUsage();
                return;
            }
        }

        log.log(ALWAYS, "  ");
        log.log(ALWAYS, "                      _..---...,\"\"-._     ,/}/)");
        log.log(ALWAYS, "                   .''        ,      ``..'(/-< ");
        log.log(ALWAYS, "                  /   _      {      )         \\ ");
        log.log(ALWAYS, "                ;   _ `.     `.   <         a(   ");
        log.log(ALWAYS, "               ,'   ( \\  )      `.  \\ __.._ .: y  ");
        log.log(ALWAYS, "              (  <\\_-) )'-.____...\\  `._   //-'   ");
        log.log(ALWAYS, "               `. `-' /-._)))      `-._))) ");
        log.log(ALWAYS, "                 `...'   ");
        log.log(ALWAYS, "               _       _   ___                     _  ");
        log.log(ALWAYS, "           ___| |_ ___| |_| | |_ _ ___ ___ ___ ___| | ");
        log.log(ALWAYS, "          | . | . | . |  _|_  | | |   |  _| -_| .'| | ");
        log.log(ALWAYS, "          |_  |___|___|_|   |_|___|_|_|_| |___|__,|_| ");
        log.log(ALWAYS, "            |_| ");
        log.log(ALWAYS, " ");
        log.log(ALWAYS, " ___        __    __         _                        __  __  ");
        log.log(ALWAYS, "  | |_  _  /  \\  |__) _ |_  (_ _  _  /  \\ _  _ _ _ |||__)/   _| ");
        log.log(ALWAYS, "  | | )(-  \\_\\/  |__)(_)|_  | (_)|   \\__/| )| (-(_|||| \\ \\__(_| ");
        log.log(ALWAYS, " ");

        if (configFileName.isEmpty() == false) configFile = configFileName;
        else configFile = CONFIG_FILE;
        
        log.info("Loading configuration file: " + configFile);
        Config config = new Config(configFile);

        log.info("Opening database");
        SqliteDb sqliteDb = new SqliteDb(config);

        runClient(config, sqliteDb);
        runTasks(sqliteDb);

    }

    private static void printUsage() {

        System.out.println("Usage: java Qbot [-h | --help] [-c file]");

    }

    public static void runClient(Config config, SqliteDb sqliteDb) {
        /* Client thread */
        Client tlsClient = new Client(config, sqliteDb);
        Thread clientThread = new Thread(tlsClient);
        clientThread.start();

        tlsClient.setThread(clientThread);

        while (tlsClient.getReady() == false) {
            try { Thread.sleep(1000); }
            catch (Exception e) { log.error("QBot/main: error while sleeping", e); }
        }

        log.info("Sending server ident");
        tlsClient.sendIdent();

        log.info("Starting CService");
        tlsClient.launchCService();
    }

    private static void runTasks(SqliteDb sqliteDb) {

        /* Thread to manage the database schedules tasks */
        SqliteDbTasks sqliteDbCleanup = new SqliteDbTasks(sqliteDb);
        Thread sqliteDbCleanupThread = new Thread(sqliteDbCleanup);
        sqliteDbCleanupThread.start();


    }

}
