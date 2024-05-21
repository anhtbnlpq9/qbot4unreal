package xyz.mjav.theqbot;

/**
 * Class to implement the channel autolimit user limit auto setting feature periodically
 */
public class ChanAutoLimit extends ScheduledTask {

    protected void exec() {
        cservice.setAutolimit();
    }

    public static ChanAutoLimit create() {
        ScheduledTask.classInit();
        return new ChanAutoLimit();
    }

    /**
     * Class constructor
     */
    public ChanAutoLimit() {
        super("ChanAutoLimit", config.getCServeAutoLimitFreq(), false);
    }

}
