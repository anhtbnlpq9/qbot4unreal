import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Threaded class to perform periodic database operations
 */
public class SqliteDbTasks implements Runnable {

    private static Logger log = LogManager.getLogger("common-log");

    private SqliteDb sqliteDb;
    public Boolean threadRunning = true;
    
    /**
     * Class constructor
     * @param sqliteDb 
     */
    public SqliteDbTasks(SqliteDb sqliteDb) {
        this.sqliteDb = sqliteDb;
    }

    public void run() {

        log.info("Started db cron thread");

        try {
            while(threadRunning == true) {
                Thread.sleep(60000); // to put inside conf file
                sqliteDb.cleanInvalidLoginTokens();
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }
}
