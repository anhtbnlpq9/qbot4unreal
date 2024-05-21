package xyz.mjav.theqbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScheduledTask implements Runnable {

    /* Static fields */

    protected static Logger log = LogManager.getLogger("common-log");

    protected static boolean   isThreadRunning = true;

    /** Thread run limit (default = 10 * period) */
    private static final long COUNT_MAX = 10;


    protected static volatile CService cservice;
    protected static volatile Config config;


    protected static final void classInit() {
        if (cservice == null) cservice = CService.getInstance();
        if (config == null) config = Config.getConfig();
    }


    public static final ScheduledTask create(String name, int period, boolean countLimited) {

        ScheduledTask scheduledTask = new ScheduledTask(name, period, countLimited);
        return scheduledTask;
    }

    protected void exec() {

    }


    /* Nonstatic fields */

    /** Thread name */
    protected String name;

    /** Thread periods counter */
    protected int     count;
    protected boolean countLimited;

    /** Thread period */
    protected int period; /* seconds */


    /**
     * Constructor
     * @param name
     * @param countLimited
     */
    protected ScheduledTask(String name, int period, boolean countLimited) {

        this.name = name;
        this.period = period;

        count = 0;
        this.countLimited = countLimited;
    }

    protected ScheduledTask() {
        ScheduledTask.classInit();;
    }

    /** Asks the thread to stop */
    public static void orderToStop() {
        isThreadRunning = false;
    }

    @Override public void run() {
        log.info("ScheduledTask::run: Starting thread " + this.name);

        if (cservice == null) cservice = CService.getInstance();
        if (config == null) config = Config.getConfig();

        while(ScheduledTask.isThreadRunning == true) {
            try {
                Thread.sleep(this.period *1000); /* x1000 because sleep() expects ms */
                if (this.countLimited == true && (this.count++) >= COUNT_MAX) Thread.currentThread().interrupt();

                // Actions to perform when thread is running
                // ...

                exec();


            }
            catch (InterruptedException e) {
                log.info(String.format("ScheduledTask::run: Stopping thread"));
                orderToStop();

                // Actions to perform when thread is stopping
                // ...

            }
        }

        log.info(String.format("ScheduledTask::run: Thread stopped"));
    }
}
