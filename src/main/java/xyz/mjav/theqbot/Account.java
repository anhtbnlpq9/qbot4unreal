package xyz.mjav.theqbot;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Account {

    /*
     * Static fields
     */

    /** Class logger */
    protected static Logger log = LogManager.getLogger("common-log");



    /*
     * Instance fields
     */

    /** Account name */
    protected String name;

    /** Suspend reason */
    protected String suspendReason;

    /** Account UUID confirmation code for sensitive command */
    protected UUID confirmationCode;

    /** Last suspend timestamp */
    protected Timestamp suspendLastTS;

    /** Account registration TS */
    protected Timestamp registrationTS;

    /** Suspend counter */
    protected Integer suspendCount;

    /** Chanserv channel registration status */
    protected Boolean isRegistered;

    /**
     * Class builder
     */
    public static abstract class Builder {

        protected Integer suspendCount               = 0;

        protected String name                        = "<tempUndefined>";
        protected String suspendReason               = "";

        protected Timestamp suspendLastTS            = new Timestamp(0L);
        protected Timestamp registrationTS           = new Timestamp(0L);

        protected Boolean isRegistered         = false;



        public Builder name(String val) {
            name = val;
            return this;
        }

        public Builder registered() {
            this.isRegistered = true;
            return this;
        }

        /**
         * Sets the registration timestamp
         * @param val timestamp
         * @return Builder
         */
        public Builder registrationTS(Timestamp val) {
            registrationTS = val;
            return this;
        }

        public Builder suspendLastTS(Timestamp val) {
            suspendLastTS = val;
            return this;
        }

        public Builder suspendCounter(Integer val) {
            suspendCount = val;
            return this;
        }

        /**
         * Sets the account suspension message
         * @param val message
         * @return Builder
         */
        public Builder suspendReason(String val) {
            suspendReason = val;
            return this;
        }

    }

    protected Account(Builder builder) {
        this.name                   = builder.name;
        this.isRegistered           = builder.isRegistered;
        this.suspendLastTS          = builder.suspendLastTS;
        this.suspendCount           = builder.suspendCount;
        this.suspendReason          = builder.suspendReason;
        this.registrationTS         = builder.registrationTS;
    }

    protected Account(String name) {
        this.name = name;
    }


    /**
     * Returns the account name
     * @return account name
     */
    public String getName() {
        return this.name;
    }


    /**
     * Gets the timestamp for the last suspend date
     * @return last channel suspension timestamp
     */
    public Timestamp getSuspendLastTS() {
        return this.suspendLastTS;
    }

    /**
     * Sets the timestamp for the last suspend date
     * @return last channel suspension timestamp
     */
    public void setSuspendLastTS(Timestamp t) {
        this.suspendLastTS = t;
    }

    /**
     * Gets the suspend counter
     * @return suspension count
     */
    public Integer getSuspendCount() {
        return this.suspendCount;
    }

    /**
     * Sets the suspend counter
     * @return suspension count
     */
    public void setSuspendCount(Integer i) {
        this.suspendCount = i;
    }

    /**
     * Increments the suspend counter
     * @return suspension count
     */
    public void incSuspendCount() {
        this.suspendCount++;
    }

    /**
     * Gets the suspend message
     * @return suspension message
     */
    public String getSuspendMessage() {
        return this.suspendReason;
    }

    /**
     * Sets the suspend message
     * @return suspension message
     */
    public void setSuspendMessage(String s) {
        this.suspendReason = s;
    }

    /**
     * Sets the UUID confirmation code
     * @param uuid confirmation code
     */
    public void setConfirmationCode(UUID uuid) {
        this.confirmationCode = uuid;
    }

    /**
     * Returns the UUID confirmation code
     * @return confirmation code
     */
    public UUID getConfirmationCode() {
        return this.confirmationCode;
    }


    public Timestamp getRegistrationTS() {
        return this.registrationTS;
    }

    public void setRegistrationTS(Timestamp channelTS) {
        this.registrationTS = channelTS;
    }


    @Override
    public String toString() {
        return this.name;
    }

}
