package xyz.mjav.theqbot;

/**
 * Threaded class to perform periodic database operations
 */
public class SqliteDbTasks extends ScheduledTask {

    private Database sqliteDb;

    protected void exec() {
        log.debug("SqliteDbTasks::exec: performing database task");
        sqliteDb.cleanInvalidLoginTokens();
    }

    /**
     * Class constructor
     * @param sqliteDb
     */
    public SqliteDbTasks(Database sqliteDb) {
        super("SqliteDbTasks", config.getDbSchedFreq(), false);
        this.sqliteDb = sqliteDb;
    }

}
