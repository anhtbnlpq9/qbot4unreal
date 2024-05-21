package xyz.mjav.theqbot;

import java.time.Instant;

/**
 * Timestamp class
 */
public class Timestamp {

    public static Timestamp value(Long l) {
        return new Timestamp(l);
    }

    /** Timestamp value */
    private Long timestamp = 0L;

    /**
     * Timestamp construstor
     * @param ts
     */
    public Timestamp(long ts) {
        this.timestamp = ts;
    }

    public Timestamp(Integer ts) {
        this.timestamp = ts.longValue();
    }

    /**
     * Timestamp construstor
     * @param ts
     */
    public Timestamp(Timestamp ts) {
        this.timestamp = ts.getValue();
    }

    /**
     * Timestamp constructor, by default sets the TS to now
     */
    public Timestamp() {
        this.timestamp = Instant.now().getEpochSecond();
    }

    /**
     * Returns the Timestamp value
     * @return
     */
    public Long getValue() {
        return this.timestamp;
    }

    /**
     * Sets the Timestamp time value
     * @param ts
     */
    public void setValue(Long ts) {
        this.timestamp = ts;
    }

    @Override public String toString() {
        return this.timestamp.toString();
    }

    /**
     * Returns the timestamp as Long
     * @return
     */
    public Long toLong() {
        return this.timestamp;
    }
}
