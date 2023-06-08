import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main class
 */
public class Qbot {

    private static final String CONFIG_FILE = "./conf/config.yml";

    private static Logger log = LogManager.getLogger("common-log");
    private static final Level INFO2 = Level.forName("INFO2", 050);

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


        log.log(INFO2, "  ");
        log.log(INFO2, "                      _..---...,\"\"-._     ,/}/)");
        log.log(INFO2, "                   .''        ,      ``..'(/-< ");
        log.log(INFO2, "                  /   _      {      )         \\ ");
        log.log(INFO2, "                ;   _ `.     `.   <         a(   ");
        log.log(INFO2, "               ,'   ( \\  )      `.  \\ __.._ .: y  ");
        log.log(INFO2, "              (  <\\_-) )'-.____...\\  `._   //-'   ");
        log.log(INFO2, "               `. `-' /-._)))      `-._))) ");
        log.log(INFO2, "                 `...'   ");
        log.log(INFO2, "               _       _   ___                     _  ");
        log.log(INFO2, "           ___| |_ ___| |_| | |_ _ ___ ___ ___ ___| | ");
        log.log(INFO2, "          | . | . | . |  _|_  | | |   |  _| -_| .'| | ");
        log.log(INFO2, "          |_  |___|___|_|   |_|___|_|_|_| |___|__,|_| ");
        log.log(INFO2, "            |_| ");
        log.log(INFO2, " ");
        log.log(INFO2, " ___        __    __         _                        __  __  ");
        log.log(INFO2, "  | |_  _  /  \\  |__) _ |_  (_ _  _  /  \\ _  _ _ _ |||__)/   _| ");
        log.log(INFO2, "  | | )(-  \\_\\/  |__)(_)|_  | (_)|   \\__/| )| (-(_|||| \\ \\__(_| ");
        log.log(INFO2, " ");

        
        if (configFileName.isEmpty() == false) configFile = configFileName;
        else configFile = CONFIG_FILE;
        
        log.info("Loading configuration file: " + configFile);
        Config config = new Config(configFile);

        log.info("Opening database");
        SqliteDb sqliteDb = new SqliteDb(config);


        /* Client thread */
        Client tlsClient = new Client(config, sqliteDb);
        Thread thread = new Thread(tlsClient);
        thread.start();

        tlsClient.setThread(thread);

        while (tlsClient.getReady() == false) {
            try { Thread.sleep(1000); }
            catch (Exception e) { e.printStackTrace(); }
        }

        log.info("Sending server ident");
        tlsClient.sendIdent();

        log.info("Starting CService");
        tlsClient.launchCService(); 

        /* Thread to manage the database schedules tasks */
        SqliteDbTasks sqliteDbCleanup = new SqliteDbTasks(sqliteDb);
        Thread sqliteDbCleanupThread = new Thread(sqliteDbCleanup);
        sqliteDbCleanupThread.start();

    }

    private static void printUsage() {

        System.out.println("Usage: java Qbot [-h | --help] [-c file]");

    }

}
