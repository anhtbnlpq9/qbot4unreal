package xyz.mjav.theqbot;

public class CacheFlushTask extends ScheduledTask {

    protected void exec() {
        Cache.flushExpired();
    }

    public static CacheFlushTask create() {
        ScheduledTask.classInit();
        return new CacheFlushTask();
    }

    /**
     * Class constructor
     */
    public CacheFlushTask() {
        super("CacheFlushTask", 300, false);
    }
}
