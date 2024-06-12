package xyz.mjav.theqbot;

public class BeiProperty {
    private int          author;
    private String       reason;
    private Timestamp    fromTS;
    private Timestamp    toTS;
    private boolean      expired;


    public static class Builder {
        private int          author   = 0;
        private String       reason   = "";
        private Timestamp    fromTS   = new Timestamp(0);
        private Timestamp    toTS     = new Timestamp(0);
        private boolean      expired  = false;

        /**
         * @param author the author to set
         */
        public Builder author(int author) {
            this.author = author;
            return this;
        }

        /**
         * @param reason the reason to set
         */
        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        /**
         * @param fromTS the fromTS to set
         */
        public Builder fromTS(Timestamp fromTS) {
            this.fromTS = fromTS;
            return this;
        }

        /**
         * @param toTS the toTS to set
         */
        public Builder toTS(Timestamp toTS) {
            this.toTS = toTS;
            return this;
        }

        /**
         * @param expired the expired to set
         */
        public Builder expired(boolean expired) {
            this.expired = expired;
            return this;
        }

        public BeiProperty build() {
            return new BeiProperty(this);
        }


    }


    public BeiProperty(Builder b) {
        this.author = b.author;
        this.expired = b.expired;
        this.fromTS = b.fromTS;
        this.reason = b.reason;
        this.toTS = b.toTS;
    }


    /**
     * @return the author
     */
    public int getAuthor() {
        return author;
    }


    /**
     * @return the reason
     */
    public String getReason() {
        return reason;
    }


    /**
     * @return the fromTS
     */
    public Timestamp getFromTS() {
        return fromTS;
    }


    /**
     * @return the toTS
     */
    public Timestamp getToTS() {
        return toTS;
    }


    /**
     * @return the expired
     */
    public boolean isExpired() {
        return expired;
    }


}