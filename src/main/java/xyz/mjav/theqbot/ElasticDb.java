package xyz.mjav.theqbot;


//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;


/**
 * Singleton class to manage a Sqlite database.
 */
public class ElasticDb /*implements Database*/ {

    //private static Logger log = LogManager.getLogger("common-log");

    private static ElasticDb instance = null;

    //private static Config config = null;

    private ElasticDb(Config config) {


    }

    /**
     * Static method to create the singleton
     */
    public static synchronized ElasticDb getInstance(Config config) {
        if (instance == null) {
            instance = new ElasticDb(config);
            return instance;
        }
        else return instance;
    }


}
