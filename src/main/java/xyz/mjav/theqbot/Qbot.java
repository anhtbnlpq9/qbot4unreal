package xyz.mjav.theqbot;

import java.io.FileNotFoundException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import xyz.mjav.theqbot.exceptions.ConfigFileErrorException;

/**
 * Main class
 */
public class Qbot {

    public static final long BOOT_TIME = Timestamp.value().getValue();

    /**
     * Default configuration file path if none provided on the command line
     */
    private static final String CONFIG_FILE = "./conf/config.yml";

    private static Logger log = LogManager.getLogger("common-log");

    /**
     * Define a new verbosity level (ALWAYS) with a level that makes it always displayed
     * Used for things like title, config summary ...
     */
    private static final Level ALWAYS = Level.forName("ALWAYS", 010);

    /**
     * Main method
     * @param args no arguments
     */
    public static void main(String[] args) {

        String configFileName = "";
        String configFile;

        Boolean killAfterConf = false;

        Database database;
        Config config = null;

        /* Parsing command arguments */
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-h") == true || arg.equals("--help")) {
                printUsage();
                return;
            }
            else if (arg.equals("-c") == true || arg.equals("--config")) { configFileName = args[++i]; }
            else if (arg.equals("-C") == true || arg.equals("--check")) { killAfterConf = true; }
            else {
                // invalid argument
                printUsage();
                return;
            }
        }

        log.log(ALWAYS, " ");
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
        log.log(ALWAYS, "                           version v" + Const.QBOT_VERSION_NUMBER);
        log.log(ALWAYS, "* Starting the Q Bot for UnrealIRCd");

        if (configFileName.isEmpty() == false) configFile = configFileName;
        else configFile = CONFIG_FILE;

        log.info("* Loading configuration file: " + configFile);

        try { config = Config.getInstance(configFile); }
        catch (ConfigFileErrorException e) { log.fatal(String.format("Configuration file errors detected (likely missing blocks). Dying.")); System.exit(0); }
        catch (FileNotFoundException e) { log.fatal(String.format("Could not open configuration file (file not found). Dying.")); System.exit(0); }
        catch (Exception e) { log.fatal(String.format("Could not open configuration file (unknown error). Dying.")); e.printStackTrace(); System.exit(0); }

        if (killAfterConf == true) {
            System.out.println("Config check passed! Exiting due to configuration check mode.");
            System.exit(0);
        }

        System.setProperty("javax.net.ssl.trustStore",          config.getTrustStorePath());
        System.setProperty("javax.net.ssl.trustStorePassword",  config.getTrustStorePassword());
        System.setProperty("javax.net.ssl.keyStore",            config.getKeyStorePath());
        System.setProperty("javax.net.ssl.keyStorePassword",    config.getKeyStorePassword());

        log.info("* Opening database");

        if (config.getDbType().equals("sqlite3")) { database = SqliteDb.getInstance(config); }
        //else if (config.getDbType().equals("elastic")) { database = ElasticDb.getInstance(config); }
        else { database = null; log.fatal("Unhandled database type."); System.exit(0); }

        ESClient esClient = runEsClient(config);
        while (esClient.isReady() != true) {
            log.info(String.format("QBot::main: Waiting for ES connection to be established"));
            try { Thread.sleep(1000); }
            catch(InterruptedException e) {  }
        }

        UserAccount.setDatabase(database); // Sets the database reference to UserAccount class
        runClient(config, database);
        runTasks(database);

    }

    /**
     * Prints the command line usage
     */
    private static void printUsage() {
        System.out.println("Usage: java Qbot [-h | --help] [-c FILE | --config FILE] [-C | --check]");
        System.out.println("");
        System.out.println("-h, --help               Prints this help.");
        System.out.println("-c, --config FILE        Defines FILE as the configuration file.");
        System.out.println("-C, --check              Checks configuration file.");
        System.out.println("End of help.");
    }

    /**
     * Starts the different operation services (client socket, CService, OperService, ...)
     * @param config reference to configuration
     * @param database reference to sqlite database
     */
    public static void runClient(Config config, Database database) {

        Server myServer = null;

        /* Client thread */
        Client tlsClient = new Client(config, database);
        Thread clientThread = new Thread(tlsClient);
        clientThread.start();

        //tlsClient.setThread(clientThread);

        while (tlsClient.isReady() == false) {
            try { Thread.sleep(1000); }
            catch (Exception e) { log.error("QBot::runClient: error while sleeping", e); }
        }

        myServer = Server.getServerBySid(config.getServerId());

        while (myServer.hasPeerResponded() != true) {

            log.info("Waiting for peer to register");

            try { Thread.sleep(2000); }
            catch (Exception e) { log.error(String.format("QBot::runClient: Error while sleeping."), e); }
        }

        while (myServer.hasEOS() != true) {

            log.info("Waiting for the final EOS");

            try { Thread.sleep(2000); }
            catch (Exception e) { log.error(String.format("QBot::runClient: Error while sleeping."), e); }
        }

        log.info("Peer has registered and we have EOS");

        log.info("Starting CService");
        tlsClient.launchCService();

        tlsClient.launchOService();
    }

    /**
     * Starts the database task scheduler
     * @param database reference to sqlite database
     */
    private static void runTasks(Database database) {

        /* Thread to manage the database schedules tasks */
        SqliteDbTasks sqliteDbCleanup = new SqliteDbTasks(database);
        Thread sqliteDbCleanupThread = new Thread(sqliteDbCleanup);
        sqliteDbCleanupThread.start();

        CacheFlushTask cacheFlushRunnable = CacheFlushTask.create();
        Thread cacheFlushRunnableThread = new Thread(cacheFlushRunnable);
        cacheFlushRunnableThread.start();

    }


    private static ESClient runEsClient(Config config) {

        ESClient esClient = ESClient.getInstance(config);
        Thread esClientThread = new Thread(esClient);
        esClientThread.start();

        return esClient;

    }

}
