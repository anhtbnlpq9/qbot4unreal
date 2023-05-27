
/**
 * Class to implement the channel autolimit user limit auto setting feature periodically
 */
public class ChanAutoLimit implements Runnable {

    private CService  cservice;
    private Boolean   threadRunning = true;
    private Integer   autoLimitFreq = 999;

    /**
     * Class constructor
     * @param cservice ChanService reference
     * @param config Configuration reference
     */
    public ChanAutoLimit(CService cservice, Config config) {
        this.cservice = cservice;
        this.autoLimitFreq = config.getCServeAutoLimitFreq();
    }

    /**
     * Method to start the thread
     */
    public void run() {
        System.out.println("* Started autolimit thread");
        while(threadRunning == true) {
            try {
                Thread.sleep(this.autoLimitFreq *1000); /* x1000 because sleep() expects ms */
                cservice.cServeSetAutolimit();
            }
            catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
}