package xyz.mjav.theqbot;

/**
 * Class to implement the channel autolimit user limit auto setting feature periodically
 */
public class ChanAutoLimitTask extends ScheduledTask {

    protected void exec() {
        cservice.setAutolimit();
    }

    public static ChanAutoLimitTask create() {
        ScheduledTask.classInit();
        return new ChanAutoLimitTask();
    }

    /**
     * Class constructor
     */
    public ChanAutoLimitTask() {
        super("ChanAutoLimit", config.getCServeAutoLimitFreq(), false);
    }

}
