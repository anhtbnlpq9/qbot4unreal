
/**
 * Threaded class to perform periodic database operations
 */
public class SqliteDbTasks implements Runnable {

    public SqliteDb sqliteDb;
    public Boolean threadRunning = true;
    
    /**
     * Class constructor
     * @param sqliteDb 
     */
    public SqliteDbTasks(SqliteDb sqliteDb) {
        this.sqliteDb = sqliteDb;
    }

    public void run() {

        System.out.println("* Started db cron thread");

        try {
            while(threadRunning == true) {
                Thread.sleep(60000); // to put inside conf file
                sqliteDb.cleanInvalidLoginTokens();
            }
        }
        catch (Exception e) { e.printStackTrace(); }
    }
}
